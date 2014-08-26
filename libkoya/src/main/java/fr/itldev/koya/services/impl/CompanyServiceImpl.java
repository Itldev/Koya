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
package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.CompanyProperties;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.CompanyService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.springframework.web.client.RestClientException;

public class CompanyServiceImpl extends AlfrescoRestService implements CompanyService {

    private static final String REST_POST_ADDCOMPANY = "/s/fr/itldev/koya/company/add";
    private static final String REST_GET_LISTCOMPANY = "/s/fr/itldev/koya/company/list.json";
    private static final String REST_DEL_DELCOMPANY = "/s/api/sites/{shortname}";
    private static final String REST_GET_LISTOFFERS = "/s/fr/itldev/koya/salesoffer/list?active={active}";
    private static final String REST_GET_PREFERENCES = "/s/fr/itldev/koya/company/preferences/{companyName}";
    private static final String REST_GET_SINGLEPREFERENCES = "/s/fr/itldev/koya/company/preferences/{companyName}?preferenceKey={preferenceKey}";
    private static final String REST_POST_PREFERENCES = "/s/fr/itldev/koya/company/preferences/{companyName}";

    private static final String REST_GET_PROPERTIES = "/s/fr/itldev/koya/company/properties/{companyName}";
    private static final String REST_POST_PROPERTIES = "/s/fr/itldev/koya/company/properties/{companyName}";

    /**
     * Méthode de creation d'une nouvelle société
     *
     * TODO la création d'un site compatible avec share nécesite des action coté
     * share pour avoir un dashboard valides :
     * share/WEB-INF/classes/alfresco/site-webscripts/org/alfresco/modules/create-site.post.json.js
     *
     * @param admin
     * @param c
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    @Override
    public Company create(User admin, Company c, String template) throws RestClientException, AlfrescoServiceException {

        //TODO verifications (nom + offrecom)
        //TODO encodage s.getTitre (url compliant) : pas urgent car il s'agit d'un POC non utilisé via cette librairie
        ItlAlfrescoServiceWrapper ret = admin.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_POST_ADDCOMPANY + "/" + c.getTitle() + "/" + c.getCurrentSaleOffer().getName() + "/" + template, ItlAlfrescoServiceWrapper.class);

        //TODO effectuer la requete sur un script coté share (si existant) afin de rendre dispo le site -- sitedata.newPreset()...
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK) && ret.getNbitems() == 1) {
            return (Company) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    @Override
    public List<Company> list(User admin) throws RestClientException, AlfrescoServiceException {//TODO search filter
        ItlAlfrescoServiceWrapper ret = admin.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LISTCOMPANY, ItlAlfrescoServiceWrapper.class);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
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
     * @param comapny
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    @Override
    public void delete(User admin, Company comapny) throws RestClientException, AlfrescoServiceException {
        admin.getRestTemplate().delete(getAlfrescoServerUrl() + REST_DEL_DELCOMPANY, comapny.getName());
    }

    @Override
    public List<SalesOffer> listSalesOffer(User admin, Boolean... active) throws AlfrescoServiceException {
        Boolean filterActif = false;
        if (active.length == 1) {
            filterActif = active[0];
        }

        ItlAlfrescoServiceWrapper ret = admin.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_LISTOFFERS, ItlAlfrescoServiceWrapper.class, filterActif);
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }

    }

    @Override
    public void loadCurrentSalesOffer(User admin, Company c) throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadSalesOfferHistory(User admin, Company c) throws AlfrescoServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Preferences getPreferences(User user, Company c) throws AlfrescoServiceException {
        Preferences prefs = user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_PREFERENCES, Preferences.class, c.getName());
        return prefs;
    }

    /**
     * Get single Preference identified by preferenceKey for a company
     *
     * @param user
     * @param c
     * @param preferenceKey
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public String getPreference(User user, Company c, String preferenceKey) throws AlfrescoServiceException {
        String pref = user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_SINGLEPREFERENCES, String.class, c.getName(), preferenceKey);

        if (pref == null) {
            return "";
        }
        //remove starting and finishing Quotes if exists    
        pref = pref.replaceAll("^\"|\"$", "");

        return pref;
    }

    @Override
    public void commitPreferences(User user, Company c, Preferences p) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().
                postForObject(getAlfrescoServerUrl() + REST_POST_PREFERENCES, p, ItlAlfrescoServiceWrapper.class, c.getName());
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    @Override
    public CompanyProperties getProperties(User user, Company c) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_PROPERTIES, ItlAlfrescoServiceWrapper.class, c.getName());
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (CompanyProperties) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    @Override
    public void commitProperties(User user, Company c, CompanyProperties p) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().
                postForObject(getAlfrescoServerUrl() + REST_POST_PROPERTIES, p, ItlAlfrescoServiceWrapper.class, c.getName());
        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }
}
