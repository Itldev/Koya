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

import fr.itldev.koya.model.Contenu;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Repertoire;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.core.io.Resource;

public interface ContenuService extends AlfrescoService {

    Contenu creerContenu(Utilisateur user, Contenu aCreer) throws AlfrescoServiceException;

    Document envoyerDocument(Utilisateur user, Resource r, Repertoire repertoire) throws AlfrescoServiceException;

    Document envoyerDocument(Utilisateur user, Resource r, Dossier dossier) throws AlfrescoServiceException;

    Contenu deplacer(Utilisateur user, Contenu aDeplacer, Repertoire desination) throws AlfrescoServiceException;

    Contenu deplacer(Utilisateur user, Contenu aDeplacer, Dossier desination) throws AlfrescoServiceException;

    List<Contenu> lister(Utilisateur user, Dossier dossier) throws AlfrescoServiceException;

}
