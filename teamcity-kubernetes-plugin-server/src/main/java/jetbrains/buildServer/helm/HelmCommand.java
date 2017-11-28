package jetbrains.buildServer.helm;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 28.11.17.
 */
public interface HelmCommand {
    @NotNull
    String getId();

    @NotNull
    String getDisplayName();

    @NotNull
    String getDescription();

    @Nullable
    PropertiesProcessor getPropertiesProcessor();

    @Nullable
    String getEditParamsJspFile();

    @Nullable
    String getViewParamsJspFile();

    @NotNull
    String describeParameters(@NotNull Map<String, String> parameters);
}
