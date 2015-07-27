<#macro recurse_macro node depth>
    <#if node.isContainer>
        
        <#list node.children as child>
            <#if child.isContainer>
                <li class="directory"><span class="niveau${depth}">
                    ${child.properties.title!child.properties.name}
                    </span>
            <#else>
                <li class="leaf">
                ${child.properties.title!child.properties.name}
            </#if>
            
            <#if child.isContainer && node.children?size != 0 >
                <ul>
                <@recurse_macro node=child depth=depth+1/>
                 </ul>
            </#if>
            </li>
        </#list>
    </#if>
</#macro>

<html>
    <head>
        <style>
            .leaf{color:#426F98;list-style-type:disc;font-size:12px;margin:0px;}
            .directory{color:#333;list-style-type:none;font-size:15px;margin-top:5px;margin-bottom:10px;}
            .niveau0{font-size:16px;font-weight:bold;}
        </style>
        <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
    </head>
    <body>
    	<div>
            <div style="width:100%;text-align:right;">            
                    <img width="${logo.width}" height="${logo.height}" src="${logo.url}" alt="logo" border="0" />                           
            </div>
            <hr>
            
            <h2>Dossier ${dossier.properties.title!dossier.properties.name}</h1>
            <ul>
                <@recurse_macro node=dossier depth=0/>
            </ul>
        </div>
    </body>
</html>
