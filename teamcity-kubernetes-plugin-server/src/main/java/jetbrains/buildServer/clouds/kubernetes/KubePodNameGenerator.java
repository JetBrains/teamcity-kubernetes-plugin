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
import jetbrains.buildServer.clouds.CloudException;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.StringUtils;
import org.jetbrains.annotations.NotNull;

public class KubePodNameGenerator {

  private final ConcurrentMap<String, AtomicInteger> myCounters = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, AtomicBoolean> myIdxTouchedMaps = new ConcurrentHashMap<>();
  private final File myIdxStorage;
  private final AtomicBoolean myIsAvailable;

  public KubePodNameGenerator(@NotNull ServerPaths serverPaths,
                              @NotNull ExecutorServices executorServices,
                              @NotNull EventDispatcher<BuildServerListener> eventDispatcher
                              ){
    myIdxStorage = new File(serverPaths.getPluginDataDirectory(), "kubeIdx");
    if (!myIdxStorage.exists()){
      myIdxStorage.mkdirs();
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
    for (File idxFile : idxes) {
      if (!idxFile.getName().endsWith(".idx"))
        continue;
      String idxName = idxFile.getName().substring(0, idxFile.getName().length()-4);
      try {
        int val = StringUtil.parseInt(FileUtil.readText(idxFile), -1);
        if (val > 0){
          getCounter(idxName).set(val);
        }
      } catch (IOException e) {}
    }
  }

  private synchronized void storeIdxes() {
    for (Map.Entry<String, AtomicBoolean> entry : myIdxTouchedMaps.entrySet()) {
      if (entry.getValue().compareAndSet(true, false)){
        final AtomicInteger counter = getCounter(entry.getKey());
        synchronized (counter) {
          try {
            final File idxFile = new File(myIdxStorage, entry.getKey() + ".idx");
            FileUtil.writeViaTmpFile(idxFile, new ByteArrayInputStream(String.valueOf(counter.get()).getBytes()), FileUtil.IOAction.DO_NOTHING);
          } catch (IOException ignored) {}
        }
      }
    }
  }

  @NotNull
  public String generateNewVmName(@NotNull KubeCloudImage image) {
    if (!myIsAvailable.get()){
      throw new CloudException("Unable to generate a name for image " + image.getId() + " - server is shutting down");
    }
    String newVmName;
    do {
      String prefix = image.getAgentNamePrefix();
      if (StringUtil.isEmptyOrSpaces(prefix)){
        prefix = image.getDockerImage();
      }
      if (StringUtil.isEmptyOrSpaces(prefix)){
        return UUID.randomUUID().toString();
      }
      prefix = StringUtil.replaceNonAlphaNumericChars(prefix.trim().toLowerCase(),'-');
      newVmName = String.format("%s-%d", prefix, getCounter(prefix).incrementAndGet());
      getTouchIdx(prefix).set(true);
    } while (image.findInstanceById(newVmName) != null);
    return newVmName;
  }

  private synchronized AtomicInteger getCounter(String prefix){
    myCounters.putIfAbsent(prefix, new AtomicInteger());
    return myCounters.get(prefix);
  }

  private synchronized AtomicBoolean getTouchIdx(String prefix){
    myIdxTouchedMaps.putIfAbsent(prefix, new AtomicBoolean());
    return myIdxTouchedMaps.get(prefix);
  }

  public static void main(String[] args) {
    final String prefix = "jetbrains/teamcity-agent:latest ";
    System.out.println(StringUtils.replaceNonAlphaNumericChars(prefix.trim().toLowerCase(), '-'));
  }

}
