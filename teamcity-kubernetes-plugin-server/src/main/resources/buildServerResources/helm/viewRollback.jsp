<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  ~ Copyright 2000-2020 JetBrains s.r.o.
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

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="jetbrains.buildServer.helm.HelmConstantsBean"/>

<div class="parameter">
    Release: <strong><props:displayValue name="${constants.rollbackCommandId}${constants.releaseName}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
    Revision: <strong><props:displayValue name="${constants.rollbackCommandId}${constants.revision}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
    Additional command flags: <strong><props:displayValue name="${constants.rollbackCommandId}${constants.additionalFlagsKey}" emptyValue="not specified"/></strong>
</div>