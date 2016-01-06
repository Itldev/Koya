/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.webscript.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Find users from query (fields starts with) with max results.
 *
 *
 */
public class Find extends AbstractWebScript {

	private Logger logger = Logger.getLogger(this.getClass());
	private static final Integer DEFAULT_MAXRESULTS = 10;

	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		String query = (String) urlParams.get(RestConstants.WSCONST_QUERY);
		String companyName = (String) urlParams.get(RestConstants.WSCONST_COMPANYNAME);
		String response;

		Integer maxResults;
		try {
			maxResults = Integer.valueOf((String) urlParams.get(RestConstants.WSCONST_MAXRESULTS));
		} catch (NumberFormatException e) {
			maxResults = DEFAULT_MAXRESULTS;
		}
		List<String> rolesFilter = new ArrayList<>();
		try {
			for (String uniqueRole : ((String) urlParams.get(RestConstants.WSCONST_ROLEFILTER)).split(",")) {
				rolesFilter.add(uniqueRole.trim());
			}
		} catch (Exception ex) {
			// silent exception
		}
		response = KoyaWebscript.getObjectAsJson(userService.find(query, maxResults, companyName, rolesFilter));
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

}
