<#escape x as jsonUtils.encodeJSONString(x)>
	{       
                 <#if error?has_content>
                "error" : "${error}",
                </#if>
                "filename" : "${filename}",
                "size" : "${size}"
                
	}
</#escape>