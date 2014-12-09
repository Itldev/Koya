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

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

/**
 * Sharing Secured items.
 *
 */
public interface ShareService extends AlfrescoService {

    /**
     * Public share SecuredItem to a user (pre created or not)
     *
     * @param user
     * @param itemToShare
     * @param sharedUserMail
     *
     */
    void shareItem(User user, SecuredItem itemToShare, String sharedUserMail) throws AlfrescoServiceException;

    /**
     * Revoke Shares SecuredItems to a list of users
     *
     * @param user
     * @param itemToUnShare
     * @param unsharedUserMail
     *
     */
    void unShareItem(User user, SecuredItem itemToUnShare, String unsharedUserMail) throws AlfrescoServiceException;

    /**
     * Show Users who can publicly access to given element.
     *
     * @param user
     * @param item
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    List<User> sharedUsers(User user, SecuredItem item) throws AlfrescoServiceException;

    /**
     * Get all securedItems shared for specified user on a company.
     *
     * @param userLogged
     * @param userToGetShares
     * @param c
     * @return
     * @throws AlfrescoServiceException
     */
    @Deprecated
    List<SecuredItem> sharedItems(User userLogged, User userToGetShares, Company c) throws AlfrescoServiceException;

    /**
     * Checks if item has any share with consumer permission
     *
     * @param item
     * @return
     */
    Boolean isSharedWithConsumerPermission(SubSpace item);
}
