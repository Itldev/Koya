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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.alfresco.util.collections.Function;
import org.apache.log4j.Logger;

import fr.itldev.koya.action.CleanUserPermissionsActionExecuter;
import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.model.permissions.SitePermission;

public class CompanyAclService {

	private Logger logger = Logger.getLogger(this.getClass());

	protected SiteService siteService;
	protected UserService userService;
	protected InvitationService invitationService;
	protected AuthorityService authorityService;
	protected AuthenticationService authenticationService;
	protected ActionService actionService;
	protected SearchService searchService;
	protected PersonService personService;

	protected KoyaNodeService koyaNodeService;
	protected KoyaMailService koyaMailService;

	/*
	 * Invitation Url Params
	 */
	private String koyaClientServerPath;
	private String koyaClientAcceptUrl;
	private String koyaClientRejectUrl;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setKoyaMailService(KoyaMailService koyaMailService) {
		this.koyaMailService = koyaMailService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	//

	public void setKoyaClientServerPath(String koyaClientServerPath) {
		this.koyaClientServerPath = koyaClientServerPath;
	}

	public String getKoyaClientServerPath() {
		return this.koyaClientServerPath;
	}

	public void setKoyaClientAcceptUrl(String koyaClientAcceptUrl) {
		this.koyaClientAcceptUrl = koyaClientAcceptUrl;
	}

	public String getKoyaClientAcceptUrl() {
		return this.koyaClientAcceptUrl;
	}

	public void setKoyaClientRejectUrl(String koyaClientRejectUrl) {
		this.koyaClientRejectUrl = koyaClientRejectUrl;
	}

	public String getKoyaClientRejectUrl() {
		return this.koyaClientRejectUrl;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	// </editor-fold>
	// TODO refine by userTypes : Collaborators Roles, Client Roles
	public List<UserRole> getAvailableRoles(Company c)
			throws KoyaServiceException {
		try {
			List<UserRole> userRoles = new ArrayList<>();
			for (String r : SitePermission.getAllAsString()) {
				userRoles.add(new UserRole(r));
			}
			return userRoles;
		} catch (SiteDoesNotExistException ex) {
			throw new KoyaServiceException(
					KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
		}
	}

	/**
	 * List both validated users and pending invitation users.
	 * 
	 * @param companyName
	 * @param permissionsFilter
	 * @return
	 */
	public List<User> listMembers(String companyName,
			List<SitePermission> permissionsFilter) {
		List<User> members = new ArrayList<>();
		members.addAll(listMembersValidated(companyName, permissionsFilter));
		members.addAll(listMembersPendingInvitation(companyName,
				permissionsFilter));
		return members;
	}

	/**
	 * 
	 * @param parent
	 * @param skipCount
	 * @param maxItems
	 * @param onlyFolders
	 * @return
	 * @throws KoyaServiceException
	 */
	public Pair<List<User>, Pair<Long, Long>> listMembersPaginated(
			String companyName, List<String> roles, final Integer skipCount,
			final Integer maxItems, final boolean withAdmins,
			final String sortField, final Boolean ascending)
			throws KoyaServiceException {

		Integer skip = skipCount;
		Integer max = maxItems;
		if (skipCount == null) {
			skip = Integer.valueOf(0);
		}
		if (maxItems == null) {
			max = Integer.MAX_VALUE;
		}

		SearchParameters sp = new SearchParameters();
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setLimitBy(LimitBy.FINAL_SIZE);
		sp.setSkipCount(skip);
		sp.setMaxItems(max);

		if (sortField != null) {
			sp.addSort(sortField, ascending);
		}

		StringBuffer query = new StringBuffer();
		if (!roles.isEmpty()) {
			String or = "";
			for (String role : roles) {
				query.append(or);
				query.append("PATH:\"/sys:system/sys:authorities/cm:")
						.append(siteService.getSiteRoleGroup(companyName, role))
						.append("/*\"");
				or = "OR ";
			}
		} else {
			query.append("PATH:\"/sys:system/sys:authorities/cm:")
					.append(siteService.getSiteGroup(companyName))
					.append("/*/*\"");
		}
		if (!withAdmins) {
			query.insert(0, "(").append(") AND");
			query.append(" NOT PARENT:\"workspace://SpacesStore/GROUP_ALFRESCO_ADMINISTRATORS\"");
		}
		logger.debug(query.toString());
		sp.setQuery(query.toString());
		ResultSet results = searchService.query(sp);
		// results.

		/**
		 * Transform List<FileInfo> as List<KoyaNode>
		 */
		List<User> users = CollectionUtils.transform(results.getNodeRefs(),
				new Function<NodeRef, User>() {

					@Override
					public User apply(NodeRef nodeRef) {
						return userService.getUserByUsername(personService
								.getPerson(nodeRef).getUserName());
					}
				});

		return new Pair<List<User>, Pair<Long, Long>>(users,
				new Pair<Long, Long>(results.getNumberFound(),
						results.getNumberFound()));
	}

	/**
	 * List members of the company already validated.
	 * 
	 * Automaticly excludes Alfresco administrators.
	 * 
	 * @param companyName
	 * @param permissionsFilter
	 * @return
	 */
	public List<User> listMembersValidated(String companyName,
			List<SitePermission> permissionsFilter) {
		final List<String> permissions = CollectionUtils
				.toListOfStrings(permissionsFilter);
		Map<String, String> members = siteService.listMembers(companyName,
				null, null, 0);

		List<Map.Entry<String, String>> companyMembers = new ArrayList(
				members.entrySet());

		if (permissionsFilter != null && !permissionsFilter.isEmpty()) {
			companyMembers = CollectionUtils.filter(companyMembers,
					new Filter<Map.Entry<String, String>>() {

						@Override
						public Boolean apply(Map.Entry<String, String> member) {
							return permissions.contains(member.getValue())
									&& !authorityService
											.isAdminAuthority(member.getKey());
						}
					});
		}

		List<User> usersOfCompanyValidated = CollectionUtils.transform(
				companyMembers,
				new Function<Map.Entry<String, String>, User>() {

					@Override
					public User apply(Map.Entry<String, String> entry) {
						return userService.getUserByUsername(entry.getKey());
					}
				});

		return usersOfCompanyValidated;
	}

	/**
	 * List Members of the company with pending invitation.
	 * 
	 * @param companyName
	 * @param permissionsFilter
	 * @return
	 */
	public List<User> listMembersPendingInvitation(final String companyName,
			final List<SitePermission> permissionsFilter) {
		List<Invitation> pendinginvitations = getPendingInvite(companyName,
				null, null);
		final List<String> permissions = CollectionUtils
				.toListOfStrings(permissionsFilter);

		if (permissionsFilter != null && !permissionsFilter.isEmpty()) {
			pendinginvitations = CollectionUtils.filter(pendinginvitations,
					new Filter<Invitation>() {

						@Override
						public Boolean apply(Invitation i) {
							return permissions.contains(i.getRoleName());
						}
					});
		}

		List<User> usersOfCompanyPendingInvitation = CollectionUtils.transform(
				pendinginvitations, new Function<Invitation, User>() {

					@Override
					public User apply(Invitation invitation) {
						return userService.getUserByUsername(invitation
								.getInviteeUserName());
					}
				});

		return usersOfCompanyPendingInvitation;
	}

	public List<Invitation> getPendingInvite(String companyId,
			String inviterId, String inviteeId) {
		final InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
		criteria.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);
		criteria.setResourceType(Invitation.ResourceType.WEB_SITE);

		if (inviterId != null) {
			criteria.setInviter(inviterId);
		}
		if (inviteeId != null) {
			criteria.setInvitee(inviteeId);
		}
		if (companyId != null) {
			criteria.setResourceName(companyId);
		}
		
		return AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<List<Invitation>>() {
					@Override
					public List<Invitation> doWork() throws Exception {
						return invitationService.searchInvitation(criteria);
					}
				});
	}

	/**
	 * 
	 * @param username
	 * @return List of companies a user belong to
	 */
	public List<Company> listCompany(String username) {
		List<SiteInfo> l = siteService.listSites(username);

		List<Company> res = CollectionUtils.transform(l,
				new Function<SiteInfo, Company>() {

					@Override
					public Company apply(SiteInfo siteInfo) {
						try {
							return koyaNodeService.getKoyaNode(
									siteInfo.getNodeRef(), Company.class);
						} catch (KoyaServiceException ex) {
						}
						return null;
					}
				});

		return res;
	}

	/**
	 * Checks if current logged user is company manager on specified company.
	 * 
	 * If company throws SiteDoesNotExistException, it may not have already been 
	 * indexed. return current user is admin in this case
	 * 
	 * @param companyName
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public Boolean isCompanyManager(String companyName) throws KoyaServiceException {
		try {
			return SitePermission.MANAGER.equals(siteService.getMembersRole(companyName,
					authenticationService.getCurrentUserName()));
		} catch (SiteDoesNotExistException ex) {
			logger.warn("isCompanyManager : company " + companyName
					+ "doesn't exists or not indexed yet : return isadminauthority");
			return authorityService.isAdminAuthority(authenticationService.getCurrentUserName());
		}
	}

	/**
	 * Returns SitePermission of user on Company if exists.
	 * 
	 * @param c
	 * @param u
	 * @return
	 */
	public SitePermission getSitePermission(Company c, User u) {
		// try with validated members

		try {
			String roleOfSite = siteService.getMembersRole(c.getName(),
					u.getUserName());
			if (roleOfSite != null) {
				return SitePermission.valueOf(roleOfSite);
			}
		} catch (NullPointerException npe) {
			logger.error("npe occurs " + npe.toString());
		}
		// if not found, try with pending invitation users
		Iterator<Invitation> it = getPendingInvite(c.getName(), null,
				u.getUserName()).iterator();
		if (it.hasNext()) {
			return SitePermission.valueOf(it.next().getRoleName());
		}

		// if no results return null;
		return null;
	}

	/**
	 * Invite user to company with defined roleName. sharedItemIs optionnal
	 * 
	 * Returns invitation if processed
	 * 
	 * 
	 * @param c
	 * @param userMail
	 * @param permission
	 * @return
	 * @throws KoyaServiceException
	 */
	public NominatedInvitation inviteMember(final Company c,
			final String userMail, final SitePermission permission,
			final KoyaNode sharedItem) throws KoyaServiceException {

		// TODO unique email unicity validation (if call inviteMember,
		// user should never already exist)
		User uTmp = null;

		try {
			uTmp = userService.getUserByEmail(userMail);
		} catch (KoyaServiceException kse) {
			// Silently catch exception
		}
		
		final User u = uTmp;

		if (u == null || getSitePermission(c, u) == null) {
			/**
			 * Workaround to resolve invite by user bug :
			 * 
			 * 
			 * 
			 * https://forums.alfresco.com/forum/installation-upgrades-
			 * configuration-integration/configuration/site-invite-failures
			 * https://issues.alfresco.com/jira/browse/ALF-20897
			 * http://forums.alfresco
			 * .com/forum/installation-upgrades-configuration
			 * -integration/configuration/problem-invite-external-users
			 * 
			 * 
			 */

			NominatedInvitation invitation = AuthenticationUtil
					.runAsSystem(new AuthenticationUtil.RunAsWork<NominatedInvitation>() {
						@Override
						public NominatedInvitation doWork() throws Exception {
							NominatedInvitation invitation = invitationService
									.inviteNominated(null, userMail, userMail,
											Invitation.ResourceType.WEB_SITE,
											c.getName(), permission.toString(),
											koyaClientServerPath,
											koyaClientAcceptUrl,
											koyaClientRejectUrl);

							koyaMailService.sendInviteMail(invitation
									.getInviteId());
							return invitation;

						}
					});

			return invitation;

		} else {
			throw new KoyaServiceException(
					KoyaErrorCodes.INVITATION_USER_ALREADY_INVITED,
					"User already invited for this company");
		}
	}

	/**
	 * Set user identified by userName specified userRole in companyName
	 * context.
	 * 
	 * User MUST already be a member of the company
	 * 
	 * @param c
	 * @param u
	 * @param role
	 * @throws KoyaServiceException
	 */
	public void setRole(Company c, User u, SitePermission role)
			throws KoyaServiceException {

		// checks if user is already a company member
		SitePermission roleSite = getSitePermission(c, u);

		if (roleSite == null) {
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_USER_MUSTBE_COMPANY_MEMBER_TO_CHANGE_COMPANYROLE);
		}
		final InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
		criteria.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);
		criteria.setResourceType(Invitation.ResourceType.WEB_SITE);
		criteria.setResourceName(c.getName());
		criteria.setInvitee(u.getUserName());

