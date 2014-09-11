package fr.itldev.koya.behaviour.security;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.alfservice.security.SubSpaceCollaboratorsAclService;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.interfaces.SubSpace;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class SetOwnerAsResponsibleOnCreate implements NodeServicePolicies.OnCreateNodePolicy {

    private final Logger logger = Logger.getLogger(this.getClass());
    private Behaviour onCreateNode;
    private PolicyComponent policyComponent;
    private KoyaNodeService koyaNodeService;
    private NodeService nodeService;

    private SubSpaceCollaboratorsAclService SubSpaceCollaboratorsAclService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSubSpaceCollaboratorsAclService(SubSpaceCollaboratorsAclService SubSpaceCollaboratorsAclService) {
        this.SubSpaceCollaboratorsAclService = SubSpaceCollaboratorsAclService;
    }

    public void init() {
        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                this.onCreateNode);

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, KoyaModel.TYPE_SPACE,
                this.onCreateNode);

    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        try {
            String creator = (String) nodeService.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_CREATOR);

            if (creator != null) {
                SubSpaceCollaboratorsAclService.grantSubSpacePermission(
                        (SubSpace) koyaNodeService.nodeRef2SecuredItem(childAssocRef.getChildRef()),
                        creator, KoyaPermissionCollaborator.RESPONSIBLE);
            } else {
                //TODO set default responsible
            }
        } catch (KoyaServiceException ex) {
            logger.error(ex.toString());
        }
    }
}
