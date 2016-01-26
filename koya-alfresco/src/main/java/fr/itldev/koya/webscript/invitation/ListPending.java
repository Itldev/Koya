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
package fr.itldev.koya.webscript.invitation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Returns user Pending invitations alfresco wide
 *
 */
public class ListPending extends AbstractWebScript {

	private CompanyAclService companyAclService;
	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		String userName = (String) urlParams.get(RestConstants.WSCONST_USERNAME);
		String response = "";

		try {
			User u = userService.getUserByUsername(userName);
			List<Invitation> invitations = companyAclService.getPendingInvite(null, null, userName);

			List<Map<String, String>> invResponse = new ArrayList<>();
			for (Invitation i : invitations) {
				Map<String, String> invit = new HashMap<>();
				invit.put("inviteId", i.getInviteId());
				invit.put("inviteTicket", ((NominatedInvitation) i).getTicket());
				invit.put("userEnabled", u.isEnabled().toString());
				invit.put("companyName", i.getResourceName());
				invit.put("inviteeEmail", ((NominatedInvitation) i).getInviteeEmail());
				invResponse.add(invit);
			}

			response = KoyaWebscript.getObjectAsJson(invResponse);

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

}
