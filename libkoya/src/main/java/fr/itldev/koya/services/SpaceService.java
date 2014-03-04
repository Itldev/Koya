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

import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface SpaceService extends AlfrescoService {

    /**
     * Create a new space
     *
     * @param user
     * @param space
     * @return
     * @throws AlfrescoServiceException
     */
    Space create(User user, Space space) throws AlfrescoServiceException;

    /**
     * Activate a space
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    void enable(User user, Space space) throws AlfrescoServiceException;

    /**
     * disable a space
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    void disable(User user, Space space) throws AlfrescoServiceException;

    /**
     * List Company Spaces Structure
     *
     * @param user
     * @param company
     * @param depth
     * @return
     * @throws AlfrescoServiceException
     */
    List<Space> list(User user, Company company, Integer... depth) throws AlfrescoServiceException;

    /**
     *
     * @param user
     * @param toMove
     * @param destination
     * @return
     * @throws AlfrescoServiceException
     */
    Space move(User user, Space toMove, Space destination) throws AlfrescoServiceException;

    /**
     *
     * @param user
     * @param toMove
     * @param destination
     * @return
     * @throws AlfrescoServiceException
     */
    Space move(User user, Space toMove, Company destination) throws AlfrescoServiceException;

    /**
     *
     * @param user
     * @param toDel
     * @throws AlfrescoServiceException
     */
    void del(User user, Space toDel) throws AlfrescoServiceException;

}
