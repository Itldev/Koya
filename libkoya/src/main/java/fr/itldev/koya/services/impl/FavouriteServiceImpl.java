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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.FavouriteService;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import fr.itldev.koya.services.impl.util.CacheConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class FavouriteServiceImpl extends AlfrescoRestService implements FavouriteService, InitializingBean {

    private static final String REST_GET_LISTFAVOURITES = "/s/fr/itldev/koya/favourites/list";
    private static final String REST_POST_FAVOURITE_STATUS = "/s/fr/itldev/koya/favourites/set";

    private Cache<User, List<SecuredItem>> userFavouritesCache;
    private CacheConfig userFavouritesCacheConfig;

    public void setUserFavouritesCacheConfig(CacheConfig userFavouritesCacheConfig) {
        this.userFavouritesCacheConfig = userFavouritesCacheConfig;
    }

    @Autowired
    UserService userService;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (userFavouritesCacheConfig == null) {
            userFavouritesCacheConfig = CacheConfig.noCache();
        }
        userFavouritesCacheConfig.debugLogConfig("userFavouritesCache");

        if (userFavouritesCacheConfig.getEnabled()) {
            userFavouritesCache = CacheBuilder.newBuilder()
                    .maximumSize(userFavouritesCacheConfig.getMaxSize())
                    .expireAfterWrite(userFavouritesCacheConfig.getExpireAfterWriteSeconds(),
                            TimeUnit.SECONDS)
                    .build();
        }

    }

    @Override
    public List<SecuredItem> getFavourites(User user) throws AlfrescoServiceException {

        if (user == null) {
            return new ArrayList<>();
        }
        List<SecuredItem> userFavourites;
        if (userFavouritesCacheConfig.getEnabled()) {

            userFavourites = userFavouritesCache.getIfPresent(user);
            if (userFavourites != null) {
                return userFavourites;
            }
        }

        userFavourites = fromJSON(new TypeReference<List<SecuredItem>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_LISTFAVOURITES, String.class));
        if (userFavouritesCacheConfig.getEnabled()) {
            userFavouritesCache.put(user, userFavourites);
        }
        return userFavourites;
    }

    @Override
    public Boolean setFavouriteValue(User user, SecuredItem item, Boolean favouriteValue) throws AlfrescoServiceException {
        if (userFavouritesCacheConfig.getEnabled()) {
            userFavouritesCache.invalidate(user);
        }
        Map<String, Object> postParams = new HashMap<>();
        postParams.put("nodeRef", item.getNodeRef());
        postParams.put("status", favouriteValue);

        Boolean status = fromJSON(new TypeReference<Boolean>() {
        }, user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_FAVOURITE_STATUS, postParams, String.class));
        //Automaticly reload user's preferences
        userService.loadPreferences(user);
        return status;
    }

    @Override
    public Boolean isFavourite(User user, SecuredItem item) throws AlfrescoServiceException {
        return getFavourites(user).contains(item);
    }

}
