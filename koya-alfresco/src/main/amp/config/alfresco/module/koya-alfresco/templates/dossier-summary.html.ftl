<#macro recurse_macro node depth>
  <#if node.isContainer>
    <tr>
	<td align='left'>
	   <#if (depth>0) >
	     <#if (depth>1) >
		 	<#list 2..depth as i>...</#list>
		  </#if>
		  <!-- <img src="/alfresco/images/icons/folder_large.png"> -->
		  ${node.properties.title!node.properties.name}
	   </#if>
	</td>
  </tr>
   <#list node.children as child>
	<#if child.isContainer && node.children?size != 0 >
		 <@recurse_macro node=child depth=depth+1/>
	</#if>
   </#list>
  </#if>
 </#macro>

<html>
	<head>
		<style>
			.global{width:227px;}
		</style>
		<meta content="text/html; charset=utf-8" http-equiv="Content-Type">		
    </head>
    <body>
    	<div styleClass="global">
	    	<h1>Dossier ${dossier.properties.title!dossier.properties.name}</h1>
	        <table> 
	               <@recurse_macro node=dossier depth=0/>
	        </table>
        </div>
    </body>
</html>
