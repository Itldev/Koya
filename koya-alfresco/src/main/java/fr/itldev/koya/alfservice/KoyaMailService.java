package fr.itldev.koya.alfservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.invitation.site.KoyaInviteSender;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 *
 *
 */
public class KoyaMailService implements InitializingBean {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final static String SHARE_NOTIFICATION_SUBJECT = "koya.share-notification.subject";
    private final static String RESET_PASSWORD_SUBJECT = "koya.reset-password.subject";
    private final static String INSTANT_NOTIFICATION_SUBJECT = "koya.newcontent-notification.subject";
    private final static String INACTIVEDOSSIER_NOTIFICATION_SUBJECT = "koya.inactivedossier-notification.subject";

    // templates locations
    private final static String TPL_MAIL_KOYAROOT = "//app:company_home/app:dictionary/app:email_templates/cm:koya_templates";

    private final static String TPL_MAIL_I18NSUBJECTS = TPL_MAIL_KOYAROOT
            + "/cm:koyamail.properties";
    private final static String TPL_MAIL_SHARENOTIF = TPL_MAIL_KOYAROOT
            + "/cm:share-notification.html.ftl";
    private final static String TPL_MAIL_RESET_PWD = TPL_MAIL_KOYAROOT
            + "/cm:reset-password.html.ftl";
    private final static String TPL_MAIL_NEWCONTENTNOTIF_ = TPL_MAIL_KOYAROOT
            + "/cm:new-content.html.ftl";
    public final static String TPL_MAIL_INVITATION = TPL_MAIL_KOYAROOT
            + "/cm:invite.html.ftl";
    private final static String TPL_MAIL_INACTIVEDOSSIERNOTIF_ = TPL_MAIL_KOYAROOT
            + "/cm:inactive-dossiers.html.ftl";

    protected NamespaceService namespaceService;
    protected FileFolderService fileFolderService;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected ActionService actionService;
    protected KoyaNodeService koyaNodeService;
    protected UserService userService;
    protected ServiceRegistry serviceRegistry;
    protected CompanyAclService companyAclService;
    protected CompanyService companyService;
    protected CompanyPropertiesService companyPropertiesService;

    protected WorkflowService workflowService;
    protected AuthenticationService authenticationService;
    protected Repository repositoryHelper;
    protected MessageService messageService;

    // Mail subject properties template
    protected RepositoryLocation i18nMailSubjectPropertiesLocation;

    // Share Notification Template
    protected RepositoryLocation shareNotificationTemplateLocation;
    protected RepositoryLocation resetPasswordTemplateLocation;
    protected RepositoryLocation newContentNoficationTemplateLocation;
    protected RepositoryLocation inactiveDossierNoficationTemplateLocation;

    /**
     * Optional parameters, if not set, use clasic share url
     */
    protected String koyaDirectLinkUrlTemplate;

    /**
     *
     *
     */
    protected HashMap<String, Object> koyaClientParams;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setKoyaDirectLinkUrlTemplate(String koyaDirectLinkUrlTemplate) {
        this.koyaDirectLinkUrlTemplate = koyaDirectLinkUrlTemplate;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setCompanyPropertiesService(
            CompanyPropertiesService companyPropertiesService) {
        this.companyPropertiesService = companyPropertiesService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setAuthenticationService(
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public HashMap getKoyaClientParams() {
        return koyaClientParams;
    }

    // </editor-fold>
    @Override
    public void afterPropertiesSet() throws Exception {
        koyaClientParams = new HashMap<>();
        koyaClientParams.put("serverPath",
                companyAclService.getKoyaClientServerPath());
        initTemplatesRefs();
    }

    private void initTemplatesRefs() {
        i18nMailSubjectPropertiesLocation = new RepositoryLocation(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                TPL_MAIL_I18NSUBJECTS, "xpath");

        shareNotificationTemplateLocation = new RepositoryLocation(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TPL_MAIL_SHARENOTIF,
                "xpath");
        resetPasswordTemplateLocation = new RepositoryLocation(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TPL_MAIL_RESET_PWD,
                "xpath");
        newContentNoficationTemplateLocation = new RepositoryLocation(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                TPL_MAIL_NEWCONTENTNOTIF_, "xpath");
        inactiveDossierNoficationTemplateLocation = new RepositoryLocation(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                TPL_MAIL_INACTIVEDOSSIERNOTIF_, "xpath");

    }

    public void sendShareNotifMail(User sender, String destMail, Company c,
            final NodeRef sharedNodeRef) throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();
        paramsMail.put(MailActionExecuter.PARAM_TO, destMail);

        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE,
                getFileTemplateRef(shareNotificationTemplateLocation));

        // TODO i18n templates
        Map<String, Serializable> templateModel = new HashMap<>();

        final SecuredItem s = koyaNodeService.getSecuredItem(sharedNodeRef);

        /**
         * Model Objects
         */
        templateModel.put("sharedItem", new HashMap() {
            {
                put("url", getDirectLinkUrl(sharedNodeRef));
                put("nodeRef", s.getNodeRef());
                put("title", s.getTitle());
            }
        });

        templateModel.put("koyaClient", koyaClientParams);

        templateModel.put("inviter", new ScriptNode(
                sender.getNodeRefasObject(), serviceRegistry));
        templateModel.put(TemplateService.KEY_COMPANY_HOME,
                repositoryHelper.getCompanyHome());
        templateModel.put("company", companyPropertiesService.getProperties(c)
                .toHashMap());

        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL,
                (Serializable) templateModel);

        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
                getI18nSubject(SHARE_NOTIFICATION_SUBJECT, templateModel));

