<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>

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

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}kubeSettings.css'/>");
</script>

<jsp:useBean id="deployments" scope="request" type="java.util.Collection<java.lang.String>"/>
<jsp:useBean id="error" scope="request" type="java.lang.String"/>

<c:choose>
    <c:when test="${not empty error}">
        <span class="testConnectionFailed">Unable to list deployments:</span><br/>
        <span><c:out value="${error}"/></span>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${empty deployments}">
                No deployments found
            </c:when>
            <c:otherwise>
                <ul class="chooser">
                    <c:forEach var="deployment" items="${deployments}">
                        <li><a style="cursor:pointer;" onclick="BS.Kube.DeploymentChooser.selectDeployment('${deployment}')"><c:out value="${deployment}"/></a></li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>