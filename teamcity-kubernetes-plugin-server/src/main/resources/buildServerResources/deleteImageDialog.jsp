<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include.jsp" %>

<jsp:useBean id="image" scope="request" type="jetbrains.buildServer.clouds.CloudImage"/>
<jsp:useBean id="instances" scope="request" type="java.util.Collection<jetbrains.buildServer.clouds.CloudInstance>"/>

<p>Are you sure you want to remove the image <b>${image.name}?</b></p>
<c:if test="${not empty instances}">
    Following cloud instance(s) will be terminated
    <ul>
        <c:forEach var="instance" items="${instances}">
            <li><c:out value="${instance.name}" /></li>
        </c:forEach>
    </ul>
</c:if>