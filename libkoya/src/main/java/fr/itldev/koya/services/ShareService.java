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
package fr.itldev.koya.services;

import java.util.List;

import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.KoyaShare;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

/**
 * Sharing Secured items.
 * 
 */
public interface ShareService extends AlfrescoService {

	/**
	 * Public share KoyaNode to a user (pre created or not)
	 * 
	 * @param user
	 * @param itemToShare
	 * @param sharedUserMail
	 * 
	 */
	KoyaShare shareItem(User user, KoyaNode itemToShare, String sharedUserMail,
			KoyaPermissionConsumer permission)
			throws AlfrescoServiceException;

	/**
	 * Revoke Shares KoyaNodes to a list of users
	 * 
	 * @param user
	 * @param itemToUnShare
	 * @param unsharedUserMail
	 * 
	 */
	void unShareItem(User user, KoyaNode itemToUnShare, String unsharedUserMail)
			throws AlfrescoServiceException;

	/**
	 * Show Users who can publicly access to given element.
	 * 
	 * @param user
	 * @param item
	 * @return
	 * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
	 */
	List<User> sharedUsers(User user, KoyaNode item,
			KoyaPermissionConsumer permission)
			throws AlfrescoServiceException;

	/**
	 * Get all KoyaNodes shared for specified user on a company.
	 * 
	 * @param userLogged
	 * @param userToGetShares
	 * @param c
	 * @return
	 * @throws AlfrescoServiceException
	 */
	@Deprecated
	List<KoyaNode> sharedItems(User userLogged, User userToGetShares,
			Company c) throws AlfrescoServiceException;

	/**
	 * Checks if item has any share with consumer permission
	 * 
	 * @param item
	 * @return
	 */
	Boolean isSharedWithConsumerPermission(Space item);
}
