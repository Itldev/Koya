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

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Validate User invitation.
 *
 *
 */
public class ValidateInvitation extends AbstractWebScript {

    private Logger logger = Logger.getLogger(this.getClass());

    private UserService userService;
    private InvitationService invitationService;
    private WorkflowService workflowService;
    protected SubSpaceAclService subSpaceAclService;
    protected ActivityService activityService;
    protected SiteService siteService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);

        final String invitationId = (String) urlParams.get("inviteId");
        final String inviteTicket = (String) urlParams.get("inviteTicket");
        final String newPassword = (String) urlParams.get("password");
        final User userInvited = new User();
        userInvited.setName((String) jsonPostMap.get("lastName"));
        userInvited.setFirstName((String) jsonPostMap.get("firstName"));

        try {
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

            /**
             * Test invitation validity code inspired by InvitationServiceImpl
             */
            List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(startTask.getPath().getId());
            if (tasks.size() != 1) {
                throw new KoyaServiceException(KoyaErrorCodes.INVITATION_ALREADY_COMPLETED);
            }

            WorkflowTask task = tasks.get(0);
            if (!taskTypeMatches(task,
                    WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING,
                    WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING)) {
                throw new KoyaServiceException(KoyaErrorCodes.INVITATION_ALREADY_COMPLETED);
            }

            /**
             * Test invite ticket validity
             */
            if (!invitation.getTicket().equals(inviteTicket)) {
                throw new KoyaServiceException(KoyaErrorCodes.INVALID_INVITATION_TICKET);
            }

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
                eModify.printStackTrace();
                throw new KoyaServiceException(KoyaErrorCodes.INVITATION_PROCESS_USER_MODIFICATION_ERROR, eModify);
            }

            //Post an activity for dossiers shared to this user 
            /*
            
             TODO make it asynchronous process to avoid lock invitation process
            
             */
            final String companyId = (String) startTask.getProperties().get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME);

//            List<SecuredItem> securedItems = subSpaceAclService.getUsersSecuredItemWithKoyaPermissions(userInvited, new ArrayList<QName>() {
//                {
//                    add(KoyaModel.TYPE_DOSSIER);
//                }
//            }, null);
//            for (final SecuredItem item : securedItems) {
//                if (siteService.getSite(item.getNodeRefasObject()).getShortName().equals(companyId)) {
//
//                    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
//                        @Override
//                        public Object doWork() throws Exception {
//                            try {
//                                activityService.postActivity(NotificationType.KOYA_SHARED,
//                                        companyId, "koya",
//                                        getActivityData(userInvited, item.getNodeRefasObject()),
//                                        invitation.getInviteeUserName());
//                            } catch (Exception ex) {
//                                logger.error("Validate Invitation Post Activity error (non blocking) : " + ex.toString());
//                                ex.printStackTrace();
//                            }
//                            return null;
//                        }
//                    }, invitation.getInviteeUserName());
//
//                }
//            }
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }

        res.setContentType(
                "application/json");
        res.getWriter()
                .write("");
    }

    /**
     * Helper method to get the activity data for a user
     *
     * @param userName user name
     * @param role role
     * @return
     */
    private String getActivityData(User user, NodeRef nodeRef) throws KoyaServiceException {
        String memberFN = user.getFirstName();
        String memberLN = user.getName();
        String userMail = user.getEmail();

        JSONObject activityData = new JSONObject();
        activityData.put("memberUserName", userMail);
        activityData.put("memberFirstName", memberFN);
        activityData.put("memberLastName", memberLN);
        activityData.put("title", (memberFN + " " + memberLN + " ("
                + userMail + ")").trim());
        activityData.put("nodeRef", nodeRef.toString());
        return activityData.toString();
    }

    private boolean taskTypeMatches(WorkflowTask task, QName... types) {
        QName taskDefName = task.getDefinition().getMetadata().getName();
        return Arrays.asList(types).contains(taskDefName);
    }

}
