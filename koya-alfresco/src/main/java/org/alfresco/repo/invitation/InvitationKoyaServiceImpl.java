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
package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.itldev.koya.model.permissions.SitePermission;

/**
 * @see org.alfresco.repo.invitation.InvitationServiceImpl
 *
 *
 * This service overrides classic invitation service. User search is now only on
 * email field ()
 *
 *
 *
 * startNominatedInvite method
 *
 *
 *
 *
 *
 */
public class InvitationKoyaServiceImpl extends InvitationServiceImpl {

    private static final Log logger = LogFactory.getLog(InvitationServiceImpl.class);

    private WorkflowAdminService workflowAdminService;
    
    private SearchService searchService;
    
    

    public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	private final int maxUserNameGenRetries = MAX_NUM_INVITEE_USER_NAME_GEN_TRIES;

    /**
     * Start the invitation process for a NominatedInvitation
     *
     * @param inviteeUserName Alfresco user name of the invitee
     * @param resourceType
     * @param resourceName
     * @param inviteeRole
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     * @return the nominated invitation which will contain the invitationId and
     * ticket which will uniqely identify this invitation for the rest of the
     * workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    @Override
    public NominatedInvitation inviteNominated(String inviteeUserName, Invitation.ResourceType resourceType,
            String resourceName, String inviteeRole, String serverPath, String acceptUrl, String rejectUrl) {
        // inviteeUserName was specified
        NodeRef person = getPersonService().getPerson(inviteeUserName);

        Serializable firstNameVal = this.getNodeService().getProperty(person, ContentModel.PROP_FIRSTNAME);
        Serializable lastNameVal = this.getNodeService().getProperty(person, ContentModel.PROP_LASTNAME);
        Serializable emailVal = this.getNodeService().getProperty(person, ContentModel.PROP_EMAIL);
        String firstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
        String lastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);
        String email = DefaultTypeConverter.INSTANCE.convert(String.class, emailVal);

        return inviteNominated(firstName, lastName, email, inviteeUserName, resourceType, resourceName, inviteeRole,
                serverPath, acceptUrl, rejectUrl);
    }

    /**
     * Start the invitation process for a NominatedInvitation
     *
     * @param inviteeFirstName
     * @param inviteeLastName
     * @param inviteeEmail
     * @param resourceType
     * @param resourceName
     * @param inviteeRole
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     * @return the nominated invitation which will contain the invitationId and
     * ticket which will uniqely identify this invitation for the rest of the
     * workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    @Override
    public NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
            Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String serverPath,
            String acceptUrl, String rejectUrl) {
        return inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, null, resourceType, resourceName,
                inviteeRole, serverPath, acceptUrl, rejectUrl);
    }

    // Temporary method
    private NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
            String inviteeUserName, Invitation.ResourceType resourceType, String resourceName, String inviteeRole,
            String serverPath, String acceptUrl, String rejectUrl) {
        // Validate the request

        // Check resource exists
        if (resourceType == Invitation.ResourceType.WEB_SITE) {
            return startNominatedInvite(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, resourceType,
                    resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        }

        throw new InvitationException("unknown resource type");
    }

    /**
     * Get an invitation from its invitation id <BR />
     * Invitations are returned which may be in progress or completed.
     *
     * @throws InvitationExceptionNotFound the invitation does not exist.
     * @throws InvitationExceptionUserError
     * @return the invitation.
     */
    private Invitation getInvitation(WorkflowTask startTask) {
        Invitation invitation = getNominatedInvitation(startTask);
        if (invitation == null) {
            invitation = getModeratedInvitation(startTask.getPath().getId());
        }
        return invitation;
    }

    private WorkflowTask getModeratedInvitationReviewTask(String invitationId) {
        WorkflowTask reviewTask = null;

        // since the invitation may have been updated e.g. invitee comments (and therefore modified date)
        // we need to get the properties from the review task (which should be the only active
        // task)
        List<WorkflowTask> tasks = getWorkflowService().getTasksForWorkflowPath(invitationId);
        for (WorkflowTask task : tasks) {
            if (taskTypeMatches(task, WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK)) {
                reviewTask = task;
                break;
            }
        }

        return reviewTask;
    }

