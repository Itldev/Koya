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
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaShare;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.services.ShareService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

/**
 *
 *
 */
public class ShareServiceImpl extends AlfrescoRestService
		implements ShareService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String REST_GET_SHAREDITEMS = "/s/fr/itldev/koya/security/clientshare/listusershares/{userName}/{companyName}?alf_ticket={alf_ticket}";
	protected static final String REST_GET_HASMEMBER = "/s/fr/itldev/koya/security/hasmember/{roleName}/{nodeRef}";

	protected static final String REST_POST_SHARESINGLE = "/s/fr/itldev/koya/security/consumershare/do?alf_ticket={alf_ticket}";
	protected static final String REST_POST_UNSHARESINGLE = "/s/fr/itldev/koya/security/consumershare/undo?alf_ticket={alf_ticket}";

	private CacheManager cacheManager;

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public KoyaShare shareItem(User user, KoyaNode itemToShare,
			String sharedUserMail, KoyaPermissionConsumer permission)
					throws AlfrescoServiceException {

		cacheManager.revokeNodeSharedWithKoyaClient(itemToShare);
		cacheManager.revokeNodeSharedWithKoyaPartner(itemToShare);

		Map<String, String> shareParams = new HashMap<>();
		shareParams.put("email", sharedUserMail);
		shareParams.put("nodeRef", itemToShare.getNodeRef().toString());
		shareParams.put("koyaPermission", permission.toString());

		return getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_SHARESINGLE, shareParams,
				KoyaShare.class, user.getTicketAlfresco());
	}

	/**
	 * Undo shares to sepcified user.
	 * 
	 * @param user
	 */
	@Override
	public void unShareItem(User user, KoyaNode itemToUnShare,
			String unsharedUserMail) throws AlfrescoServiceException {
		cacheManager.revokeNodeSharedWithKoyaClient(itemToUnShare);
		cacheManager.revokeNodeSharedWithKoyaPartner(itemToUnShare);

		Map<String, String> unshareParams = new HashMap<>();
		unshareParams.put("email", unsharedUserMail);
		unshareParams.put("nodeRef", itemToUnShare.getNodeRef().toString());

		getTemplate().postForObject(
				getAlfrescoServerUrl() + REST_POST_UNSHARESINGLE, unshareParams,
				String.class, user.getTicketAlfresco());
	}

	/**
	 * Show Users who can publicly access to given element.
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	@Override
	public List<User> sharedUsers(User user, KoyaNode item,
			KoyaPermissionConsumer permission) throws AlfrescoServiceException {
		return fromJSON(new TypeReference<List<User>>() {
		}, getTemplate().getForObject(
				getAlfrescoServerUrl()
						+ DossierServiceImpl.REST_GET_LISTMEMBERSHIP,
				String.class, permission, item.getNodeRef(),
				user.getTicketAlfresco()));
	}

	/**
	 * Get all KoyaNodes shared for specified user on a company.
	 * 
	 * @param userLogged
	 * @param userToGetShares
	 * @param c
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Override
	public List<KoyaNode> sharedItems(User userLogged, User userToGetShares,
			Company c) throws AlfrescoServiceException {

		return fromJSON(new TypeReference<List<KoyaNode>>() {
		}, getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_SHAREDITEMS, String.class,
				userToGetShares.getUserName(), c.getName(),
				userLogged.getTicketAlfresco()));

	}

	/**
	 * Checks if item has any share with KoyaClient permission
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public Boolean isSharedWithKoyaClient(Space item) {

		if (item == null) {
			return Boolean.FALSE;
		}
		Boolean shared = cacheManager
				.getNodeSharedWithKoyaClient((KoyaNode) item);

		if (shared != null) {
			return shared;
		}
		shared = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_HASMEMBER, Boolean.class,
				KoyaPermissionConsumer.CLIENT, item.getNodeRef());
		cacheManager.setNodeSharedWithKoyaClient((KoyaNode) item, shared);
		return shared;

	}

	
	/**
	 * Checks if item has any share with KoyaPartner permission
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public Boolean isSharedWithKoyaPartner(Space item) {

		if (item == null) {
			return Boolean.FALSE;
		}
		Boolean shared = cacheManager
				.getNodeSharedWithKoyaPartner((KoyaNode) item);

		if (shared != null) {
			return shared;
		}
		shared = getTemplate().getForObject(
				getAlfrescoServerUrl() + REST_GET_HASMEMBER, Boolean.class,
				KoyaPermissionConsumer.PARTNER, item.getNodeRef());
		cacheManager.setNodeSharedWithKoyaPartner((KoyaNode) item, shared);
		return shared;

	}

	
}
