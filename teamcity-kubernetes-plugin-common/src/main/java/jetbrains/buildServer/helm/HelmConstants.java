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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public class HelmConstants {
    public static final String HELM_RUN_TYPE = "jetbrains.helm";

    public static final String HELM_PATH_CONFIG_PARAM = "Helm_Path";

    public static final String HELM_INSTALL_COMMAND = "install";
    public static final String HELM_DELETE_COMMAND = "delete";
    public static final String HELM_UPGRADE_COMMAND = "upgrade";
    public static final String HELM_ROLLBACK_COMMAND = "rollback";
    public static final String HELM_TEST_COMMAND = "test";

    public static final String CHART = "chart";
    public static final String ADDITIONAL_FLAGS = "additionalFlags";
    public static final String RELEASE_NAME = "releaseName";
    public static final String REVISION = "revision";
    public static final String COMMAND_ID = "command";

    public String getChartKey() {
        return CHART;
    }

    public String getReleaseName() {
        return RELEASE_NAME;
    }

    public String getRevision() {
        return REVISION;
    }

    public String getAdditionalFlagsKey() {
        return ADDITIONAL_FLAGS;
    }

    public String getDeleteCommandId() {
        return HELM_DELETE_COMMAND;
    }

    public String getInstallCommandId() {
        return HELM_INSTALL_COMMAND;
    }

    public String getRollbackCommandId() {
        return HELM_ROLLBACK_COMMAND;
    }

    public String getTestCommandId() {
        return HELM_TEST_COMMAND;
    }

    public String getUpgradeCommandId() {
        return HELM_UPGRADE_COMMAND;
    }

}
