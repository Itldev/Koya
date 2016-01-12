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
package fr.itldev.koya.webscript.global;

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
 * Get Secured Item parents webscript.
 * 
 */
public class GetParent extends AbstractWebScript {
	private KoyaNodeService koyaNodeService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		NodeRef node = null;
		try {
			node = koyaNodeService.getNodeRef((String) urlParams.get(RestConstants.WSCONST_NODEREF));
		} catch (Exception ex) {
			throw new WebScriptException("KoyaError cannot build noderef: " + ex.toString());

		}
		Integer nbAncestor;
		try {
			nbAncestor = Integer.valueOf((String) urlParams.get(RestConstants.WSCONST_NBANCESTOR));
		} catch (NumberFormatException ex) {
			nbAncestor = KoyaNodeService.NB_ANCESTOR_INFINTE;
		}

		Boolean failSafe;
		try {
			failSafe = Boolean.valueOf((String) urlParams.get("failSafe"));
		} catch (NumberFormatException ex) {
			failSafe = false;
		}
		String response;
		if (!failSafe) {
			try {
				response = KoyaWebscript.getObjectAsJson(koyaNodeService.getParentsList(node, nbAncestor));

			} catch (KoyaServiceException ex) {
				throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
			}
		} else {
			// failSafe mode
			try {
				response = KoyaWebscript.getObjectAsJson(koyaNodeService.getParentsList(node, nbAncestor));
			} catch (RuntimeException ex) {
				response = "";

			}
		}

		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
