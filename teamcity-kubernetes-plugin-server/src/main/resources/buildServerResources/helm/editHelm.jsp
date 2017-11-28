

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>

<c:set var="commandTitle">Command:<bs:help urlPrefix="https://docs.helm.sh/helm/#helm" file=""/></c:set>
<props:selectSectionProperty name="${params.commandKey}" title="${commandTitle}" note="">
    <c:forEach items="${params.commands}" var="command">
        <props:selectSectionPropertyContent value="${command.displayName}" caption="${command.description}">
            <jsp:include page="${teamcityPluginResourcesPath}/kube/${command.editPage}"/>
        </props:selectSectionPropertyContent>
    </c:forEach>
</props:selectSectionProperty>