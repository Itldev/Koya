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
package fr.itldev.koya.webscript.dossier;

import java.io.IOException;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * 
 * Checks if element has confidential flag
 * 
 */
public class IsConfidential extends AbstractWebScript {

	private SpaceAclService spaceAclService;
	private KoyaNodeService koyaNodeService;

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		Boolean isConfidential = false;
		try {
			KoyaNode item = koyaNodeService
					.getKoyaNode(koyaNodeService.getNodeRef((String) urlParams.get(RestConstants.WSCONST_NODEREF)));

			isConfidential = spaceAclService.isConfidential(item);

		} catch (KoyaServiceException ex) {
			// silently ignore exception
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(isConfidential.toString());
	}

}
