<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<tr>
    <th><label for="${constants.chartKey}">Helm chart: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${constants.chartKey}" className="longField"/>
        <bs:vcsTree fieldId="${constants.chartKey}" treeId="${constants.chartKey}"/>
        <i class="icon-magic" style="cursor:pointer;" title="Choose chart reference" ></i>
      </span>
        <span class="smallNote">Chart to install. Can be a chart reference, path to packaged chart or unpacked chart directory, or absolute URL</span>
        <span class="error" id="error_${constants.chartKey}"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <th><label for="${constants.addtionalFlagsKey}">Additional 'helm install' command flags: </label></th>
    <td><props:textProperty name="${constants.addtionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${constants.addtionalFlagsKey}"></span>
        <span class="smallNote">Additional options for 'helm install' command line</span>
    </td>
</tr>