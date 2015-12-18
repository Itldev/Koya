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
package fr.itldev.koya.webscript.resetpassword;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.resetpassword.activiti.ResetPasswordModel;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Reset Password Request Webscript
 * 
 */
public class ResetRequest extends AbstractWebScript {

	public static final String WORKFLOW_DEFINITION_NAME_RESET_PASSWORD = "activiti$resetPassword";

	private WorkflowService workflowService;
	private UserService userService;
	private CompanyAclService companyAclService;
	private KoyaMailService koyaMailService;

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setKoyaMailService(KoyaMailService koyaMailService) {
		this.koyaMailService = koyaMailService;
	}

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		Map<String, Object> jsonMap = KoyaWebscript.getJsonMap(req);
		String userEmail = (String) jsonMap.get("userEmail");
		String resetUrl = (String) jsonMap.get("resetUrl");

		AuthenticationUtil.setRunAsUserSystem();

		try {

			/**
			 * Check if user has pending invitation.If true, do not start reset
			 * workflow .
			 */
			User u = userService.getUserByEmail(userEmail);

			if (userService.isDisabled(u)) {
				/**
				 * If person is disabled : do not start reset password
				 * procedure. try to send invitation again if exists
				 * 
				 */
				List<Invitation> invitations = companyAclService
						.getPendingInvite(null, null, u.getUserName());
				if (invitations != null && invitations.size() == 1) {

					koyaMailService.sendInviteMail(invitations.get(0)
							.getInviteId());
					throw new KoyaServiceException(
							KoyaErrorCodes.NORESETPWD_INVITATION_SENT_AGAIN);
				} else {
					throw new KoyaServiceException(
							KoyaErrorCodes.NORESETPWD_ONDISABLED_USERS);
				}
			}

			WorkflowDefinition wfd = workflowService
					.getDefinitionByName(WORKFLOW_DEFINITION_NAME_RESET_PASSWORD);

			/**
			 * TODO check if user has existing workflow : in this case resend
			 * mail.
			 * 
			 * specific workflow
			 */
			/**
			 * Workflow properties :
			 */
			Map<QName, Serializable> workflowProps = new HashMap<>();
			workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION,
					userEmail + " reset password request");

			workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, u.getNodeRef());
			workflowProps.put(ResetPasswordModel.PROP_RESETURL, resetUrl);
			workflowProps.put(ResetPasswordModel.PROP_RESETTICKET,
					GUID.generate());

			/**
			 * Start task with given properties
			 */
			WorkflowPath wfPath = workflowService.startWorkflow(wfd.getId(),
					workflowProps);

			WorkflowTask startTask = workflowService.getStartTask(wfPath
					.getInstance().getId());
			workflowService.endTask(startTask.getId(), null);

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());

		}

		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write("");
	}
}
