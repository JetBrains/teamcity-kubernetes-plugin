package jetbrains.buildServer.helm;

import java.util.Collection;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 28.11.17.
 */
public class HelmParametersBean {
    public String getCommandKey() {
        return HelmConstants.COMMAND_ID;
    }

    public Collection<HelmCommand> getCommands() {
        return HelmCommands.getAll();
    }
}
