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
    <th><label for="${constants.chartKey}">Helm chart: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${constants.installCommandId}${constants.chartKey}" className="longField"/>
        <bs:vcsTree fieldId="${constants.installCommandId}${constants.chartKey}" treeId="${constants.installCommandId}${constants.chartKey}"/>
      </span>
        <span class="smallNote">Chart to install. Can be a chart reference, path to packaged chart or unpacked chart directory, or absolute URL</span>
        <span class="error" id="error_${constants.installCommandId}${constants.chartKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.installCommandId}${constants.additionalFlagsKey}">Additional 'helm install' command flags: </label></th>
    <td><props:textProperty name="${constants.installCommandId}${constants.additionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.installCommandId}${constants.additionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm install' command line</span>
    </td>
</tr>