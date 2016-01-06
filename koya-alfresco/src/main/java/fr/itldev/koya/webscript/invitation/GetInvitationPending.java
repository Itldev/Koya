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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Return True if given inviteId matches a pending invitation
 *
 */
public class GetInvitationPending extends AbstractWebScript {

	private WorkflowService workflowService;

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
		res.setContentType("application/json;charset=UTF-8");

		final String invitationId = (String) urlParams.get("inviteId");
		List<WorkflowTask> tasks = null;

		try {

			tasks = workflowService.getTasksForWorkflowPath(invitationId);

			if (tasks.size() != 1) {
				res.getWriter().write(Boolean.FALSE.toString());
				return;
			}

			WorkflowTask task = tasks.get(0);

			if (taskTypeMatches(task, WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING,
					WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING)) {
				res.getWriter().write(Boolean.TRUE.toString());
				return;
			}

			res.getWriter().write(Boolean.FALSE.toString());
			return;

		} catch (RuntimeException rex) {
		} catch (Exception rex) {
		}

		res.getWriter().write(Boolean.FALSE.toString());

	}

	private boolean taskTypeMatches(WorkflowTask task, QName... types) {
		QName taskDefName = task.getDefinition().getMetadata().getName();
		return Arrays.asList(types).contains(taskDefName);
	}
}
