package fr.itldev.koya.behaviour.security;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.policies.KoyaPermissionsPolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.log4j.Logger;

/**
 * This Behaviour updates parents subspaces nodes permissions after grant
 * permission on specific node.
 *
 */
public class UpdateParentNodesAfterGrantKoyaPermission implements
        KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy {

    private final Logger logger = Logger.getLogger(this.getClass());
    private Behaviour afterGrantKoyaPermission;
    private PolicyComponent policyComponent;
    private SubSpaceAclService subSpaceAclService;
    private KoyaNodeService koyaNodeService;
    private PermissionService permissionService;

    private NodeService nodeService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void init() {

        // Create behaviours
        this.afterGrantKoyaPermission = new JavaBehaviour(this, "afterGrantKoyaPermission", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                this.afterGrantKoyaPermission);
        this.policyComponent.bindClassBehaviour(KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy.QNAME, KoyaModel.TYPE_SPACE,
                this.afterGrantKoyaPermission);

    }

    /**
     *
     * @param subSpace
     * @param authority
     * @param permission
     */
    @Override
    public void afterGrantKoyaPermission(SubSpace subSpace, final String authority, KoyaPermission permission) {

        NodeRef pNode = nodeService.getPrimaryParent(subSpace.getNodeRef()).getParentRef();
        SecuredItem sNode;
        try {
            sNode = koyaNodeService.getSecuredItem(pNode);
        } catch (KoyaServiceException kex) {
            //silently catch any koyaException on creating SecuredItem : do nothing
            return;
        }
        //if parent is a subspace 
        if (SubSpace.class.isAssignableFrom(sNode.getClass()) /*and user has client role*/) {

            final SubSpace parent = (SubSpace) sNode;

            //Test if autority to set has read permission on parent.
            boolean userCanReadParent = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork< Boolean>() {
                @Override
                public Boolean doWork() throws Exception {
                    return permissionService.hasPermission(
                            parent.getNodeRef(), KoyaPermissionConsumer.CLIENT.toString())
                            .equals(AccessStatus.ALLOWED);
                }
            }, authority);

            if (!userCanReadParent) {
                //chained permissions modifications is done by system users

                AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork() {
                    @Override
                    public Object doWork() throws Exception {
                        subSpaceAclService.grantSubSpacePermission(parent, authority, KoyaPermissionConsumer.CLIENT);
                        return null;
                    }
                });

            }

        }

    }

}
