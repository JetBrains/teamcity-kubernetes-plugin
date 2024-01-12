<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>



<script type="text/javascript">
    BS.LoadStyleSheetDynamically("<c:url value='${teamcityPluginResourcesPath}kubeSettings.css'/>");
</script>

<jsp:useBean id="namespaces" scope="request" type="java.util.Collection<java.lang.String>"/>
<jsp:useBean id="error" scope="request" type="java.lang.String"/>

<c:choose>
    <c:when test="${not empty error}">
        <span class="testConnectionFailed">Unable to list namespaces:</span><br/>
        <span><c:out value="${error}"/></span>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${empty namespaces}">
                No namespaces found
            </c:when>
            <c:otherwise>
                <ul class="chooser">
                    <c:forEach var="namespace" items="${namespaces}">
                        <li><a style="cursor:pointer;" onclick="BS.Kube.NamespaceChooser.selectNamespace('${namespace}')"><c:out value="${namespace}"/></a></li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>