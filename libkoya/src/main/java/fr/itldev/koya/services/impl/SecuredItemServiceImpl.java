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

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.FavouriteService;
import fr.itldev.koya.services.SecuredItemService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.web.client.RestTemplate;

public class SecuredItemServiceImpl extends AlfrescoRestService implements SecuredItemService {

    protected static final String REST_GET_DELITEM = "/s/fr/itldev/koya/global/delete/{nodeRef}";
    protected static final String REST_GET_RENAMEITEM = "/s/fr/itldev/koya/global/rename/{nodeRef}";
    private static final String REST_GET_PARENTS = "/s/fr/itldev/koya/global/parents/{nodeRef}?nbAncestor={nbAncestor}";
    private static final String REST_GET_PARENTS_INFINITE = "/s/fr/itldev/koya/global/parents/{nodeRef}";

    private String alfrescoServerUrl;

    private RestTemplate template;

    private FavouriteService favouriteService;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public String getAlfrescoServerUrl() {
        return alfrescoServerUrl;
    }

    @Override
    public void setAlfrescoServerUrl(String alfrescoServerUrl) {
        this.alfrescoServerUrl = alfrescoServerUrl;
    }

    @Override
    public RestTemplate getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(RestTemplate template) {
        this.template = template;
    }

    public void setFavouriteService(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    // </editor-fold>
    /**
     * deletes item.
     *
     * @param user
     * @param securedItem
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    @Override
    public void delete(User user, SecuredItem securedItem) throws AlfrescoServiceException {
        /**
         * remove from favourites before delete if necessary
         */

        if (favouriteService.isFavourite(user, securedItem)) {
            /**
             * this action is realized in order to invalidate user favourites
             * client cache. 
             * 
             * Unset favourite is also done on server side before delete node
             * 
             */
            favouriteService.setFavouriteValue(user, securedItem, Boolean.FALSE);
        }

        user.getRestTemplate().getForObject(
                alfrescoServerUrl + REST_GET_DELITEM,
                String.class, securedItem.getNodeRef());
    }

    /**
     * Renames item.
     *
     * @param user
     * @param securedItem
     * @param newName
     */
    @Override
    public void rename(User user, SecuredItem securedItem, String newName) throws AlfrescoServiceException {
        Map<String, String> postParams = new HashMap();

        postParams.put("newName", newName);

        user.getRestTemplate().postForObject(
                alfrescoServerUrl + REST_GET_RENAMEITEM, postParams,
                String.class,
                securedItem.getNodeRef());
    }

    /**
     * Get Secured Item Parent if exists.
     *
     * @param user
     * @param securedItem
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public SecuredItem getParent(User user, SecuredItem securedItem)
            throws AlfrescoServiceException {

        List<SecuredItem> parents = fromJSON(new TypeReference<List<SecuredItem>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_PARENTS,
                String.class, securedItem.getNodeRef(), 1));

        if (parents.isEmpty()) {
            return null;
        } else {
            return parents.get(0);
        }

    }

    /**
     * Get Secured Item Parent if exists.
     *
     * @param user
     * @param securedItem
     * @return
     * @throws AlfrescoServiceException
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SecuredItem> getParents(User user, SecuredItem securedItem)
            throws AlfrescoServiceException {

        return fromJSON(new TypeReference<List<SecuredItem>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_PARENTS_INFINITE,
                String.class, securedItem.getNodeRef()));
    }

}
