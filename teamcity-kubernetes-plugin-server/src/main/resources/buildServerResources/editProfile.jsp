<%@ page import="jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy" %>
<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<jsp:useBean id="cons" class="jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="agentPools" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.agentPools.AgentPool>"/>
<jsp:useBean id="authStrategies" scope="request" type="java.util.Collection<jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy>"/>
<jsp:useBean id="podTemplateProviders" scope="request" type="java.util.Collection<jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider>"/>

<jsp:useBean id="testConnectionUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="namespaceChooserUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="deploymentChooserUrl" class="java.lang.String" scope="request"/>

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
        <td>
            <props:textProperty name="${cons.kubernetesNamespace}" className="longField"/>
            <i class="icon-magic" style="cursor:pointer;" title="Choose namespace" onclick="BS.Kube.NamespaceChooser.showPopup(this, '<c:url value="${namespaceChooserUrl}"/>')"></i>
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
                    <div class="smallNote hidden auth-ui ${strategy.id}" style="margin-left: 0"><c:out value="${description}"/></div>
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
        <th><label for="secure:${cons.password}">Password: <l:star/></label></th>
        <td><props:passwordProperty name="secure:${cons.password}" className="longField"/>
            <span id="error_secure:${cons.password}" class="error"></span>
            <span class="smallNote">Password of authorized Kubernetes user.</span>
        </td>
    </tr>
    <tr class="hidden client-cert auth-ui">
        <th><label for="${cons.clientCertData}">Client Certificate: <l:star/></label></th>
        <td><props:multilineProperty name="${cons.clientCertData}"
                                     className="longField"
                                     linkTitle="Enter X509 Client Sertificate Content"
                                     cols="35" rows="3"
                                     expanded="true"/>
            <span id="error_${cons.clientCertData}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden token auth-ui">
        <th><label for="secure:${cons.authToken}">Token: <l:star/></label></th>
        <td><props:passwordProperty name="secure:${cons.authToken}" className="longField"/>
            <span id="error_secure:${cons.authToken}" class="error"></span>
            <span class="smallNote">Bearer Token.</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"></th>
        <td class="noBorder">
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
                <th class="name">Image Description</th>
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
        <tr class="advancedSetting">
            <th><label for="${cons.agentNamePrefix}">Agent name prefix:</label></th>
            <td><input type="text" id="${cons.agentNamePrefix}" class="longField configParam"/>
                <span id="error_${cons.agentNamePrefix}" class="error option-error option-error_${cons.agentNamePrefix}"></span>
                <span class="smallNote">If no or incorrect prefix provided, default value <strong>KUBE</strong> will be used</span>
            </td>
        </tr>
        <tr>
            <th><label for="${cons.podSpecMode}">Pod Specification: <l:star/></label></th>
            <td>
                <div>
                    <c:set var="selectedPodSpecMode" value="${propertiesBean.properties[cons.podSpecMode]}" />
                    <props:selectProperty name="${cons.podSpecMode}" className="longField">
                        <props:option value="notSelected" selected="${empty selectedPodSpecMode}">--- Choose what you need ---</props:option>
                        <c:forEach var="podTemplateProvider" items="${podTemplateProviders}">
                            <props:option value="${podTemplateProvider.id}" selected="${not empty selectedPodSpecMode and podTemplateProvider.id eq selectedPodSpecMode}"><c:out value="${podTemplateProvider.displayName}"/></props:option>
                        </c:forEach>
                    </props:selectProperty>
                    <span class="error option-error option-error_${cons.podSpecMode}"></span>
                </div>
                <c:forEach var="podTemplateProvider" items="${podTemplateProviders}">
                    <c:set var="description" value="${podTemplateProvider.description}"/>
                    <c:if test="${not empty description}">
                        <span class="smallNote"><c:out value="${description}"/></span>
                    </c:if>
                </c:forEach>
            </td>
        </tr>
        <tr class="hidden simple pod-spec-ui">
            <th>Docker image:&nbsp;<l:star/></th>
            <td>
                <div>
                    <input type="text" id="${cons.dockerImage}" value="" class="longField" data-id="${cons.dockerImage}" data-err-id="${cons.dockerImage}"/>
                    <div class="smallNoteAttention">Docker image name to use.</div>
                    <span class="error option-error option-error_${cons.dockerImage}"></span>
                </div>
            </td>
        </tr>
        <tr class="hidden simple pod-spec-ui">
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
        <tr class="hidden simple pod-spec-ui">
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
        <tr class="hidden simple pod-spec-ui">
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
        <tr class="hidden deployment-base pod-spec-ui">
            <th>Deployment name:&nbsp;<l:star/></th>
            <td>
                <div>
                    <input type="text" id="${cons.sourceDeployment}" value="" class="longField" data-id="${cons.sourceDeployment}" data-err-id="${cons.sourceDeployment}"/>
                    <i class="icon-magic" style="cursor:pointer;" title="Choose deployment" onclick="BS.Kube.DeploymentChooser.showPopup(this, '<c:url value="${deploymentChooserUrl}"/>')"></i>
                    <div class="smallNoteAttention">Deployment to use as a pod template.</div>
                    <span class="error option-error option-error_${cons.sourceDeployment}"></span>
                </div>
            </td>
        </tr>
        <tr class="advancedSetting">
            <th>Max number of instances:</th>
            <td>
                <div>
                    <input type="text" id="${cons.maxInstances}" value="" class="longField" data-id="${cons.maxInstances}" data-err-id="${cons.maxInstances}"/>
                </div>
                <span class="error option-error option-error_${cons.maxInstances}"></span>
            </td>
        </tr>
        <tr>
            <th><label for="${cons.agentPoolIdField}">Agent pool: <l:star/></label></th>
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

    <admin:showHideAdvancedOpts containerId="KubeImageDialog" optsKey="kubeCloudSettings"/>
    <admin:highlightChangedFields containerId="KubeImageDialog"/>

    <div class="popupSaveButtonsBlock">
        <forms:submit label="Add" type="button" id="kubeAddImageButton"/>
        <forms:button title="Cancel" id="kubeCancelAddImageButton">Cancel</forms:button>
    </div>
</bs:dialog>

<bs:dialog dialogId="KubeDeleteImageDialog" title="Delete Kubernetes Cloud Image" closeCommand="BS.Kube.DeleteImageDialog.close()"
           dialogClass="KubeDeleteImageDialog" titleId="KubeDeleteImageDialogTitle">
    <div id="deleteImageDialogText"><p>Are you sure you want to remove the image %image name%?</p></div>
    <div id="deleteImageDialogTerminateInstances">
        Following cloud instance(s) will be terminated
        <ul>
            <li>ec2-54-88-47-151-5</li>
            <li>ec2-54-88-47-151-6</li>
            <li>ec2-54-88-47-151-7</li>
        </ul>
    </div>
    <div class="popupSaveButtonsBlock">
        <forms:submit label="Delete" type="button" id="kubeDeleteImageButton"/>
        <forms:button title="Cancel" id="kubeCancelDeleteImageButton">Cancel</forms:button>
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