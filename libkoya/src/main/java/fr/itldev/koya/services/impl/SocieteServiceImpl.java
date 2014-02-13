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

package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.impl.OffreCommerciale;
import fr.itldev.koya.model.impl.Societe;
import fr.itldev.koya.model.impl.Utilisateur;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.SocieteService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.web.client.RestClientException;

public class SocieteServiceImpl extends AlfrescoRestService implements SocieteService {

    private static final String REST_POST_ADDSOCIETE = "/s/fr/itldev/koya/societe/add";
    private static final String REST_GET_LISTSOCIETE = "/s/fr/itldev/koya/societe/list.json";
    private static final String REST_DEL_DELSOCIETE = "/s/api/sites/{shortname}";
    private static final String REST_GET_LISTOFFRES = "/s/fr/itldev/koya/offrecommerciale/list?active={active}";

    /**
     * Méthode de creation d'une nouvelle société
     *
     * TODO la création d'un site compatible avec share nécesite des action coté
     * share pour avoir un dashboard valides :
     * share/WEB-INF/classes/alfresco/site-webscripts/org/alfresco/modules/create-site.post.json.js
     *
     * @param admin
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    @Override
    public Societe creerNouvelle(Utilisateur admin, Societe s) throws RestClientException, AlfrescoServiceException {

        //TODO verifications (nom + offrecom)
        //TODO encodage s.getTitre (url compliant) : pas urgent car il s'agit d'un POC non utilisé via cette librairie
        ItlAlfrescoServiceWrapper ret = admin.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_POST_ADDSOCIETE + "/" + s.getTitre() + "/" + s.getOffreEnCours().getNom(), ItlAlfrescoServiceWrapper.class);

        //TODO effectuer la requete sur un script coté share (si existant) afin de rendre dispo le site -- sitedata.newPreset()...
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Societe) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    @Override
    public List<Societe> lister(Utilisateur admin) throws RestClientException, AlfrescoServiceException {//TODO search filter
        ItlAlfrescoServiceWrapper ret = admin.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LISTSOCIETE, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }
    }

    /**
     * Méthode de suppression d'une société
     *
     * Implémentée mais pas utilisée : en pratique, on préférera désactiver une
     * société (site) plutot que de la supprimer.
     *
     *
     * @param admin
     * @param societe
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    @Override
    public void supprimer(Utilisateur admin, Societe societe) throws RestClientException, AlfrescoServiceException {
        admin.getRestTemplate().delete(getAlfrescoServerUrl() + REST_DEL_DELSOCIETE, societe.getNom());
    }

    @Override
    public List<OffreCommerciale> listerOffresCommerciales(Utilisateur admin, Boolean... active) throws AlfrescoServiceException {
        Boolean filterActif = false;
        if (active.length == 1) {
            filterActif = active[0];
        }

        ItlAlfrescoServiceWrapper ret = admin.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LISTOFFRES, ItlAlfrescoServiceWrapper.class, filterActif);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }

    }

    @Override
    public void chargerOffreCommercialeEncours(Utilisateur admin, Societe s) throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void chargerHistoriqueOffresCommerciales(Utilisateur admin, Societe s) throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
