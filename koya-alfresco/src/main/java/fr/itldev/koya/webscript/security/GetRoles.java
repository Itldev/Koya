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
package fr.itldev.koya.webscript.security;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * List Available Roles on specified KoyaNode.
 *
 */
public class GetRoles extends AbstractWebScript {

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

		String nodeId = (String) urlParams.get(RestConstants.WSCONST_NODEID);
		String response;

		try {

			KoyaNode k = koyaNodeService
					.getKoyaNode(new NodeRef("workspace://SpacesStore/" + nodeId));

			if (Company.class.isAssignableFrom(k.getClass())) {
				response = KoyaWebscript
						.getObjectAsJson(companyAclService.getAvailableRoles((Company) k));
			} else {
				// TODO return node specific roles for master user role : eg for
				// SiteConsumer > KoyaPartner & KoyaClient
				response = KoyaWebscript.getObjectAsJson(KoyaPermission.getAll());
			}

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