    private ModeratedInvitation getModeratedInvitation(String invitationId) {
        WorkflowTask reviewTask = getModeratedInvitationReviewTask(invitationId);
        ModeratedInvitation invitation = getModeratedInvitation(invitationId, reviewTask);
        return invitation;
    }

    private ModeratedInvitation getModeratedInvitation(String invitationId, WorkflowTask reviewTask) {
        ModeratedInvitation invitation = null;

        if (reviewTask != null) {
            Map<QName, Serializable> properties = reviewTask.getProperties();
            invitation = new ModeratedInvitationImpl(invitationId, properties);
        }

        return invitation;
    }

    private NominatedInvitation getNominatedInvitation(WorkflowTask startTask) {
        NominatedInvitation invitation = null;
        if (taskTypeMatches(startTask, WorkflowModelNominatedInvitation.WF_TASK_INVITE_TO_SITE)) {
            Date inviteDate = startTask.getPath().getInstance().getStartDate();
            String invitationId = startTask.getPath().getInstance().getId();
            invitation = new NominatedInvitationImpl(invitationId, inviteDate, startTask.getProperties());
        }
        return invitation;
    }

    private boolean taskTypeMatches(WorkflowTask task, QName... types) {
        QName taskDefName = task.getDefinition().getMetadata().getName();
        return Arrays.asList(types).contains(taskDefName);
    }

