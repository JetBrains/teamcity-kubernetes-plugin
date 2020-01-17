<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

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

<c:forEach items="${helmParametersBean.commands}" var="command">
    <c:if test="${propertiesBean.properties[helmParametersBean.commandKey] eq command.id}">
        <div class="parameter">
            Command: <strong><c:out value="${command.displayName}"/></strong>
        </div>
        <jsp:include page="${teamcityPluginResourcesPath}/helm/${command.viewParamsJspFile}"/>
    </c:if>
</c:forEach>