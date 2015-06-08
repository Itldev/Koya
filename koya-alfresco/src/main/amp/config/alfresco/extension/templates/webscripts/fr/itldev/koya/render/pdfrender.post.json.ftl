<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#list result?keys as key>
      <#assign value = result[key]>
      <#if value?is_number || value?is_boolean>
   "${key}": ${value?string}<#if key_has_next>,</#if>
      <#else>
   "${key}": "${value?string}"<#if key_has_next>,</#if>
      </#if>
   </#list>
}
</#escape>