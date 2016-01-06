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
package fr.itldev.koya.webscript.content;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * 
 * Content Listing Webscript
 * 
 * 
 * 
 */
public class ListContentTree extends AbstractWebScript {

	private static final Integer DEFAULT_MAX_DEPTH = 50;

	private KoyaNodeService koyaNodeService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);
		String response = "";

		try {
			NodeRef parent = koyaNodeService.getNodeRef((String) urlParamsMap.get(RestConstants.WSCONST_NODEREF));

			Boolean onlyFolders = ((String) urlParamsMap.get(RestConstants.WSCONST_ONLYFOLDERS)).equals("true");

			Integer depth = null;

			if (urlParamsMap.containsKey(RestConstants.WSCONST_MAXDEPTH)) {
				depth = new Integer((String) urlParamsMap.get(RestConstants.WSCONST_MAXDEPTH));
			} else {
				depth = DEFAULT_MAX_DEPTH;
			}
			response = KoyaWebscript.getObjectAsJson(koyaNodeService.listChildrenAsTree(parent, depth, onlyFolders));

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

	private String processPatternFilter(String paramFilterExpr) {
		if (paramFilterExpr == null || paramFilterExpr.isEmpty()) {
			return null;
		}
		if (!paramFilterExpr.endsWith("*")) {
			paramFilterExpr += "*";
		}
		return paramFilterExpr;
	}

}
