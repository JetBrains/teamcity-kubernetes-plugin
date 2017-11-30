<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<tr>
    <th><label for="${constants.deleteCommandId}${constants.releaseName}">Release name: <l:star/></label></th>
    <td>
        <span>
            <props:textProperty name="${constants.deleteCommandId}${constants.releaseName}" className="longField"/>
        </span>
        <span class="error" id="error_${constants.deleteCommandId}${constants.releaseName}"></span>
        <span class="smallNote">Release name to delete from Kubernetes. Removes all of the resources associated with the last release of the chart.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.deleteCommandId}${constants.additionalFlagsKey}">Additional 'helm delete' command flags: </label></th>
    <td><props:textProperty name="${constants.deleteCommandId}${constants.additionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.deleteCommandId}${constants.additionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm delete' command line</span>
    </td>
</tr>