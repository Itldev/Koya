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
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface SecuService {

    /**
     * Returns directly granted Users to item.
     *
     * @param user
     * @param item
     * @return
     */
    List<User> usersGrantedDirect(User user, SecuredItem item);

    /**
     * Returns inherited granted Users to item.
     *
     * @param user
     * @param item
     * @return
     */
    List<User> usersGrantedInherit(User user, SecuredItem item);

    /**
     * List available userRoles for a company.
     *
     * @param userLogged
     * @param c
     * @return
     * @throws AlfrescoServiceException
     */
    List<UserRole> listAvailableRoles(User userLogged, Company c) throws AlfrescoServiceException;

    /**
     * Get current Role in Company context for specified User.
     *
     * @param userLogged
     * @param c
     * @param userToGetRole
     * @return
     * @throws AlfrescoServiceException
     */
    UserRole getUserRole(User userLogged, Company c, User userToGetRole) throws AlfrescoServiceException;

    /**
     * Set userRole in Company context for specified User.
     *
     * @param userLogged
     * @param c
     * @param userNameSetRole
     * @param roleName
     * @throws AlfrescoServiceException
     */
    void setUserRole(User userLogged, Company c, String userNameSetRole, String roleName) throws AlfrescoServiceException;

}
