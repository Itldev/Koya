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
import java.util.Map;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * change user password
 * 
 * user can change his password given old one
 * 
 * admin can force change user password
 * 
 * TODO encrypt password with user key?
 * 
 * 
 */
public class ChangePassword extends AbstractWebScript {

	private UserService userService;
	private MutableAuthenticationService authenticationService;
	private AuthorityService authorityService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		Map<String, Object> params = KoyaWebscript.getJsonMap(req);

		Boolean isAdmin = authorityService.isAdminAuthority(authenticationService.getCurrentUserName());

		String username = null;
		String oldpwd = null;
		String newpwd = null;

		try {
			username = (String) params.get("userName");
		} catch (Exception e) {
		}

		try {
			oldpwd = (String) params.get("oldPwd");
		} catch (Exception e) {
		}

		try {
			newpwd = (String) params.get("newPwd");
		} catch (Exception e) {
		}

		try {

			if (username != null && isAdmin) {
				userService.adminForceChangePassword(username, newpwd);
			} else {
				userService.changePassword(oldpwd, newpwd);
			}
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write("");
	}
}
