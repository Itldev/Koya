package org.alfresco.repo.invitation;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.alfresco.repo.i18n.MessageService;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarAcceptUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteTicket;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeGenPassword;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviterUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRejectUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRole;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarServerPath;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.invitation.site.KoyaInviteSender;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 *
 */
public class KoyaInviteHelper extends InviteHelper implements InitializingBean {

    private static final Log logger = LogFactory.getLog(ModeratedActionReject.class);

    private static final Collection<String> sendInvitePropertyNames = Arrays.asList(wfVarInviteeUserName,//
            wfVarResourceName,//
            wfVarInviterUserName,//
            wfVarInviteeUserName,//
            wfVarRole,//
            wfVarInviteeGenPassword,//
            wfVarResourceName,//
            wfVarInviteTicket,//
            wfVarServerPath,//
            wfVarAcceptUrl,//
            wfVarRejectUrl,
            InviteSender.WF_INSTANCE_ID);

    private Repository repositoryHelper;
    private ServiceRegistry serviceRegistry;

    private ActionService actionService;
    private InvitationService invitationService;
    private MutableAuthenticationService authenticationService;
    private MessageService messageService;
    private NamespaceService namespaceService;
    private PersonService personService;
    private SiteService siteService;
    private TemplateService templateService;
    private WorkflowService workflowService;
    private NodeService nodeService;

    private InviteSender inviteSender;

    /**
     *
     * Koya specific property
     */
    private KoyaMailService koyaMailService;

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    @Override
    public void afterPropertiesSet() {
        this.actionService = serviceRegistry.getActionService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
        this.invitationService = serviceRegistry.getInvitationService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.personService = serviceRegistry.getPersonService();
        this.siteService = serviceRegistry.getSiteService();
        this.templateService = serviceRegistry.getTemplateService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.nodeService = serviceRegistry.getNodeService();
        this.inviteSender = new KoyaInviteSender(serviceRegistry, repositoryHelper, messageService,
                koyaMailService);
    }

    /**
     * redefined method in order to execute membership setting as system user so
     * collaborator can send invitations that are acceptable.
     *
     * @param invitee
     * @param siteName
     * @param role
     * @param runAsUser
     * @param overrideExisting
     */
    @Override
    public void addSiteMembership(final String invitee, final String siteName,
            final String role, final String runAsUser, final boolean overrideExisting) {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                if (overrideExisting || !siteService.isMember(siteName, invitee)) {
                    siteService.setMembership(siteName, invitee, role);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    @Override
    public void acceptNominatedInvitation(Map<String, Object> executionVariables) {
        final String invitee = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String siteShortName = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarResourceName);
        String inviter = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        String role = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarRole);

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                if (authenticationService.isAuthenticationMutable(invitee)) {
                    authenticationService.setAuthenticationEnabled(invitee, true);
                }
                return null;
            }
        });
        addSiteMembership(invitee, siteShortName, role, inviter, false);
    }

    @Override
    public void sendNominatedInvitation(String inviteId, Map<String, Object> executionVariables) {
        if (invitationService.isSendEmails()) {
            Map<String, String> properties = makePropertiesFromContextVariables(executionVariables, sendInvitePropertyNames);

            String packageName = WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService).replace(":", "_");
            ScriptNode packageNode = (ScriptNode) executionVariables.get(packageName);
            String packageRef = packageNode.getNodeRef().toString();
            properties.put(InviteSender.WF_PACKAGE, packageRef);

            properties.put(InviteSender.WF_INSTANCE_ID, inviteId);

            inviteSender.sendMail(properties);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> makePropertiesFromContextVariables(Map<?, ?> executionVariables, Collection<String> propertyNames) {
        return CollectionUtils.filterKeys((Map<String, String>) executionVariables, CollectionUtils.containsFilter(propertyNames));
    }

    /**
     * @param messageService the messageService to set
     */
    @Override
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * @param repositoryHelper the repositoryHelper to set
     */
    @Override
    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
