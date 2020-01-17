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
public class HelmConstantsBean {
    public String getChartKey() {
        return HelmConstants.CHART;
    }

    public String getReleaseName() {
        return HelmConstants.RELEASE_NAME;
    }

    public String getRevision() {
        return HelmConstants.REVISION;
    }

    public String getAdditionalFlagsKey() {
        return HelmConstants.ADDITIONAL_FLAGS;
    }

    public String getDeleteCommandId() {
        return HelmConstants.HELM_DELETE_COMMAND_NAME;
    }

    public String getInstallCommandId() {
        return HelmConstants.HELM_INSTALL_COMMAND_NAME;
    }

    public String getRollbackCommandId() {
        return HelmConstants.HELM_ROLLBACK_COMMAND_NAME;
    }

    public String getTestCommandId() {
        return HelmConstants.HELM_TEST_COMMAND_NAME;
    }

    public String getUpgradeCommandId() {
        return HelmConstants.HELM_UPGRADE_COMMAND_NAME;
    }
}
