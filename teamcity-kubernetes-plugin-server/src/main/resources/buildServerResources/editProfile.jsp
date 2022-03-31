<%@ page import="jetbrains.buildServer.clouds.kubernetes.connector.ImagePullPolicy" %>
<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<%--
  ~ Copyright 2000-2022 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="cons" class="jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="agentPools" scope="request" type="java.util.Collection<jetbrains.buildServer.serverSide.agentPools.AgentPool>"/>
<jsp:useBean id="authStrategies" scope="request" type="java.util.Collection<jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategy>"/>
<jsp:useBean id="additionalSettings" scope="request" type="java.util.Map<java.lang.String, java.lang.Object>"/>
<jsp:useBean id="podTemplateProviders" scope="request" type="java.util.Collection<jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider>"/>

<jsp:useBean id="testConnectionUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="namespaceChooserUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="deploymentChooserUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="deleteImageUrl" class="java.lang.String" scope="request"/>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}kubeSettings.css'/>");
</script>

</table>

<table class="runnerFormTable">
    <tr class="common-settings hide-kubeconfig">
        <th><label for="${cons.apiServerUrl}">Kubernetes API server URL:<l:star/></label></th>
        <td><props:textProperty name="${cons.apiServerUrl}" className="longField"/>
            <span id="error_${cons.apiServerUrl}" class="error"></span>
            <span class="smallNote">Target Kubernetes API server URL</span>
        </td>
    </tr>
    <tr class="common-settings hide-kubeconfig">
        <th><label for="${cons.caCertData}">Certificate Authority (CA):</label></th>
        <td><props:multilineProperty name="secure:${cons.caCertData}"
                                     className="longField"
                                     expanded="false"
                                     linkTitle="Enter CA certificate content"
                                     cols="35" rows="3"/>
            <span id="error_${cons.caCertData}" class="error"></span>
            <span class="smallNote">Leave empty to skip TLS verification (insecure option)</span>
        </td>
    </tr>
    <tr class="common-settings hide-kubeconfig">
        <th><label for="${cons.kubernetesNamespace}">Kubernetes namespace: </label></th>
        <td>
            <div style="white-space: nowrap">
                <props:textProperty name="${cons.kubernetesNamespace}" className="longField">
                    <jsp:attribute name="afterTextField"><i class="icon-magic" style="cursor:pointer;" title="Choose namespace" onclick="BS.Kube.NamespaceChooser.showPopup(this, '<c:url value="${namespaceChooserUrl}"/>')"></i></jsp:attribute>
                </props:textProperty>
            </div>
            <span id="error_${cons.kubernetesNamespace}" class="error"></span>
            <span class="smallNote">Kubernetes namespace to use. Leave empty to use the default namespace.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${cons.authStrategy}">Authentication strategy:<l:star/></label></th>
        <td>
            <div>
                <c:set var="selectedAuthStrategy" value="${propertiesBean.properties[cons.authStrategy]}" />
                <props:selectProperty name="${cons.authStrategy}" className="longField">
                    <props:option value="" selected="${empty selectedAuthStrategy}">--- Choose authentication strategy ---</props:option>
                    <c:forEach var="authStrategy" items="${authStrategies}">
                        <props:option value="${authStrategy.id}" selected="${not empty selectedAuthStrategy and authStrategy.id eq selectedAuthStrategy}"><c:out value="${authStrategy.displayName}"/></props:option>
                    </c:forEach>
                </props:selectProperty>
                <span id="error_${cons.authStrategy}" class="error"></span>
            </div>
        </td>
    </tr>
    <tr class="hidden user-passwd auth-ui">
        <th><label for="${cons.username}">Username:<l:star/></label></th>
        <td><props:textProperty name="${cons.username}" className="longField"/>
            <span id="error_${cons.username}" class="error"></span>
            <span class="smallNote">Username of an authorized Kubernetes user</span>
        </td>
    </tr>
    <tr class="hidden user-passwd auth-ui">
        <th><label for="secure:${cons.password}">Password:<l:star/></label></th>
        <td><props:passwordProperty name="secure:${cons.password}" className="longField"/>
            <span id="error_secure:${cons.password}" class="error"></span>
            <span class="smallNote">Password of an authorized Kubernetes user</span>
        </td>
    </tr>
    <tr class="hidden client-cert auth-ui">
        <th><label for="${cons.clientCertData}">Client certificate:<l:star/></label></th>
        <td><props:multilineProperty name="secure:${cons.clientCertData}"
                                     className="longField"
                                     linkTitle="Enter client certificate content"
                                     expanded="false"
                                     cols="35" rows="3"/>
            <span id="error_${cons.clientCertData}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden client-cert auth-ui">
        <th><label for="${cons.clientKeyData}">Client Key:<l:star/></label></th>
        <td><props:multilineProperty name="secure:${cons.clientKeyData}"
                                     expanded="false"
                                     className="longField"
                                     linkTitle="Enter client key content"
                                     cols="35" rows="3"/>
            <span id="error_${cons.clientKeyData}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden token auth-ui">
        <th><label for="secure:${cons.authToken}">Token:<l:star/></label></th>
        <td>
            <props:passwordProperty name="secure:${cons.authToken}" className="longField"/>
            <span id="error_secure:${cons.authToken}" class="error"></span>
            <span class="smallNote">Bearer token</span>
        </td>
    </tr>
    <tr class="hidden oidc auth-ui">
        <th><label for="${cons.oidcIssuerUrl}">IdP issuer URL:<l:star/></label></th>
        <td><props:textProperty name="${cons.oidcIssuerUrl}" className="longField"/>
            <span id="error_${cons.oidcIssuerUrl}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden oidc auth-ui">
        <th><label for="${cons.oidcClientId}">Client ID:<l:star/></label></th>
        <td><props:textProperty name="${cons.oidcClientId}" className="longField"/>
            <span id="error_${cons.oidcClientId}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden oidc auth-ui">
        <th><label for="secure:${cons.oidcClientSecret}">Client secret:<l:star/></label></th>
        <td>
            <props:passwordProperty name="secure:${cons.oidcClientSecret}" className="longField"/>
            <span id="error_secure:${cons.oidcClientSecret}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden oidc auth-ui">
        <th><label for="secure:${cons.oidcRefreshToken}">Refresh token:<l:star/></label></th>
        <td>
            <props:passwordProperty name="secure:${cons.oidcRefreshToken}" className="longField"/>
            <span id="error_secure:${cons.oidcRefreshToken}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden eks auth-ui">
        <th><label for="${cons.eksUseInstanceProfile}">Use server instance profile: </label></th>
        <td><props:checkboxProperty name="${cons.eksUseInstanceProfile}"/>
            <span id="error_${cons.eksUseInstanceProfile}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden eks auth-ui aws-credential">
        <th><label for="${cons.eksAccessId}">Access ID:<l:star/></label></th>
        <td><props:textProperty name="${cons.eksAccessId}" className="longField"/>
            <span id="error_${cons.eksAccessId}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden eks auth-ui aws-credential">
        <th><label for="secure:${cons.eksSecretKey}">Secret Key:<l:star/></label></th>
        <td>
            <props:passwordProperty name="secure:${cons.eksSecretKey}" className="longField"/>
            <span id="error_secure:${cons.eksSecretKey}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden eks auth-ui">
        <th><label for="${cons.eksAssumeIamRole}">Assume an IAM role: </label></th>
        <td><props:checkboxProperty name="${cons.eksAssumeIamRole}"/>
            <span id="error_${cons.eksAssumeIamRole}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden eks auth-ui aws-iam">
        <th><label for="${cons.eksIAMRoleArn}">IAM role ARN:<l:star/></label></th>
        <td><props:textProperty name="${cons.eksIAMRoleArn}" className="longField"/>
            <span id="error_${cons.eksIAMRoleArn}" class="error"></span>
        </td>
    </tr>
    <tr class="hidden eks auth-ui">
        <th><label for="${cons.eksClusterName}">Cluster name:<l:star/></label></th>
        <td><props:textProperty name="${cons.eksClusterName}" className="longField"/>
            <span id="error_${cons.eksClusterName}" class="error"></span>
        </td>
    </tr>

    <tr class="hidden kubeconfig auth-ui">
        <th><label for="${cons.kubeconfigContext}">Current Context:<l:star/></label></th>
        <td>
            <c:set var="currentContext"><c:out value="${additionalSettings.get('currentContext')}"/></c:set>
            <c:if test="${currentContext != null}">
                <c:set var="contextNames"><c:out value="${additionalSettings.get('contextNames')}"/></c:set>
                <c:set var="selectedContext"
                ><c:if test="${not empty propertiesBean.properties[cons.kubeconfigContext]}"
                ><c:out value="${propertiesBean.properties[cons.kubeconfigContext]}" /></c:if
                ><c:if test="${empty propertiesBean.properties[cons.kubeconfigContext]}"
                ><c:out value="" /></c:if></c:set>
                <props:selectProperty name="${cons.kubeconfigContext}" id="${cons.kubeconfigContext}" enableFilter="${true}">
                    <props:option value="" selected="${'' eq selectedContext}"><c:out value="Current context (${currentContext})"/></props:option>
                    <c:forEach var="context" items="${contextNames}">
                        <props:option value="${context}" selected="${context eq selectedContext}"
                        ><c:out value="${context}"/></props:option>
                    </c:forEach>
                </props:selectProperty>
                <span id="error_${cons.eksClusterName}" class="error"></span>
            </c:if>
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
            <props:textProperty name="${cons.profileInstanceLimit}" className="settings longField"/>
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
                <th class="name">Image description</th>
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
            </td>
        </tr>
        <tr>
            <th><label for="${cons.podSpecMode}">Pod specification:<l:star/></label></th>
            <td>
                <div>
                    <c:set var="selectedPodSpecMode" value="${propertiesBean.properties[cons.podSpecMode]}" />
                    <props:selectProperty name="${cons.podSpecMode}" className="longField">
                        <props:option value="notSelected" selected="${empty selectedPodSpecMode}"><c:out value="<Select pod specification>"/></props:option>
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
                    <div class="smallNoteAttention">Docker image name to use</div>
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
                    <div class="smallNoteAttention">Policy to use by kubelet to pull an image <a
                        href="https://kubernetes.io/docs/concepts/containers/images/#updating-images"
                        target="_blank" rel="noreferrer"><bs:helpIcon/></a></div>
                    <span class="error option-error option-error_${cons.imagePullPolicy}"></span>
                </div>
            </td>
        </tr>
        <tr class="hidden simple pod-spec-ui">
            <th>Command:</th>
            <td>
                <div>
                    <input type="text" id="${cons.dockerCommand}" value="" class="longField" data-id="${cons.dockerCommand}" data-err-id="${cons.dockerCommand}"/>
                    <div class="smallNoteAttention">Command to use. Leave empty to use the Docker image's 'ENTRYPOINT'. <a
                        href="https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#notes" target="_blank"
                        rel="noreferrer"><bs:helpIcon/></a></div>
                    <span class="error option-error option-error_${cons.dockerCommand}"></span>
                </div>
            </td>
        </tr>
        <tr class="hidden simple pod-spec-ui">
            <th>Command arguments:</th>
            <td>
                <div>
                    <input type="text" id="${cons.dockerArguments}" value="" class="longField" data-id="${cons.dockerArguments}" data-err-id="${cons.dockerArguments}"/>
                    <div class="smallNoteAttention">Arguments for the command. Leave empty to use the Docker image's 'CMD'. <a
                        href="https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#notes" target="_blank"
                        rel="noreferrer"><bs:helpIcon/></a></div>
                    <span class="error option-error option-error_${cons.dockerArguments}"></span>
                </div>
            </td>
        </tr>
        <tr class="hidden custom-pod-template pod-spec-ui">
            <th><label for="${cons.customPodTemplate}">Pod template content:<l:star/></label></th>
            <td class="codeHighlightTD">
                <props:multilineProperty highlight="yaml" expanded="${true}" name="${cons.customPodTemplate}" rows="10" cols="30"
                                         linkTitle="Edit the custom pod's YAML content" className="longField"/>
                <span class="error option-error option-error_${cons.customPodTemplate}"></span>
            </td>
        </tr>
        <tr class="hidden deployment-base pod-spec-ui">
            <th>Deployment name:&nbsp;<l:star/></th>
            <td>
                <div style="white-space: nowrap">
                    <input type="text" id="${cons.sourceDeployment}" value="" class="longField" data-id="${cons.sourceDeployment}" data-err-id="${cons.sourceDeployment}"/>
                    <i class="icon-magic" style="cursor:pointer;" title="Choose deployment" onclick="BS.Kube.DeploymentChooser.showPopup(this, '<c:url value="${deploymentChooserUrl}"/>')"></i>
                </div>
                <div class="smallNoteAttention">Deployment to use as a pod template</div>
                <span class="error option-error option-error_${cons.sourceDeployment}"></span>
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
            <th><label for="${cons.agentPoolIdField}">Agent pool:<l:star/></label></th>
            <td>
                <select id="${cons.agentPoolIdField}" data-id="${cons.agentPoolIdField}" class="longField configParam">
                    <props:option value=""><c:out value="<Select agent pool>"/></props:option>
                    <c:forEach var="ap" items="${agentPools}">
                        <props:option selected="${ap.agentPoolId eq propertiesBean.properties['agent_pool_id']}" value="${ap.agentPoolId}"><c:out value="${ap.name}"/></props:option>
                    </c:forEach>
                </select>
                <span class="error option-error option-error_${cons.agentPoolIdField}"></span>
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

    <div id="kubeDeleteImageDialogBody"></div>

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
            BS.Kube.DeleteImageDialog.url = '<c:url value="${deleteImageUrl}"/>';
            BS.Kube.ProfileSettingsForm.initialize();
        }
    });
</script>

<table class="runnerFormTable" style="margin-top: 3em;">