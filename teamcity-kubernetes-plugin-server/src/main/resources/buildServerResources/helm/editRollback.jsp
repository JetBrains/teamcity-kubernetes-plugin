<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<tr>
    <th><label for="${constants.releaseName}">Release name: <l:star/></label></th>
    <td>
        <span>
            <props:textProperty name="${constants.releaseName}" className="longField"/>
            <i class="icon-magic" style="cursor:pointer;" title="Choose releases" ></i>
        </span>
        <span class="error" id="error_${constants.releaseName}"></span>
        <span class="smallNote">Release to upgrade.</span>
    </td>
</tr>

<tr>
    <th><label for="${constants.revision}">Revision: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${constants.revision}" className="longField"/>
        <i class="icon-magic" style="cursor:pointer;" title="Choose revision" ></i>
      </span>
        <span class="smallNote">Old release revision.</span>
        <span class="error" id="error_${constants.revision}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.addtionalFlagsKey}">Additional 'helm rollback' command flags: </label></th>
    <td><props:textProperty name="${constants.addtionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.addtionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm rollback' command line</span>
    </td>
</tr>