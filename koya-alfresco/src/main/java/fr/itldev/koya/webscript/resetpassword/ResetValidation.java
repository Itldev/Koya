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

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.resetpassword.activiti.ResetPasswordModel;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Set new password Webscript.
 *
 */
public class ResetValidation extends AbstractWebScript {

    private WorkflowService workflowService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        AuthenticationUtil.setRunAsUserSystem();

        Map<String, Object> jsonMap = KoyaWebscript.getJsonMap(req);

        String resetId = (String) jsonMap.get("resetId");
        String resetTicket = (String) jsonMap.get("resetTicket");
        String newPassword = (String) jsonMap.get("newPassword");

        try {
            WorkflowTask startTask = workflowService.getStartTask(resetId);
            String taskTicket = (String) startTask.getProperties().get(ResetPasswordModel.PROP_RESETTICKET);
            //compare tickets
            if (!resetTicket.equals(taskTicket)) {
                throw new KoyaServiceException(KoyaErrorCodes.INVALID_RESETPASSWORD_TICKET);
            }

            List<WorkflowTask> tasks;

            try {
                tasks = workflowService.getTasksForWorkflowPath(startTask.getPath().getId());
            } catch (WorkflowException wex) {
                throw new KoyaServiceException(KoyaErrorCodes.INVALID_RESETPASSWORD_ID);
            }
            if (tasks.size() == 1) {
                WorkflowTask task = tasks.get(0);
                Map<QName, Serializable> workflowProps = new HashMap<>();
                workflowProps.put(ResetPasswordModel.PROP_RESET_NEWPASSWORD, newPassword);

                workflowService.updateTask(task.getId(), workflowProps, null, null);

                workflowService.endTask(task.getId(), null);

            } else {
                logger.error("error recup task");
            }

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }

        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("");
    }
}
