package fr.itldev.koya.behaviour;

import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.policies.SharePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShareNotificationBehaviour implements SharePolicies.AfterSharePolicy {

    protected static Log logger = LogFactory.getLog(ShareNotificationBehaviour.class);

    private PolicyComponent policyComponent;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail, Invitation invitation) {
        if (invitation == null) {
            /**
             * TODO send koya specific sharing alert
             */
            logger.error("TODO send subspace sharing alert by email");
        } else {
            //Nothing to do : invitation sent 
        }

    }

}
