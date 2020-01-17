<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<%--
  ~ Copyright 2000-2020 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<tr>
    <th><label for="${constants.upgradeCommandId}${constants.releaseName}">Release name: <l:star/></label></th>
    <td>
        <span>
            <props:textProperty name="${constants.upgradeCommandId}${constants.releaseName}" className="longField"/>
        </span>
        <span class="error" id="error_${constants.upgradeCommandId}${constants.releaseName}"></span>
        <span class="smallNote">Release to upgrade.</span>
    </td>
</tr>

<tr>
    <th><label for="${constants.upgradeCommandId}${constants.chartKey}">Chart: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${constants.upgradeCommandId}${constants.chartKey}" className="longField"/>
        <bs:vcsTree fieldId="${constants.upgradeCommandId}${constants.chartKey}" treeId="${constants.upgradeCommandId}${constants.chartKey}"/>
      </span>
        <span class="smallNote">New version of chart.</span>
        <span class="error" id="error_${constants.upgradeCommandId}${constants.chartKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.upgradeCommandId}${constants.additionalFlagsKey}">Additional 'helm upgrade' command flags: </label></th>
    <td><props:textProperty name="${constants.upgradeCommandId}${constants.additionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.upgradeCommandId}${constants.additionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm upgrade' command line</span>
    </td>
</tr>