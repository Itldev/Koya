package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class KoyaMailService {
    
    private final Logger logger = Logger.getLogger(this.getClass());
    
    private final static String SHARE_NOTIFICATION_SUBJECT = "koya.share-notification.subject";
    private final static String RESET_PASSWORD_SUBJECT = "koya.reset-password.subject";
    private final static String INSTANT_NOTIFICATION_SUBJECT = "koya.newcontent-notification.subject";
    
    protected NamespaceService namespaceService;
    protected FileFolderService fileFolderService;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected ActionService actionService;
    protected KoyaNodeService koyaNodeService;
    protected UserService userService;
    protected ServiceRegistry serviceRegistry;

    //Mail subject properties template
    protected RepositoryLocation i18nMailSubjectPropertiesLocation;

    //Share Notification Template
    protected RepositoryLocation shareNotificationTemplateLocation;
    protected RepositoryLocation resetPasswordTemplateLocation;
    protected RepositoryLocation newContentNoficationTemplateLocation;
    /**
     * Optional parameters, if not set, use clasic share url
     */
    protected String koyaDirectLinkUrlTemplate;

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
    
    public void setI18nMailSubjectPropertiesLocation(RepositoryLocation i18nMailSubjectPropertiesLocation) {
        this.i18nMailSubjectPropertiesLocation = i18nMailSubjectPropertiesLocation;
    }
    
    public void setShareNotificationTemplateLocation(RepositoryLocation shareNotificationTemplateLocation) {
        this.shareNotificationTemplateLocation = shareNotificationTemplateLocation;
    }
    
    public void setResetPasswordTemplateLocation(RepositoryLocation resetPasswordTemplateLocation) {
        this.resetPasswordTemplateLocation = resetPasswordTemplateLocation;
    }
    
    public void setNewContentNoficationTemplateLocation(RepositoryLocation newContentNoficationTemplateLocation) {
        this.newContentNoficationTemplateLocation = newContentNoficationTemplateLocation;
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

    //</editor-fold>
    public void sendShareNotifMail(User sender, String destMail, NodeRef sharedNodeRef) throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();
        
        paramsMail.put(MailActionExecuter.PARAM_TO, destMail);
        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT, getI18nSubject(SHARE_NOTIFICATION_SUBJECT));
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, getFileTemplateRef(shareNotificationTemplateLocation));

        //TODO i18n templates
        Map<String, Serializable> templateModel = new HashMap<>();
        Map<String, Serializable> templateParams = new HashMap<>();
        
        SecuredItem s = koyaNodeService.getSecuredItem(sharedNodeRef);
        /**
         * TODO use global-properties param to set reset request url
         */
        templateParams.put("directAccessUrl", getDirectLinkUrl(sharedNodeRef));
        templateParams.put("sharedItemNodeRef", s.getNodeRef());
        templateParams.put("sharedItemName", s.getTitle());
        templateParams.put("inviterName", sender.getName());
        templateParams.put("inviterFirstName", sender.getFirstName());
        templateParams.put("inviterEmail", sender.getEmail());
        
        templateModel.put("args", (Serializable) templateParams);
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
        
        actionService.executeAction(actionService.createAction(
                MailActionExecuter.NAME, paramsMail), null);
        
    }
    
    public void sendResetRequestMail(String destMail, String resetRequestUrl) throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();
        
        paramsMail.put(MailActionExecuter.PARAM_TO, destMail);
        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT, getI18nSubject(RESET_PASSWORD_SUBJECT));
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, getFileTemplateRef(resetPasswordTemplateLocation));

        //TODO i18n templates
        Map<String, Serializable> templateModel = new HashMap<>();
        Map<String, Serializable> templateParams = new HashMap<>();

        /**
         * TODO use global-properties param to set reset request url
         */
        templateParams.put("resetRequestUrl", resetRequestUrl);
        
        templateModel.put("args", (Serializable) templateParams);
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
        
        actionService.executeAction(actionService.createAction(
                MailActionExecuter.NAME, paramsMail), null);
    }
    
    public void sendNewContentNotificationMail(User dest, final NodeRef sharedItem) throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();
        
        paramsMail.put(MailActionExecuter.PARAM_TO, dest.getEmail());
        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT, getI18nSubject(INSTANT_NOTIFICATION_SUBJECT));
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, getFileTemplateRef(newContentNoficationTemplateLocation));
        
        Map<String, Serializable> templateModel = new HashMap<>();
        
        templateModel.put("person", new ScriptNode(
                userService.getUserByUsername((String) nodeService.getProperty(sharedItem, ContentModel.PROP_MODIFIER)).getNodeRefasObject(), serviceRegistry));
        
        templateModel.put("document", new HashMap() {
            {
                put("name", nodeService.getProperty(sharedItem, ContentModel.PROP_TITLE));
                put("siteShortName", koyaNodeService.getFirstParentOfType(sharedItem, Company.class).getTitle());
                put("directLinkUrl", getDirectLinkUrl(sharedItem));
            }
        });
        templateModel.put("date", nodeService.getProperty(sharedItem, ContentModel.PROP_UPDATED));

        /**
         * TODO Add company and dossiers (or all path ) references to template
         */
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
        
        actionService.executeAction(actionService.createAction(
                MailActionExecuter.NAME, paramsMail), null);
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
        paramsMail.put(MailActionExecuter.PARAM_TO_MANY, new ArrayList(wrapper.getTo()));

        /**
         * DO NOT set MailActionExecuter.PARAM_FROM with wrapper.getFrom() if
         * not not null --> mail always sent with default mail adress. not
         * forged address
         */
        /**
         * Get subject and body Templates
         */
        if (wrapper.getTemplateXPath() != null) {
            RepositoryLocation templateLoc = new RepositoryLocation();//defaultQuery language = xpath
            templateLoc.setPath(wrapper.getTemplateXPath());
            
            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, getFileTemplateRef(templateLoc));
            Map<String, Serializable> templateModel = new HashMap<>();
            
            templateModel.put("args", (Serializable) wrapper.getTemplateParams());
            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
        } else {
            paramsMail.put(MailActionExecuter.PARAM_TEXT, wrapper.getContent());
        }
        
        if (wrapper.getTemplateKoyaSubjectKey() != null) {
            
            String subject = (String) getI18nSubject(wrapper.getTemplateKoyaSubjectKey());

            /**
             * TODO replace MailActionExecuter.PARAM_SUBJECT_PARAMS parameter
             */
            for (int i = 0; i < wrapper.getTemplateKoyaSubjectParams().size(); i++) {
                subject = subject.replace("{" + i + "}", wrapper.getTemplateKoyaSubjectParams().get(i));
            }
            paramsMail.put(MailActionExecuter.PARAM_SUBJECT, subject);
            
        } else {
            paramsMail.put(MailActionExecuter.PARAM_SUBJECT, wrapper.getSubject());
        }

        /**
         * Action execution
         */
        actionService.executeAction(actionService.createAction(
                MailActionExecuter.NAME, paramsMail), null);
        
    }
    
    public String getI18nSubject(String propKey) throws KoyaServiceException {
        Properties i18n = koyaNodeService.readPropertiesFileContent(
                getFileTemplateRef(i18nMailSubjectPropertiesLocation));
        if (i18n == null) {
            throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_INVALID_SUBJECT_PROPERTIES_PATH,
                    "Invalid koya Mail subject properties path : "
                    + i18nMailSubjectPropertiesLocation.getPath());
        }
        
        String value = i18n.getProperty(propKey);
        
        if (value != null && !value.isEmpty()) {
            return value;
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_SUBJECT_KEY_NOT_EXISTS_IN_PROPERTIES, " missing key = " + propKey);
        }
    }

    /**
     * Returns nodeRef of template location. retruns I18n version if found
     *
     * @param templateRepoLocation
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public NodeRef getFileTemplateRef(RepositoryLocation templateRepoLocation) throws KoyaServiceException {
        String locationType = templateRepoLocation.getQueryLanguage();
        
        if (locationType.equals(SearchService.LANGUAGE_XPATH)) {
            StoreRef store = templateRepoLocation.getStoreRef();
            String xpath = templateRepoLocation.getPath();
            
            try {
                List<NodeRef> nodeRefs = searchService.selectNodes(
                        nodeService.getRootNode(store), xpath, null, namespaceService, false);
                if (nodeRefs.size() != 1) {
                    throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE,
                            nodeRefs.size() + " nodes match search");
                }
                return fileFolderService.getLocalizedSibling(nodeRefs.get(0));
            } catch (SearcherException e) {
                throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_CANNOT_FIND_TEMPLATE, e);
            }
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_UNSUPPORTED_TEMPLATE_LOCATION_TYPE,
                    "given type : " + locationType + " expected xpath");
        }
    }
    
    public String getDirectLinkUrl(NodeRef n) {
        if (koyaDirectLinkUrlTemplate == null || koyaDirectLinkUrlTemplate.isEmpty()) {
            return "#";//TODO build share url
        } else {
            return koyaDirectLinkUrlTemplate.replace("{nodeRef}", n.toString());
        }
    }
}