		List<Invitation> invitations = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<List<Invitation>>() {
					@Override
					public List<Invitation> doWork() throws Exception {
						return invitationService.searchInvitation(criteria);
					}
				});

		if (!invitations.isEmpty()) {
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_CANT_MODIFY_USER_PENDING_INVITE_ROLE);
		} else {
			try {
				siteService.setMembership(c.getName(), u.getUserName(),
						role.toString());
			} catch (SiteDoesNotExistException ex) {
				throw new KoyaServiceException(
						KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
			}
		}

	}

	/**
	 * Revoke user access to defined company.
	 * 
	 * @param c
	 * @param u
	 * @throws KoyaServiceException
	 */
	public void removeFromMembers(final Company c, final User u)
			throws KoyaServiceException {
		List<Invitation> invitations = getPendingInvite(c.getName(), null,
				u.getUserName());
		if (invitations.isEmpty()) {
			siteService.removeMembership(c.getName(), u.getUserName());

			// run backend action that cleans all users koya specific
			// permissions
			// on company spaces he can access
			try {
				Map<String, Serializable> paramsClean = new HashMap<>();
				paramsClean.put("userName", u.getUserName());
				Action cleanUserAuth = actionService.createAction(
						CleanUserPermissionsActionExecuter.NAME, paramsClean);
				cleanUserAuth.setExecuteAsynchronously(true);
				actionService.executeAction(cleanUserAuth,
						siteService.getSite(c.getName()).getNodeRef());
			} catch (InvalidNodeRefException ex) {
				logger.error("Error cleaning user " + u.getUserName()
						+ " permissions on spaces while revoking "
						+ c.getName() + "access - " + ex.toString());
			}
		} else {
			for (final Invitation i : invitations) {
				AuthenticationUtil
						.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

							@Override
							public Void doWork() throws Exception {
								try {
									invitationService.cancel(i.getInviteId());
								} catch (Exception e) {
									logger.error("Error removing user "
											+ u.getUserName()
											+ " invitation while revoking "
											+ c.getName() + "access - "
											+ e.toString());
								}
								return null;
							}
						});
			}

		}
	}

}
