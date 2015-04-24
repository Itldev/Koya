package fr.itldev.koya.behaviour.security;

import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.policies.KoyaPermissionsPolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

/**
 * This Behaviour deletes specific permission on parents subspaces if necessary
 *
 *
 *
 */
public class UpdateParentNodesBeforeRevokeKoyaPermission implements
        KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy {

    private final Logger logger = Logger.getLogger(this.getClass());
    private Behaviour beforeRevokeKoyaPermission;
    private PolicyComponent policyComponent;
    private SubSpaceAclService subSpaceAclService;
    private KoyaNodeService koyaNodeService;
    private PermissionService permissionService;
    private NodeService nodeService;
    private SiteService siteService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    //</editor-fold>
    public void init() {
        // Create behaviours
        this.beforeRevokeKoyaPermission = new JavaBehaviour(this, "beforeRevokeKoyaPermission",
                Behaviour.NotificationFrequency.FIRST_EVENT);
        this.policyComponent.bindClassBehaviour(
                KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                this.beforeRevokeKoyaPermission);
        this.policyComponent.bindClassBehaviour(
                KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy.QNAME, KoyaModel.TYPE_SPACE,
                this.beforeRevokeKoyaPermission);

    }

    /**
     * Before revoking permissions on a subspace for a CONSUMER role user
     * member, it check if it's primary parent still need a KoyaClient
     *
     *
     * At the end, it removes user membership from SiteConsumer group
     *
     * @param subSpace
     * @param authority
     * @param permission
     */
    @Override
    public void beforeRevokeKoyaPermission(SubSpace subSpace, String authority, KoyaPermission permission) {

        logger.debug("Chained permissions deletion user=" + authority + ": ref =  " + subSpace.getName() + "(" + subSpace.getClass().getSimpleName() + ")");

        try {
            Company c = koyaNodeService.getFirstParentOfType(subSpace.getNodeRef(),Company.class);
            if (!SitePermission.CONSUMER.equals(siteService.getMembersRole(c.getName(), authority))) {
                return;
            }

            final NodeRef parentNode = nodeService.getPrimaryParent(subSpace.getNodeRef()).getParentRef();
            boolean userCanReadParentNode = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork< Boolean>() {
                @Override
                public Boolean doWork() throws Exception {
                    return permissionService.hasPermission(
                            parentNode, PermissionService.READ).equals(AccessStatus.ALLOWED);
                }
            }, authority);

            logger.error("parent is subspace  ? = " + SubSpace.class.isAssignableFrom(koyaNodeService.getSecuredItem(parentNode).getClass()));
            logger.error("can read parent ? = " + userCanReadParentNode);

            if (SubSpace.class.isAssignableFrom(koyaNodeService.getSecuredItem(parentNode).getClass())
                    && userCanReadParentNode) {

                final SubSpace parent = (SubSpace) koyaNodeService.getSecuredItem(parentNode);

                //loop through current node parent childs
                for (final ChildAssociationRef car : nodeService.getChildAssocs(parent.getNodeRef())) {

                    boolean userCanReadChild = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork< Boolean>() {
                        @Override
                        public Boolean doWork() throws Exception {
                            return permissionService.hasPermission(
                                    car.getChildRef(), KoyaPermissionConsumer.CLIENT.toString())
                                    .equals(AccessStatus.ALLOWED);
                        }
                    }, authority);

                    if (userCanReadChild) {
                        logger.error("one readable child .... return ");
                        //At least one child is readable -> do nothing
                        return;
                    }
                }

                logger.error("revoke parent perm (" + parent.getName() + ")");
                //no readable child -> revoke CONSUMER_CLIENT permissions on parent
                subSpaceAclService.revokeSubSpacePermission(parent, authority, KoyaPermissionConsumer.CLIENT);
            } else {
                logger.error("Remove company '" + c.getName() + "' access for user '" + authority + "' ?");
//                siteService.removeMembership(c.getName(), authority);
            }

        } catch (KoyaServiceException ex) {
            logger.error("Error update befor revoke permissions" + ex.toString());
        }
    }

}
