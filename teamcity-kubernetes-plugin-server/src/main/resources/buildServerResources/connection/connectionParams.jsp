<%@ page import="jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProviderImpl" %>
<%@ page import="jetbrains.buildServer.clouds.kubernetes.web.KubeDeleteImageDialogController" %>
<%@ page import="jetbrains.buildServer.clouds.kubernetes.web.KubeProfileEditController" %>
<%@ include file="/include.jsp" %>

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
<jsp:useBean id="project" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>
<%@ page import="jetbrains.buildServer.clouds.kubernetes.auth.KubeAuthStrategyProviderImpl" %>
<c:set var="testConnectionUrl" value="/plugins/teamcity-kubernetes-plugin/<%=KubeProfileEditController.EDIT_KUBE_HTML%>?testConnection=true"/>
<c:set var="deleteImageUrl" value="/plugins/teamcity-kubernetes-plugin/<%=KubeDeleteImageDialogController.URL%>"/>
<c:set var="authStrategies" value="<%=KubeAuthStrategyProviderImpl.getAll(project.getProjectId())%>"/>
<c:set var="additionalSettings" value="<%=KubeAuthStrategyProviderImpl.getAdditionalSettings(project.getProjectId())%>"/>

<script type="text/javascript">
  BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}kubeSettings.css'/>");
</script>


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
      <c:set var="selectedAuthStrategy" value="${propertiesBean.properties[cons.authStrategy]}"/>
      <props:selectProperty name="${cons.authStrategy}" className="longField">
        <props:option value="" selected="${empty selectedAuthStrategy}">--- Choose authentication strategy ---</props:option>
        <c:forEach var="authStrategy" items="${authStrategies}">
          <props:option value="${authStrategy.id}" selected="${not empty selectedAuthStrategy and authStrategy.id eq selectedAuthStrategy}"><c:out
              value="${authStrategy.displayName}"/></props:option>
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
      <c:set var="selectedContext"
      ><c:if test="${not empty propertiesBean.properties[cons.kubeconfigContext]}"
      ><c:forEach var="ctx" items="${additionalSettings.get('contextNames')}"
      ><c:if test="${ctx eq propertiesBean.properties[cons.kubeconfigContext]}"
      ><c:out value="${propertiesBean.properties[cons.kubeconfigContext]}"/></c:if
      ></c:forEach></c:if
      ><c:if test="${empty propertiesBean.properties[cons.kubeconfigContext]}"
      ><c:out value=""/></c:if></c:set>
      <c:if test="${(not empty propertiesBean.properties[cons.kubeconfigContext]) and (empty selectedContext)}"
      ><c:set var="selectedContext" value="EMPTY_VALUE"/></c:if>
      <props:selectProperty name="${cons.kubeconfigContext}" id="${cons.kubeconfigContext}" enableFilter="${true}">
        <c:if test="${selectedContext eq 'EMPTY_VALUE'}">
          <props:option value="EMPTY_VALUE" selected="${true}">&lt;Please select&gt;</props:option>
        </c:if>
        <props:option value="" selected="${'' eq selectedContext}"><c:out value="Current context (${currentContext})"/></props:option>
        <c:forEach var="context" items="${additionalSettings.get('contextNames')}">
          <props:option value="${context}" selected="${context eq selectedContext}"
          ><c:out value="${context}"/></props:option>
        </c:forEach>
      </props:selectProperty>
      <span id="error_${cons.kubeconfigContext}" class="error"></span>
    </c:if>
  </td>
</tr>
<tr>
  <th class="noBorder"></th>
  <td class="noBorder">
    <forms:button id="kubeTestConnectionButton" onclick="BS.Kube.ProfileSettingsForm.testConnection();">Test connection</forms:button>
  </td>
</tr>

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
      BS.Kube.DeleteImageDialog.url = '<c:url value="${deleteImageUrl}"/>';
      BS.Kube.ProfileSettingsForm.initialize();
    }
  });
</script>