<#--

    Koya is an alfresco module that provides a corporate orientated dataroom.

    Copyright (C) Itl Developpement 2014

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see `<http://www.gnu.org/licenses/>`.

-->

<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/create-company/create-company.css" group="console"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/console/consoletool.js" group="console"/>
   <@script src="${url.context}/res/components/create-company/create-company.js" group="console"/>
</@>


<@markup id="widgets">
   <@createWidgets group="console"/>
</@>

<@markup id="html">
  <@uniqueIdDiv>
    <#assign el=args.htmlid?html>
      <div id="${el}-body" class="create-company">
        <div id="${el}-create-company" class="hidden">
	  <div class="hd">
	    <@markup id="title">${msg("header.createCompany")}</@markup>
	  </div>
	  <div class="bd">
	    <form id="${el}-create-company-form" method="POST" action="${url.context}/service/components/console/create-company">
              <#-- FIELDS -->
	      <@markup id="fields">
	        <#-- TITLE -->
	        <div class="yui-gd">
	           <div class="yui-u first"><label for="${el}-create-company-title">${msg("label.name")}:</label></div>
	           <div class="yui-u"><input id="${el}-create-company-title" type="text" name="title" tabindex="0" maxlength="255" />&nbsp;*</div>
	        </div>
                <#-- SALESOFFER -->
	        <div class="yui-gd">
	           <div class="yui-u first"><label for="${el}-create-company-salesOffer">${msg("label.offer")}:</label></div>
	           <div class="yui-u">
	              <select id="${el}-create-company-salesOffer" name="salesOffer" tabindex="0">
	                 <#list salesOffers as salesOffer>
	                    <option value="${salesOffer.donnees.name}">${salesOffer.donnees.name}</option>
	                 </#list>
	              </select>
	           </div>
	        </div>
                <#-- SPACES TEMPLATES -->
	        <div class="yui-gd">
	           <div class="yui-u first"><label for="${el}-create-company-spacesTemplate">${msg("label.template")}:</label></div>
	           <div class="yui-u">
	              <select id="${el}-create-company-spaceTemplate" name="spaceTemplate" tabindex="0">
	                 <#list spaceTemplates as spaceTemplate>
	                    <option value="${spaceTemplate.name}">${spaceTemplate.name}</option>
	                 </#list>
	              </select>
	           </div>
	        </div>
	        <#-- SITEPRESET -->
	        <div class="yui-gd">
	           <div class="yui-u first"><label for="${el}-create-company-sitePreset">${msg("label.type")}:</label></div>
	           <div class="yui-u">
	              <select id="${el}-create-company-sitePreset" name="sitePreset" tabindex="0">
	                 <#list sitePresets as sitePreset>
	                    <option value="${sitePreset.id}">${sitePreset.name}</option>
	                 </#list>
	              </select>
	           </div>
	        </div>
	      </@markup>

	      <div class="bdft">
	        <#-- BUTTONS -->
	        <input type="submit" id="${el}-create-company-ok-button" value="${msg("button.ok")}" tabindex="0"/>
	        <input type="button" id="${el}-create-company-cancel-button" value="${msg("button.cancel")}" tabindex="0"/>
	      </div>
	    </form>
	  </div>
	</div>
      </div>
    </div>
  </@>
</@>
