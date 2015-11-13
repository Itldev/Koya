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
package fr.itldev.koya.services.impl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.client.RestClientException;

import fr.itldev.koya.model.impl.Activity;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.AuthTicket;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

public class UserServiceImpl extends AlfrescoRestService implements UserService {

	private static final String REST_GET_LOGIN = "/s/api/login.json?u={username}&pw={password}";
	private static final String REST_POST_PERSONFROMMAIL = "/s/fr/itldev/koya/user/getbyauthkey?alf_ticket={alf_ticket}";
	private static final String REST_DEL_LOGOUT = "/s/api/login/ticket/{ticket}?alf_ticket={alf_ticket}";
	private static final String REST_POST_MODIFYDETAILS = "/s/fr/itldev/koya/user/modifydetails?alf_ticket={alf_ticket}";
	private static final String REST_GET_FINDUSERS = "/s/fr/itldev/koya/user/find?"
			+ "query={query}&maxResults={maxresults}&companyName={companyName}&roleFilter={roleFilter}&alf_ticket={alf_ticket}";
	private static final String REST_POST_CHANGEPASSWORD = "/s/fr/itldev/koya/user/changepassword?alf_ticket={alf_ticket}";
	private static final String REST_GET_GROUPS = "/s/fr/itldev/koya/user/groups?alf_ticket={alf_ticket}";

	// ===== Preferences
	private static final String REST_GET_PREFERENCES = "/s/api/people/{userid}/preferences?alf_ticket={alf_ticket}";
	private static final String REST_POST_PREFERENCES = "/s/api/people/{userid}/preferences?alf_ticket={alf_ticket}";
	private static final String REST_DELETE_PREFERENCES = "/s/api/people/{userid}/preferences?pf={preferencefilter?}&alf_ticket={alf_ticket}";

	// ====== reset password BPM
	private static final String REST_POST_RESET_PASSWORD_REQUEST = "/s/fr/itldev/koya/resetpassword/request";
	private static final String REST_POST_RESET_PASSWORD_VALIDATION = "/s/fr/itldev/koya/resetpassword/validation";

	// ====== user activities
	private static final String REST_GET_ACTIVITIES = "/s/fr/itldev/koya/activities/feed/user?format=json&alf_ticket={alf_ticket}";

	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * Authenticates user with authentication key that could be his login or
	 * email.
	 * 
	 * 
	 * TODO give md5 or other secured password instead of clear.
	 * 
	 * @param authKey
	 * @param password
	 * @return
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	@Override
	public User login(String authKey, String password)
			throws RestClientException, AlfrescoServiceException {
		AuthTicket ticket = null;
		try {
			ticket = getTemplate().getForObject(
					getAlfrescoServerUrl() + REST_GET_LOGIN, AuthTicket.class,
					authKey, password);
		} catch (Exception ex) {
			throw ex;
		}
		// Get User Object
		Map<String, Serializable> emailPostWrapper = new HashMap<>();
		emailPostWrapper.put("authKey", authKey);
		User user = getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_PERSONFROMMAIL,
				emailPostWrapper, User.class, ticket);

		// Authentication ticket integration
		user.setTicketAlfresco(ticket.toString());

