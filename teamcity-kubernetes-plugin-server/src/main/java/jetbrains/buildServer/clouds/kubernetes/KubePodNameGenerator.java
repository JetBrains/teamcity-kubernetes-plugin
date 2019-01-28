package jetbrains.buildServer.clouds.kubernetes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jetbrains.buildServer.clouds.CloudException;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.ThreadUtil;
import org.jetbrains.annotations.NotNull;

public class KubePodNameGenerator {

  private final ConcurrentMap<String, AtomicInteger> myCounters = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, AtomicBoolean> myIdxTouchedMaps = new ConcurrentHashMap<>();
  private final File myIdxStorage;
  private final AtomicBoolean myIsAvailable;
  private final ReadWriteLock myLock = new ReentrantReadWriteLock();

  public KubePodNameGenerator(@NotNull ServerPaths serverPaths,
                              @NotNull ExecutorServices executorServices,
                              @NotNull EventDispatcher<BuildServerListener> eventDispatcher
                              ){
    myIdxStorage = new File(serverPaths.getPluginDataDirectory(), "kubeIdx");
    if (!myIdxStorage.exists()){
      myIdxStorage.mkdirs();
    }
    if (!myIdxStorage.isDirectory()){
      throw new CloudException("Unable to create a directory for kube plugin VM indexes");
    }
    myIsAvailable = new AtomicBoolean(true);

    loadIdxes(myIdxStorage);

    eventDispatcher.addListener(new BuildServerAdapter(){
      @Override
      public void serverShutdown() {
        myIsAvailable.set(false);
        storeIdxes();
      }
    });
    executorServices.getNormalExecutorService().scheduleWithFixedDelay(this::storeIdxes, 60, 60, TimeUnit.SECONDS);
  }

  private synchronized void loadIdxes(@NotNull final File idxStorage) {
    final File[] idxes = idxStorage.listFiles();
    if (idxes == null){

    }
    for (File idxFile : idxes) {
      if (!idxFile.getName().endsWith(".idx"))
        continue;
      String idxName = idxFile.getName().substring(0, idxFile.getName().length()-4);
      try {
        int val = StringUtil.parseInt(FileUtil.readText(idxFile), -1);
        if (val > 0){
          myCounters.putIfAbsent(idxName, new AtomicInteger(0));
          myCounters.get(idxName).set(val);
        }
      } catch (IOException e) {}
    }
  }

  private void storeIdxes() {
    // wait for generation operations to finish:
    try {
      if (!myLock.writeLock().tryLock(100, TimeUnit.MILLISECONDS)) {
        Loggers.AGENT.warn("Waited more than 100ms to store Kube indexes");
      }
      for (Map.Entry<String, AtomicBoolean> entry : myIdxTouchedMaps.entrySet()) {
        if (entry.getValue().compareAndSet(true, false)) {
          final AtomicInteger counter = myCounters.get(entry.getKey());
          try {
            final File idxFile = new File(myIdxStorage, entry.getKey() + ".idx");
            FileUtil.writeViaTmpFile(idxFile, new ByteArrayInputStream(String.valueOf(counter.get()).getBytes()), FileUtil.IOAction.DO_NOTHING);
          } catch (IOException ignored) {
          }
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      myLock.writeLock().unlock();
    }
  }

  @NotNull
  public String generateNewVmName(@NotNull KubeCloudImage image) {
    try {
      myLock.readLock().lock();
      if (!myIsAvailable.get()){
        throw new CloudException("Unable to generate a name for image " + image.getId() + " - server is shutting down");
      }
      String newVmName;
      do {
        String prefix = image.getAgentNamePrefix();
        if (StringUtil.isEmptyOrSpaces(prefix)) {
          prefix = image.getDockerImage();
        }
        if (StringUtil.isEmptyOrSpaces(prefix)) {
          return UUID.randomUUID().toString();
        }
        prefix = StringUtil.replaceNonAlphaNumericChars(prefix.trim().toLowerCase(), '-');
        newVmName = String.format("%s-%d", prefix, getNextCounter(prefix));
        setTouched(prefix);

      } while (image.findInstanceById(newVmName) != null);
      return newVmName;
    } finally {
      myLock.readLock().unlock();
    }
  }

  private int getNextCounter(String prefix){
    myCounters.putIfAbsent(prefix, new AtomicInteger());
    return myCounters.get(prefix).incrementAndGet();
  }

  private void setTouched(String prefix){
    myIdxTouchedMaps.putIfAbsent(prefix, new AtomicBoolean());
    myIdxTouchedMaps.get(prefix).set(true);
  }

}
