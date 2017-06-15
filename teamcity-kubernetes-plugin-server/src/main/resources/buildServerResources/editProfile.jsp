<%@ page import="ekoshkin.teamcity.clouds.kubernetes.connector.ImagePullPolicy" %>
<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<jsp:useBean id="cons" class="ekoshkin.teamcity.clouds.kubernetes.KubeParametersConstants"/>
<jsp:useBean id="testConnectionUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="agentPools" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.agentPools.AgentPool>"/>
<jsp:useBean id="authStrategies" scope="request" type="java.util.Collection<ekoshkin.teamcity.clouds.kubernetes.auth.KubeAuthStrategy>"/>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}kubeSettings.css'/>");
</script>

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
        <th><label for="${cons.kubernetesNamespace}">Kubernetes Namespace: </label></th>
        <td><props:textProperty name="${cons.kubernetesNamespace}" className="longField"/>
            <span id="error_${cons.kubernetesNamespace}" class="error"></span>
            <span class="smallNote">Kubernetes namespace to use. Leave blank to use default namespace.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.authStrategy}">Authentication Strategy: <l:star/></label></th>
        <td>
            <div>
                <c:set var="selectedAuthStrategy" value="${propertiesBean.properties[cons.authStrategy]}" />
                <props:selectProperty name="${cons.authStrategy}">
                    <props:option value="" selected="${empty selectedAuthStrategy}">--- Choose authentication strategy ---</props:option>
                    <c:forEach var="authStrategy" items="${authStrategies}">
                        <props:option value="${authStrategy.id}" selected="${not empty selectedAuthStrategy and authStrategy.id eq selectedAuthStrategy}"><c:out value="${authStrategy.displayName}"/></props:option>
                    </c:forEach>
                </props:selectProperty>
                <span id="error_${cons.authStrategy}" class="error"></span>
            </div>
            <c:forEach var="strategy" items="${authStrategies}">
                <c:set var="description" value="${strategy.description}"/>
                <c:if test="${not empty description}">
                    <span class="smallNote"><c:out value="${description}"/></span>
                </c:if>
            </c:forEach>
        </td>
    </tr>
    <tr class="hidden user-passwd auth-ui">
        <th><label for="${cons.username}">Username: <l:star/></label></th>
        <td><props:textProperty name="${cons.username}" className="longField"/>
            <span id="error_${cons.username}" class="error"></span>
            <span class="smallNote">Authorized Kubernetes user.</span>
        </td>
    </tr>
    <tr class="hidden user-passwd auth-ui">
        <th><label for="${cons.password}">Password: <l:star/></label></th>
        <td><props:textProperty name="${cons.password}" className="longField"/>
            <span id="error_${cons.password}" class="error"></span>
            <span class="smallNote">Password of authorized Kubernetes user.</span>
        </td>
    </tr>
    <tr>
        <th></th>
        <td>
            <forms:button id="kubeTestConnectionButton" onclick="BS.Kube.ProfileSettingsForm.testConnection();">Test connection</forms:button>
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

<h2 class="noBorder section-header">Agent images</h2>

<div class="buttonsWrapper">
    <div class="imagesTableWrapper hidden">
        <table id="kubeImagesTable" class="settings imagesTable hidden">
            <tbody>
            <tr>
                <th class="name">Docker Image</th>
                <th class="name">Max # of instances</th>
                <th class="name" colspan="2"></th>
            </tr>
            </tbody>
        </table>
        <c:set var="sourceImagesJson" value="${propertiesBean.properties['source_images_json']}"/>
        <input type="hidden" class="jsonParam" name="prop:source_images_json" id="source_images_json" value="<c:out value='${sourceImagesJson}'/>"/>
        <input type="hidden" id="initial_images_list"/>
    </div>
    <forms:addButton title="Add image" id="showAddImageDialogButton">Add image</forms:addButton>
</div>

<bs:dialog dialogId="testConnectionDialog" dialogClass="vcsRootTestConnectionDialog" title="Test Connection" closeCommand="BS.TestConnectionDialog.close();"
           closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails" class="mono"></div>
</bs:dialog>

