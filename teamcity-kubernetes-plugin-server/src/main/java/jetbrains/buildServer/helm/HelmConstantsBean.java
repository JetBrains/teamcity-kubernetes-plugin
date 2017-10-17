package jetbrains.buildServer.helm;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class HelmConstantsBean {
    public String getChartKey() {
        return HelmConstants.CHART;
    }

    public String getAddtionalFlagsKey() {
        return HelmConstants.ADDITIONAL_FLAGS;
    }
}
