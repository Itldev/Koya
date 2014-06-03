/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.webscript.user;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;

/**
 * Find users from query (fields starts with) with max results.
 *
 *
 */
public class Find extends KoyaWebscript {

    private static final Integer DEFAULT_MAXRESULTS = 10;

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        String query = (String) urlParams.get("query");

        Integer maxResults;
        try {
            maxResults = Integer.valueOf((String) urlParams.get("maxResults"));
        } catch (NumberFormatException e) {
            maxResults = DEFAULT_MAXRESULTS;
        }
        String sitename = null;
        try {
            sitename = (String) urlParams.get("sitename");
        } catch (Exception ex) {

        }

        if (sitename == null) {
            wrapper.addItems(userService.find(query, maxResults));
        } else {

            /**
             * Find in company filtered on role SiteConsumer because this
             * request is used to autocomplete users for sharing : ie only for
             * this groups users.
             *
             * TODO More flexible implementation. particularly when there will
             * be differents spaces per company. (ex user can be collaborator on
             * specific space and have no permission on other one but be shared
             * a specific Dossier)
             *
             */
            String roleFilter = "SiteConsumer";

            wrapper.addItems(userService.findInCompany(query, roleFilter, maxResults, sitename));
        }

        return wrapper;
    }

}
