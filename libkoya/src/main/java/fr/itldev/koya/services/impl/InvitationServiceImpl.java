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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaInvite;
import fr.itldev.koya.services.InvitationService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class InvitationServiceImpl extends AlfrescoRestService implements
		InvitationService {

	private static final String REST_GET_INVITATION = "/s/fr/itldev/koya/invitation/invitation/{userName}/{companyName}";
	private static final String REST_GET_INVITATIONPENDING = "/s/fr/itldev/koya/invitation/pending/{inviteId}";
	private static final String REST_POST_INVITATION = "/s/fr/itldev/koya/invitation/sendmail";
	private static final String REST_POST_VALIDUSERBYINVITE = "/s/fr/itldev/koya/invitation/validate";
	private static final String REST_POST_INVITEUSER = "/s/fr/itldev/koya/invitation/invite";

	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * Invite user identified by email on company with rolename granted.
	 * 
	 * @param userLogged
	 * @param c
	 * @param userEmail
	 * @param roleName
	 * @throws AlfrescoServiceException
	 */
	@Override
	public KoyaInvite inviteUser(User userLogged, Company c, String userEmail,
			String roleName) throws AlfrescoServiceException {

		cacheManager.revokeInvitations(userEmail);

		KoyaInvite iw = new KoyaInvite();
		iw.setCompanyName(c.getName());
		iw.setEmail(userEmail);
		iw.setRoleName(roleName);

		return userLogged.getRestTemplate()
				.postForObject(getAlfrescoServerUrl() + REST_POST_INVITEUSER,
						iw, KoyaInvite.class);
	}

	/**
	 * Validate invitation giving user modifications;
	 * 
	 * @param user
	 * @param inviteId
	 * @param inviteTicket
	 * @throws AlfrescoServiceException
	 */
	@Override
	public User validateInvitation(User user, String inviteId,
			String inviteTicket) throws AlfrescoServiceException {

		Map<String, String> params = new HashMap<>();
		params.put("inviteId", inviteId);
		params.put("inviteTicket", inviteTicket);
		params.put("password", user.getPassword());
		params.put("lastName", user.getName());
		params.put("firstName", user.getFirstName());
		params.put("civilTitle", user.getCivilTitle());

		User u = getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_VALIDUSERBYINVITE, params,
				User.class);
		cacheManager.revokeInvitations(u.getEmail());
		return u;
	}

	/**
	 * Get user's invitation on company if exists.
	 * 
	 * @param user
	 * @param c
	 * @param userToGetInvitaion
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public Map<String, String> getInvitation(User user, Company c,
			User userToGetInvitaion) throws AlfrescoServiceException {
		Map<String, String> m = cacheManager.getInvitations(userToGetInvitaion
				.getEmail());
		if (m != null) {
			if (m.isEmpty()) {
				return null;
			} else {
				return m;
			}
		}

		m = fromJSON(
				new TypeReference<Map>() {
				},
				user.getRestTemplate().getForObject(
						getAlfrescoServerUrl() + REST_GET_INVITATION,
						String.class, userToGetInvitaion.getUserName(),
						c.getName()));

		Map<String, String> value;
		if (m == null) {
			value = new HashMap<>();
		} else {
			value = m;
		}
		cacheManager.setInvitations(userToGetInvitaion.getEmail(), value);
		return m;

	}

	/**
	 * Checks anynomously if given inviteId exists (is is a pending invite).
	 * 
	 * @param inviteId
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public Boolean isInvitationPending(String inviteId)
			throws AlfrescoServiceException {
		return getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_INVITATIONPENDING,
				String.class, inviteId).equals("true");
	}

	/**
	 * Send invitation mail again to invitee based on invitationId
	 * 
	 * @param user
	 * @param inviteId
	 * @throws AlfrescoServiceException
	 */
	@Override
	public void reSendInviteMail(User user, String inviteId)
			throws AlfrescoServiceException {
		user.getRestTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_INVITATION, inviteId,
				String.class);
	}
}
