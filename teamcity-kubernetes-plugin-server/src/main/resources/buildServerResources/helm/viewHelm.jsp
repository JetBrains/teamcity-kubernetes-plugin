<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

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