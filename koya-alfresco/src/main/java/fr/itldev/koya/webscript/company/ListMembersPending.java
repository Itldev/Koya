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
package fr.itldev.koya.webscript.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * List Company members.
 */
public class ListMembersPending extends AbstractWebScript {

	private CompanyAclService companyAclService;
	private KoyaNodeService koyaNodeService;

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
		String companyName = urlParams.get(RestConstants.WSCONST_COMPANYNAME);
		String roleFilter = urlParams.get(RestConstants.WSCONST_ROLEFILTER);

		String response;
		try {

			List<SitePermission> permisssionsFilter = new ArrayList<>();

			try {
				for (String role : roleFilter.split(",")) {
					permisssionsFilter.add(SitePermission.valueOf(role));
				}
			} catch (Exception ex) {
			}

			response = KoyaWebscript
					.getObjectAsJson(companyAclService.listMembersPendingInvitation(companyName, permisssionsFilter));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

}
