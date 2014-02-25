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

import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface SpaceService extends AlfrescoService {

    /**
     * Créer un nouvel espace
     *
     * @param user
     * @param space
     * @return
     * @throws AlfrescoServiceException
     */
    Space create(User user, Space space) throws AlfrescoServiceException;

    /**
     * Activer un espace
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    void enable(User user, Space space) throws AlfrescoServiceException;

    /**
     * Désactiver un espace
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    void disable(User user, Space space) throws AlfrescoServiceException;

    /**
     * Liste plate de tous les espaces de la société
     *
     * @param user
     * @param company
     * @return
     * @throws AlfrescoServiceException
     */
    List<Space> list(User user, Company company) throws AlfrescoServiceException;

    /**
     * Liste arborescente de tous les espaces de la société
     *
     * @param user
     * @param company
     * @return
     * @throws AlfrescoServiceException
     */
    List<Space> listAsTree(User user, Company company) throws AlfrescoServiceException;

}
