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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.KoyaActivityPoster;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.ModelService;
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
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.model.permissions.SitePermission;
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
	protected CompanyAclService companyAclService;
	protected TransactionService transactionService;
	protected FileFolderService fileFolderService;
	protected KoyaActivityPoster koyaActivityPoster;
	private ModelService modelService;
	private OwnableService ownableService;

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

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setKoyaActivityPoster(KoyaActivityPoster koyaActivityPoster) {
		this.koyaActivityPoster = koyaActivityPoster;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	// </editor-fold>
	/**
	 * Registers the share policies
	 */
	public void init() {
		// Register the various policies

		beforeShareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.BeforeSharePolicy.class);
		afterShareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.AfterSharePolicy.class);
		beforeUnshareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.BeforeUnsharePolicy.class);
		afterUnshareDelegate = policyComponent
				.registerClassPolicy(SharePolicies.AfterUnsharePolicy.class);

	}

	/*
	 * ========= Client Share ===============
	 */

	/**
	 * Share Space with client defined by his email adress
	 * 
	 * if user with such an email address, creates and invite him. Then grant
	 * Space permission KoyaPermissionConsumer.CLIENT by adding to corresponding
	 * group
	 * 
	 * @param space
	 * @param userMail
	 * @return
	 * @throws KoyaServiceException
	 */
	public NominatedInvitation clientShare(final Space space,
			final String userMail) throws KoyaServiceException {

		User inviter = userService.getUserByUsername(authenticationService
				.getCurrentUserName());

		logger.info("[Share] : {'user':'" + inviter.getEmail()
				+ "','invitee':'" + userMail + "','koyaNode':'"
				+ space.toString() + "','permission':'"
				+ KoyaPermissionConsumer.CLIENT.toString() + "}");

		beforeShareDelegate.get(nodeService.getType(space.getNodeRef()))
				.beforeShareItem(space.getNodeRef(), userMail, inviter);

		// Get company the shared Node belongs To
		Company company = koyaNodeService.getFirstParentOfType(
				space.getNodeRef(), Company.class);

		SitePermission userPermissionInCompany = null;
		User u = null;

		try {
			u = userService.getUserByEmail(userMail);
		} catch (KoyaServiceException kse) {
			// silently catch exception
		}

		if (u != null) {
			userPermissionInCompany = companyAclService.getSitePermission(
					company, u);
		}

		NominatedInvitation invitation = null;
		// If user can't access specified company then invite him even if he
		// already exists in alfresco
		if (userPermissionInCompany == null) {
			logger.info("[Invite] : {'invitee':'" + userMail + "','company':'"
					+ company + "','permission':'" + SitePermission.CONSUMER
					+ "}");

			invitation = companyAclService.inviteMember(company, userMail,
					SitePermission.CONSUMER, space);

			u = userService.getUserByUsername(invitation.getInviteeUserName());
			userPermissionInCompany = companyAclService.getSitePermission(
					company, u);
		}

		if (!userPermissionInCompany.equals(SitePermission.CONSUMER)) {
			logger.error("Consumer Share not available for "
					+ userPermissionInCompany.toString() + " users");
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_USER_MUSTBE_CONSUMER_TO_APPLY_PERMISSION);
		}

		/**
		 * User should not already have any permission on node
		 * 
		 * User should not belong to any group which name contains Current Space
		 * nodeRef id (cf group naming policy)
		 */

		Set<String> authorities = authorityService.getAuthoritiesForUser(u
				.getUserName());

		for (String a : authorities) {
			if (a.contains(space.getNodeRef().getId())) {
				throw new KoyaServiceException(
						KoyaErrorCodes.SECU_USER_ALREADY_HAVE_PERMISSION_ON_SPACE);
			}
		}

		addKoyaAuthority(space, KoyaPermissionConsumer.CLIENT, u);

		afterShareDelegate.get(nodeService.getType(space.getNodeRef()))
				.afterShareItem(space.getNodeRef(), u, inviter);

		/**
		 * Post a Share activity using ShareSpaceActivityPoster
		 */
		koyaActivityPoster.postSpaceShared(u, inviter.getUserName(), space);

		return invitation;

	}

	/**
	 * Remove KoyaPermissionConsumer.CLIENT for user defined by email address
	 * 
	 * 
	 * @param space
	 * @param userMail
	 */
	public void clientUnshare(final Space space, final String userMail) {
		final User u = userService.getUserByEmail(userMail);

		User revoker = userService.getUserByUsername(authenticationService
				.getCurrentUserName());

		logger.info("[Unshare] : {'user':'" + revoker.getEmail()
				+ "','unshared':'" + u.getEmail() + "','koyaNode':'"
				+ space.toString() + "','permission':'"
				+ KoyaPermissionConsumer.CLIENT.toString() + "}");

		beforeUnshareDelegate.get(nodeService.getType(space.getNodeRef()))
				.beforeUnshareItem(space.getNodeRef(), userMail, revoker);

		// Gets the user involved in unsharing - throws execption if not found
		removeKoyaAuthority(space, KoyaPermissionConsumer.CLIENT, u);

		afterUnshareDelegate.get(nodeService.getType(space.getNodeRef()))
				.afterUnshareItem(space.getNodeRef(), u, revoker);

		/**
		 * Post an Unshare activity using UnshareSpaceActivityPoster
		 */
		koyaActivityPoster.postSpaceUnshared(userMail, revoker.getUserName(),
				space);
	}

	/*
	 * * ========= Collaborator Share ===============
	 */

	public void collaboratorShare(Space space, User user,
			KoyaPermissionCollaborator perm) throws KoyaServiceException {

		/**
		 * TODO generate specific activity if necessary
		 * 
		 * TODO use this method from web scripts if activity is generated.
		 * Currently only used from imports
		 */

		// Get company the shared Node belongs To
		Company company = koyaNodeService.getFirstParentOfType(
				space.getNodeRef(), Company.class);
		SitePermission userPermissionInCompany = companyAclService
				.getSitePermission(company, user);

		// user should exist for company as a site Collaborator or site manager
		// member
		if (userPermissionInCompany.equals(SitePermission.COLLABORATOR)
				|| userPermissionInCompany.equals(SitePermission.MANAGER)) {
			addKoyaAuthority(space, perm, user);
		} else {
			logger.error("Collaborator Share not available for "
					+ userPermissionInCompany.toString() + " users");
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_USER_MUSTBE_COLLABORATOR_OR_ADMIN_TO_APPLY_PERMISSION);
		}
	}

	/*
	 * ========= Basics permissions and group Grant/revoke Methods=======
	 */

	public void addKoyaAuthority(Space space, KoyaPermission permission,
			User user) {

		String groupName = "GROUP_"
				+ space.getAuthorityName(permission.toString());

		// first check if authority not already exists in group
		if (authorityService.getContainedAuthorities(AuthorityType.USER,
				groupName, true).contains(user.getUserName())) {
			return;
		}

		logger.info("[Grant] : {'user':'" + user.getEmail() + "','authority':'"
				+ permission.toString() + "','koyaNode':'" + space.toString()
				+ "','permission':'" + permission.toString() + "}");
		authorityService.addAuthority(groupName, user.getUserName());
	}

	public void removeKoyaAuthority(Space space, KoyaPermission permission,
			User user) {

		logger.info("[Revoke] : {'user':'" + user.getUserName()
				+ "','koyaNode':'" + space.toString() + "','permission':'"
				+ permission.toString() + "}");

		authorityService.removeAuthority(
				"GROUP_" + space.getAuthorityName(permission.toString()),
				user.getUserName());
	}

	/**
	 * Iterates on all KoyaPermissions groups setted on space. Try to remove
	 * user from group.
	 * 
	 * Do not test, better silently fail if any error
	 * 
	 * @param s
	 * @param u
	 */
	public void removeAnyKoyaAuthority(Space s, User u) {
		for (AccessPermission ap : permissionService.getAllSetPermissions(s
				.getNodeRef())) {
			try {
				String grpShortName = ap.getAuthority().substring(
						ap.getAuthority().lastIndexOf("_") + 1);
				KoyaPermission kPerm = KoyaPermission.valueOf(grpShortName);
				removeKoyaAuthority(s, kPerm, u);
			} catch (Exception e) {
			}
		}
	}

	public Boolean hasMembers(Space s, KoyaPermission permission)
			throws KoyaServiceException {

		try {
			return !authorityService.getContainedAuthorities(
					AuthorityType.USER,
					"GROUP_" + s.getAuthorityName(permission.toString()), true)
					.isEmpty();
		} catch (Exception e) {
			logger.error("Trying to check if " + s.getName()
					+ " has Members of type " + permission.toString() + " : "
					+ e.toString());
			return false;
		}
	}

	/**
	 * Get all spaces a user can access with given Koya Permission. Limited to
	 * company scope
	 * 
	 * @param u
	 * @return
	 */
	public List<Space> getKoyaUserSpaces(User u, KoyaPermission permission,
			Company c) {
		List<Space> spaces = new ArrayList<>();

		Set<String> authorities = authorityService.getAuthoritiesForUser(u
				.getUserName());

		for (String a : authorities) {

			if (a.endsWith(permission.toString())) {
				Space s = getKoyaNodeFromGroupName(a);
				if (s != null
						&& koyaNodeService.getFirstParentOfType(s.getNodeRef(),
								Company.class).equals(c)) {
					spaces.add(s);
				}
			}
		}
		return spaces;
	}

	/**
	 * Get all spaces a user can access with any permission. Limited to company
	 * scope
	 * 
	 * @param u
	 * @return
	 */
	public List<Space> getKoyaUserSpaces(User u, Company c) {
		List<Space> spaces = new ArrayList<>();

		Set<String> authorities = authorityService.getAuthoritiesForUser(u
				.getUserName());

		for (String a : authorities) {

			Space s = getKoyaNodeFromGroupName(a);
			if (s != null
					&& koyaNodeService.getFirstParentOfType(s.getNodeRef(),
							Company.class).equals(c)) {
				spaces.add(s);
			}
		}
		return spaces;
	}

	/**
	 * Build KoyaNode that matches group name. Node Id is contained in koya
	 * Groups string
	 * 
	 * Pattern is GROUP_<TYPE>_<NodeId>_<KoyaPermission>
	 * 
	 * @param groupName
	 * @return
	 */
	private Space getKoyaNodeFromGroupName(String groupName) {
		Pattern p = Pattern.compile("GROUP_.*_(.*)_.*");
		try {
			Matcher m = p.matcher(groupName);
			if (m.find()) {
				NodeRef n = new NodeRef("workspace://SpacesStore/" + m.group(1));
				return koyaNodeService.getKoyaNode(n, Space.class);
			}
		} catch (Exception e) {
		}
		return null;

	}

	/**
	 * List users who have rolename role on space
	 * 
	 * @param space
	 * @param roleName
	 * @return
	 */
	public List<User> listMembership(Space space, KoyaPermission permission) {
		List<User> users = new ArrayList<User>();

		// TODO user security
		for (String username : authorityService.getContainedAuthorities(
				AuthorityType.USER,
				"GROUP_" + space.getAuthorityName(permission.toString()), true)) {
			users.add(userService.getUserByUsername(username));
		}

		return users;
	}

	/**
	 * List user authorities involved in permissions on defined node
	 * 
	 * This method is used for defining activity notification receivers
	 * 
	 * @param n
	 * @param permissions
	 * @return
	 */
	public Set<String> listUsersAuthorities(final NodeRef n,
			final List<KoyaPermission> permissions) {

		return AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Set<String>>() {
					@Override
					public Set<String> doWork() throws Exception {

						// first select candidates authorities
						// ie Groups which name ends with one of permissions in
						// parameter
						// Then add all contained user authorities

						// cf permissions Groups implementation in
						// SpaceAclService

						Set<String> usersId = new HashSet<>();

						for (AccessPermission ap : permissionService
								.getAllSetPermissions(n)) {
							for (KoyaPermission p : permissions) {
								if (ap.getAuthority().endsWith(p.toString())) {

									usersId.addAll(authorityService
											.getContainedAuthorities(
													AuthorityType.USER,
													ap.getAuthority(), true));
									break;
								}
							}
						}
						return usersId;
					}
				});
	}

	/*
	 * ==================================================================
	 * =================== Init and delete node =========================
	 * ==================================================================
	 */

	/*
	 * TODO config loading
	 */

	private static Map<String, String> GROUP_SITE_PERMISSIONS_SPACE = new HashMap<String, String>() {
		{
			put(SitePermission.MANAGER.toString(),
					SitePermission.MANAGER.toString());
			put(SitePermission.CONTRIBUTOR.toString(),
					SitePermission.CONTRIBUTOR.toString());
			put(SitePermission.COLLABORATOR.toString(),
					SitePermission.CONTRIBUTOR.toString());
		}
	};

	private static Map<String, String> GROUP_SITE_PERMISSIONS_DOSSIER = new HashMap<String, String>() {
		{
			put(SitePermission.MANAGER.toString(),
					SitePermission.MANAGER.toString());
			put(SitePermission.CONTRIBUTOR.toString(),
					SitePermission.CONTRIBUTOR.toString());
			put(SitePermission.COLLABORATOR.toString(),
					SitePermission.CONSUMER.toString());
		}
	};

	private static Map<String, String> GROUP_KOYA_PERMISSIONS_SPACE = new HashMap<String, String>() {
		{
			put("KoyaResponsible", "KoyaResponsible");
			put("KoyaMember", "KoyaMember");
			put("KoyaClient", "KoyaClient");
			put("KoyaPartner", "KoyaPartner");
			put("KoyaSpaceReader", "KoyaClient");
		}
	};

	private static Map<String, String> GROUP_KOYA_PERMISSIONS_DOSSIER = new HashMap<String, String>() {
		{
			put("KoyaResponsible", "KoyaResponsible");
			put("KoyaMember", "KoyaMember");
			put("KoyaClient", "KoyaClient");
			put("KoyaPartner", "KoyaPartner");
		}
	};
	private static String[] KOYARESPONSIBLES_BLACKLIST = { "admin", "" };

	public void initSpaceAcl(final Space space) {

		// getKoyaNodes Hierachy
		List<KoyaNode> parents = koyaNodeService.getParentsList(
				space.getNodeRef(), KoyaNodeService.NB_ANCESTOR_INFINTE);
		Company c = null;
		Space firstParentSpace = null;

		try {
			if (parents.get(0).getClass().isAssignableFrom(Company.class)) {
				c = (Company) parents.get(0);
			} else {
				firstParentSpace = (Space) parents.get(0);
				c = (Company) parents.get(parents.size() - 1);
			}

		} catch (Exception ex) {
			logger.error("Error in node hierachy " + ex.toString());
		}

		// Clear the node inherited permissions
		permissionService
				.setInheritParentPermissions(space.getNodeRef(), false);

		Map<String, String> groupSitePermissions = null;
		Map<String, String> groupKoyaPermissions = null;
		if (Space.class.equals(space.getClass())) {
			groupSitePermissions = GROUP_SITE_PERMISSIONS_SPACE;
			groupKoyaPermissions = GROUP_KOYA_PERMISSIONS_SPACE;
		} else if (Dossier.class.equals(space.getClass())) {
			groupSitePermissions = GROUP_SITE_PERMISSIONS_DOSSIER;
			groupKoyaPermissions = GROUP_KOYA_PERMISSIONS_DOSSIER;
		} else {
			logger.error("Error Applying default permissions on node creation - unhandled node type : "
					+ space.getKtype());
			return;
		}

		/*
		 * Setting default site permissions on node
		 */
		for (String permissionGroupName : groupSitePermissions.keySet()) {
			permissionService.setPermission(space.getNodeRef(), siteService
					.getSiteRoleGroup(c.getName(), permissionGroupName),
					groupSitePermissions.get(permissionGroupName), true);
		}

		String nodeHierachyPath = buildHierachyPath(parents);

		String tmpFirstParentSpaceAuthorityName = null;
		if (firstParentSpace != null) {
			tmpFirstParentSpaceAuthorityName = firstParentSpace
					.getAuthorityName("KoyaSpaceReader");
		}

		final String firstParentSpaceAuthorityName = tmpFirstParentSpaceAuthorityName;

		/*
		 * 
		 * 
		 * 
		 * Create master authority group for this node
		 */
		final String masterGroupAuthorityName = space.getAuthorityName(null);
		final String masterGroupDispAuthorityName = buildGroupDispName(space,
				"", nodeHierachyPath);

		// authority creation executed as System user
		AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
					@Override
					public Void doWork() throws Exception {

						authorityService.createAuthority(AuthorityType.GROUP,
								masterGroupAuthorityName,
								masterGroupDispAuthorityName, null);

						// Add this authority to parent KoyaSpaceReader
						// authority to allow
						// listing permission
						if (firstParentSpaceAuthorityName != null) {
							authorityService.addAuthority("GROUP_"
									+ firstParentSpaceAuthorityName, "GROUP_"
									+ masterGroupAuthorityName);
						}
						return null;
					}
				});

		/*
		 * Create Groups and Setting default koya permissions on node
		 */

		for (String permissionGroupName : groupKoyaPermissions.keySet()) {

			final String authorityName = space
					.getAuthorityName(permissionGroupName);
			final String dispAuthorityName = buildGroupDispName(space,
					permissionGroupName, nodeHierachyPath);

			// authority creation executed as System user
			AuthenticationUtil
					.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
						@Override
						public Void doWork() throws Exception {

							authorityService.createAuthority(
									AuthorityType.GROUP, authorityName,
									dispAuthorityName, null);

							// Add to master authority group for this node
							authorityService.addAuthority("GROUP_"
									+ masterGroupAuthorityName, "GROUP_"
									+ authorityName);

							return null;
						}
					});

			// set permission on node
			permissionService.setPermission(space.getNodeRef(), "GROUP_"
					+ authorityName,
					groupKoyaPermissions.get(permissionGroupName), true);
		}

		/**
		 * On node creation, creator is set a responsible if he doesn't belongs
		 * responsibles blacklist
		 * 
		 */
		final String spaceResponsiblesGroupName = "GROUP_"
				+ space.getAuthorityName("KoyaResponsible");
		final String creator = (String) nodeService.getProperty(
				space.getNodeRef(), ContentModel.PROP_CREATOR);

		if (creator != null
				&& !Arrays.asList(KOYARESPONSIBLES_BLACKLIST).contains(creator)
				&& !creator.equals(modelService.getCompanyImporterUsername(c
						.getName()))) {

			AuthenticationUtil
					.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
						@Override
						public Void doWork() throws Exception {
							authorityService.addAuthority(
									spaceResponsiblesGroupName, creator);
							return null;
						}
					});

		}

		/*
		 * Remove node owner. If no owner, node permission can be disable for
		 * creator.
		 */
		AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
					@Override
					public Void doWork() throws Exception {
						ownableService.setOwner(space.getNodeRef(),
								OwnableService.NO_OWNER);
						return null;
					}
				});

	}

	/**
	 * 
	 * @param dossier
	 * @param koyaClientDir
	 */
	public void initSingleDossierKoyaClientDirAcl(Dossier dossier,
			NodeRef koyaClientDir) {
		permissionService
				.setPermission(
						koyaClientDir,
						"GROUP_"
								+ dossier
										.getAuthorityName(KoyaPermissionCollaborator.MEMBER
												.toString()),
						KoyaPermissionCollaborator.MEMBER.toString(), true);

		permissionService
				.setPermission(
						koyaClientDir,
						"GROUP_"
								+ dossier
										.getAuthorityName(KoyaPermissionCollaborator.RESPONSIBLE
												.toString()),
						KoyaPermissionCollaborator.RESPONSIBLE.toString(), true);

		permissionService.setPermission(koyaClientDir,
				"GROUP_" + dossier.getAuthorityName("KoyaClient"),
				"SiteContributor", true);

	}

	/**
	 * 
	 * @param dossier
	 * @param koyaClientDir
	 */
	public void initCompanyKoyaClientDirAcl(Company company,
			NodeRef companyKoyaClientDir) {

		// Clear the node inherited permissions
		permissionService.setInheritParentPermissions(companyKoyaClientDir,
				false);
		permissionService.setPermission(companyKoyaClientDir, siteService
				.getSiteRoleGroup(company.getName(),
						SitePermission.MANAGER.toString()),
				SitePermission.MANAGER.toString(), true);
		permissionService.setPermission(companyKoyaClientDir, siteService
				.getSiteRoleGroup(company.getName(),
						SitePermission.CONTRIBUTOR.toString()),
				SitePermission.CONTRIBUTOR.toString(), true);
		permissionService.setPermission(companyKoyaClientDir, siteService
				.getSiteRoleGroup(company.getName(),
						SitePermission.COLLABORATOR.toString()),
				SitePermission.CONTRIBUTOR.toString(), true);

		permissionService.setPermission(companyKoyaClientDir, siteService
				.getSiteRoleGroup(company.getName(),
						SitePermission.CONSUMER.toString()),
				SitePermission.CONSUMER.toString(), true);
	}

	/**
	 * Remove all koya Specific authorities Group setted on space
	 * 
	 * TODO chained deletion if deleted item is a Space.class Object and have
	 * child dossier (impossible now)
	 * 
	 * @param space
	 */
	public void removeAllKoyaGroups(Space space) {
		for (AccessPermission ap : permissionService.getAllSetPermissions(space
				.getNodeRef())) {
			try {
				String grpShortName = ap.getAuthority().substring(
						ap.getAuthority().lastIndexOf("_") + 1);
				KoyaPermission kPerm = KoyaPermission.valueOf(grpShortName);
				if (kPerm != null) {
					authorityService.deleteAuthority(ap.getAuthority());
				}

			} catch (Exception e) {
			}
		}

		// delete master node group authority
		final String masterGroupAuthorityName = "GROUP_"
				+ space.getAuthorityName(null);
		authorityService.deleteAuthority(masterGroupAuthorityName);
	}

	/*
	 * 
	 * ================= Private Group Building helpers ==================
	 */

	private String buildGroupDispName(Space s, String roleName,
			String hierachyPath) {
		String dispName = s.getKtype() + " " + s.getName() + " " + roleName;

		if (!hierachyPath.isEmpty()) {
			dispName += "(" + hierachyPath + ")";
		}

		return dispName;
	}

	private String buildHierachyPath(List<KoyaNode> parents) {
		String hierachy = "";
		String sep = "";
		for (KoyaNode n : parents) {
			hierachy += sep + n.getName();
			sep = "/";
		}
		return hierachy;
	}

	/*
	 * 
	 * ================= Permissions Getting Methods ==================
	 */

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

	/*
	 * 
	 * ================= Confidential Space methods ==================
	 */

	/**
	 * Set or unset confidential flag on Space (ie Space or dossier)
	 * 
	 * puts or removes koyaConfidential aspect on node and change collaborators
	 * permissions.
	 * 
	 * On confidential elements, collaborators don't have any default access.
	 * 
	 * @param u
	 * @param i
	 * @throws KoyaServiceException
	 */
	public Boolean toggleConfidential(Space space, Boolean confidential)
			throws KoyaServiceException {

		User u = userService.getUserByUsername(authenticationService
				.getCurrentUserName());

		Company c = koyaNodeService.getFirstParentOfType(space.getNodeRef(),
				Company.class);

		// check user permissions
		// only manager or element responsibles can toggle confidential aspect
		// on item
		List<User> itemResponsibles = listMembership(space,
				KoyaPermissionCollaborator.RESPONSIBLE);

		if (!(itemResponsibles.contains(u)
				|| companyAclService.isCompanyManager(c.getName()) || authorityService
					.isAdminAuthority(u.getUserName()))) {
			/**
			 * User must either reponsable of element OR company Manager OR
			 * Alfresco Administrator
			 */
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_CONFIDENTIAL_USER_CANT_TOGGLE_FLAG);
		}

		// modify permissions
		if (confidential) {
			nodeService.addAspect(space.getNodeRef(),
					KoyaModel.ASPECT_CONFIDENTIAL, null);

			permissionService.clearPermission(space.getNodeRef(), siteService
					.getSiteRoleGroup(c.getName(),
							SitePermission.COLLABORATOR.toString()));

		} else {
			nodeService.removeAspect(space.getNodeRef(),
					KoyaModel.ASPECT_CONFIDENTIAL);
			permissionService.setPermission(space.getNodeRef(), siteService
					.getSiteRoleGroup(c.getName(),
							SitePermission.COLLABORATOR.toString()),
					SitePermission.CONSUMER.toString(), true);
		}
		return isConfidential(space);
	}

	public Boolean isConfidential(KoyaNode i) throws KoyaServiceException {
		return nodeService.hasAspect(i.getNodeRef(),
				KoyaModel.ASPECT_CONFIDENTIAL);
	}

}
