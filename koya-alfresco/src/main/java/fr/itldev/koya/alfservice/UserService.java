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
package fr.itldev.koya.alfservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.PropertyMap;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserConnection;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 *
 */
public class UserService {

	private final Logger logger = Logger.getLogger(this.getClass());

	protected NodeService nodeService;
	protected PersonService personService;
	protected SearchService searchService;
	protected MutableAuthenticationService authenticationService;
	protected SiteService siteService;
	protected InvitationService invitationService;
	protected KoyaNodeService koyaNodeService;

	// <editor-fold defaultstate="collapsed" desc="getters/setters">
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setAuthenticationService(
			MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}
	

	// </editor-fold>
	/**
	 * User creation method.
	 * 
	 * @param userToCreate
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public void createUser(User userToCreate) throws KoyaServiceException {

		PropertyMap propsUser = new PropertyMap();
		propsUser.put(ContentModel.PROP_USERNAME, userToCreate.getUserName());
		propsUser.put(ContentModel.PROP_FIRSTNAME, userToCreate.getFirstName());
		propsUser.put(ContentModel.PROP_LASTNAME, userToCreate.getName());
		propsUser.put(ContentModel.PROP_EMAIL, userToCreate.getEmail());
		if (!personService.personExists(userToCreate.getUserName())) {
			authenticationService.createAuthentication(userToCreate
					.getUserName(), userToCreate.getPassword().toCharArray());
			personService.createPerson(propsUser);

			NodeRef userNr = personService
					.getPerson(userToCreate.getUserName());
			nodeService.addAspect(userNr, KoyaModel.ASPECT_CIVILTITLED, null);
			nodeService.setProperty(userNr, KoyaModel.PROP_CIVILTITLE,
					userToCreate.getCivilTitle());

		} else {
			throw new KoyaServiceException(KoyaErrorCodes.LOGIN_ALREADY_EXISTS);
		}
	}

	/**
	 * Modify user fields according to user object.
	 * 
	 * @param userToModify
	 * @throws KoyaServiceException
	 */
	public void modifyUser(User userToModify) throws KoyaServiceException {

		// TODO check who request user modification : user can only modify his
		// own information
		// admin can modify everyone informations
		if (personService.personExists(userToModify.getUserName())) {
			NodeRef userNr = personService
					.getPerson(userToModify.getUserName());

			// update 4 fields : firstname,lastname,email,emailFeedDisabled
			nodeService.setProperty(userNr, ContentModel.PROP_FIRSTNAME,
					userToModify.getFirstName());
			nodeService.setProperty(userNr, ContentModel.PROP_LASTNAME,
					userToModify.getName());
			nodeService.setProperty(userNr, ContentModel.PROP_EMAIL,
					userToModify.getEmail());
			nodeService.setProperty(userNr,
					ContentModel.PROP_EMAIL_FEED_DISABLED,
					userToModify.getEmailFeedDisabled());

			if (!nodeService.hasAspect(userNr, KoyaModel.ASPECT_CIVILTITLED)) {
				nodeService
						.addAspect(userNr, KoyaModel.ASPECT_CIVILTITLED, null);
			}

			nodeService.setProperty(userNr, KoyaModel.PROP_CIVILTITLE,
					userToModify.getCivilTitle());

			// TODO change password if necessary + uncrypted password
			// nodeService.setProperty(userNr, ContentModel.PROP_PASSWORD,
			// userToModify.getPassword());
		} else {
			throw new KoyaServiceException(KoyaErrorCodes.UNKNOWN_USER);
		}

	}

