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
package fr.itldev.koya.webscript.user;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Validate User invitation.
 *
 *
 */
public class ValidateInvitation extends KoyaWebscript {
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    private UserService userService;
    private InvitationService invitationService;
    private WorkflowService workflowService;
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }
    
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
    
    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        /* ======== params ========*/
        final String invitationId = (String) urlParams.get("inviteId");
        final String inviteTicket = (String) urlParams.get("inviteTicket");
        final String newPassword = (String) urlParams.get("password");
        final User userInvited = new User();
        userInvited.setName((String) jsonPostMap.get("lastName"));
        userInvited.setFirstName((String) jsonPostMap.get("firstName"));

        /**/
        final NominatedInvitation invitation;
        try {
            invitation = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< NominatedInvitation>() {
                @Override
                public NominatedInvitation doWork() throws Exception {
                    try {
                        return (NominatedInvitation) invitationService.getInvitation(invitationId);
                    } catch (Exception ex) {
                        logger.error("Error getting invitation from invitationId : " + ex.toString());
                        throw ex;
                        
                    }
                }
            });
        } catch (RuntimeException rex) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_INVITATION_ID);
        }
        WorkflowTask startTask;
        try {
            startTask = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< WorkflowTask>() {
                @Override
                public WorkflowTask doWork() throws Exception {
                    try {
                        return workflowService.getStartTask(invitationId);
                    } catch (Exception ex) {
                        logger.error("Error getting WorkflowTask from invitationId : " + ex.toString());
                        throw ex;
                    }
                }
            });
        } catch (RuntimeException rex) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_INVITATION_ID);
        }
        
        if (!invitation.getTicket().equals(inviteTicket)) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_INVITATION_TICKET);
        } else {
            userInvited.setUserName(invitation.getInviteeUserName());
            userInvited.setEmail(invitation.getInviteeEmail());
            
            Map<QName, Serializable> taskProps = startTask.getProperties();
            final String oldPassword = (String) taskProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD);

            //First accept invitation
            Exception eInvite = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Exception>() {
                @Override
                public Exception doWork() throws Exception {
                    
                    try {
                        
                        invitationService.accept(invitationId, inviteTicket);
                    } catch (Exception ex) {
                        logger.error("Error duaring invitation accept" + ex.toString());
                        //TODO detect already accepted/rejected invitation -> KoyaErrorCodes.INVITATION_ALREADY_COMPLETED
                        //startTask.getState().equals(WorkflowTaskState.COMPLETED condition is always true even if not already accepted ...
                        
                        return ex;
                    }
                    return null;
                }
            }, invitation.getInviteeUserName());
            
            if (eInvite != null) {
                throw new KoyaServiceException(KoyaErrorCodes.INVITATION_PROCESS_ACCEPT_ERROR, eInvite);
            }

            // then modify user properties and password
            Exception eModify = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Exception>() {
                @Override
                public Exception doWork() throws Exception {
                    try {
                        
                        userService.modifyUser(userInvited);
                        userService.changePassword(oldPassword, newPassword);
                    } catch (Exception ex) {
                        return ex;
                    }
                    return null;
                }
            }, invitation.getInviteeUserName());
            
            if (eModify != null) {
                throw new KoyaServiceException(KoyaErrorCodes.INVITATION_PROCESS_USER_MODIFICATION_ERROR, eModify);
            }
            
            return wrapper;
        }
        
    }
    
}
