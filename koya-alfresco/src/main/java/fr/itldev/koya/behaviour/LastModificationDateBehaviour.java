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
                ContentModel.ASPECT_AUDITABLE,
                this.onDeleteNode);

        // Update or create behaviour
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE,
                this.onContentUpdate);

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ContentModel.ASPECT_AUDITABLE,
                this.onAddAspect);

    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef,
            boolean isNodeArchived) {

        if (nodeService.exists(childAssocRef.getChildRef())) {
            dossierService.addOrUpdateLastModifiedDate(childAssocRef.getChildRef());
        }
    }

    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

        if (nodeService.exists(nodeRef)) {
            dossierService.addOrUpdateLastModifiedDate(nodeRef);
        }
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {

        if (nodeService.getType(nodeRef).equals(KoyaModel.TYPE_DOSSIER)) {
            dossierService.addOrUpdateLastModifiedDate(nodeRef);
        }
    }

}
