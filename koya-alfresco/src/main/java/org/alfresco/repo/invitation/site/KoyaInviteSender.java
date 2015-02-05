package org.alfresco.repo.invitation.site;

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarAcceptUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteTicket;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeGenPassword;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviterUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRejectUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRole;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarServerPath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ModelUtil;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.surf.util.URLEncoder;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;

/**
 * Ovverride invite sender in order to provide custom invite mail subject
 *
 */
public class KoyaInviteSender extends InviteSender {

    private static final String EMAIL_SUBJECT = "koya.invitation.invitesender.email.subject";
    private Logger logger = Logger.getLogger(this.getClass());

    // koya sepecic injections
    private KoyaMailService koyaMailService;
    //

    private static final List<String> expectedProperties = Arrays.asList(wfVarInviteeUserName,//
            WorkflowModelNominatedInvitation.wfVarResourceName,//
            wfVarInviterUserName,//
            wfVarInviteeUserName,//
            WorkflowModelNominatedInvitation.wfVarRole,//
            WorkflowModelNominatedInvitation.wfVarInviteeGenPassword,//
            WorkflowModelNominatedInvitation.wfVarResourceName,//
            WorkflowModelNominatedInvitation.wfVarInviteTicket,//
            WorkflowModelNominatedInvitation.wfVarServerPath,//
            WorkflowModelNominatedInvitation.wfVarAcceptUrl,//
            WorkflowModelNominatedInvitation.wfVarRejectUrl, WF_INSTANCE_ID,//
            WF_PACKAGE);

    private final ActionService actionService;
    private final NodeService nodeService;
    private final PersonService personService;
    private final SearchService searchService;
    private final SiteService siteService;
    private final Repository repository;
    private final MessageService messageService;
    private final FileFolderService fileFolderService;
//    private final SysAdminParams sysAdminParams;
    private final RepoAdminService repoAdminService;
    private final NamespaceService namespaceService;
    private final KoyaNodeService koyaNodeService;
    private final CompanyAclService companyAclService;
    private final CompanyService companyService;

    public KoyaInviteSender(ServiceRegistry services, Repository repository, MessageService messageService,
            KoyaMailService koyaMailService, KoyaNodeService koyaNodeService,
            CompanyAclService companyAclService,CompanyService companyService) {

        super(services, repository, messageService);
        this.actionService = services.getActionService();
        this.nodeService = services.getNodeService();
        this.personService = services.getPersonService();
        this.searchService = services.getSearchService();
        this.siteService = services.getSiteService();
        this.fileFolderService = services.getFileFolderService();
//        this.sysAdminParams = services.getSysAdminParams();
        this.repoAdminService = services.getRepoAdminService();
        this.namespaceService = services.getNamespaceService();
        this.repository = repository;
        this.messageService = messageService;

        /**
         * Koya specific injections load
         */
        this.koyaMailService = koyaMailService;
        this.koyaNodeService = koyaNodeService;
        this.companyAclService = companyAclService;
        this.companyService = companyService;
    }

