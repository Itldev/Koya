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

import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * 
 * Toggle confidentiality
 * 
 */
public class ToggleConfidential extends AbstractWebScript {

	private SpaceAclService spaceAclService;
	private KoyaNodeService koyaNodeService;
	private AuthenticationService authenticationService;
	private UserService userService;

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
		Map<String, Object> postParams = KoyaWebscript.getJsonMap(req);

		Boolean isConfidential = false;
		try {
			KoyaNode item = koyaNodeService.getKoyaNode(koyaNodeService
					.getNodeRef((String) urlParams
							.get(KoyaWebscript.WSCONST_NODEREF)));
			User u = userService.getUserByUsername(authenticationService
					.getCurrentUserName());

			isConfidential = spaceAclService.toggleConfidential(u, item,
					Boolean.valueOf(postParams.get("confidential").toString()));

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(isConfidential.toString());
	}
}
