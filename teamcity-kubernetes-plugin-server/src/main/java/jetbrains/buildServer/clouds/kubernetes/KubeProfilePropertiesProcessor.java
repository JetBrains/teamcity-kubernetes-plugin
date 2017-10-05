package jetbrains.buildServer.clouds.kubernetes;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.API_SERVER_URL;
import static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.AUTH_STRATEGY;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeProfilePropertiesProcessor implements PropertiesProcessor {
    @Override
    public Collection<InvalidProperty> process(Map<String, String> map) {
        Collection<InvalidProperty> invalids = new ArrayList<>();
        if(StringUtil.isEmptyOrSpaces(map.get(API_SERVER_URL))) invalids.add(new InvalidProperty(API_SERVER_URL, "Kubernetes API server URL must not be empty"));
        if(StringUtil.isEmptyOrSpaces(map.get(AUTH_STRATEGY))) invalids.add(new InvalidProperty(AUTH_STRATEGY, "Authentication strategy must be selected"));
        return invalids;
    }
}
