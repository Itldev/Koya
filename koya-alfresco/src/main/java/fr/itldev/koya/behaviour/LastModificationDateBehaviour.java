/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Dossier;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import org.apache.log4j.Logger;

/**
 *
 * @author nico
 */
public class LastModificationDateBehaviour implements NodeServicePolicies.OnDeleteNodePolicy, ContentServicePolicies.OnContentUpdatePolicy {

    private Logger logger = Logger.getLogger(LastModificationDateBehaviour.class);

// Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private KoyaNodeService koyaNodeService;
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

    public void init() {

        // Create behaviours
        this.onDeleteNode = new JavaBehaviour(this, "onDeleteNode", NotificationFrequency.TRANSACTION_COMMIT);
        this.onContentUpdate = new JavaBehaviour(this, "onContentUpdate", NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        //Delete behaviour
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                this.onDeleteNode
        );

        // Update or create behaviour
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                this.onContentUpdate
        );
    }

    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
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
            // We want to update the lastModified Aspect only for content, not for thumbnail or whatever
            return;
        }

        Dossier d = null;

        logger.debug("node " + nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE) + "/" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME) + "of type " + nodeService.getType(nodeRef).getLocalName() + " Modified");
        for (Map.Entry<QName, Serializable> e : nodeService.getProperties(nodeRef).entrySet()) {
            logger.debug(e.getKey().getLocalName() + " : " + e.getValue().toString());
        }
        //get dossier
        try {

            d = koyaNodeService.getFirstParentOfType(nodeRef, Dossier.class);
        } catch (KoyaServiceException ex) {
            logger.error("error while determinating nodeRef Koya Typed parents");
        }
        if (d != null) {

            logger.debug("Updating lastModificationDate of dossier : " + d.getTitle());
            final NodeRef n = d.getNodeRefasObject();
            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< Object>() {
                @Override
                public Object doWork() throws Exception {

                    //Add lastModified Aspect if not already present
                    if (!nodeService.hasAspect(n, KoyaModel.ASPECT_LASTMODIFIED)) {
                        Map<QName, Serializable> props = new HashMap<>();
                        nodeService.addAspect(n, KoyaModel.ASPECT_LASTMODIFIED, props);
                    }

                    nodeService.setProperty(n, KoyaModel.PROP_LASTMODIFICATIONDATE, new Date());
                    return null;
                }
            });
        }
    }

}
