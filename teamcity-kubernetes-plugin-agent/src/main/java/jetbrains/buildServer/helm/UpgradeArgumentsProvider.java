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

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 19.10.17.
 */
public class UpgradeArgumentsProvider implements HelmCommandArgumentsProvider {
    @NotNull
    @Override
    public String getCommandId() {
        return HELM_UPGRADE_COMMAND_NAME;
    }

    @NotNull
    @Override
    public List<String> getArguments(@NotNull Map<String, String> runnerParameters) {
        String release = runnerParameters.get(HELM_UPGRADE_COMMAND_NAME + RELEASE_NAME);
        String chart = runnerParameters.get(HELM_UPGRADE_COMMAND_NAME + CHART);
        String additionalFlags = runnerParameters.get(HELM_UPGRADE_COMMAND_NAME + ADDITIONAL_FLAGS);
        return StringUtil.splitCommandArgumentsAndUnquote(String.format("%s %s %s %s", HELM_UPGRADE_COMMAND, StringUtil.emptyIfNull(release), StringUtil.emptyIfNull(chart), StringUtil.emptyIfNull(additionalFlags)));
    }
}
