package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.policies.SharePolicies;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShareMailNotificationBehaviour implements SharePolicies.AfterSharePolicy {

    private final static String SHARE_NOTIFICATION_SUBJECT = "koya.share-notification.subject";

    protected static Log logger = LogFactory.getLog(ShareMailNotificationBehaviour.class);

    protected PolicyComponent policyComponent;
    protected String templatePath;
    protected String i18nPropertiesPath;
    protected ActionService actionService;
    protected ContentService contentService;
    protected KoyaNodeService koyaNodeService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setI18nPropertiesPath(String i18nPropertiesPath) {
        this.i18nPropertiesPath = i18nPropertiesPath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

    }

    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail, Invitation invitation, User inviter) {

        if (invitation == null) {

            NodeRef templateNodeRef = koyaNodeService.getNodeRefLucenePath(templatePath);
            if (templateNodeRef == null) {
                logger.error("Invalid Share Mail Notification template path : " + templatePath);
                return;
            }
            Properties i18n = koyaNodeService.readPropertiesFileContent(
                    koyaNodeService.getNodeRefLucenePath(i18nPropertiesPath));
            if (i18n == null) {
                logger.error("Invalid koya Mail properties path : " + i18nPropertiesPath);
                return;
            }

            //TODO check if user has any previous pending invitation (on other company for ex)
            /**
             * Params Setting
             */
            Map<String, Serializable> paramsMail = new HashMap<>();

            paramsMail.put(MailActionExecuter.PARAM_TO, userMail);

            /**
             * Get subject from properties file in repository
             */
            paramsMail.put(MailActionExecuter.PARAM_SUBJECT, i18n.getProperty(SHARE_NOTIFICATION_SUBJECT));

            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, templateNodeRef);

            //TODO i18n templates
            Map<String, Serializable> templateModel = new HashMap<>();
            Map<String, Serializable> templateParams = new HashMap<>();

            SecuredItem s = null;
            try {
                s = koyaNodeService.nodeRef2SecuredItem(nodeRef);
            } catch (KoyaServiceException ex) {
            }

            templateParams.put("sharedItemName", s.getName());
            templateParams.put("inviterName", inviter.getName());
            templateParams.put("inviterFirstName", inviter.getFirstName());
            templateParams.put("inviterEmail", inviter.getEmail());

            templateModel.put("args", (Serializable) templateParams);
            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);

            /**
             * Action execution
             */
            actionService.executeAction(actionService.createAction(
                    MailActionExecuter.NAME, paramsMail), null);

        } else {
            //Nothing to do : invitation already sent 
        }

    }

}
