
package jetbrains.buildServer.clouds.kubernetes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jetbrains.buildServer.clouds.CloudException;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class KubePodNameGeneratorImpl implements KubePodNameGenerator {

  private final ConcurrentHashMap.KeySetView<String, Boolean> myUsedReusedNames = ConcurrentHashMap.newKeySet();
  private final ConcurrentMap<String, AtomicInteger> myCounters = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, AtomicBoolean> myIdxTouchedMaps = new ConcurrentHashMap<>();
  private final File myIdxStorage;
  private final AtomicBoolean myIsAvailable;
  private final ReadWriteLock myLock = new ReentrantReadWriteLock();
  private static final String FILE_EXTENSION = ".idx";

  public KubePodNameGeneratorImpl(
          @NotNull ServerPaths serverPaths,
          @NotNull ExecutorServices executorServices,
          @NotNull EventDispatcher<BuildServerListener> eventDispatcher
  ) {
    myIdxStorage = new File(serverPaths.getPluginDataDirectory(), "kubeIdx");
    if (!myIdxStorage.exists()) {
      myIdxStorage.mkdirs();
    }
    if (!myIdxStorage.isDirectory()) {
      throw new CloudException("Unable to create a directory for kube plugin VM indexes");
    }
    myIsAvailable = new AtomicBoolean(true);

    loadIdxes(myIdxStorage);

    eventDispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void serverShutdown() {
        myIsAvailable.set(false);
        storeIdxes(true);
      }
    });
    executorServices.getNormalExecutorService().scheduleWithFixedDelay(() -> storeIdxes(false), 60, 60, TimeUnit.SECONDS);
  }

  private synchronized void loadIdxes(@NotNull final File idxStorage) {
    final File[] idxes = idxStorage.listFiles();
    if (idxes == null) {
      return;
    }
    for (File idxFile : idxes) {
      if (!idxFile.getName().endsWith(FILE_EXTENSION))
        continue;
      String idxName = idxFile.getName().substring(0, idxFile.getName().length() - FILE_EXTENSION.length());
      try {
        int val = StringUtil.parseInt(new String(Files.readAllBytes(idxFile.toPath()), StandardCharsets.UTF_8), -1);
        if (val > 0){
          myCounters.putIfAbsent(idxName, new AtomicInteger(0));
          myCounters.get(idxName).set(val);
        }
      } catch (IOException e) {
        Loggers.AGENT.warn("Failed to load Kube index from file " + idxFile.getName(), e);
      }
    }
  }

  private void storeIdxes(boolean shuttingDown) {
    // wait for generation operations to finish, unless we're in the server shutdown phase or set by internal property
    Lock lock = myLock.writeLock();
    boolean locked = false;
    try {
      locked = lock.tryLock(100, TimeUnit.MILLISECONDS);
      if (!locked) {
        if (shuttingDown) {
          Loggers.AGENT.warn("Waited more than 100ms to store Kube indexes, forcing Kube indexes storage due to server shutdown");
        } else if (TeamCityProperties.getBoolean("teamcity.kube.pods.nameGenerator.periodicalPersist.force")) {
          Loggers.AGENT.warn("Waited more than 100ms to store Kube indexes, forcing Kube indexes storage due to internal property");
        } else {
          Loggers.AGENT.warn("Waited more than 100ms to store Kube indexes, skip this time");
          return;
        }
      }
      for (Map.Entry<String, AtomicBoolean> entry : myIdxTouchedMaps.entrySet()) {
        if (entry.getValue().compareAndSet(true, false)) {
          final AtomicInteger counter = myCounters.get(entry.getKey());
          try {
            final File idxFile = new File(myIdxStorage, entry.getKey() + FILE_EXTENSION);
            FileUtil.writeViaTmpFile(idxFile, new ByteArrayInputStream(String.valueOf(counter.get()).getBytes(StandardCharsets.UTF_8)), FileUtil.IOAction.DO_NOTHING);
          } catch (IOException ignored) {
          }
        }
      }
    } catch (InterruptedException e) {
      Loggers.AGENT.warn("Interrupted while waiting for Kube indexes storage lock", e);
    } finally {
      if (locked) {
        lock.unlock();
      }
    }
  }

  @NotNull
  public String generateNewVmName(@NotNull KubeCloudImage image) {
    if (!myIsAvailable.get()) {
      throw new CloudException("Unable to generate a name for image " + image.getId() + " - server is shutting down");
    }

    String prefix = image.getAgentNamePrefix();
    if (StringUtil.isEmptyOrSpaces(prefix)) {
      prefix = image.getDockerImage();
    }
    if (StringUtil.isEmptyOrSpaces(prefix)) {
      return UUID.randomUUID().toString();
    }
    prefix = StringUtil.replaceNonAlphaNumericChars(prefix.trim().toLowerCase(), '-');

    String newVmName;
    if (image.isReusingNames() && TeamCityProperties.getBooleanOrTrue("teamcity.kube.pods.nameGenerator.reuseNames")) {
      int counter = 1;
      while (true) {
        newVmName = String.format("%s-%d", prefix, counter);
        // not used by any running instance
        if (image.findInstanceById(newVmName) == null) {
          // ensure that another thread won't use the same name, temporarily save the name
          // it will be unlocked shortly after by the `nameSaved` method.
          if (myUsedReusedNames.add(newVmName)) {
            return newVmName;
          }
        }
        counter++;
      }
    }

    do {
      Lock lock = myLock.readLock();
      lock.lock();
      try {
        int counter = getNextCounter(prefix);
        newVmName = String.format("%s-%d", prefix, counter);
        setTouched(prefix);
      } finally {
        lock.unlock();
      }
    } while (image.findInstanceById(newVmName) != null);
    return newVmName;
  }

  @Override
  public void vmNameSaved(@NotNull String instanceName) {
    myUsedReusedNames.remove(instanceName);
  }

  private int getNextCounter(String prefix){
    return myCounters.computeIfAbsent(prefix, k -> new AtomicInteger()).incrementAndGet();
  }

  private void setTouched(String prefix){
    myIdxTouchedMaps.computeIfAbsent(prefix, k -> new AtomicBoolean()).set(true);
  }

}