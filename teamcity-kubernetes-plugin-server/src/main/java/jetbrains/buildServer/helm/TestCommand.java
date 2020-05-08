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
public class TestCommand implements HelmCommand {
    @NotNull
    @Override
    public String getId() {
        return HELM_TEST_COMMAND;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Test";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Runs the tests for a release.";
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> result = new Vector<InvalidProperty>();
            final String chart = properties.get(RELEASE_NAME);
            if (PropertiesUtil.isEmptyOrNull(chart)) {
                result.add(new InvalidProperty(RELEASE_NAME, "Release name must be specified"));
            }
            return result;
        };
    }

    @Nullable
    @Override
    public String getEditParamsJspFile() {
        return "editTest.jsp";
    }

    @Nullable
    @Override
    public String getViewParamsJspFile() {
        return "viewTest.jsp";
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        String flags = parameters.get(ADDITIONAL_FLAGS);
        return String.format("Release name: %s\nAdditional flags: %s", parameters.get(RELEASE_NAME), flags != null ? flags : "not specified");
    }
}
