<%@ page import="jetbrains.buildServer.clouds.kubernetes.connection.KubernetesConnectionConstants" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>

<c:set var="display_name_param" value="<%=KubernetesConnectionConstants.DISPLAY_NAME_PARAM%>"/>


<tr>
  <th><label for="${display_name_param}">Display name:</label><l:star/></th>
  <td>
    <props:textProperty name="${display_name_param}" className="longField"/>
    <span class="smallNote nowrap">Provide a name to distinguish this connection from others</span>
    <span class="error" id="error_displayName"></span>
  </td>
</tr>

<jsp:include page="connection/connectionParams.jsp"/>