<bs:dialog dialogId="KubeImageDialog" title="Add Kubernetes Cloud Image" closeCommand="BS.Kube.ImageDialog.close()"
           dialogClass="KubeImageDialog" titleId="KubeImageDialogTitle">
    <table class="runnerFormTable paramsTable">
        <tr>
            <th>Docker image:&nbsp;<l:star/></th>
            <td>
                <div>
                    <input type="text" id="${cons.dockerImage}" value="" class="longField" data-id="${cons.dockerImage}" data-err-id="${cons.dockerImage}"/>
                    <div class="smallNoteAttention">Docker image name to use.</div>
                    <span class="error option-error option-error_${cons.dockerImage}"></span>
                </div>
            </td>
        </tr>
        <tr>
            <th>Image pull policy:</th>
            <td>
                <div>
                    <select name="${cons.imagePullPolicy}" id="${cons.imagePullPolicy}" data-id="${cons.imagePullPolicy}" data-err-id="${cons.imagePullPolicy}">
                        <c:forEach var="policy" items="<%= ImagePullPolicy.values() %>">
                            <props:option value="${policy.name}"><c:out value="${policy.displayName}"/></props:option>
                        </c:forEach>
                    </select>
                    <div class="smallNoteAttention">Policy to use by Kubelet to pull an image.
                        &nbsp;<a href="https://kubernetes.io/docs/concepts/containers/images/#updating-images"><bs:helpIcon/></a></div>
                    <span class="error option-error option-error_${cons.imagePullPolicy}"></span>
                </div>
            </td>
        </tr>
        <tr>
            <th>Docker command:</th>
            <td>
                <div>
                    <input type="text" id="${cons.dockerCommand}" value="" class="longField" data-id="${cons.dockerCommand}" data-err-id="${cons.dockerCommand}"/>
                    <div class="smallNoteAttention">Docker entrypoint to use. The docker image's ENTRYPOINT is used if this is not provided.
                        &nbsp;<a href="https://kubernetes.io/docs/api-reference/v1.6/#container-v1-core"><bs:helpIcon/></a></div>
                    <span class="error option-error option-error_${cons.dockerCommand}"></span>
                </div>
            </td>
        </tr>
        <tr>
            <th>Docker Arguments:</th>
            <td>
                <div>
                    <input type="text" id="${cons.dockerArguments}" value="" class="longField" data-id="${cons.dockerArguments}" data-err-id="${cons.dockerArguments}"/>
                    <div class="smallNoteAttention">Arguments for docker entrypoint. The docker image's CMD is used if this is not provided.
                        &nbsp;<a href="https://kubernetes.io/docs/api-reference/v1.6/#container-v1-core"><bs:helpIcon/></a></div>
                    <span class="error option-error option-error_${cons.dockerArguments}"></span>
                </div>
            </td>
        </tr>
        <tr>
            <th>Max number of instances:</th>
            <td>
                <div>
                    <input type="text" id="maxInstances" value="" class="longField" data-id="maxInstances" data-err-id="maxInstances"/>
                </div>
                <span class="error option-error option-error_maxInstances"></span>
            </td>
        </tr>
        <tr class="advancedSetting">
            <th><label for="${cons.agentPoolIdField}">Agent pool:</label></th>
            <td>
                <select id="${cons.agentPoolIdField}" data-id="${cons.agentPoolIdField}" class="longField configParam">
                    <props:option value=""><c:out value="<Please select agent pool>"/></props:option>
                    <c:forEach var="ap" items="${agentPools}">
                        <props:option value="${ap.agentPoolId}"><c:out value="${ap.name}"/></props:option>
                    </c:forEach>
                </select>
                <span id="error_${cons.agentPoolIdField}" class="error"></span>
            </td>
        </tr>
    </table>
    <div class="popupSaveButtonsBlock">
        <forms:submit label="Add" type="button" id="kubeAddImageButton"/>
        <forms:button title="Cancel" id="kubeCancelAddImageButton">Cancel</forms:button>
    </div>
</bs:dialog>

<script type="text/javascript">
    $j.ajax({
        url: "<c:url value="${teamcityPluginResourcesPath}kubeSettings.js"/>",
        dataType: "script",
        cache: true,
        success: function () {
            BS.Kube.ProfileSettingsForm.testConnectionUrl = '<c:url value="${testConnectionUrl}"/>';
            BS.Kube.ProfileSettingsForm.initialize();
        }
    });
</script>

<table class="runnerFormTable" style="margin-top: 3em;">