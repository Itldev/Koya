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
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Get user's role on specified KoyaNode
 *
 *
 */
public class GetRole extends AbstractWebScript {

	private KoyaNodeService koyaNodeService;
	private UserService userService;
	private CompanyAclService companyAclService;
	private SpaceAclService spaceAclService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		String userName = (String) urlParams.get(RestConstants.WSCONST_USERNAME);
		String nodeId = (String) urlParams.get(RestConstants.WSCONST_NODEID);

		String response;

		try {
			User u = userService.getUserByUsername(userName);
			KoyaNode n = koyaNodeService
					.getKoyaNode(new NodeRef("workspace://SpacesStore/" + nodeId));
			UserRole ur = null;
			if (Company.class.isAssignableFrom(n.getClass())) {
				SitePermission permission = companyAclService.getSitePermission((Company) n, u);
				ur = new UserRole(permission != null
						? companyAclService.getSitePermission((Company) n, u).toString() : null);
			} else {
				// permission for spaces
				KoyaPermission kp = spaceAclService.getKoyaPermission((Space) n, u);
				ur = new UserRole(kp != null ? kp.toString() : null);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("User role for " + u.getUserName() + " on node " + n.getName() + " > "
						+ ur.toString());
			}
			response = KoyaWebscript.getObjectAsJson(ur);

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

}
