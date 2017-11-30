<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<tr>
    <th><label for="${constants.rollbackCommandId}${constants.releaseName}">Release name: <l:star/></label></th>
    <td>
        <span>
            <props:textProperty name="${constants.rollbackCommandId}${constants.releaseName}" className="longField"/>
        </span>
        <span class="error" id="error_${constants.rollbackCommandId}${constants.releaseName}"></span>
        <span class="smallNote">Release to rolls back.</span>
    </td>
</tr>

<tr>
    <th><label for="${constants.rollbackCommandId}${constants.revision}">Revision: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${constants.rollbackCommandId}${constants.revision}" className="longField"/>
      </span>
        <span class="smallNote">Previous release revision.</span>
        <span class="error" id="error_${constants.rollbackCommandId}${constants.revision}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.rollbackCommandId}${constants.additionalFlagsKey}">Additional 'helm rollback' command flags: </label></th>
    <td><props:textProperty name="${constants.rollbackCommandId}${constants.additionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.rollbackCommandId}${constants.additionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm rollback' command line</span>
    </td>
</tr>