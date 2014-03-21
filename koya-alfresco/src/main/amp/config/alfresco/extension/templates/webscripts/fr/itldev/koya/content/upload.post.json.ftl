<#escape x as jsonUtils.encodeJSONString(x)>
	{              
                "filename" : "${upload.name}",
                "size" : "${upload.properties.content.size}"                
	}
</#escape>