    /**
     * @param workflowAdminService the workflowAdminService to set
     */
    @Override
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService) {
        this.workflowAdminService = workflowAdminService;
        super.setWorkflowAdminService(workflowAdminService);

    }

    /**
     * Creates a person for the invitee with a generated user name.
     *
     * @param inviteeFirstName first name of invitee
     * @param inviteeLastName last name of invitee
     * @param inviteeEmail email address of invitee
     * @return invitee user name
     */
    private String createInviteePerson(String inviteeFirstName, String inviteeLastName, String inviteeEmail) {
        // Attempt to generate user name for invitee
        // which does not belong to an existing person
        // Tries up to MAX_NUM_INVITEE_USER_NAME_GEN_TRIES
        // at which point a web script exception is thrown
        String inviteeUserName = null;
        int i = 0;
        do {
            inviteeUserName = getUserNameGenerator().generateUserName(inviteeFirstName, inviteeLastName, inviteeEmail, i);
            i++;
        } while (getPersonService().personExists(inviteeUserName) && (i < getMaxUserNameGenRetries()));

        // if after 10 tries is not able to generate a user name for a
        // person who doesn't already exist, then throw a web script exception
        if (getPersonService().personExists(inviteeUserName)) {

            logger.debug("Failed - unable to generate username for invitee.");

            Object[] objs = {inviteeFirstName, inviteeLastName, inviteeEmail};
            throw new InvitationException("invitation.invite.unable_generate_id", objs);
        }

        // create a person node for the invitee with generated invitee user name
        // and other provided person property values
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, inviteeUserName);
        properties.put(ContentModel.PROP_FIRSTNAME, inviteeFirstName);
        properties.put(ContentModel.PROP_LASTNAME, inviteeLastName);
        properties.put(ContentModel.PROP_EMAIL, inviteeEmail);

        final String finalUserName = inviteeUserName;
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            @SuppressWarnings("synthetic-access")
            @Override
            public Object doWork() throws Exception {
                NodeRef person = getPersonService().createPerson(properties);
                //MNT-9101 Share: Cancelling an invitation for a disabled user, the user gets deleted in the process.
                getNodeService().addAspect(person, ContentModel.ASPECT_ANULLABLE, null);
                getPermissionService().setPermission(person, finalUserName, PermissionService.ALL_PERMISSIONS, true);

                return null;
            }

        }, AuthenticationUtil.getSystemUserName());

        return inviteeUserName;
    }

    /**
     * Creates a disabled user account for the given invitee user name with a
     * generated password
     *
     * @param inviteeUserName
     * @return password generated for invitee user account
     */
    private String createInviteeDisabledAccount(String inviteeUserName) {
        // generate password using password generator
        char[] generatedPassword = getPasswordGenerator().generatePassword().toCharArray();

        // create disabled user account for invitee user name with generated
        // password
        getAuthenticationService().createAuthentication(inviteeUserName, generatedPassword);
        getAuthenticationService().setAuthenticationEnabled(inviteeUserName, false);

        return String.valueOf(generatedPassword);
    }

    /**
     * Starts the Invite workflow
     *
     * @param inviteeFirstName first name of invitee
     * @param inviteeLastNamme last name of invitee
     * @param inviteeEmail email address of invitee
     * @param siteShortName short name of site that the invitee is being invited
     * to by the inviter
     * @param inviteeSiteRole role under which invitee is being invited to the
     * site by the inviter
     * @param serverPath externally accessible server address of server hosting
     * invite web scripts
     */
    private NominatedInvitation startNominatedInvite(String inviteeFirstName, String inviteeLastName,
            String inviteeEmail, String inviteeUserName, Invitation.ResourceType resourceType,
            String siteShortName, String inviteeSiteRole, String serverPath, String acceptUrl, String rejectUrl) {

        // get the inviter user name (the name of user web script is executed
        // under)
        String inviterUserName = getAuthenticationService().getCurrentUserName();
        boolean created = false;

        checkManagerRole(inviterUserName, resourceType, siteShortName);

        if (logger.isInfoEnabled()) {
            logger.info("startInvite() inviterUserName=" + inviterUserName + " inviteeUserName=" + inviteeUserName
                    + " inviteeFirstName=" + inviteeFirstName + " inviteeLastName=" + inviteeLastName
                    + " inviteeEmail=" + inviteeEmail + " siteShortName=" + siteShortName + " inviteeSiteRole="
                    + inviteeSiteRole);
        }
        //
        // if we have not explicitly been passed an existing user's user name
        // then ....
        //
        // if a person already exists who has the given invitee email address
        //
        // 1) obtain invitee user name from first person found having the
        // invitee email address
        // 2) handle error conditions -
        // (invitee already has an invitation in progress for the given site,
        // or he/she is already a member of the given site
        //        
        if (inviteeUserName == null || inviteeUserName.trim().length() == 0) {

            inviteeUserName = null;

			/**
			 * ITL : replace
			 * getPersonService().getPeopleFilteredByProperty(ContentModel
			 * .PROP_EMAIL, inviteeEmail, 100); 
			 * 
			 * by user email field search to
			 * avoid PersonService Warn log message : 
			 * 
			 * "PersonService.getPeopleFilteredByProperty() is being called to
			 * find people by {http://www.alfresco.org/model/content/1.0}email.
			 * Only PROP_FIRSTNAME, PROP_LASTNAME, PROP_USERNAME are now used in
			 * the search, so fewer nodes may be returned than expected of there
			 * are more than 100 users in total"
			 * 
			 */

			String luceneQuery = "TYPE:\"cm:person\" AND @koya\\:mail:\""
					+ inviteeEmail + "\"";
			Set<NodeRef> peopleWithInviteeEmail = new HashSet<NodeRef>();

			ResultSet rs = null;
			try {
				rs = searchService.query(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						SearchService.LANGUAGE_LUCENE, luceneQuery);
				for (ResultSetRow r : rs) {
					peopleWithInviteeEmail.add(r.getNodeRef());
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
            
            //search 
            //Set<NodeRef> peopleWithInviteeEmail = getPersonService().getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, inviteeEmail, 100);
            
            if (peopleWithInviteeEmail.size() > 0) {
                // get person already existing who has the given
                // invitee email address
                NodeRef personRef = peopleWithInviteeEmail.iterator().next();

                // got a match on email
                // get invitee user name of that person
                Serializable userNamePropertyVal = this.getNodeService().getProperty(personRef, ContentModel.PROP_USERNAME);
                inviteeUserName = DefaultTypeConverter.INSTANCE.convert(String.class, userNamePropertyVal);

                if (logger.isDebugEnabled()) {
                    logger.debug("not explictly passed username - found matching email, resolved inviteeUserName="
                            + inviteeUserName);
                }
            }

            if (inviteeUserName == null) {
                // This shouldn't normally happen. Due to the fix for ETHREEOH-3268, the link to invite external users
                // should be disabled when the authentication chain does not allow it.
                if (!getAuthenticationService().isAuthenticationCreationAllowed()) {
                    throw new InvitationException("invitation.invite.authentication_chain");
                }
                // else there are no existing people who have the given invitee
                // email address so create new person
                inviteeUserName = createInviteePerson(inviteeFirstName, inviteeLastName, inviteeEmail);

                created = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("not explictly passed username - created new person, inviteeUserName="
                            + inviteeUserName);
                }
            }
        }

        /**
         * throw exception if person is already a member of the given site
         */
        if (getSiteService().isMember(siteShortName, inviteeUserName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed - invitee user is already a member of the site.");
            }

            Object objs[] = {inviteeUserName, inviteeEmail, siteShortName};
            throw new InvitationExceptionUserError("invitation.invite.already_member", objs);
        }

        //
        // If a user account does not already exist for invitee user name
        // then create a disabled user account for the invitee.
        // Hold a local reference to generated password if disabled invitee
        // account
        // is created, otherwise if a user account already exists for invitee
        // user name, then local reference to invitee password will be "null"
        //
        final String initeeUserNameFinal = inviteeUserName;

        String inviteePassword = created ? AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
            @SuppressWarnings("synthetic-access")
            @Override
            public String doWork() {
                return createInviteeDisabledAccount(initeeUserNameFinal);
            }
        }, AuthenticationUtil.getSystemUserName()) : null;

        // create a ticket for the invite - this is used
        String inviteTicket = GUID.generate();

        //
        // Start the invite workflow with inviter, invitee and site properties
        //
        WorkflowDefinition wfDefinition = getWorkflowDefinition(true);

        // Get invitee person NodeRef to add as assignee
        NodeRef inviteeNodeRef = getPersonService().getPerson(inviteeUserName);
        SiteInfo siteInfo = getSiteService().getSite(siteShortName);
        String siteDescription = siteInfo.getDescription();
        if (siteDescription == null) {
            siteDescription = "";
        } else if (siteDescription.length() > 255) {
            siteDescription = siteDescription.substring(0, 255);
        }

        // get the workflow description
        String workflowDescription = generateWorkflowDescription(siteInfo, "invitation.nominated.workflow.description");

        // create workflow properties
        Map<QName, Serializable> workflowProps = new HashMap<>(32);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME, inviterUserName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL, inviteeEmail);
        workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, inviteeNodeRef);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_FIRSTNAME, inviteeFirstName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_LASTNAME, inviteeLastName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD, inviteePassword);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME, siteShortName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE, siteInfo.getTitle());
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION, siteDescription);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE, resourceType.toString());
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE, inviteeSiteRole);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH, serverPath);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL, acceptUrl);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL, rejectUrl);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET, inviteTicket);

        return (NominatedInvitation) startWorkflow(wfDefinition, workflowProps);
    }

    private Invitation startWorkflow(WorkflowDefinition wfDefinition, Map<QName, Serializable> workflowProps) {
        NodeRef wfPackage = getWorkflowService().createPackage(null);
        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

        // start the workflow
        WorkflowPath wfPath = getWorkflowService().startWorkflow(wfDefinition.getId(), workflowProps);

        //
        // complete invite workflow start task to send out the invite email
        //
        // get the workflow tasks
        String workflowId = wfPath.getInstance().getId();
        WorkflowTask startTask = getWorkflowService().getStartTask(workflowId);

        // attach empty package to start task, end it and follow with transition
        // that sends out the invite
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Invite workflow task by attaching empty package...");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Transitioning Invite workflow task...");
        }
        try {
            getWorkflowService().endTask(startTask.getId(), null);
        } catch (RuntimeException err) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed - caught error during Invite workflow transition: " + err.getMessage());
            }
            throw err;
        }

        Invitation invitation = getInvitation(startTask);
        return invitation;
    }

    /**
     * Return Activiti workflow definition unless Activiti engine is disabled.
     *
     * @param isNominated TODO
     * @return
     */
    private WorkflowDefinition getWorkflowDefinition(boolean isNominated) {
        String workflowName = isNominated ? getNominatedDefinitionName() : getModeratedDefinitionName();
        WorkflowDefinition definition = getWorkflowService().getDefinitionByName(workflowName);
        if (definition == null) {
            // handle workflow definition does not exist
            Object objs[] = {workflowName};
            throw new InvitationException("invitation.error.noworkflow", objs);
        }
        return definition;
    }

    private String getNominatedDefinitionName() {
        if (workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID)) {
            return WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI;
        } else if (workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID)) {
            return WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME;
        }
        throw new IllegalStateException("None of the Workflow engines supported by teh InvitationService are currently enabled!");
    }

    private String getModeratedDefinitionName() {
        if (workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID)) {
            return WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI;
        } else if (workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID)) {
            return WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME;
        }
        throw new IllegalStateException("None of the Workflow engines supported by teh InvitationService are currently enabled!");
    }

    /**
     * Check that the specified user has manager role over the resource.
     *
     * @param userId
     * @throws InvitationException
     */
    private void checkManagerRole(String userId, Invitation.ResourceType resourceType, String siteShortName) {
        // if inviter is not the site manager then throw web script exception
        String inviterRole = getSiteService().getMembersRole(siteShortName, userId);

        /**
         * Koya modification : allow Collaborators to send invitation
         */
        if ((inviterRole != null)
                && (inviterRole.equals(SitePermission.MANAGER.toString())
                || inviterRole.equals(SitePermission.COLLABORATOR.toString()))) {
        } else {
            Object objs[] = {userId, siteShortName};
            throw new InvitationExceptionForbidden("invitation.invite.not_site_manager", objs);
        }
    }

    private int getMaxUserNameGenRetries() {
        return maxUserNameGenRetries;
    }

    /**
     * Validator for invitationId
     *
     * @param invitationId
     */
    private void validateInvitationId(String invitationId) {
        final String ID_SEPERATOR_REGEX = "\\$";
        String[] parts = invitationId.split(ID_SEPERATOR_REGEX);
        if (parts.length != 2) {
            Object objs[] = {invitationId};
            throw new InvitationExceptionUserError("invitation.error.invalid_inviteId_format", objs);
        }
    }

    private WorkflowTask getStartTask(String invitationId) {
        validateInvitationId(invitationId);
        WorkflowTask startTask = null;
        try {
            startTask = getWorkflowService().getStartTask(invitationId);
        } catch (WorkflowException we) {
            // Do nothing
        }
        if (startTask == null) {
            Object objs[] = {invitationId};
            throw new InvitationExceptionNotFound("invitation.error.not_found", objs);
        }
        return startTask;
    }

    @Override
    public Invitation accept(String invitationId, String ticket) {
        WorkflowTask startTask = getStartTask(invitationId);
        NominatedInvitation invitation = getNominatedInvitation(startTask);
        if (invitation == null) {
            throw new InvitationException("State error, accept may only be called on a nominated invitation.");
        }
        // Check invitationId and ticket match
        if (invitation.getTicket().equals(ticket) == false) {
            //TODO localise msg
            String msg = "Response to invite has supplied an invalid ticket. The response to the invitation could thus not be processed";
            throw new InvitationException(msg);
        }
        endInvitation(startTask,
                WorkflowModelNominatedInvitation.WF_TRANSITION_ACCEPT, null,
                WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING, WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);

        //MNT-9101 Share: Cancelling an invitation for a disabled user, the user gets deleted in the process.
        /*
        
         KOYA removed Code : fixes invitation by Manager bug
        
        
         NodeRef person = personService.getPersonOrNull(invitation.getInviterUserName());
         if (person != null && nodeService.hasAspect(person, ContentModel.ASPECT_ANULLABLE)) {            
         nodeService.removeAspect(person, ContentModel.ASPECT_ANULLABLE);
         }*/
        return invitation;
    }

    private void endInvitation(WorkflowTask startTask, String transition, Map<QName, Serializable> properties, QName... taskTypes) {
        // Deleting a person can cancel their invitations. Cancelling invitations can delete inactive persons! So prevent infinite looping here
        if (TransactionalResourceHelper.getSet(getClass().getName()).add(startTask.getPath().getInstance().getId())) {
            List<WorkflowTask> tasks = getWorkflowService().getTasksForWorkflowPath(startTask.getPath().getId());
            if (tasks.size() == 1) {
                WorkflowTask task = tasks.get(0);
                if (taskTypeMatches(task, taskTypes)) {
                    if (properties != null) {
                        getWorkflowService().updateTask(task.getId(), properties, null, null);
                    }                    
                    getWorkflowService().endTask(task.getId(), transition);
                    return;
                }
            }
            // Throw exception if the task not found.
            Object objs[] = {startTask.getPath().getInstance().getId()};
            throw new InvitationExceptionUserError("invitation.invite.already_finished", objs);
        }
    }

}
