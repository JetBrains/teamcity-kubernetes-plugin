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

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 17.10.17.
 */
public interface HelmConstants {
    String HELM_RUN_TYPE = "jetbrains.helm";

    String HELM_PATH_CONFIG_PARAM = "Helm_Path";

    String HELM_INSTALL_COMMAND = "install";
    String HELM_INSTALL_COMMAND_NAME = "helm-install";

    String HELM_DELETE_COMMAND = "delete";
    String HELM_DELETE_COMMAND_NAME = "helm-delete";

    String HELM_UPGRADE_COMMAND = "upgrade";
    String HELM_UPGRADE_COMMAND_NAME = "helm-upgrade";

    String HELM_ROLLBACK_COMMAND = "rollback";
    String HELM_ROLLBACK_COMMAND_NAME = "helm-rollback";

    String HELM_TEST_COMMAND = "test";
    String HELM_TEST_COMMAND_NAME = "helm-test";

    String CHART = "teamcity.helm.chart";
    String ADDITIONAL_FLAGS = "teamcity.helm.additionalFlags";
    String RELEASE_NAME = "teamcity.helm.releaseName";
    String REVISION = "teamcity.helm.revision";
    String COMMAND_ID = "teamcity.helm.command";
}
