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

import fr.itldev.koya.model.impl.Espace;
import fr.itldev.koya.model.impl.Societe;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface EspaceService extends AlfrescoService {

    /**
     * Créer un nouvel espace
     *
     * @param user
     * @param espace
     * @return
     * @throws AlfrescoServiceException
     */
    Espace creerNouveau(Utilisateur user, Espace espace) throws AlfrescoServiceException;

    /**
     * Activer un espace
     *
     * @param user
     * @param espace
     * @throws AlfrescoServiceException
     */
    void activer(Utilisateur user, Espace espace) throws AlfrescoServiceException;

    /**
     * Désactiver un espace
     *
     * @param user
     * @param espace
     * @throws AlfrescoServiceException
     */
    void desactiver(Utilisateur user, Espace espace) throws AlfrescoServiceException;

    /**
     * Liste plate de tous les espaces de la société
     *
     * @param user
     * @param societe
     * @return
     * @throws AlfrescoServiceException
     */
    List<Espace> listEspaces(Utilisateur user, Societe societe) throws AlfrescoServiceException;

    /**
     * Liste arborescente de tous les espaces de la société
     *
     * @param user
     * @param societe
     * @return
     * @throws AlfrescoServiceException
     */
    List<Espace> listEspacesArbo(Utilisateur user, Societe societe) throws AlfrescoServiceException;

}
