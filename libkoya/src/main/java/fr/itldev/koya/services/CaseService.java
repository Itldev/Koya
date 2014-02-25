/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.services;

import fr.itldev.koya.model.impl.Case;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface CaseService extends AlfrescoService {

    /**
     * Creates a new case
     *
     * @param user
     * @param koyaCase
     * @return
     * @throws AlfrescoServiceException
     */
    Case create(User user, Case koyaCase) throws AlfrescoServiceException;

    /**
     *
     * @param user
     * @param koyaCase
     * @return
     * @throws AlfrescoServiceException
     */
    Case edit(User user, Case koyaCase) throws AlfrescoServiceException;

    /**
     * List all Space Cases
     *
     * @param user
     * @param space
     * @param filter
     * @return
     * @throws AlfrescoServiceException
     */
    List<Case> list(User user, Space space, String... filter) throws AlfrescoServiceException;

}
