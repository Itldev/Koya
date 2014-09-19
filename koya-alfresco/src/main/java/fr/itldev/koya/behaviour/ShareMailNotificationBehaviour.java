package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.policies.SharePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShareMailNotificationBehaviour implements SharePolicies.AfterSharePolicy {
    
    protected static Log logger = LogFactory.getLog(ShareMailNotificationBehaviour.class);
    
    protected PolicyComponent policyComponent;
    
    protected KoyaMailService koyaMailService;
    
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }
    
    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }
    
    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        
    }
    
    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail, Invitation invitation, User inviter) {
        
        if (invitation == null) {
            try {
                koyaMailService.sendShareNotifMail(inviter, userMail, nodeRef);
            } catch (KoyaServiceException ex) {
                logger.fatal("erreur a remonter a l'user : " + ex.toString());
            }
        } else {
            //Nothing to do : invitation already sent 
        }
        
    }
    
}
