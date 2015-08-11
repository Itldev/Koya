package fr.itldev.koya.behaviour;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;


import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Dossier;
import java.io.Serializable;
import org.alfresco.repo.cache.SimpleCache;


/**
 *
 * Updates dossier's last modification date on content modification
 */
public class LastModificationDateBehaviour implements
        NodeServicePolicies.OnDeleteNodePolicy,
        NodeServicePolicies.OnAddAspectPolicy,
        ContentServicePolicies.OnContentUpdatePolicy {

    private Logger logger = Logger
            .getLogger(LastModificationDateBehaviour.class);

    // Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private KoyaNodeService koyaNodeService;
    private DossierService dossierService;
    /**
     * Cache for nodes and dossier witch have been updated<br/>
     * KEY: The Node's NodeRef<br/>
     * VALUE: IGNORED<br/>
     */
    private SimpleCache<NodeRef, Serializable> lastModifiedSharedCache;
    // Behaviours
    private Behaviour onDeleteNode;
    private Behaviour onContentUpdate;
    private Behaviour onAddAspect;

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

    public DossierService getDossierService() {
        return dossierService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public void setLastModifiedSharedCache(
            SimpleCache<NodeRef, Serializable> lastModifiedSharedCache) {
        this.lastModifiedSharedCache = lastModifiedSharedCache;
    }

    public void init() {

        // Create behaviours
        this.onDeleteNode = new JavaBehaviour(this, "onDeleteNode",
                NotificationFrequency.TRANSACTION_COMMIT);
        this.onContentUpdate = new JavaBehaviour(this, "onContentUpdate",
                NotificationFrequency.TRANSACTION_COMMIT);
        this.onAddAspect = new JavaBehaviour(this, "onAddAspect",
                NotificationFrequency.TRANSACTION_COMMIT);
        // Bind behaviours to node policies
        // Delete behaviour
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE, this.onDeleteNode);

        // Update or create behaviour
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE, this.onContentUpdate);

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE, this.onAddAspect);
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef,
            boolean isNodeArchived) {
        if (!lastModifiedSharedCache.contains(childAssocRef.getChildRef())
                && !lastModifiedSharedCache.contains(childAssocRef.getParentRef())) {

            if (existCondition(childAssocRef.getChildRef())
                    && (typeCondition(childAssocRef.getChildRef(),
                            ContentModel.TYPE_CONTENT) || typeCondition(
                            childAssocRef.getChildRef(), ContentModel.TYPE_FOLDER))) {
                // failover find first parent of type dossier
                try {
                    Dossier d = koyaNodeService.getFirstParentOfType(
                            childAssocRef.getChildRef(), Dossier.class);
                    if (!lastModifiedSharedCache.contains(d.getNodeRef())) {

                        lastModifiedSharedCache.put(childAssocRef.getChildRef(), "");
                        lastModifiedSharedCache.put(childAssocRef.getParentRef(), "");
                        lastModifiedSharedCache.put(d.getNodeRef(), "");
                        dossierService.updateLastModificationDate(d);
                    }
                } catch (Exception e) {
                    // silently return
                }
            }
        }
    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
        if (!lastModifiedSharedCache.contains(nodeRef)) {
            if (existCondition(nodeRef)
                    && (typeCondition(nodeRef, KoyaModel.TYPE_DOSSIER)
                    || typeCondition(nodeRef, ContentModel.TYPE_CONTENT) || typeCondition(
                            nodeRef, ContentModel.TYPE_FOLDER))) {
                // failover find first parent of type dossier
                try {
                    Dossier d = koyaNodeService.getFirstParentOfType(nodeRef,
                            Dossier.class);
                    if (!lastModifiedSharedCache.contains(d.getNodeRef())) {
                        lastModifiedSharedCache.put(nodeRef, "");
                        lastModifiedSharedCache.put(d.getNodeRef(), "");
                        dossierService.updateLastModificationDate(d);
                    }
                } catch (Exception e) {
                    // silently return
                }
            }
        }
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
        if (!lastModifiedSharedCache.contains(nodeRef)) {

            if (typeCondition(nodeRef, KoyaModel.TYPE_DOSSIER)) {

                try {
                    Dossier d = koyaNodeService.getKoyaNode(nodeRef, Dossier.class);
                    if (!lastModifiedSharedCache.contains(d.getNodeRef())) {
                        lastModifiedSharedCache.put(nodeRef, "");
                        lastModifiedSharedCache.put(d.getNodeRef(), "");
                        dossierService.updateLastModificationDate(d);
                    }
                } catch (Exception e) {
                    // silently return
                }
            }
        }
    }

    //Add onCreateNode policy ? folder add ?
    private Boolean existCondition(NodeRef n) {
        return nodeService.exists(n);
    }

    private Boolean typeCondition(NodeRef n, QName type) {
        return nodeService.getType(n).equals(type);
    }

}
