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
package fr.itldev.koya.webscript.share;

import java.io.IOException;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SpaceConsumersAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * List users shares in a company.
 * 
 * 
 */
public class ListUserShares extends AbstractWebScript {

	private SpaceConsumersAclService spaceConsumersAclService;
	private KoyaNodeService koyaNodeService;
	private UserService userService;

	public void setSpaceConsumersAclService(
			SpaceConsumersAclService spaceConsumersAclService) {
		this.spaceConsumersAclService = spaceConsumersAclService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
		String userName = (String) urlParams
				.get(KoyaWebscript.WSCONST_USERNAME);
		String companyName = (String) urlParams
				.get(KoyaWebscript.WSCONST_COMPANYNAME);
		String response;

		try {
			User u = userService.getUserByUsername(userName);
			Company c = koyaNodeService.companyBuilder(companyName);
			response = KoyaWebscript.getObjectAsJson(spaceConsumersAclService
					.listKoyaNodes(c, u, KoyaPermissionConsumer.getAll()));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
