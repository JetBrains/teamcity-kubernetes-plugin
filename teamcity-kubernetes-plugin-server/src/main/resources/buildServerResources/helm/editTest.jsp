<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<tr>
    <th><label for="${constants.testCommandId}${constants.releaseName}">Release name: <l:star/></label></th>
    <td>
        <span>
            <props:textProperty name="${constants.testCommandId}${constants.releaseName}" className="longField"/>
        </span>
        <span class="error" id="error_${constants.testCommandId}${constants.releaseName}"></span>
        <span class="smallNote">Release name to test. The tests to be run are defined in the chart that was installed.</span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.testCommandId}${constants.additionalFlagsKey}">Additional 'helm test' command flags: </label></th>
    <td><props:textProperty name="${constants.testCommandId}${constants.additionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.testCommandId}${constants.additionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm test' command line</span>
    </td>
</tr>