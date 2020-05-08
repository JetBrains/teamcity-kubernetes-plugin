/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.helm;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.helm.HelmConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 28.11.17.
 */
public class HelmParametersBean {
    public String getCommandKey() {
        return COMMAND_ID;
    }

    @Nullable
    public String getNote(@NotNull String command, @NotNull String property){
        if (command.equals(HELM_DELETE_COMMAND)){
            if (property.equals(RELEASE_NAME)){
                return "Release name to delete from Kubernetes. Removes all of the resources associated with the last release of the chart";
            }
        } else if (command.equals(HELM_INSTALL_COMMAND)){
            if (property.equals(CHART)){
                return "Chart to install. Can be a chart reference, path to a packaged chart or an unpacked chart directory, or an absolute URL";
            }
        } else if (command.equals(HELM_UPGRADE_COMMAND)){
            if (property.equals(RELEASE_NAME)){
                return "Release to upgrade";
            } else if (property.equals(CHART)){
                return "New version of chart";
            }
        } else if (command.equals(HELM_ROLLBACK_COMMAND)){
            if (property.equals(RELEASE_NAME)){
                return "Release to rollback";
            } else if (property.equals(REVISION)){
                return "Release revision to rollback onto";
            }
        } else if (command.equals(HELM_TEST_COMMAND)){
            if (property.equals(RELEASE_NAME)){
                return "Release name to test. The tests to be run are defined in the chart that was installed";
            }
        }
        return null;
    }

    @Nullable
    public String getAdditionalArgsNote(@NotNull String command){
        if (command.equals(HELM_DELETE_COMMAND)) {
            return "helm delete";
        } else if (command.equals(HELM_INSTALL_COMMAND)) {
            return "helm install";
        } else if (command.equals(HELM_UPGRADE_COMMAND)) {
            return "helm upgrade";
        } else if (command.equals(HELM_ROLLBACK_COMMAND)) {
            return "helm rollback";
        } else if (command.equals(HELM_TEST_COMMAND)) {
            return "helm test";
        }
        return null;
    }

    public Collection<HelmCommand> getCommands() {
        return HelmCommands.getAll();
    }
}
