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

/**
 * Create Site POST Component
 * 
 * Reponsible for call the /api/sites to generate the st:site folder structure then
 * creating the Surf config structure on the web and repo tier. The config creation
 * will retry if a timeout occurs - if total failure occurs to create to the config
 * then the st:site node will be deleted and error reported.
 */

function main()
{
    model.success = false;

    var clientRequest = json.toString();

    // Convert client json request to a usable js object to retrieve site preset name
    var clientJSON = eval('(' + clientRequest + ')');

    // Call the repo to create the st:site folder structure
    var connector = remote.connect("alfresco");

    var repoResponse = connector.post("/fr/itldev/koya/company/add",
            "{\"title\":\"" + stringUtils.urlEncode(clientJSON.title) + "\"" +
            ",\"template\":\"" + stringUtils.urlEncode(clientJSON.spaceTemplate) + "\"" +
            ",\"salesoffer\":\"" + stringUtils.urlEncode(clientJSON.salesOffer) + "\"}"
            , "application/json");


    if (repoResponse.status.code === 401)
    {
        status.setCode(repoResponse.status, "error.loggedOut");
    }
    if (repoResponse.status.code === 500)
    {
        status.setCode(repoResponse.status, repoResponse.message);
    }
    else
    {
        var company = eval('(' + repoResponse + ')');
        // Check if we got a positive result from create site
        if (company) {
            model.shortName = company.name;
            // Yes we did, now create the Surf objects in the web-tier and the associated configuration elements
            // Retry a number of times until success - remove the site on total failure
            for (var r = 0; r < 3 && !model.success; r++) {
                var tokens = [];
                tokens["siteid"] = model.shortName;
                model.success = sitedata.newPreset(clientJSON.sitePreset, tokens);
            }

            if (!model.success) {
                // if we get here - it was a total failure to create the site config - even after retries
                // Delete the st:site folder structure and set error handler
                conn.del("/api/sites/" + encodeURIComponent(model.shortName));
                status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "error.create");
            }
        }
    }
}

main();