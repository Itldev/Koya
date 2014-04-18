{ 
  "items" : [
    <#list children as child>
    {
        "isfolder" : <#if child.isContainer>true<#else>false</#if>,
        "noderef" : "${child.nodeRef}",
        "path" : "${child.qnamePath}",
        "name" : "${child.name}"
    }<#if child_has_next>,</#if>
  </#list>
  ]
}
