package ekoshkin.teamcity.clouds.kubernetes;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 27.05.17.
 */
public class KubeProfilePropertiesProcessor implements PropertiesProcessor {
    @Override
    public Collection<InvalidProperty> process(Map<String, String> map) {
        return Collections.emptyList();
    }
}
