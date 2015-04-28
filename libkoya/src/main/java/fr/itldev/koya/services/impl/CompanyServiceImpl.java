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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.client.RestClientException;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.CompanyProperties;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.CompanyService;
import fr.itldev.koya.services.cache.CacheManager;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class CompanyServiceImpl extends AlfrescoRestService implements CompanyService {

    private static final String REST_POST_ADDCOMPANY = "/s/fr/itldev/koya/company/add";
    private static final String REST_GET_LISTCOMPANY = "/s/fr/itldev/koya/company/list.json";
    private static final String REST_GET_COMPANY = "/s/fr/itldev/koya/company/get/{companyName}";

    private static final String REST_GET_LISTMEMBERS = "/s/fr/itldev/koya/company/members/{companyName}";
    private static final String REST_GET_LISTMEMBERS_ROLEFILTER = "/s/fr/itldev/koya/company/members/{companyName}/{roleFilter}";

    private static final String REST_DEL_DELCOMPANY = "/s/api/sites/{shortname}";
    private static final String REST_GET_LISTOFFERS = "/s/fr/itldev/koya/salesoffer/list?active={active}";
    private static final String REST_GET_PREFERENCES = "/s/fr/itldev/koya/company/preferences/{companyName}";
    private static final String REST_POST_PREFERENCES = "/s/fr/itldev/koya/company/preferences/{companyName}";

    private static final String REST_GET_PROPERTIES = "/s/fr/itldev/koya/company/properties/{companyName}";
    private static final String REST_POST_PROPERTIES = "/s/fr/itldev/koya/company/properties/{companyName}";

    private CacheManager cacheManager;
    
    
    public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
     * Company creation
     *
     * @param admin
     * @param title
     * @param salesOfferName
     * @param spaceTemplate
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    @Override
    public Company create(User admin, String title, String salesOfferName, String spaceTemplate) throws RestClientException, AlfrescoServiceException {

        Map<String, String> params = new HashMap<>();

        params.put("title", title);
        params.put("salesoffer", salesOfferName);
        params.put("spacetemplate", spaceTemplate);

        //TODO verifications (name + offrecom)
        return fromJSON(new TypeReference<Company>() {
        }, admin.getRestTemplate().postForObject(getAlfrescoServerUrl()
                + REST_POST_ADDCOMPANY, params, String.class));
    }

    @Override
    public List<Company> list(User admin) throws RestClientException, AlfrescoServiceException {//TODO search filter
        return fromJSON(new TypeReference<List<Company>>() {
        }, admin.getRestTemplate()
                .getForObject(getAlfrescoServerUrl() + REST_GET_LISTCOMPANY, String.class));
    }
    
    
    public Company get(User user, String shortName) throws RestClientException, AlfrescoServiceException{
     
        return  user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_COMPANY,
                        Company.class, shortName);
    }


    /**
     * Company removing method
     *
     * Never used.
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

    /**
     * List CompanyMembers by type filter setted.
     *
     * @param userLogged
     * @param company
     * @param rolesFilter
     * @return
     */
    @Override
    public List<User> listMembers(User userLogged, Company company, List<String> rolesFilter)
            throws RestClientException, AlfrescoServiceException {

        if (rolesFilter == null) {
            return fromJSON(new TypeReference<List<User>>() {
            }, userLogged.getRestTemplate().getForObject(
                    getAlfrescoServerUrl() + REST_GET_LISTMEMBERS, String.class, company.getName()));
        } else {

            String filterString = "";
            String sep = "";
            for (String role : rolesFilter) {
                filterString += sep + role;
                sep = ",";
            }

            return fromJSON(new TypeReference<List<User>>() {
            }, userLogged.getRestTemplate().getForObject(
                    getAlfrescoServerUrl() + REST_GET_LISTMEMBERS_ROLEFILTER, String.class, company.getName(), filterString));
        }

    }

    @Override
    public List<SalesOffer> listSalesOffer(User admin, Boolean... active) throws AlfrescoServiceException {
        Boolean filterActif = false;
        if (active.length == 1) {
            filterActif = active[0];
        }
        return fromJSON(new TypeReference<List<SalesOffer>>() {
        }, admin.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_LISTOFFERS, String.class, filterActif));

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

        if (c == null) {
            return null;
        }
        Preferences p = cacheManager.getCompanyPreferences(c);           
		if (p != null) {
			return p;
		}

        p = fromJSON(new TypeReference<Preferences>() {
        }, user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_PREFERENCES, String.class, c.getName()));
        cacheManager.setCompanyPreferences(c, p);
        return p;
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

        Object pref = getPreferences(user, c).get(preferenceKey);

        if (pref == null) {
            return "";
        }
        return pref.toString();
    }

    @Override
    public void commitPreferences(User user, Company c, Preferences p) throws AlfrescoServiceException {
        user.getRestTemplate().
                postForObject(getAlfrescoServerUrl() + REST_POST_PREFERENCES,
                        p, String.class, c.getName());

    }

    @Override
    public CompanyProperties getProperties(User user, Company c) throws AlfrescoServiceException {
        return fromJSON(new TypeReference<CompanyProperties>() {
        }, user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_PROPERTIES,
                        String.class, c.getName()));
    }

    @Override
    public void commitProperties(User user, Company c, CompanyProperties p) throws AlfrescoServiceException {
        user.getRestTemplate().
                postForObject(getAlfrescoServerUrl() + REST_POST_PROPERTIES, p, String.class, c.getName());
    }
}
