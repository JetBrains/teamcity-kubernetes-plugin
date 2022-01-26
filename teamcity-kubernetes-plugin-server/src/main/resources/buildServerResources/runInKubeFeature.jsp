<%@ page import="static jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>
<%--
  ~ Copyright 2000-2022 JetBrains s.r.o.
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
<jsp:useBean id="buildForm"  scope="request" type="jetbrains.buildServer.controllers.admin.projects.EditableBuildTypeSettingsForm"/>
<jsp:useBean id="buildFeature"  scope="request" type="jetbrains.buildServer.clouds.kubernetes.buildFeature.RunInKubeFeature"/>

<tr>
  <td colspan="2">
    <em>Run Build in Kubernetes</em>
  </td>
</tr>
<tr>
  <th>
    Docker image name:
  </th>
  <td>
    <props:textProperty name="<%=RUN_IN_KUBE_DOCKER_IMAGE%>" className="longField"/>
  </td>
</tr>
<tr>
  <th>
    Kubernetes Source:
  </th>
  <td>
    <div style="float:left">
      <props:selectProperty name="<%=RUN_IN_KUBE_AGENT_SOURCE%>">
        <props:option value="">No Image Selected</props:option>
        <c:forEach var="imgPair" items="${buildFeature.showProfilesAndImages(buildForm.project)}">
          <props:option value="${imgPair.first}"><c:out value="${imgPair.second}"/></props:option>
        </c:forEach>
      </props:selectProperty>
    </div>
  </td>
</tr>