		// load users rest prefrences
		loadPreferences(user);
		//
		user.setPassword(password);
		return user;
	}

	@Override
	public Boolean logout(User user) throws AlfrescoServiceException {
		try {
			getTemplate().delete(getAlfrescoServerUrl() + REST_DEL_LOGOUT,
					user.getTicketAlfresco(), user.getTicketAlfresco());
		} catch (RestClientException rce) {
			throw new AlfrescoServiceException(rce.getMessage(),
					KoyaErrorCodes.CANNOT_LOGOUT_USER);
		}

		// TODO treat returns
		return null;

	}

	@Override
	public void createUser(User userAdmin, User toCreate) {
		// exception if doesn't work
	}

	/**
	 * Updates users preferences from alfresco server. Erases unsaved local
	 * preferences.
	 * 
	 * @param user
	 */
	@Override
	public void loadPreferences(User user) {
		loadPreferences(user, user);
	}

	@Override
	public void loadPreferences(User userLog, User userToGetPrefs) {
		Preferences preferences = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_PREFERENCES,
				Preferences.class, userToGetPrefs.getUserName(),
				userLog.getTicketAlfresco());
		userToGetPrefs.setPreferences(preferences);
	}

	/**
	 * Writes local preferences to alfresco server.
	 * 
	 * @param user
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	@Override
	public void commitPreferences(User user) throws AlfrescoServiceException {
		commitPreferences(user, user);
	}

	@Override
	public void commitPreferences(User userLog, User userToCommitPrefs)
			throws AlfrescoServiceException {

		if (userToCommitPrefs.getPreferences() != null) {
			// 1 - send new and modified keys
			getTemplate().postForObject(
					getAlfrescoServerUrl() + REST_POST_PREFERENCES,
					userToCommitPrefs.getPreferences(), Preferences.class,
					userToCommitPrefs.getUserName(),
					userLog.getTicketAlfresco());

			// 2 - updates preferences from server
			Preferences prefsToCommit = userToCommitPrefs.getPreferences();
			loadPreferences(userLog, userToCommitPrefs);

			// 3 - if less preferences to commit than updates --> some keys have
			// to be deleted.
			if (prefsToCommit.size() < userToCommitPrefs.getPreferences()
					.size()) {

				String deleteFilter = "";
				String sep = "";
				for (String k : userToCommitPrefs.getPreferences().keySet()) {
					if (!prefsToCommit.keySet().contains(k)) {
						deleteFilter += sep + k;
						sep = ",";
					}
				}
				getTemplate().delete(
						getAlfrescoServerUrl() + REST_DELETE_PREFERENCES,
						userToCommitPrefs.getUserName(), deleteFilter,
						userLog.getTicketAlfresco());
				loadPreferences(userLog, userToCommitPrefs);
			}

		} else {
			throw new AlfrescoServiceException("No user preference to commit",
					0);
		}

	}

	@Override
	public void commitProperties(User user) throws AlfrescoServiceException {
		commitProperties(user, user);
	}

	@Override
	public void commitProperties(User userLog, User userToCommitProps)
			throws AlfrescoServiceException {
		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_MODIFYDETAILS,
				userToCommitProps, String.class, userLog.getTicketAlfresco());
	}

	@Override
	public void changePassword(User userLog, String oldPassword,
			String newPassword) throws AlfrescoServiceException,
			MalformedURLException {

		Map<String, String> params = new HashMap<String, String>();
		params.put("oldPwd", oldPassword);
		params.put("newPwd", newPassword);

		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_CHANGEPASSWORD, params,
				String.class, userLog.getTicketAlfresco());

	}

	/**
	 * find users list wich first/last name or email starts with query. Return
	 * list limitated by maxResults.
	 * 
	 * if company is not null limit results to company scope. rolesFilter can
	 * refine results in this company context (not taken in account if no
	 * company is set)
	 * 
	 * @param userLog
	 * @param query
	 * @param maxResults
	 * @param company
	 * @param rolesFilter
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public List<User> find(User userLog, String query, Integer maxResults,
			Company company, List<String> rolesFilter)
			throws AlfrescoServiceException {

		String companyName = "";
		String roles = "";

		if (company != null) {
			companyName = company.getName();
		}
		if (rolesFilter != null && rolesFilter.size() > 0) {
			String sep = "";
			for (String r : rolesFilter) {
				roles += sep + r;
				sep = ",";
			}
		}

		return fromJSON(
				new TypeReference<List<User>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_FINDUSERS,
						String.class, query, maxResults, companyName, roles,
						userLog.getTicketAlfresco()));

	}

	/**
	 * Get user Object from email.
	 * 
	 * @param user
	 * @param email
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public User getUserFromEmail(User user, String email)
			throws AlfrescoServiceException {
		Map<String, Serializable> emailPostWrapper = new HashMap<>();
		emailPostWrapper.put("authKey", email);
		return fromJSON(
				new TypeReference<User>() {
				},
				getTemplate().postForObject(
						getAlfrescoServerUrl() + REST_POST_PERSONFROMMAIL,
						emailPostWrapper, String.class,
						user.getTicketAlfresco()));
	}

	@Override
	public User getUserFromEmailFailProof(User user, String email) {
		Map<String, Serializable> emailPostWrapper = new HashMap<>();
		emailPostWrapper.put("authKey", email);
		emailPostWrapper.put("failProof", true);
		try {
			return fromJSON(
					new TypeReference<User>() {
					},
					getTemplate().postForObject(
							getAlfrescoServerUrl() + REST_POST_PERSONFROMMAIL,
							emailPostWrapper, String.class,
							user.getTicketAlfresco()));
		} catch (RestClientException e) {
			return null;
		}
	}

	/**
	 * Send Reset password request to alfresco server.
	 * 
	 * 
	 * @param userEmail
	 * @param resetUrl
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void sendResetPasswordRequest(String userEmail, String resetUrl)
			throws AlfrescoServiceException {
		Map<String, String> params = new HashMap<>();
		params.put("userEmail", userEmail);
		params.put("resetUrl", resetUrl);

		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_RESET_PASSWORD_REQUEST,
				params, String.class);
	}

	/**
	 * Validate password modification in reset password process.
	 * 
	 * Authenticated by resetId + resetTicket.
	 * 
	 * @param resetId
	 * @param resetTicket
	 * @param newPassword
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void validateNewPasswordfromResetProcess(String resetId,
			String resetTicket, String newPassword)
			throws AlfrescoServiceException {

		Map<String, String> params = new HashMap<>();
		params.put("resetId", resetId);
		params.put("resetTicket", resetTicket);
		params.put("newPassword", newPassword);
		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_RESET_PASSWORD_VALIDATION,
				params, String.class);
	}

	@Override
	public List<String> getGroups(User user) {

		List<String> groups = cacheManager.getUserGroups(user);
		if (groups != null) {
			return groups;
		}

		groups = fromJSON(
				new TypeReference<List<String>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_GROUPS, String.class,
						user.getTicketAlfresco()));

		cacheManager.setUserGroups(user, groups);

		return groups;

	}

	/**
	 * List available activities for user
	 * 
	 * @param user
	 * @return
	 * @throws AlfrescoServiceException
	 */
	public List<Activity> listActivities(User user)
			throws AlfrescoServiceException {
		return fromJSON(
				new TypeReference<List<Activity>>() {
				},
				getTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_ACTIVITIES,
						String.class, user.getTicketAlfresco()));
	}

}
