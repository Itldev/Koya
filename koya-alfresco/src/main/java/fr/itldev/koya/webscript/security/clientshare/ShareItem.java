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
package fr.itldev.koya.webscript.security.clientshare;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.json.KoyaInvite;
import fr.itldev.koya.model.json.KoyaShare;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Share Spaces
 * 
 */
public class ShareItem extends AbstractWebScript {

	private SpaceAclService spaceAclService;
	private KoyaNodeService koyaNodeService;

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	/**
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		Map<String, Object> params = KoyaWebscript.getJsonMap(req);
		String response;
		try {
			NodeRef n = koyaNodeService.getNodeRef((String) params
					.get(KoyaWebscript.WSCONST_NODEREF));

			Space space = koyaNodeService.getKoyaNode(n, Space.class);

			String userMail = (String) params.get(KoyaWebscript.WSCONST_EMAIL);

			NominatedInvitation i = spaceAclService
					.clientShare(space, userMail);

			KoyaShare koyaShare = new KoyaShare(space, userMail,
					KoyaPermissionConsumer.CLIENT.toString());

			if (i != null) {
				koyaShare.setKoyaInvite(new KoyaInvite(i));
			}
			response = KoyaWebscript.getObjectAsJson(koyaShare);

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

}
