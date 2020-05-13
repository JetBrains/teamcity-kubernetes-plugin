<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

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
<jsp:useBean id="teamcityPluginResourcesPath" scope="request" type="java.lang.String"/>
<jsp:useBean id="helmParametersBean" class="jetbrains.buildServer.helm.HelmParametersBean"/>
<jsp:useBean id="cons" class="jetbrains.buildServer.helm.HelmConstants"/>

<c:set var="commandTitle">Command:<bs:help urlPrefix="https://docs.helm.sh/helm/#helm" file=""/></c:set>
<script>
    BS.Helm = {
      Comments : {},

       onHelmCommandChange: function() {
         var command = $j('#command').val();
         $j('.helmOption').hide();
         if (command !== '') {
          $j('.'+command).show();
          if (this.Comments[command]['${cons.chartKey}']){
            $j('.chartNote').text(this.Comments[command]['${cons.chartKey}'])
          }
          if (this.Comments[command]['${cons.releaseName}']){
            $j('.releaseNote').text(this.Comments[command]['${cons.releaseName}'])
          }
          if (this.Comments[command]['${cons.revision}']){
            $j('.revisionNote').text(this.Comments[command]['${cons.revision}'])
          }
          $j('.commandText').text(BS.Helm.Comments[command]['commandText']);
         }
       }
    };
    $j(document).ready(function(){
      <c:forEach items="${helmParametersBean.commands}" var="command">
      BS.Helm.Comments['${command.id}'] = {};
        <c:if test="${helmParametersBean.getNote(command.id, cons.chartKey) != null}"
        >BS.Helm.Comments['${command.id}']['${cons.chartKey}'] = '${helmParametersBean.getNote(command.id, cons.chartKey)}';
        </c:if><c:if test="${helmParametersBean.getNote(command.id, cons.releaseName) != null}"
        >BS.Helm.Comments['${command.id}']['${cons.releaseName}'] = '${helmParametersBean.getNote(command.id, cons.releaseName)}';
        </c:if><c:if test="${helmParametersBean.getNote(command.id, cons.revision) != null}"
        >BS.Helm.Comments['${command.id}']['${cons.revision}'] = '${helmParametersBean.getNote(command.id, cons.revision)}';
        </c:if>
      BS.Helm.Comments['${command.id}']['commandText'] = '${helmParametersBean.getAdditionalArgsNote(command.id)}';
      </c:forEach>
      BS.Helm.onHelmCommandChange();
    });

</script>

<tr class="dockerCommandSelector">
    <th>Helm command:</th>
    <td>
        <props:selectProperty onchange="BS.Helm.onHelmCommandChange()" name="${helmParametersBean.commandKey}" enableFilter="true" className="mediumField">
            <props:option value=""><c:out value="-- Choose Helm command --"/></props:option>
            <c:forEach items="${helmParametersBean.commands}" var="command">
                <props:option value="${command.id}"><c:out value="${command.displayName}"/></props:option>
            </c:forEach>
        </props:selectProperty>
    </td>
</tr>

<tr class="helmOption ${cons.installCommandId} ${cons.upgradeCommandId}">
    <th><label for="${cons.chartKey}">Helm chart: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${cons.chartKey}" className="longField"/>
        <bs:vcsTree fieldId="${cons.chartKey}" treeId="${cons.chartKey}"/>
      </span>
        <span class="smallNote chartNote"></span>
        <span class="error" id="error_${cons.chartKey}"></span>
    </td>
</tr>

<tr class="helmOption ${cons.deleteCommandId} ${cons.rollbackCommandId} ${cons.testCommandId} ${cons.upgradeCommandId}">
    <th><label for="${cons.releaseName}">Release name: <l:star/></label></th>
    <td>
        <span>
            <props:textProperty name="${cons.releaseName}" className="longField"/>
        </span>
        <span class="error" id="error_${cons.releaseName}"></span>
        <span class="smallNote releaseNote"></span>
    </td>
</tr>

<tr class="helmOption ${cons.deleteCommandId} ${cons.rollbackCommandId} ${cons.testCommandId} ${cons.upgradeCommandId}">
    <th><label for="${cons.revision}">Revision: <l:star/></label></th>
    <td>
      <span>
        <props:textProperty name="${cons.revision}" className="longField"/>
      </span>
        <span class="smallNote revisionNote"></span>
        <span class="error" id="error_${cons.revision}"></span>
    </td>
</tr>

<tr class="advancedSetting helmOption ${cons.deleteCommandId} ${cons.installCommandId} ${cons.rollbackCommandId} ${cons.testCommandId} ${cons.upgradeCommandId}">
  <th><label for="${cons.additionalFlagsKey}">Additional '<span class="commandText"></span>' command flags: </label></th>
    <td><props:textProperty name="${cons.additionalFlagsKey}" className="longField"/>
        <span class="error" id="error_${cons.additionalFlagsKey}"></span>
        <span class="smallNote">Additional options for the '<span class="commandText"></span>' command line</span>
    </td>
</tr>