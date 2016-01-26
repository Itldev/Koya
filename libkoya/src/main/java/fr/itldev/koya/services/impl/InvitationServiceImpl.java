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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaInvite;
import fr.itldev.koya.services.InvitationService;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class InvitationServiceImpl extends AlfrescoRestService implements InvitationService, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String REST_GET_LISTPENDINGINVITATIONS = "/s/fr/itldev/koya/invitation/listpending/{userName}";
	private static final String REST_POST_INVITATION = "/s/fr/itldev/koya/invitation/sendmail?alf_ticket={alf_ticket}";
	private static final String REST_POST_VALIDUSERBYINVITE = "/s/fr/itldev/koya/invitation/validate";
	private static final String REST_POST_INVITEUSER = "/s/fr/itldev/koya/invitation/invite?alf_ticket={alf_ticket}";

	private CacheManager cacheManager;
	private UserService userService;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	

	public void setUserService(UserService userService) {
		this.userService = userService;
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
		User u = userService.getUserFromEmailFailProof(userLogged, userEmail);
		if(u != null){
			cacheManager.revokeInvitations(u.getUserName());
		}
				

		KoyaInvite iw = new KoyaInvite();
		iw.setCompanyName(c.getName());
		iw.setEmail(userEmail);
		iw.setRoleName(roleName);

		return getTemplate()
				.postForObject(getAlfrescoServerUrl() + REST_POST_INVITEUSER,
						iw, KoyaInvite.class,userLogged.getTicketAlfresco());
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

		String enabled = Boolean.FALSE.toString();		
		try{
			enabled = user.isEnabled().toString();			
		}catch(Exception e){
			
		}
		
		Map<String, String> params = new HashMap<>();
		params.put("inviteId", inviteId);
		params.put("inviteTicket", inviteTicket);
		params.put("password", user.getPassword());
		params.put("lastName", user.getName());
		params.put("firstName", user.getFirstName());
		params.put("civilTitle", user.getCivilTitle());
		params.put("userEnabled", enabled);

		User u = getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_VALIDUSERBYINVITE, params,
				User.class);
		cacheManager.revokeInvitations(u.getUserName());
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
	public Map<String, String> getInvitation(Company c, String userName)
			throws AlfrescoServiceException {
		List<Map<String, String>> invitations = listInvitations(userName);
		for (Map<String, String> i : invitations) {
			if (i.get("companyName").equals(c.getName())) {
				return i;
			}
		}
		return null;
	}
	
	private Logger logger = Logger.getLogger(this.getClass());
	@Override
	public List<Map<String, String>> listInvitations(String userName)
			throws AlfrescoServiceException {

		List<Map<String, String>> m = cacheManager.getInvitations(userName);
		if (m != null) {			
				return m;			
		}
		List<Map<String, String>> invList;
		invList = fromJSON(new TypeReference<List>() {
		}, getTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LISTPENDINGINVITATIONS, String.class,
				userName));
		
		logger.error(" request user "+userName + " >> "+invList);

		cacheManager.setInvitations(userName, invList);
		return invList;
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
		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_INVITATION, inviteId,
				String.class,user.getTicketAlfresco());
	}
}
