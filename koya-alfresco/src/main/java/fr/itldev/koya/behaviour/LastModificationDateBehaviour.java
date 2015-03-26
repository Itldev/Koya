package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Dossier;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.transaction.UserTransaction;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.dao.ConcurrencyFailureException;

/**
 *
 * Updates dossier's last modification date on content modification
 */
public class LastModificationDateBehaviour implements
        NodeServicePolicies.OnDeleteNodePolicy,
        ContentServicePolicies.OnContentUpdatePolicy {

    private Logger logger = Logger
            .getLogger(LastModificationDateBehaviour.class);

    // Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private KoyaNodeService koyaNodeService;
    private TransactionService transactionService;

    // Behaviours
    private Behaviour onDeleteNode;
    private Behaviour onContentUpdate;

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public PolicyComponent getPolicyComponent() {
        return policyComponent;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public KoyaNodeService getKoyaNodeService() {
        return koyaNodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void init() {

        // Create behaviours
        this.onDeleteNode = new JavaBehaviour(this, "onDeleteNode",
                NotificationFrequency.TRANSACTION_COMMIT);
        this.onContentUpdate = new JavaBehaviour(this, "onContentUpdate",
                NotificationFrequency.TRANSACTION_COMMIT);
        // this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
        // NotificationFrequency.TRANSACTION_COMMIT);
        // Bind behaviours to node policies
        // Delete behaviour
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT, this.onDeleteNode);

        // Update or create behaviour
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.TYPE_CONTENT, this.onContentUpdate);
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef,
            boolean isNodeArchived) {
        if (nodeService.exists(childAssocRef.getChildRef())) {
            addOrUpdateLastModifiedDate(childAssocRef.getChildRef());
        }
    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
        if (nodeService.exists(nodeRef)) {
            addOrUpdateLastModifiedDate(nodeRef);
        }
    }

    private void addOrUpdateLastModifiedDate(NodeRef nodeRef) {
        if (!nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
            // We want to update the lastModified Aspect only for content, not
            // for thumbnail or whatever
            return;
        }

        logger.trace("node "
                + nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)
                + "/"
                + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                + " of type " + nodeService.getType(nodeRef).getLocalName()
                + " Modified");
        for (Map.Entry<QName, Serializable> e : nodeService.getProperties(
                nodeRef).entrySet()) {
            logger.trace(e.getKey().getLocalName() + " : "
                    + e.getValue().toString());
        }
        // get dossier
        try {

            final Dossier d = koyaNodeService
                    .getFirstParentOfType(nodeRef, Dossier.class);
            if (d != null) {

                logger.trace("Updating lastModificationDate of dossier : "
                        + d.getTitle());
                final NodeRef n = d.getNodeRefasObject();
                AuthenticationUtil
                        .runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
                            @Override
                            public Object doWork() throws Exception {
                                //Quick and Dirty hack to avoid multiple files uploads faillure
                                UserTransaction transaction = transactionService.getNonPropagatingUserTransaction();
                                try {
                                    transaction.begin();

                                    // Add lastModified Aspect if not already
                                    // present
                                    if (!nodeService.hasAspect(n,
                                            KoyaModel.ASPECT_LASTMODIFIED)) {
                                        Map<QName, Serializable> props = new HashMap<>();
                                        nodeService.addAspect(n,
                                                KoyaModel.ASPECT_LASTMODIFIED,
                                                props);
                                    }

                                    nodeService.setProperty(n,
                                            KoyaModel.PROP_LASTMODIFICATIONDATE,
                                            new Date());
                                    nodeService.setProperty(n,
                                            KoyaModel.PROP_NOTIFIED, Boolean.FALSE);
                                    transaction.commit();
                                } catch (Exception cfe) {
                                   // logger.warn("ConcurrencyFailureException on dossier " + d.getTitle());
                                    transaction.rollback();
                                }
                                return null;
                            }
                        });
            }
        } catch (KoyaServiceException ex) {
            logger.error("error while determinating nodeRef Koya Typed parents", ex);
        }

    }

}
