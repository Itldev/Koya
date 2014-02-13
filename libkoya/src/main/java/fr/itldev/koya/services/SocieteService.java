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

import fr.itldev.koya.model.impl.OffreCommerciale;
import fr.itldev.koya.model.impl.Societe;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.web.client.RestClientException;

public interface SocieteService extends AlfrescoService {

    /**
     * Création d'une nouvelle société.
     *
     * TODO passer un paramètre de template.
     *
     * @param admin
     * @param s
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    Societe creerNouvelle(Utilisateur admin, Societe s) throws RestClientException, AlfrescoServiceException;

    /**
     *
     * @param admin
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    List<Societe> lister(Utilisateur admin) throws RestClientException, AlfrescoServiceException;

    /**
     * Suppression d'une société - méthode spécifique comparée à la suppression
     * d'un noeud classique car il s'agit d'un site au sens Alfresco.
     *
     * @param admin
     * @param societe
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    void supprimer(Utilisateur admin, Societe societe) throws RestClientException, AlfrescoServiceException;

    /**
     * Liste les offres commerciales disponibles.
     *
     * @param admin
     * @param active
     * @return
     * @throws
     * fr.itldev.koya.services.Exceptions.AlfrescoServiceException
     */
    List<OffreCommerciale> listerOffresCommerciales(Utilisateur admin, Boolean... active) throws AlfrescoServiceException;

    /**
     * Charge l'offre commerciale en cours dans la société.
     *
     * @param admin
     * @param s
     * @throws AlfrescoServiceException
     */
    void chargerOffreCommercialeEncours(Utilisateur admin, Societe s) throws AlfrescoServiceException;

    /**
     * Charge l'historique des offres commerciales de la société.
     *
     * @param admin
     * @param s
     * @throws AlfrescoServiceException
     */
    void chargerHistoriqueOffresCommerciales(Utilisateur admin, Societe s) throws AlfrescoServiceException;

}