    @Override
    public void sendMail(Map<String, String> properties) {

        checkProperties(properties);

        ParameterCheck.mandatory("Properties", properties);
        NodeRef inviter = personService.getPerson(properties.get(wfVarInviterUserName));
        String inviteeName = properties.get(wfVarInviteeUserName);
        NodeRef invitee = personService.getPerson(inviteeName);
        Action mail = actionService.createAction(MailActionExecuter.NAME);
        mail.setExecuteAsynchronously(true);
        mail.setParameterValue(MailActionExecuter.PARAM_FROM, getEmail(inviter));
        mail.setParameterValue(MailActionExecuter.PARAM_TO, getEmail(invitee));

        try {
            /**
             * KOYA : specific email subject
             *
             */

            mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, koyaMailService.getI18nSubject(EMAIL_SUBJECT).replace("{0}", getSiteName(properties)));
            /**
             *
             */
            mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT_PARAMS, new Object[]{
                ModelUtil.getProductName(repoAdminService), getSiteName(properties)});
            mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, getEmailTemplateNodeRef());
            mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,
                    (Serializable) buildMailTextModel(properties, inviter, invitee));
            mail.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
            actionService.executeAction(mail, getWorkflowPackage(properties));
        } catch (KoyaServiceException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * @param properties
     */
    private void checkProperties(Map<String, String> properties) {
        Set<String> keys = properties.keySet();
        if (!keys.containsAll(expectedProperties)) {
            LinkedList<String> missingProperties = new LinkedList<>(expectedProperties);
            missingProperties.removeAll(keys);
            throw new InvitationException("The following mandatory properties are missing:\n" + missingProperties);
        }
    }

    private String getEmail(NodeRef person) {
        return (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL);
    }

    private NodeRef getEmailTemplateNodeRef() {
        List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(),
                "app:company_home/app:dictionary/app:email_templates/cm:koya_templates/cm:invite.html.ftl", null,
                this.namespaceService, false);        

        if (nodeRefs.size() == 1) {
            // Now localise this
            NodeRef base = nodeRefs.get(0);
            NodeRef local = fileFolderService.getLocalizedSibling(base);
            return local;
        } else {
            throw new InvitationException("Cannot find the email template!");
        }
    }

    private Map<String, Serializable> buildMailTextModel(Map<String, String> properties, NodeRef inviter, NodeRef invitee) {
        // Set the core model parts
        // Note - the user part is skipped, as that's implied via the run-as
        Map<String, Serializable> model = new HashMap<String, Serializable>();
        model.put(TemplateService.KEY_COMPANY_HOME, repository.getCompanyHome());
        model.put(TemplateService.KEY_USER_HOME, repository.getUserHome(inviter));
        model.put(TemplateService.KEY_PRODUCT_NAME, ModelUtil.getProductName(repoAdminService));

        // Build up the args for rendering inside the template
        Map<String, String> args = buildArgs(properties, inviter, invitee);
        model.put("args", (Serializable) args);
        
        
        try{
        	//Koya invitation specific variables
        	//TODO get invite Items from specific workflow model
        	//model.put("InviteItem", false);
        	
        	model.put("company",companyService.getProperties(properties.get(wfVarResourceName)).toHashMap());
        }catch(Exception e){        	
        }
        // All done
        return model;
    }

    private NodeRef getWorkflowPackage(Map<String, String> properties) throws KoyaServiceException {
        String packageRef = properties.get(WF_PACKAGE);
        return koyaNodeService.getNodeRef(packageRef);
    }

    private String getSiteName(Map<String, String> properties) {
        String siteFullName = properties.get(wfVarResourceName);
        SiteInfo site = siteService.getSite(siteFullName);

        if (site == null) {
            throw new InvitationException("The site " + siteFullName + " could not be found.");
        }

        String siteName = site.getShortName();
        String siteTitle = site.getTitle();
        if (siteTitle != null && siteTitle.length() > 0) {
            siteName = siteTitle;
        }
        return siteName;
    }

    private Map<String, String> buildArgs(Map<String, String> properties, NodeRef inviter, NodeRef invitee) {
        String params = buildUrlParamString(properties);
        String acceptLink = makeLink(properties.get(wfVarServerPath), properties.get(wfVarAcceptUrl), params);
        String rejectLink = makeLink(properties.get(wfVarServerPath), properties.get(wfVarRejectUrl), params);

        Map<String, String> args = new HashMap<String, String>();
        args.put("inviteePersonRef", invitee.toString());
        args.put("inviterPersonRef", inviter.toString());
        args.put("siteName", getSiteName(properties));
        args.put("inviteeSiteRole", getRoleName(properties));
        args.put("inviteeUserName", properties.get(wfVarInviteeUserName));
        args.put("inviteeGenPassword", properties.get(wfVarInviteeGenPassword));
        args.put("acceptLink", acceptLink);
        args.put("rejectLink", rejectLink);               
        args.put("koyaClientServerPath",companyAclService.getKoyaClientServerPath());
        
        return args;
    }

    private String buildUrlParamString(Map<String, String> properties) {
        StringBuilder params = new StringBuilder("?inviteId=");
        params.append(properties.get(WF_INSTANCE_ID));
        params.append("&inviteeUserName=");
        params.append(URLEncoder.encode(properties.get(wfVarInviteeUserName)));
        params.append("&siteShortName=");
        params.append(properties.get(wfVarResourceName));
        params.append("&inviteTicket=");
        params.append(properties.get(wfVarInviteTicket));
        return params.toString();
    }

    private String getRoleName(Map<String, String> properties) {
        String roleName = properties.get(wfVarRole);
        String role = messageService.getMessage("invitation.invitesender.email.role." + roleName);
        if (role == null) {
            role = roleName;
        }
        return role;
    }
}
