// --
//  Koya is an alfresco module that provides a corporate orientated dataroom.
//
//  Copyright (C) Itl Developpement 2014
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Affero General Public License as
//  published by the Free Software Foundation, either version 3 of the
//  License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License
//  along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
// --


function main()
{
    var sitePresets = [{id: "site-dashboard", name: msg.get("title.collaborationSite")}];
    model.sitePresets = sitePresets;

// Get list of salesOffers:
    var connector = remote.connect("alfresco"),
            remoteUrl = "/fr/itldev/koya/salesoffer/list",
            result = connector.get(remoteUrl);
    if (result.status != status.STATUS_OK)
    {
        status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to call salesoffer list webscript. " +
                "Status: " + result.status + ", response: " + result.response);
        return null;
    }
    var salesOffers = eval('(' + result.response + ')');
    model.salesOffers = salesOffers;


    var remoteUrl = "/fr/itldev/koya/listfolder/app:company_home/app:dictionary/app:koya_space_templates",
            result = connector.get(remoteUrl);
    if (result.status != status.STATUS_OK) {
        status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to call listfolder list webscript. " +
                "Status: " + result.status + ", response: " + result.response);
        return null;
    }
   
    var spaceTemplates = eval('(' + result.response + ')');
    model.spaceTemplates = spaceTemplates.items;
    
    
    var widget = {
        id: "CreateCompany",
        name: "Koya.CreateCompany",
        options: {
        }
    };
    model.widgets = [widget];
}

main();

