package fr.itldev.koya.behaviour.security;

import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.interfaces.SubSpace;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

/**
 * This Behaviour deletes koya specific permissions before node deleting so that
 * parents permissions wil be updated.
 *
 */
public class RevokeKoyaPermissionsBeforeDelete implements
        NodeServicePolicies.BeforeDeleteNodePolicy {

    private final Logger logger = Logger.getLogger(this.getClass());
    private Behaviour beforeDeleteNode;
    private PolicyComponent policyComponent;
    private SubSpaceAclService subSpaceAclService;
    private KoyaNodeService koyaNodeService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void init() {
        this.beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.FIRST_EVENT);
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                this.beforeDeleteNode);

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, KoyaModel.TYPE_SPACE,
                this.beforeDeleteNode);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        try {
            SubSpace s = (SubSpace) koyaNodeService.nodeRef2SecuredItem(nodeRef);
            subSpaceAclService.cleanAllKoyaSubSpacePermissions(s);
        } catch (KoyaServiceException ex) {
            logger.error("before delete node error : " + ex.toString());
        }
    }

}