        actionService
                .executeAction(actionService.createAction(
                        MailActionExecuter.NAME, paramsMail), null, false, true);

    }

    public void sendResetRequestMail(String destMail, String resetRequestUrl)
            throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();

        paramsMail.put(MailActionExecuter.PARAM_TO, destMail);

        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE,
                getFileTemplateRef(resetPasswordTemplateLocation));

        // TODO i18n templates
        Map<String, Serializable> templateModel = new HashMap<>();

        /**
         * TODO use global-properties param to set reset request url
         */
        templateModel.put("resetRequestUrl", resetRequestUrl);
        templateModel.put("koyaClient", koyaClientParams);

        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL,
                (Serializable) templateModel);

        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
                getI18nSubject(RESET_PASSWORD_SUBJECT, templateModel));

        actionService
                .executeAction(actionService.createAction(
                        MailActionExecuter.NAME, paramsMail), null, false, true);
    }

    public void sendNewContentNotificationMail(User dest,
            final NodeRef sharedItem) throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();

        paramsMail.put(MailActionExecuter.PARAM_TO, dest.getEmail());

        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE,
                getFileTemplateRef(newContentNoficationTemplateLocation));

        Map<String, Serializable> templateModel = new HashMap<>();

        templateModel.put("koyaClient", koyaClientParams);
        templateModel.put(
                "person",
                new ScriptNode(userService.getUserByUsername(
                        (String) nodeService.getProperty(sharedItem,
                                ContentModel.PROP_MODIFIER))
                        .getNodeRefasObject(), serviceRegistry));

        templateModel.put("date",
                nodeService.getProperty(sharedItem, ContentModel.PROP_UPDATED));

        Company c = koyaNodeService.getFirstParentOfType(sharedItem,
                Company.class);

        templateModel.put("company", companyPropertiesService.getProperties(c)
                .toHashMap());

        final String compTitle = c.getTitle();

        templateModel.put("document", new HashMap() {
            {
                put("name", nodeService.getProperty(sharedItem,
                        ContentModel.PROP_TITLE));
                put("siteShortName", compTitle);
                put("directLinkUrl", getDirectLinkUrl(sharedItem));
            }
        });

        /**
         * TODO Add company and dossiers (or all path ) references to template
         */
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL,
                (Serializable) templateModel);

        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
                getI18nSubject(INSTANT_NOTIFICATION_SUBJECT, templateModel));

        actionService
                .executeAction(actionService.createAction(
                        MailActionExecuter.NAME, paramsMail), null, false, true);
    }

    public void sendInactiveDossierNotification(User dest, NodeRef space,
            List<NodeRef> inactiveDossiers, Company c)
            throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();

        paramsMail.put(MailActionExecuter.PARAM_TO, dest.getEmail());

        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE,
                getFileTemplateRef(inactiveDossierNoficationTemplateLocation));

        Map<String, Serializable> templateModel = new HashMap<>();
        templateModel.put(TemplateService.KEY_COMPANY_HOME,
                repositoryHelper.getCompanyHome());

        templateModel.put("koyaClient", koyaClientParams);
        List<Map<String, Serializable>> params = CollectionUtils.transform(
                inactiveDossiers,
                new Function<NodeRef, Map<String, Serializable>>() {

                    @Override
                    public Map<String, Serializable> apply(final NodeRef value) {
                        return new HashMap<String, Serializable>() {
                            {
                                put("nodeRef", value);
                                put("url", getDirectLinkUrl(value));

                            }
                        };
                    }
                });
        templateModel.put("inactiveDossiers", (Serializable) params);

        templateModel.put("company", companyPropertiesService.getProperties(c)
                .toHashMap());

        /**
         * TODO Add company and dossiers (or all path ) references to template
         */
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL,
                (Serializable) templateModel);

        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(
                MailActionExecuter.PARAM_SUBJECT,
                getI18nSubject(INACTIVEDOSSIER_NOTIFICATION_SUBJECT,
                        templateModel));

        actionService
                .executeAction(actionService.createAction(
                        MailActionExecuter.NAME, paramsMail), space, false,
                        true);
    }

    /**
     * Send again invitation mail return destination email adress.
     * 
     * @param inviteId
     * @return
     * @throws KoyaServiceException
     */
    public String sendInviteMail(final String inviteId)
            throws KoyaServiceException {

        WorkflowTask task = AuthenticationUtil
                .runAsSystem(new AuthenticationUtil.RunAsWork<WorkflowTask>() {
                    @Override
                    public WorkflowTask doWork() throws Exception {
                        return workflowService.getStartTask(inviteId);
                    }
                });

        KoyaInviteSender koyaInviteSender = new KoyaInviteSender(
                serviceRegistry, repositoryHelper, messageService, this,
                koyaNodeService, companyAclService, companyService,
                companyPropertiesService, koyaClientParams);

        Map<String, String> properties = new HashMap<>();

        properties
                .put(WorkflowModelNominatedInvitation.wfVarInviteeUserName,
                        (String) task
                                .getProperties()
                                .get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME));
        properties.put(
                WorkflowModelNominatedInvitation.wfVarAcceptUrl,
                (String) task.getProperties().get(
                        WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL));
        properties.put(
                WorkflowModelNominatedInvitation.wfVarServerPath,
                (String) task.getProperties().get(
                        WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH));
        properties.put(
                WorkflowModelNominatedInvitation.wfVarRole,
                (String) task.getProperties().get(
                        WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE));
        properties
                .put(WorkflowModelNominatedInvitation.wfVarInviterUserName,
                        (String) task
                                .getProperties()
                                .get(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME));
        properties
                .put(WorkflowModelNominatedInvitation.wfVarInviteeGenPassword,
                        (String) task
                                .getProperties()
                                .get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD));
        properties
                .put(WorkflowModelNominatedInvitation.wfVarResourceName,
                        (String) task
                                .getProperties()
                                .get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME));
        properties.put(
                WorkflowModelNominatedInvitation.wfVarRejectUrl,
                (String) task.getProperties().get(
                        WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL));
        properties
                .put(WorkflowModelNominatedInvitation.wfVarInviteTicket,
                        (String) task
                                .getProperties()
                                .get(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET));

        properties.put(
                WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId,
                (String) task.getProperties().get(
                        WorkflowModel.PROP_WORKFLOW_INSTANCE_ID));

        properties.put(InviteSender.WF_PACKAGE, ((NodeRef) task.getProperties()
                .get(WorkflowModel.ASSOC_PACKAGE)).toString());
        properties.put(InviteSender.WF_INSTANCE_ID, inviteId);

        koyaInviteSender.sendMail(properties);
        return (String) task.getProperties().get(
                WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL);
    }

    /**
     * 
     * @param wrapper
     * @throws KoyaServiceException
     */
    public void sendMail(MailWrapper wrapper) throws KoyaServiceException {

        /**
         * Params Setting
         */
        Map<String, Serializable> paramsMail = new HashMap<>();
        paramsMail.put(MailActionExecuter.PARAM_TO_MANY,
                new ArrayList(wrapper.getTo()));

        Map<String, Serializable> templateModel = new HashMap<>();
        /**
         * Get subject and body Templates
         */
        if (wrapper.getTemplateXPath() != null) {
            RepositoryLocation templateLoc = new RepositoryLocation();// defaultQuery
            // language
            // =
            // xpath
            templateLoc.setPath(wrapper.getTemplateXPath());

            // TODO wrapper should only indicate mail template name. Root path
            // is determniated
            // by company context (spcific template or koya generic template or
            // null)
            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE,
                    getFileTemplateRef(templateLoc));

            templateModel.put("args",
                    (Serializable) wrapper.getTemplateParams());
            templateModel.put("koyaClient", koyaClientParams);

            Company c = koyaNodeService.getSecuredItem(
                    koyaNodeService.getNodeRef(wrapper.getCompanyNodeRef()),
                    Company.class);
            templateModel.put("company", companyPropertiesService
                    .getProperties(c).toHashMap());

            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL,
                    (Serializable) templateModel);
        } else {
            paramsMail.put(MailActionExecuter.PARAM_TEXT, wrapper.getContent());
        }

        if (wrapper.getTemplateKoyaSubjectKey() != null) {

            String subject = (String) getI18nSubject(
                    wrapper.getTemplateKoyaSubjectKey(), templateModel);

            /**
             * TODO replace MailActionExecuter.PARAM_SUBJECT_PARAMS parameter
             */
            for (int i = 0; i < wrapper.getTemplateKoyaSubjectParams().size(); i++) {
                subject = subject.replace("{" + i + "}", wrapper
                        .getTemplateKoyaSubjectParams().get(i));
            }
            paramsMail.put(MailActionExecuter.PARAM_SUBJECT, subject);

        } else {
            paramsMail.put(MailActionExecuter.PARAM_SUBJECT,
                    wrapper.getSubject());
        }

        /**
         * Action execution
         */
        actionService
                .executeAction(actionService.createAction(
                        MailActionExecuter.NAME, paramsMail), null);

    }

    public String getI18nSubject(String propKey,
            Map<String, Serializable> templateValues)
            throws KoyaServiceException {
        Properties i18n = koyaNodeService
                .readPropertiesFileContent(getFileTemplateRef(i18nMailSubjectPropertiesLocation));
        if (i18n == null) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.KOYAMAIL_INVALID_SUBJECT_PROPERTIES_PATH,
                    "Invalid koya Mail subject properties path : "
                            + i18nMailSubjectPropertiesLocation.getPath());
        }

        String mailSubject = i18n.getProperty(propKey);

        if (mailSubject != null && !mailSubject.isEmpty()) {
            // replace templates values in subject if exists

            Pattern p = Pattern.compile("\\$\\{([^}]+)\\}");
            Matcher m = p.matcher(mailSubject);
            while (m.find()) {
                String varName = m.group(1);
                String varReplaced = null;

                try {
                    Object replaceValue = templateValues;
                    for (String varChunk : varName.split("\\.")) {
                        replaceValue = ((Map) replaceValue).get(varChunk);
                    }
                    varReplaced = replaceValue.toString();
                } catch (Exception e) {
                }

                if (varReplaced != null) {
                    // Do replacement in value String
                    mailSubject = mailSubject.replace("${" + varName + "}",
                            varReplaced);
                } else {
                    logger.warn("mail Subject remplacement token doesn't match a variable : "
                            + m.group());
                }
            }

            logger.trace("mail subject = " + mailSubject);

            return mailSubject;
        } else {
            throw new KoyaServiceException(
                    KoyaErrorCodes.KOYAMAIL_SUBJECT_KEY_NOT_EXISTS_IN_PROPERTIES,
                    " missing key = " + propKey);
        }
    }

    /**
     * Returns nodeRef of template location. retruns I18n version if found
     * 
     * @param templateRepoLocation
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public NodeRef getFileTemplateRef(RepositoryLocation templateRepoLocation)
            throws KoyaServiceException {
        String locationType = templateRepoLocation.getQueryLanguage();

        if (locationType.equals(SearchService.LANGUAGE_XPATH)) {
            StoreRef store = templateRepoLocation.getStoreRef();
            String xpath = templateRepoLocation.getPath();

            try {
                List<NodeRef> nodeRefs = searchService.selectNodes(
                        nodeService.getRootNode(store), xpath, null,
                        namespaceService, false);
                if (nodeRefs.size() != 1) {
                    throw new KoyaServiceException(
                            KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE,
                            nodeRefs.size() + " nodes match search");
                }
                return fileFolderService.getLocalizedSibling(nodeRefs.get(0));
            } catch (SearcherException e) {
                throw new KoyaServiceException(
                        KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE, e);
            }
        } else {
            throw new KoyaServiceException(
                    KoyaErrorCodes.KOYAMAIL_UNSUPPORTED_TEMPLATE_LOCATION_TYPE,
                    "given type : " + locationType + " expected xpath");
        }
    }

    public String getDirectLinkUrl(NodeRef n) {
        if (koyaDirectLinkUrlTemplate == null
                || koyaDirectLinkUrlTemplate.isEmpty()) {
            return "#";// TODO build share url
        } else {
            return koyaDirectLinkUrlTemplate.replace("{nodeRef}", n.toString());
        }
    }

}
