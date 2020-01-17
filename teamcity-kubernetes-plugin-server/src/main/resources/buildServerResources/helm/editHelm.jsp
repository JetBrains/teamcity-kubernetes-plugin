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
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>
<jsp:useBean id="helmParametersBean" class="jetbrains.buildServer.helm.HelmParametersBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<c:set var="commandTitle">Command:<bs:help urlPrefix="https://docs.helm.sh/helm/#helm" file=""/></c:set>
<props:selectSectionProperty name="${helmParametersBean.commandKey}" title="${commandTitle}" note="">
    <props:selectSectionPropertyContent value="" caption="-- Choose Helm command --"/>
    <c:forEach items="${helmParametersBean.commands}" var="command">
        <props:selectSectionPropertyContent value="${command.id}" caption="${command.displayName}">
            <jsp:include page="${teamcityPluginResourcesPath}/helm/${command.editParamsJspFile}"/>
        </props:selectSectionPropertyContent>
    </c:forEach>
</props:selectSectionProperty>