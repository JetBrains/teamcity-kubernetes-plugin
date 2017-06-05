<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<jsp:useBean id="cons" class="ekoshkin.teamcity.clouds.kubernetes.KubeConstants"/>
<jsp:useBean id="testConnectionUrl" class="java.lang.String" scope="request"/>

</table>

<table class="runnerFormTable">
    <tr>
        <th><label for="${cons.apiServerUrl}">Kubernetes API server URL: <l:star/></label></th>
        <td><props:textProperty name="${cons.apiServerUrl}" className="longField"/>
            <span id="error_${cons.apiServerUrl}" class="error"></span>
            <span class="smallNote">Target Kubernetes API server URL</span>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.seviceAccountName}">Service account name: <l:star/></label></th>
        <td><props:textProperty name="${cons.seviceAccountName}" className="longField"/>
            <span id="error_${cons.seviceAccountName}" class="error"></span>
            <span class="smallNote">Name of the service account use to access API</span>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.seviceAccountToken}">Service account token: <l:star/></label></th>
        <td><props:textProperty name="${cons.seviceAccountToken}" className="longField"/>
            <span id="error_${cons.seviceAccountToken}" class="error"></span>
            <span class="smallNote">The signed JWT to use as a bearer token to authenticate as the given service account.</span>
        </td>
    </tr>
    <tr>
        <th></th>
        <td>
            <forms:button id="kubeTestConnectionButton" onclick="BS.Kube.ProfileSettingsForm.testConnection();">Test connection</forms:button>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.kubernetesNamespace}">Kubernetes Namespace: </label></th>
        <td><props:textProperty name="${cons.kubernetesNamespace}" className="longField"/>
            <span id="error_${cons.kubernetesNamespace}" class="error"></span>
            <span class="smallNote">Kubernetes namespace to use. Leave blanc to use default namespace.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.profileInstanceLimit}">Maximum instances count:</label></th>
        <td>
            <props:textProperty name="${cons.profileInstanceLimit}" className="settings"/>
            <span id="error_${cons.profileInstanceLimit}" class="error"></span>
            <span class="smallNote">Maximum number of instances that can be started. Use blank to have no limit</span>
        </td>
    </tr>
</table>

<bs:dialog dialogId="testConnectionDialog" dialogClass="vcsRootTestConnectionDialog" title="Test Connection" closeCommand="BS.TestConnectionDialog.close();"
           closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails" class="mono"></div>
</bs:dialog>

<script type="text/javascript">
    $j.ajax({
        url: "<c:url value="${teamcityPluginResourcesPath}kubeSettings.js"/>",
        dataType: "script",
        cache: true,
        success: function () {
            BS.Kube.ProfileSettingsForm.testConnectionUrl = '<c:url value="${testConnectionUrl}"/>';
        }
    });
</script>