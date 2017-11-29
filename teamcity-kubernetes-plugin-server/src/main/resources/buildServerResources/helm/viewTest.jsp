<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<div class="parameter">
    Release: <strong><props:displayValue name="${constants.testCommandId}${constants.releaseName}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
    Additional command flags: <strong><props:displayValue name="${constants.testCommandId}${constants.additionalFlagsKey}" emptyValue="not specified"/></strong>
</div>