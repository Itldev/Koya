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

import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.CompanyProperties;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClientException;

public interface CompanyService extends AlfrescoService {

    /**
     * Company creation
     *
     * @param admin
     * @param title
     * @param salesOfferName
     * @param template
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    Company create(User admin, String title, String salesOfferName, String template) throws RestClientException, AlfrescoServiceException;

    /**
     *
     * @param admin
     * @return
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    List<Company> list(User admin) throws RestClientException, AlfrescoServiceException;

    /**
     * Company deletion
     *
     * @param admin
     * @param company
     * @throws RestClientException
     * @throws AlfrescoServiceException
     */
    void delete(User admin, Company company) throws RestClientException, AlfrescoServiceException;

    /**
     * List CompanyMembers by type filter setted.
     *
     * @param userLogged
     * @param company
     * @param rolesFilter
     * @return
     */
    List<User> listMembers(User userLogged, Company company, List<String> rolesFilter) throws RestClientException, AlfrescoServiceException;

    /**
     * List Sales Offers
     *
     * @param admin
     * @param active
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    List<SalesOffer> listSalesOffer(User admin, Boolean... active) throws AlfrescoServiceException;

    /**
     * Change current sales offer for a company
     *
     * @param admin
     * @param s
     * @throws AlfrescoServiceException
     */
    void loadCurrentSalesOffer(User admin, Company s) throws AlfrescoServiceException;

    /**
     * Loads Sales Offer History
     *
     * @param admin
     * @param c
     * @throws AlfrescoServiceException
     */
    void loadSalesOfferHistory(User admin, Company c) throws AlfrescoServiceException;

    /**
     * get All preferences for a company.
     *
     * Unsaved local preferences will be erased.
     *
     * @param user
     * @param c
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    public Preferences getPreferences(User user, Company c) throws AlfrescoServiceException;

    /**
     * Get single Preference identified by preferenceKey for a company.
     *
     * @param user
     * @param c
     * @param preferenceKey
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    public String getPreference(User user, Company c, String preferenceKey) throws AlfrescoServiceException;

    /**
     * Commit localy modified preferences.
     *
     * @param user
     * @param c
     * @param p
     * @throws AlfrescoServiceException
     */
    public void commitPreferences(User user, Company c, Preferences p) throws AlfrescoServiceException;

    /**
     * get properties for a company.
     *
     *
     * @param user
     * @param c
     * @return
     * @throws AlfrescoServiceException
     */
    public CompanyProperties getProperties(User user, Company c) throws AlfrescoServiceException;

    /**
     * Commit localy modified properties.
     *
     * @param user
     * @param c
     * @param p
     * @throws AlfrescoServiceException
     */
    public void commitProperties(User user, Company c, CompanyProperties p) throws AlfrescoServiceException;
}
