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

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import static jetbrains.buildServer.helm.HelmConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class UpgradeCommand implements HelmCommand {
    @NotNull
    @Override
    public String getId() {
        return HELM_UPGRADE_COMMAND;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Upgrade";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Upgrades a release to a new version of a chart";
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> result = new Vector<InvalidProperty>();
            final String chart = properties.get(CHART);
            if (PropertiesUtil.isEmptyOrNull(chart)) {
                result.add(new InvalidProperty(CHART, "Chart must be specified"));
            }
            final String releaseName = properties.get(RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(releaseName)) {
                result.add(new InvalidProperty(RELEASE_NAME, "Release name must be specified"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFile() {
        return "editUpgrade.jsp";
    }

    @Nullable
    @Override
    public String getViewParamsJspFile() {
        return "viewUpgrade.jsp";
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(ADDITIONAL_FLAGS);
        return String.format("Release: %s\nChart: %s\nAdditional flags: %s",
                             parameters.get(RELEASE_NAME),
                             parameters.get(CHART),
                             flags != null ? flags : "not specified");
    }
}
