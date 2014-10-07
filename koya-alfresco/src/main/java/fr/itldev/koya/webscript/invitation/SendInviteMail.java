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

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.invitation.site.KoyaInviteSender;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Checks if user has a pending invitaion for specified company
 *
 *
 */
public class SendInviteMail extends AbstractWebScript {

    private Logger logger = Logger.getLogger(this.getClass());

    private WorkflowService workflowService;
    private ServiceRegistry serviceRegistry;
    private Repository repositoryHelper;
    private MessageService messageService;
    private KoyaMailService koyaMailService;
    private KoyaNodeService koyaNodeService;
    private CompanyAclService companyAclService;
    private AuthenticationService authenticationService;
    private UserService userService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, Object> postParams = KoyaWebscript.getJsonMap(req);
        final String inviteId = (String) postParams.get("inviteId");
        Map<String, String> returnValues = new HashMap<>();

        try {

            WorkflowTask task = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< WorkflowTask>() {
                @Override
                public WorkflowTask doWork() throws Exception {
                    return workflowService.getStartTask(inviteId);
                }
            });

            /**
             * Security : user must be SiteManager or SiteCollaborator of
             * current company
             */
            String companyName = (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME);
            User u = userService.getUserByUsername(authenticationService.getCurrentUserName());
            SitePermission userPermissionInCompany = companyAclService.getSitePermission(
                    koyaNodeService.companyBuilder(companyName), u);

            if (!userPermissionInCompany.equals(SitePermission.COLLABORATOR)    
                    && !userPermissionInCompany.equals(SitePermission.MANAGER)) {
                throw new KoyaServiceException(KoyaErrorCodes.INVITATION_PROCESS_NOT_ALLOWED_RESEND_MAIL);
            }

            KoyaInviteSender koyaInviteSender = new KoyaInviteSender(serviceRegistry,
                    repositoryHelper, messageService,
                    koyaMailService, koyaNodeService);

            Map<String, String> properties = new HashMap<>();

            properties.put(WorkflowModelNominatedInvitation.wfVarInviteeUserName,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME));
            properties.put(WorkflowModelNominatedInvitation.wfVarAcceptUrl,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL));
            properties.put(WorkflowModelNominatedInvitation.wfVarServerPath,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH));
            properties.put(WorkflowModelNominatedInvitation.wfVarRole,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE));
            properties.put(WorkflowModelNominatedInvitation.wfVarInviterUserName,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME));
            properties.put(WorkflowModelNominatedInvitation.wfVarInviteeGenPassword,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD));
            properties.put(WorkflowModelNominatedInvitation.wfVarResourceName,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME));
            properties.put(WorkflowModelNominatedInvitation.wfVarRejectUrl,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL));
            properties.put(WorkflowModelNominatedInvitation.wfVarInviteTicket,
                    (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET));

            properties.put(WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId,
                    (String) task.getProperties().get(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID));

            properties.put(InviteSender.WF_PACKAGE, ((NodeRef) task.getProperties().get(WorkflowModel.ASSOC_PACKAGE)).toString());
            properties.put(InviteSender.WF_INSTANCE_ID, inviteId);

            koyaInviteSender.sendMail(properties);

            returnValues.put("destEmail", (String) task.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL));

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }

        res.setContentType("application/json");
        res.getWriter().write(KoyaWebscript.getObjectAsJson(returnValues));
    }

}
