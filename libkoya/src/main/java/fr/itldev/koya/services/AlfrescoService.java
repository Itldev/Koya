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
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import java.util.Map;

public interface AlfrescoService {

    /**
     * Deletes item.
     *
     * @param user
     * @param securedItem
     */
    void delete(User user, SecuredItem securedItem);

    /**
     * Renames item.
     *
     * @param user
     * @param securedItem
     * @param newName
     */
    void rename(User user, SecuredItem securedItem, String newName);

    /**
     *
     * Returns Secured Item Parent if exists.
     *
     * @param user
     * @param securedItem
     * @return
     * @throws AlfrescoServiceException
     */
    SecuredItem getParent(User user, SecuredItem securedItem) throws AlfrescoServiceException;

    /**
     * Returns SecuredItems ancestors list.
     *
     * @param user
     * @param securedItem
     * @return
     * @throws AlfrescoServiceException
     */
    List<SecuredItem> getParents(User user, SecuredItem securedItem) throws AlfrescoServiceException;

    /**
     * Get Informations about server and modules.
     *
     * @param user
     * @return
     */
    Map getServerInfos(User user);

}
