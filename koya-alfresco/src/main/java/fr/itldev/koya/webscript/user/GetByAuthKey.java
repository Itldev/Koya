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

import java.io.IOException;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Get User by Authentication Key.
 * 
 * This key can either be the username or the user mail address. Service used by
 * library login method
 * 
 * 
 */
public class GetByAuthKey extends AbstractWebScript {

	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, Object> jsonMap = KoyaWebscript.getJsonMap(req);

		String authKey = (String) jsonMap.get("authKey");
		Boolean failProof = Boolean.FALSE;

		try {
			failProof = Boolean.valueOf(jsonMap.get("failProof").toString());
		} catch (NullPointerException ex) {

		}
		String response = "";

		User u = userService.getUserByUsername(authKey);
		if (u == null) {
			try {
				u = userService.getUserByEmail(authKey);
			} catch (RuntimeException ex) {
				if (!failProof) {
					throw new WebScriptException(
							"KoyaError : " + ((KoyaServiceException) ex).getErrorCode().toString());
				}
			}
		}

		response = KoyaWebscript.getObjectAsJson(u);
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
