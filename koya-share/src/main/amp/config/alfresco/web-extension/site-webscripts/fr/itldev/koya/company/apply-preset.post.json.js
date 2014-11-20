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
    model.success = false;

    var clientRequest = json.toString();
    // Convert client json request to a usable js object to retrieve site preset name
    var clientJSON = eval('(' + clientRequest + ')');


    var companyName = clientJSON.companyName;
    // Check if we got a positive result from create site
    if (companyName) {


        // Yes we did, now create the Surf objects in the web-tier and the associated configuration elements
        // Retry a number of times until success - remove the site on total failure
        for (var r = 0; r < 3 && !model.success; r++) {
            var tokens = [];
            tokens["siteid"] = companyName;
            sitedata.newPreset(clientJSON.sitePreset, tokens);
            model.success = true;
        }
    }
}


main();