<#function artifactFormat p>

<#-- Declare missingLicenses[] and missingLicenseUrls[] -->

    <#assign missingLicenses = [
    ["asm:asm", "BSD 3-Clause"],
    ["asm:asm-commons", "BSD 3-Clause"],
    ["asm:asm-tree", "BSD 3-Clause"],
    ["com.cenqua.shaj:shaj", "Apache 2.0"],
    ["commons-codec:commons-codec", "Apache 2.0"],
    ["cglib:cglib-nodep", "Apache 2.0"],
    ["javax.servlet:jstl", "CDDL 1.1 / GPL 2.0"],
    ["jdom:jdom", "Apache 2.0"],
    ["nekohtml:nekohtml", "Apache 2.0"],
    ["org.antlr:antlr-runtime", "BSD 3-Clause"],
    ["org.eclipse.mylyn.github:org.eclipse.egit.github.core", "Eclipse Public License 1.0"],
    ["oro:oro", "Apache 2.0"]
    ]/>

    <#assign missingLicenseUrls = [
    ["Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0.txt"],
    ["BSD 3-Clause", "https://asm.ow2.io/license.html"],
    ["CDDL 1.1 / GPL 2.0", "https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html"],
    ["Eclipse Public License 1.0", "http://www.eclipse.org/legal/epl-v10.html"]
    ]/>

    <#assign result = ""/>

<#-- Fill in missing license names and urls from missingLicenses[] and missingLicenseUrls[] -->

    <#if p.licenses?size == 0>

        <#assign licenseName = ""/>
        <#assign licenseUrl = ""/>


        <#list missingLicenses as l1>
            <#if l1[0] == "${p.groupId}:${p.artifactId}">
                <#assign licenseName = l1[1]/>
                <#list missingLicenseUrls as l2>
                    <#if l1[1] == l2[0]>
                        <#assign licenseUrl = l2[1]/>
                    </#if>
                </#list>
            </#if>
        </#list>


	    <#assign result>
 		${result}
   {
      "name": "${p.groupId!""}:${p.artifactId!""}",
      "version": "${p.version!""}",
      "url": "${p.url!""}",
      "license": "${licenseName!""}",
      "licenseUrl": "${licenseUrl!""}"
   },
	    </#assign>

    <#else>

    <#-- Iterate on automatically detected licenses -->

        <#list p.licenses as l>
            <#assign licenseName = ""/>
            <#assign licenseUrl = ""/>

        <#-- Divide license by new lines, because one l.name can store several licenses -->

            <#assign siblingLicenses = (l.name)?split("\n")>
            <#assign siblingLicenseUrls = (l.url!"")?split("\n")>
            <#list siblingLicenses as siblingLicense>
                <#assign licenseName = siblingLicense?trim/>
                <#assign licenseUrl = siblingLicenseUrls[siblingLicense?index]?trim/>
                <#if licenseName?ends_with(" and")>
                    <#assign licenseName = licenseName?substring(0, licenseName?length - 4)>
                </#if>
                <#if licenseUrl?ends_with(" and")>
                    <#assign licenseUrl = licenseUrl?substring(0, licenseUrl?length - 4)>
                </#if>
		        <#assign result>
                    ${result}
   {
      "name": "${p.groupId!""}:${p.artifactId!""}",
      "version": "${p.version!""}",
      "url": "${p.url!""}",
      "license": "${licenseName!""}",
      "licenseUrl": "${licenseUrl!""}"
   },
		        </#assign>
            </#list>
        </#list>
   </#if>

    <#return result>
</#function>

<#if dependencyMap?size == 0>
	<#else>
[
  <#list dependencyMap as e>
      <#assign project = e.getKey()/>
      <#assign licenses = e.getValue()/>
${artifactFormat(project)}
  </#list>
]
</#if>