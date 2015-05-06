package fr.itldev.koya.behaviour.security;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.model.permissions.SitePermission;

/**
 * This Behaviour deletes specific permission on parents spaces if necessary
 * 
 * 
 * 
 */
public class UpdateParentNodesBeforeRevokeKoyaPermission  {

	private final Logger logger = Logger.getLogger(this.getClass());
	private Behaviour beforeRevokeKoyaPermission;
	private PolicyComponent policyComponent;
	private SpaceAclService spaceAclService;
	private KoyaNodeService koyaNodeService;
	private PermissionService permissionService;
	private NodeService nodeService;
	private SiteService siteService;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
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

	// </editor-fold>
	public void init() {
		// Create behaviours
//		this.beforeRevokeKoyaPermission = new JavaBehaviour(this,
//				"beforeRevokeKoyaPermission",
//				Behaviour.NotificationFrequency.FIRST_EVENT);
//		this.policyComponent.bindClassBehaviour(
//				KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy.QNAME,
//				KoyaModel.TYPE_DOSSIER, this.beforeRevokeKoyaPermission);
//		this.policyComponent.bindClassBehaviour(
//				KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy.QNAME,
//				KoyaModel.TYPE_SPACE, this.beforeRevokeKoyaPermission);

	}

	/**
	 * Before revoking permissions on a space for a CONSUMER role user
	 * member, it check if it's primary parent still need a KoyaClient
	 * 
	 * 
	 * At the end, it removes user membership from SiteConsumer group
	 * 
	 * @param space
	 * @param authority
	 * @param permission
	 */
	public void beforeRevokeKoyaPermission(Space space, String authority,
			KoyaPermission permission) {

		logger.debug("Chained permissions deletion user=" + authority
				+ ": ref =  " + space.getName() + "("
				+ space.getClass().getSimpleName() + ")");

		try {
			Company c = koyaNodeService.getFirstParentOfType(
					space.getNodeRef(), Company.class);
			if (!SitePermission.CONSUMER.equals(siteService.getMembersRole(
					c.getName(), authority))) {
				return;
			}

			final NodeRef parentNode = nodeService.getPrimaryParent(
					space.getNodeRef()).getParentRef();
			boolean userCanReadParentNode = AuthenticationUtil.runAs(
					new AuthenticationUtil.RunAsWork<Boolean>() {
						@Override
						public Boolean doWork() throws Exception {
							return permissionService.hasPermission(parentNode,
									PermissionService.READ).equals(
									AccessStatus.ALLOWED);
						}
					}, authority);

			logger.error("parent is space  ? = "
					+ Space.class.isAssignableFrom(koyaNodeService.getKoyaNode(
							parentNode).getClass()));
			logger.error("can read parent ? = " + userCanReadParentNode);

			if (Space.class.isAssignableFrom(koyaNodeService.getKoyaNode(
					parentNode).getClass())
					&& userCanReadParentNode) {

				final Space parent = koyaNodeService.getKoyaNode(parentNode,
						Space.class);

				// loop through current node parent childs
				for (final ChildAssociationRef car : nodeService
						.getChildAssocs(parent.getNodeRef())) {

					boolean userCanReadChild = AuthenticationUtil.runAs(
							new AuthenticationUtil.RunAsWork<Boolean>() {
								@Override
								public Boolean doWork() throws Exception {
									return permissionService.hasPermission(
											car.getChildRef(),
											KoyaPermissionConsumer.CLIENT
													.toString()).equals(
											AccessStatus.ALLOWED);
								}
							}, authority);

					if (userCanReadChild) {
						logger.error("one readable child .... return ");
						// At least one child is readable -> do nothing
						return;
					}
				}

				logger.error("revoke parent perm (" + parent.getName() + ")");
				// no readable child -> revoke CONSUMER_CLIENT permissions on
				// parent
				spaceAclService.revokeSpacePermission(parent, authority,
						KoyaPermissionConsumer.CLIENT);
			} else {
				logger.error("Remove company '" + c.getName()
						+ "' access for user '" + authority + "' ?");
				// siteService.removeMembership(c.getName(), authority);
			}

		} catch (KoyaServiceException ex) {
			logger.error("Error update befor revoke permissions"
					+ ex.toString());
		}
	}

}