	/**
	 * Change users password
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public void changePassword(String oldPassword, String newPassword)
			throws KoyaServiceException {
		try {
			authenticationService.updateAuthentication(
					authenticationService.getCurrentUserName(),
					oldPassword.toCharArray(), newPassword.toCharArray());
		} catch (AuthenticationException aex) {
			throw new KoyaServiceException(
					KoyaErrorCodes.CANT_MODIFY_USER_PASSWORD);
		}
	}

	/**
	 * return users list that matches query . email, lastname or first name
	 * starts with query String.
	 * 
	 * 
	 * 
	 * @param queryStartsWith
	 * @param maxResults
	 *            - 0 = no limit
	 * @param companyName
	 * @param companyRolesFilter
	 * @return
	 */
	public List<User> find(String queryStartsWith, int maxResults,
			String companyName, List<String> companyRolesFilter) {
		List<User> users = new ArrayList<>();

		if (queryStartsWith == null) {
			queryStartsWith = "";
		}
		queryStartsWith = queryStartsWith.toLowerCase();

		// application global search
		if (companyName == null || companyName.isEmpty()) {

			String luceneRequest = "TYPE:\"cm:person\" AND (@cm\\:lastName:\""
					+ queryStartsWith + "*\" OR @cm\\:firstName:\""
					+ queryStartsWith + "*\" OR @cm\\:email:\""
					+ queryStartsWith + "*\" )";

			logger.trace(luceneRequest);
			ResultSet rs = null;
			try {
				rs = searchService.query(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						SearchService.LANGUAGE_LUCENE, luceneRequest);
				for (ResultSetRow r : rs) {
					users.add(buildUser(r.getNodeRef()));
					if (users.size() >= maxResults) {
						break;
					}
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
		} else {

			// TODO apply user filter
			Map<String, String> companyMembers = new HashMap<>();
			if (companyRolesFilter != null && companyRolesFilter.size() > 0) {
				for (String role : companyRolesFilter) {
					companyMembers.putAll(siteService.listMembers(companyName,
							null, role, 0, true));
				}
			} else {
				companyMembers.putAll(siteService.listMembers(companyName,
						null, null, 0, true));
			}

			for (String userName : companyMembers.keySet()) {
				// remove from results where query is not name|firstname|email
				// substring
				// ---> prevent display changed mail adress (username not
				// changed )
				User u = buildUser(personService.getPerson(userName));
				if (u.getName().toLowerCase().startsWith(queryStartsWith)
						|| u.getFirstName().toLowerCase()
								.startsWith(queryStartsWith)
						|| u.getEmail().toLowerCase()
								.startsWith(queryStartsWith)) {
					users.add(buildUser(personService.getPerson(userName)));
				}

				if (users.size() >= maxResults) {
					break;
				}
			}

		}

		logger.trace(users.size() + " results found");

		return users;
	}

	public User buildUser(NodeRef userNodeRef) {
		User u = new User();

		// TODO complete build with all properties
		u.setUserName((String) nodeService.getProperty(userNodeRef,
				ContentModel.PROP_USERNAME));
		u.setFirstName((String) nodeService.getProperty(userNodeRef,
				ContentModel.PROP_FIRSTNAME));
		u.setName((String) nodeService.getProperty(userNodeRef,
				ContentModel.PROP_LASTNAME));
		u.setEmail((String) nodeService.getProperty(userNodeRef,
				ContentModel.PROP_EMAIL));
		u.setEmailFeedDisabled((Boolean) nodeService.getProperty(userNodeRef,
				ContentModel.PROP_EMAIL_FEED_DISABLED));
		u.setEnabled(!(Boolean) nodeService.hasAspect(userNodeRef,
				ContentModel.ASPECT_PERSON_DISABLED));
		u.setNodeRef(userNodeRef);
		u.setCivilTitle((String) nodeService.getProperty(userNodeRef,
				KoyaModel.PROP_CIVILTITLE));

		return u;
	}

	public User getUserByUsername(final String username) {
		if (personService.personExists(username)) {
			return buildUser(personService.getPerson(username));
		} else {
			return null;
		}
	}

	/**
	 * Get User by authenticationKey that could mail address or username.
	 * 
	 * @param authKey
	 * @return
	 * @throws fr.itldev.koya.exception.KoyaServiceException
	 */
	public User getUser(final String authKey) throws KoyaServiceException {

		if (personService.personExists(authKey)) {
			return buildUser(personService.getPerson(authKey));
		} else {
			String luceneRequest = "TYPE:\"cm:person\" AND @cm\\:email:\""
					+ authKey + "\" ";
			List<User> users = new ArrayList<>();
			ResultSet rs = null;
			try {
				rs = searchService.query(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						SearchService.LANGUAGE_LUCENE, luceneRequest);
				for (ResultSetRow r : rs) {
					users.add(buildUser(r.getNodeRef()));
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
			}
			if (users.isEmpty()) {
				throw new KoyaServiceException(
						KoyaErrorCodes.NO_SUCH_USER_IDENTIFIED_BY_AUTHKEY,
						authKey);
			} else if (users.size() > 1) {
				throw new KoyaServiceException(
						KoyaErrorCodes.MANY_USERS_IDENTIFIED_BY_AUTHKEY,
						authKey);
			} else {
				return users.get(0);
			}
		}
	}

	/**
	 * Return user found by email. Return null if not found : no exception
	 * thrown
	 * 
	 * @param authKey
	 * @return
	 */
	public User getUserByEmailFailOver(final String authKey) {
		String luceneRequest = "TYPE:\"cm:person\" AND @cm\\:email:\""
				+ authKey + "\" ";
		List<User> users = new ArrayList<>();
		ResultSet rs = null;
		try {
			rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_LUCENE, luceneRequest);
			for (ResultSetRow r : rs) {
				users.add(buildUser(r.getNodeRef()));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		if (users.isEmpty() || users.size() > 1) {
			return null;
		} else {
			return users.get(0);
		}
	}

	public List<UserConnection> getConnectionLog(String userName,
			List<String> companyFilter, Integer maxResults) {
		List<UserConnection> connectionLog = new ArrayList<>();
		// TODO build full connection log
		return connectionLog;
	}

	/**
	 * Checks if user has disabled aspect
	 * 
	 * @param u
	 * @return
	 */
	public Boolean isDisabled(User u) {
		return nodeService.getAspects(u.getNodeRef()).contains(
				ContentModel.ASPECT_PERSON_DISABLED);
	}
	
	/**
	 * Add Node to user SharedElements List
	 * 
	 * @param u
	 * @param n
	 */
	public void addSharedNode(String userMail, NodeRef n) {
		String name = nodeService.getProperty(n, ContentModel.PROP_NAME)
				.toString();
		try {
			User u = getUser(userMail);

			if (!nodeService.hasAspect(u.getNodeRef(),
					KoyaModel.ASPECT_USERSHARES)) {
				nodeService.addAspect(u.getNodeRef(),
						KoyaModel.ASPECT_USERSHARES, null);
			}

			// list existing associations
			List<AssociationRef> sharedNodeAssocs = nodeService
					.getTargetAssocs(u.getNodeRef(),
							KoyaModel.ASSOC_USER_SHAREDNODES);
			Boolean exists = false;
			for (AssociationRef ar : sharedNodeAssocs) {
				if (ar.getTargetRef().equals(n)) {
					exists = true;
					break;
				}

			}

			if (!exists) {
				nodeService.createAssociation(u.getNodeRef(), n,
						KoyaModel.ASSOC_USER_SHAREDNODES);
				logger.trace("Add userShares Association between "+userMail + " and "+name);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param u
	 * @param n
	 */
	public void removeSharedNode(String userMail, NodeRef n) {
		String name = nodeService.getProperty(n, ContentModel.PROP_NAME)
				.toString();
		try {
			User u = getUser(userMail);

			if (nodeService.hasAspect(u.getNodeRef(),
					KoyaModel.ASPECT_USERSHARES)) {
				nodeService.addAspect(u.getNodeRef(),
						KoyaModel.ASPECT_USERSHARES, null);
			}		
			nodeService.removeAssociation(u.getNodeRef(), n,
					KoyaModel.ASSOC_USER_SHAREDNODES);
			logger.trace("Removes userShares Association between "+userMail + " and "+name);

		} catch (Exception e) {
		}
	}

	/**
	 * List all User's Koya Nodes. company filter is optionnal
	 * 
	 * @param u
	 * @return
	 */
	public List<Space> getSharedKoyaNodes(String userName, Company c) {
		List<Space> sharedKoyaNodes = new ArrayList<Space>();
		try {
			User u = getUser(userName);
			if (nodeService.hasAspect(u.getNodeRef(),
					KoyaModel.ASPECT_USERSHARES)) {

				List<AssociationRef> sharedNodeAssocs = nodeService
						.getTargetAssocs(u.getNodeRef(),
								KoyaModel.ASSOC_USER_SHAREDNODES);

				for (AssociationRef ar : sharedNodeAssocs) {
					try {
						if (c == null
								|| c.equals(koyaNodeService
										.getFirstParentOfType(
												ar.getTargetRef(),
												Company.class))) {
							sharedKoyaNodes.add(koyaNodeService
									.getKoyaNode(ar.getTargetRef(),Space.class));
						}
					} catch (KoyaServiceException e) {
					}
				}
			}
		} catch (KoyaServiceException kse) {
			logger.error("error listing user " + userName + " shares "
					+ kse.getMessage());
		}
		return sharedKoyaNodes;
	}
	
}
