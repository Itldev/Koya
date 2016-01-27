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

package fr.itldev.koya.webscript.space;

import java.io.IOException;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 *
 */
public class ListSpace extends AbstractWebScript {

	private static final Integer DEFAULT_MAX_DEPTH = 50;

	private SpaceService spaceService;

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		String name = (String) jsonPostMap.get(RestConstants.WSCONST_NAME);
		String response;
		try {
			Integer depth;
			if (urlParams.containsKey(RestConstants.WSCONST_MAXDEPTH)) {
				depth = new Integer((String) urlParams.get(RestConstants.WSCONST_MAXDEPTH));
			} else {
				depth = DEFAULT_MAX_DEPTH;
			}
			response = KoyaWebscript.getObjectAsJson(spaceService.list(name, depth));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
