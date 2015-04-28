/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.alfservice.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.policies.KoyaPermissionsPolicies;
import fr.itldev.koya.policies.SharePolicies;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * Spaces Permissions Service
 * 
 */
public class SpaceAclService {

	private final Logger logger = Logger.getLogger(this.getClass());

	protected PermissionService permissionService;
	protected KoyaNodeService koyaNodeService;
	protected NodeService nodeService;
	protected AuthenticationService authenticationService;
	protected SearchService searchService;
	protected AuthorityService authorityService;
	protected SiteService siteService;
	protected InvitationService invitationService;
	protected UserService userService;
	protected PolicyComponent policyComponent;
	protected CompanyService companyService;
	protected SpaceService spaceService;
	protected DossierService dossierService;
	protected CompanyAclService companyAclService;
	protected TransactionService transactionService;

	/*
	 * Policy delegates
	 */
	// Grant Permission on node delegate
	private ClassPolicyDelegate<KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy> afterGrantKoyaPermissionDelegate;
	private ClassPolicyDelegate<KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy> beforeRevokeKoyaPermissionDelegate;
	// sharing delegates
	private ClassPolicyDelegate<SharePolicies.BeforeSharePolicy> beforeShareDelegate;
	private ClassPolicyDelegate<SharePolicies.AfterSharePolicy> afterShareDelegate;
	private ClassPolicyDelegate<SharePolicies.BeforeUnsharePolicy> beforeUnshareDelegate;
	private ClassPolicyDelegate<SharePolicies.AfterUnsharePolicy> afterUnshareDelegate;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setDossierService(DossierService dossierService) {
		this.dossierService = dossierService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	// </editor-fold>
	/**
	 * Registers the share policies
	 */
	public void init() {
		// Register the various policies
		afterGrantKoyaPermissionDelegate = policyComponent
				.registerClassPolicy(KoyaPermissionsPolicies.AfterGrantKoyaPermissionPolicy.class);
		beforeRevokeKoyaPermissionDelegate = policyComponent
				.registerClassPolicy(KoyaPermissionsPolicies.BeforeRevokeKoyaPermissionPolicy.class);
		beforeShareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.BeforeSharePolicy.class);
		afterShareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.AfterSharePolicy.class);
		beforeUnshareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.BeforeUnsharePolicy.class);
		afterUnshareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.AfterUnsharePolicy.class);

	}

	/**
	 * Set default spaces permissions.
	 * 
	 * @param spaceItem
	 * @throws KoyaServiceException
	 */
	public void initSpaceWithDefaultPermissions(Space space)
			throws KoyaServiceException {

		Company c = koyaNodeService.getFirstParentOfType(
				space.getNodeRef(), Company.class);
		// Clear the node inherited permissions
		permissionService.setInheritParentPermissions(
				space.getNodeRef(), false);
		/*
		 * Setting default permissions on node
		 */
		// Sitemanager keeps manager permissions
		permissionService.setPermission(space.getNodeRef(), siteService
				.getSiteRoleGroup(c.getName(),
						SitePermission.MANAGER.toString()),
				SitePermission.MANAGER.toString(), true);

		// SiteContributor keeps contributor permissions
		permissionService.setPermission(space.getNodeRef(), siteService
				.getSiteRoleGroup(c.getName(),
						SitePermission.CONTRIBUTOR.toString()),
				SitePermission.CONTRIBUTOR.toString(), true);

		/*
		 * SiteCollaborator gets contributor permission on Space in order to
		 * create Sub Spaces or Dossiers. SiteCollaborator gets consumer
		 * permission on Dossier in order to read content.
		 */
		if (Space.class.isAssignableFrom(space.getClass())) {
			permissionService.setPermission(space.getNodeRef(),
					siteService.getSiteRoleGroup(c.getName(),
							SitePermission.COLLABORATOR.toString()),
					SitePermission.CONTRIBUTOR.toString(), true);
		} else {
			permissionService.setPermission(space.getNodeRef(),
					siteService.getSiteRoleGroup(c.getName(),
							SitePermission.COLLABORATOR.toString()),
					SitePermission.CONSUMER.toString(), true);
		}

		// Consumer has no default permission
	}

	/**
	 * 
	 * @param space
	 * @param authority
	 * @param permission
	 */
	public void grantSpacePermission(final Space space,
			final String authority, KoyaPermission permission) {
		logger.debug("Grant permission '" + permission.toString() + "' to '"
				+ authority + "' on '" + space.getTitle() + "' ("
				+ space.getClass().getSimpleName() + ")");

		permissionService.setPermission(space.getNodeRef(), authority,
				permission.toString(), true);

		afterGrantKoyaPermissionDelegate.get(
				nodeService.getType(space.getNodeRef()))
				.afterGrantKoyaPermission(space, authority, permission);

	}

	/**
	 * Revoke specified permissions on node.
	 * 
	 * Revoke permission on node and, if parent other childs doesn't have the
	 * same permission, do it for the parents
	 * 
	 * @param space
	 * @param authority
	 * @param permission
	 * 
	 */
	public void revokeSpacePermission(Space space, String authority,
			KoyaPermission permission) {
		logger.debug("Revoke permission '" + permission.toString() + "' to '"
				+ authority + "' on '" + space.getTitle() + "' ("
				+ space.getClass().getSimpleName() + ")");
		beforeRevokeKoyaPermissionDelegate.get(
				nodeService.getType(space.getNodeRef()))
				.beforeRevokeKoyaPermission(space, authority, permission);

		permissionService.deletePermission(space.getNodeRef(), authority,
				permission.toString());// >>> NPE dans les TU ???

		// TODO after hook that check if user needs to stay company member
	}

	/**
	 * 
	 * /**
	 * 
	 * @param space
	 * @param userMail
	 * @param perm
	 * @param sharedByImporter
	 * @throws KoyaServiceException
	 */
	public void shareKoyaNode(final Space space,
			final String userMail, final KoyaPermission perm,
			final Boolean sharedByImporter) throws KoyaServiceException {

		User inviter = userService.getUserByUsername(authenticationService
				.getCurrentUserName());

		beforeShareDelegate.get(nodeService.getType(space.getNodeRef()))
				.beforeShareItem(space.getNodeRef(), userMail, inviter,
						sharedByImporter);

		shareKoyaNodeImpl(space, userMail, perm);

		afterShareDelegate.get(nodeService.getType(space.getNodeRef()))
				.afterShareItem(space.getNodeRef(), userMail, inviter,
						sharedByImporter);

		logger.info("[Koya] public sharing : user " + inviter.getEmail()
				+ "has shared " + space.toString() + " with user "
				+ userMail);

	}

	protected void shareKoyaNodeImpl(Space space, String userMail,
			KoyaPermission perm) throws KoyaServiceException {
		throw new KoyaServiceException(0);// TODO errror code - shoulnever be
											// called
	}

	public void unShareKoyaNode(Space space, String userMail,
			KoyaPermission perm) throws KoyaServiceException {

		User revoker = userService.getUserByUsername(authenticationService
				.getCurrentUserName());
		beforeUnshareDelegate.get(nodeService.getType(space.getNodeRef()))
				.beforeUnshareItem(space.getNodeRef(), userMail, revoker);
		logger.debug("Unshare " + space.getName() + " for " + userMail
				+ " permission = " + perm.toString());

		// Gets the user involved in unsharing - throws execption if not found
		User u = userService.getUser(userMail);

		if (Dossier.class.isAssignableFrom(space.getClass())) {
			revokeSpacePermission(space, u.getUserName(), perm);
		} else {
			logger.error("Unsupported unsharing type "
					+ space.getClass().getSimpleName());
		}
		afterUnshareDelegate.get(nodeService.getType(space.getNodeRef()))
				.afterUnshareItem(space.getNodeRef(), userMail, revoker);

	}

	/**
	 * Removes all given space koya specific permissions
	 * 
	 * @param space
	 */
	public void cleanAllKoyaSpacePermissions(Space space) {
		logger.debug("Clean All permissions on '" + space.getTitle() + "' ("
				+ space.getClass().getSimpleName() + ")");

		for (KoyaPermission p : KoyaPermission.getAll()) {
			for (AccessPermission ap : permissionService
					.getAllSetPermissions(space.getNodeRef())) {
				if (p.equals(ap.getPermission())) {
					revokeSpacePermission(space, ap.getAuthority(), p);
				}
			}
		}
	}

	/**
	 * 
	 * List Users who have a specific KoyaPermission defined in filter on the
	 * node.
	 * 
	 * If no filter or empty, list all KoyaPermissions
	 * 
	 * @param s
	 * @param permissions
	 * @return
	 */
	public List<User> listUsers(KoyaNode s, List<KoyaPermission> permissions) {
		List<User> users = new ArrayList<>();

		if (permissions == null || permissions.isEmpty()) {
			permissions = KoyaPermission.getAll();
		}

		for (AccessPermission ap : permissionService.getAllSetPermissions(s
				.getNodeRef())) {

			try {
				if (permissions.contains(KoyaPermission.valueOf(ap
						.getPermission()))) {
					User u = userService.getUserByUsername(ap.getAuthority());
					if (u != null) {
						users.add(u);
					}
				}
			} catch (IllegalArgumentException iex) {

			}
		}
		return users;
	}

	/**
	 * 
	 * List all secured Items a user has specific KoyaPermission setted in a
	 * company context.
	 * 
	 * 
	 * @param c
	 * @param u
	 * @param permissions
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public List<KoyaNode> listKoyaNodes(Company c, User u,
			List<KoyaPermission> permissions) throws KoyaServiceException {
		if (permissions == null || permissions.isEmpty()) {
			permissions = KoyaPermission.getAll();
		}
		return listItemSharedRecursive(
				spaceService.list(c.getName(), Integer.MAX_VALUE), u,
				permissions);
	}

	private List<KoyaNode> listItemSharedRecursive(List<Space> spaces, User u,
			List<KoyaPermission> permissions) throws KoyaServiceException {
		List<KoyaNode> items = new ArrayList<>();

		for (Space s : spaces) {
			items.addAll(listItemSharedRecursive(s.getChildSpaces(), u,
					permissions));
			// check if current space is shared with user as site consumer
			for (AccessPermission ap : permissionService.getAllSetPermissions(s
					.getNodeRef())) {
				if (ap.getAuthority().equals(u.getUserName())
						&& permissions.contains(ap.getPermission())) {
					items.add(s);
				}
			}

			// check if current space children (ie dossiers) are shared with
			// user as site consumer
			for (Dossier d : dossierService.list(s.getNodeRef())) {
				for (AccessPermission ap : permissionService
						.getAllSetPermissions(d.getNodeRef())) {
					if (ap.getAuthority().equals(u.getUserName())
							&& permissions.contains(ap.getPermission())) {
						items.add(d);
					}
				}
			}
		}
		return items;
	}

	/**
	 * Builds Koya permissions on given NodeRef for authenticated user.
	 * 
	 * @param n
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public Permissions getPermissions(NodeRef n) throws KoyaServiceException {
		return getPermissions(
				userService.getUserByUsername(authenticationService
						.getCurrentUserName()), n);
	}

	/**
	 * Builds Koya permissions on given NodeRef for specified user.
	 * 
	 * @param u
	 * @param n
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public Permissions getPermissions(User u, NodeRef n)
			throws KoyaServiceException {

		Permissions p = new Permissions(u.getUserName(), n);
		/**
		 * TODO get permissions method with user and node parameters search in
		 * alfresco API
		 * 
		 */
		List<String> userPermissions = new ArrayList<>();
		for (AccessPermission ap : permissionService.getAllSetPermissions(n)) {
			try {
				if (ap.getAuthority().equals(u.getUserName())) {
					userPermissions.add(ap.getPermission());
				}
			} catch (IllegalArgumentException iex) {
			}
		}

		//
		p.canReadProperties(permissionService.hasPermission(n,
				PermissionService.READ_PROPERTIES).equals(AccessStatus.ALLOWED));
		p.canWriteProperties(permissionService.hasPermission(n,
				PermissionService.WRITE_PROPERTIES)
				.equals(AccessStatus.ALLOWED));
		//
		p.canCreateChildren(permissionService.hasPermission(n,
				PermissionService.CREATE_CHILDREN).equals(AccessStatus.ALLOWED));
		p.canDeleteChildren(permissionService.hasPermission(n,
				PermissionService.DELETE_CHILDREN).equals(AccessStatus.ALLOWED));
		p.canReadChildren(permissionService.hasPermission(n,
				PermissionService.READ_CHILDREN).equals(AccessStatus.ALLOWED));
		p.canLinkChildren(permissionService.hasPermission(n,
				PermissionService.LINK_CHILDREN).equals(AccessStatus.ALLOWED));
		//
		p.canReadContent(permissionService.hasPermission(n,
				PermissionService.READ_CONTENT).equals(AccessStatus.ALLOWED));
		p.canWriteContent(permissionService.hasPermission(n,
				PermissionService.WRITE_CONTENT).equals(AccessStatus.ALLOWED));
		p.canExecuteContent(permissionService.hasPermission(n,
				PermissionService.EXECUTE_CONTENT).equals(AccessStatus.ALLOWED));
		//
		p.canDeleteNode(permissionService.hasPermission(n,
				PermissionService.DELETE_NODE).equals(AccessStatus.ALLOWED));
		//
		p.canDeleteAssociations(permissionService.hasPermission(n,
				PermissionService.DELETE_ASSOCIATIONS).equals(
				AccessStatus.ALLOWED));
		p.canReadAssociations(permissionService.hasPermission(n,
				PermissionService.READ_ASSOCIATIONS).equals(
				AccessStatus.ALLOWED));
		p.canCreateAssociations(permissionService.hasPermission(n,
				PermissionService.CREATE_ASSOCIATIONS).equals(
				AccessStatus.ALLOWED));
		//
		p.canReadPermissions(permissionService.hasPermission(n,
				PermissionService.READ_PERMISSIONS)
				.equals(AccessStatus.ALLOWED));
		p.canChangePermissions(permissionService.hasPermission(n,
				PermissionService.CHANGE_PERMISSIONS).equals(
				AccessStatus.ALLOWED));
		/*
		 * ======= Koya specific permissions ========
		 */
		p.canShareWithCustomer(p.getCanChangePermissions()
				|| userPermissions.contains(KoyaPermissionCollaborator.MEMBER
						.toString()));

		return p;

	}

	/**
	 * 
	 * Returns user's KoyaNodes filtered by type that have one of
	 * KoyaPermission filter item set.
	 * 
	 * This method return application wide nodes. no matter the company.
	 * 
	 * @param u
	 * @param typesFilter
	 * @param koyaPermisssionFilter
	 * @return
	 */
	@Deprecated
	public List<KoyaNode> getUserskoyaNodeWithKoyaPermissions(User u,
			List<QName> typesFilter, List<KoyaPermission> koyaPermisssionFilter) {

		List<KoyaNode> koyaNodesWithKoyaPermissions = new ArrayList<>();

		/**
		 * If a KoyaNode is readable, it returned by user lucene search. If
		 * not, it's hidden.
		 */
		String luceneRequest = "";

		if (typesFilter == null || typesFilter.isEmpty()) {
			typesFilter = new ArrayList<>();
			typesFilter.add(KoyaModel.TYPE_COMPANY);
			typesFilter.add(KoyaModel.TYPE_SPACE);
			typesFilter.add(KoyaModel.TYPE_DOSSIER);
		}

		List<String> koyaPermisssionFilterString = new ArrayList<>();
		if (koyaPermisssionFilter == null || koyaPermisssionFilter.isEmpty()) {
			koyaPermisssionFilterString = KoyaPermission.getAllAsString();
		} else {
			for (KoyaPermission k : koyaPermisssionFilter) {
				koyaPermisssionFilterString.add(k.toString());
			}
		}

		// build lucene request with filter types
		String orSep = "";
		for (QName t : typesFilter) {
			luceneRequest += orSep + "TYPE:\""
					+ KoyaModel.TYPES_SHORT_PREFIX.get(t) + "\"";
			orSep = " OR ";
		}

		ResultSet rs = null;
		try {
			rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_LUCENE, luceneRequest);
			for (ResultSetRow r : rs) {
				try {

					for (AccessPermission ap : permissionService
							.getAllSetPermissions(r.getNodeRef())) {
						if (koyaPermisssionFilterString.contains(ap
								.getPermission())
								&& ap.getAuthority().equals(u.getUserName())) {
							koyaNodesWithKoyaPermissions.add(koyaNodeService
									.getKoyaNode(r.getNodeRef()));
							break;
						}
					}
				} catch (KoyaServiceException ex) {

				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return koyaNodesWithKoyaPermissions;
	}

	/**
	 * Set or unset confidential flag on Space (ie Space or dossier)
	 * 
	 * puts or removes koyaConfidential aspect on node and change collaborators
	 * permissions.
	 * 
	 * On confidential elements, colloborators don't have any default acces.
	 * 
	 * @param u
	 * @param i
	 * @throws KoyaServiceException
	 */
	public Boolean toggleConfidential(User u, KoyaNode i, Boolean confidential)
			throws KoyaServiceException {

		// check input elements (must be Space or Dossier) > later resolved by
		// inherit Dossier from Space

		if (!(Space.class.isAssignableFrom(i.getClass()) || Dossier.class
				.isAssignableFrom(i.getClass()))) {
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_CONFIDENTIAL_FLAG_TYPE_ERROR);
		}

		Company c = koyaNodeService.getFirstParentOfType(i.getNodeRef(),
				Company.class);

		// check user permissions
		// only manager or element responsibles can toggle confidential aspect
		// on item

		List<User> itemResponsibles = listUsers(i,
				Collections.unmodifiableList(new ArrayList<KoyaPermission>() {
					{
						add(KoyaPermissionCollaborator.RESPONSIBLE);
					}
				}));

		if (!itemResponsibles.contains(u)
				|| !companyAclService.isCompanyManager(c.getName())
				|| !authorityService.isAdminAuthority(u.getUserName())) {
			/**
			 * User must either reponsable of element OR company Manager OR
			 * Alfresco Adminitrator
			 */
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_CONFIDENTIAL_USER_CANT_TOGGLE_FLAG);
		}

		// modify permissions
		if (confidential) {
			nodeService.addAspect(i.getNodeRef(),
					KoyaModel.ASPECT_CONFIDENTIAL, null);

			permissionService.clearPermission(i.getNodeRef(), siteService
					.getSiteRoleGroup(c.getName(),
							SitePermission.COLLABORATOR.toString()));

		} else {
			nodeService.removeAspect(i.getNodeRef(),
					KoyaModel.ASPECT_CONFIDENTIAL);
			permissionService.setPermission(i.getNodeRef(), siteService
					.getSiteRoleGroup(c.getName(),
							SitePermission.COLLABORATOR.toString()),
					SitePermission.CONSUMER.toString(), true);
		}
		return isConfidential(i);
	}

	public Boolean isConfidential(KoyaNode i) throws KoyaServiceException {
		return nodeService.hasAspect(i.getNodeRef(),
				KoyaModel.ASPECT_CONFIDENTIAL);
	}

}
