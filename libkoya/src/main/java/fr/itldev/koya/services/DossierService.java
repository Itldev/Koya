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

import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Espace;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface DossierService extends AlfrescoService {

    /**
     * Créer un nouveau dossier
     *
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    Dossier creerNouveau(Utilisateur user, Dossier dossier) throws AlfrescoServiceException;

    /**
     *
     * @param user
     * @param dossier
     * @return
     * @throws AlfrescoServiceException
     */
    Dossier editer(Utilisateur user, Dossier dossier) throws AlfrescoServiceException;

    /**
     * Liste tous les dossiers d'un espace
     *
     * @param user
     * @param espace
     * @param filter
     * @return
     * @throws AlfrescoServiceException
     */
    List<Dossier> lister(Utilisateur user, Espace espace, String... filter) throws AlfrescoServiceException;

}
