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
<jsp:useBean id="podTemplateProviders" scope="request" type="java.util.Collection<jetbrains.buildServer.clouds.kubernetes.podSpec.BuildAgentPodTemplateProvider>"/>

<jsp:useBean id="namespaceChooserUrl" class="java.lang.String" scope="request"/>
<jsp:useBean id="deploymentChooserUrl" class="java.lang.String" scope="request"/>

<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}kubeSettings.css'/>");
</script>


<table class="runnerFormTable">
    <jsp:include page="editConnection.jsp"/>
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


<table class="runnerFormTable" style="margin-top: 3em;">