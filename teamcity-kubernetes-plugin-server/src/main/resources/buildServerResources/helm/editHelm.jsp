<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

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