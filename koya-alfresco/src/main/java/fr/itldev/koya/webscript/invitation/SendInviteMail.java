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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Send invite mail with given inviteId
 *
 *
 */
public class SendInviteMail extends AbstractWebScript {

	protected KoyaMailService koyaMailService;
	protected CompanyAclService companyAclService;
	protected WorkflowService workflowService;
	protected AuthenticationService authenticationService;
	protected UserService userService;
	protected KoyaNodeService koyaNodeService;

	public void setKoyaMailService(KoyaMailService koyaMailService) {
		this.koyaMailService = koyaMailService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, Object> postParams = KoyaWebscript.getJsonMap(req);
		final String inviteId = (String) postParams.get("inviteId");
		Map<String, String> returnValues = new HashMap<>();

		try {
			WorkflowTask task = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<WorkflowTask>() {
				@Override
				public WorkflowTask doWork() throws Exception {
					return workflowService.getStartTask(inviteId);
				}
			});

			/**
			 * Security : user must be SiteManager or SiteCollaborator of
			 * current company
			 */
			String companyName = (String) task.getProperties()
					.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME);
			User u = userService.getUserByUsername(authenticationService.getCurrentUserName());
			SitePermission userPermissionInCompany = companyAclService
					.getSitePermission(koyaNodeService.companyBuilder(companyName), u);

			if (!userPermissionInCompany.equals(SitePermission.COLLABORATOR)
					&& !userPermissionInCompany.equals(SitePermission.MANAGER)) {
				throw new KoyaServiceException(KoyaErrorCodes.INVITATION_PROCESS_NOT_ALLOWED_RESEND_MAIL);
			}

			returnValues.put("destEmail", koyaMailService.sendInviteMail(inviteId));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		}

		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(KoyaWebscript.getObjectAsJson(returnValues));
	}

}
