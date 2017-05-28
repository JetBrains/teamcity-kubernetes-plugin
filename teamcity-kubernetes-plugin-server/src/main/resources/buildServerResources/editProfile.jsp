<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<jsp:useBean id="cons" class="ekoshkin.teamcity.clouds.kubernetes.KubeConstants"/>

</table>

<table class="runnerFormTable">
<tr>
  <th><label for="secure:${cons.apiServerUrl}">Kubernetes API server URL: <l:star/></label></th>
  <td><props:textProperty name="secure:${cons.apiServerUrl}" className="longField"/>
    <span id="error_secure:${cons.apiServerUrl}" class="error"></span>
    <span class="smallNote">Target Kubernetes API server URL</span>
  </td>
</tr>
<tr>
  <th><label for="secure:${cons.seviceAccountName}">Service account name: <l:star/></label></th>
  <td><props:textProperty name="secure:${cons.seviceAccountName}" className="longField"/>
    <span id="error_secure:${cons.seviceAccountName}" class="error"></span>
    <span class="smallNote">Name of the service account use to access API</span>
  </td>
</tr>
<tr>
  <th><label for="secure:${cons.seviceAccountToken}">Service account token: <l:star/></label></th>
  <td><props:passwordProperty name="secure:${cons.seviceAccountToken}" className="longField"/>
    <span id="error_secure:${cons.seviceAccountToken}" class="error"></span>
    <span class="smallNote">The signed JWT to use as a bearer token to authenticate as the given service account.</span>
  </td>
</tr>
</table>