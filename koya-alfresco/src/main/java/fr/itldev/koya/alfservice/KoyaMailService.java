package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 *
 *
 */
public class KoyaMailService {

    private final static String SHARE_NOTIFICATION_SUBJECT = "koya.share-notification.subject";

    protected NamespaceService namespaceService;
    protected FileFolderService fileFolderService;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected ActionService actionService;
    protected KoyaNodeService koyaNodeService;

    //Mail subject properties template
    protected RepositoryLocation i18nMailSubjectPropertiesLocation;

    //Share Notification Template
    protected RepositoryLocation shareNotificationTemplateLocation;

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

    public void sendShareNotifMail(User sender, String destMail, NodeRef sharedNodeRef) throws KoyaServiceException {
        Map<String, Serializable> paramsMail = new HashMap<>();

        Properties i18n = getI18nSubjectProperties();

        paramsMail.put(MailActionExecuter.PARAM_TO, destMail);
        /**
         * Get subject from properties file in repository
         */
        paramsMail.put(MailActionExecuter.PARAM_SUBJECT, i18n.getProperty(SHARE_NOTIFICATION_SUBJECT));
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, getFileTemplateRef(shareNotificationTemplateLocation));

        //TODO i18n templates
        Map<String, Serializable> templateModel = new HashMap<>();
        Map<String, Serializable> templateParams = new HashMap<>();

        SecuredItem s = koyaNodeService.nodeRef2SecuredItem(sharedNodeRef);
        templateParams.put("sharedItemName", s.getName());
        templateParams.put("inviterName", sender.getName());
        templateParams.put("inviterFirstName", sender.getFirstName());
        templateParams.put("inviterEmail", sender.getEmail());

        templateModel.put("args", (Serializable) templateParams);
        paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

        actionService.executeAction(actionService.createAction(
                MailActionExecuter.NAME, paramsMail), null);

    }

    public Properties getI18nSubjectProperties() throws KoyaServiceException {
        Properties i18n = koyaNodeService.readPropertiesFileContent(
                getFileTemplateRef(i18nMailSubjectPropertiesLocation));
        if (i18n == null) {
            throw new KoyaServiceException(KoyaErrorCodes.KOYAMAIL_INVALID_SUBJECT_PROPERTIES_PATH,
                    "Invalid koya Mail subject properties path : "
                    + i18nMailSubjectPropertiesLocation.getPath());
        }
        return i18n;
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
}
