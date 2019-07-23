<%@ page import="jetbrains.buildServer.clouds.kubernetes.KubeParametersConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="buildForm"  scope="request" type="jetbrains.buildServer.controllers.admin.projects.EditableBuildTypeSettingsForm"/>
<jsp:useBean id="buildFeature"  scope="request" type="jetbrains.buildServer.clouds.kubernetes.buildFeature.RunInKubeFeature"/>

<tr>
  <td colspan="2">
    <em>Run Build in Kubernetes</em>
  </td>
</tr>
<tr>
  <td colspan="2">
    <div style="float:left">
      <props:selectProperty name="<%=KubeParametersConstants.RUN_IN_KUBE_PARAM%>">
        <props:option value="">No Image Selected</props:option>
        <c:forEach var="imgPair" items="${buildFeature.showProfilesAndImages(buildForm.project)}">
          <props:option value="${imgPair.first}"><c:out value="${imgPair.second}"/></props:option>
        </c:forEach>
      </props:selectProperty>
    </div>
  </td>
</tr>

