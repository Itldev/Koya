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
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.services.ShareService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import fr.itldev.koya.services.impl.util.CacheConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 *
 */
public class ShareServiceImpl extends AlfrescoRestService implements ShareService, InitializingBean {

    protected static final String REST_GET_SHAREDUSERS = "/s/fr/itldev/koya/share/sharedusers/{noderef}";
    protected static final String REST_GET_SHAREDITEMS = "/s/fr/itldev/koya/share/listusershares/{userName}/{companyName}";
    protected static final String REST_GET_ISSHAREDWITHCONSUMER = "/s/fr/itldev/koya/share/consumer/{noderef}";

    protected static final String REST_POST_SHARESINGLE = "/s/fr/itldev/koya/share/do";
    protected static final String REST_POST_UNSHARESINGLE = "/s/fr/itldev/koya/share/undo";

    private Cache<SubSpace, Boolean> nodeSharedWithConsumerCache;
    private CacheConfig nodeSharedWithConsumerCacheConfig;

    public void setNodeSharedWithConsumerCacheConfig(CacheConfig nodeSharedWithConsumerCacheConfig) {
        this.nodeSharedWithConsumerCacheConfig = nodeSharedWithConsumerCacheConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (nodeSharedWithConsumerCacheConfig == null) {
            nodeSharedWithConsumerCacheConfig = CacheConfig.noCache();
        }
        nodeSharedWithConsumerCacheConfig.debugLogConfig("nodeSharedWithConsumerCache");

        if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
            nodeSharedWithConsumerCache = CacheBuilder.newBuilder()
                    .maximumSize(nodeSharedWithConsumerCacheConfig.getMaxSize())
                    .expireAfterWrite(nodeSharedWithConsumerCacheConfig.getExpireAfterWriteSeconds(),
                            TimeUnit.SECONDS)
                    .build();
        }
    }

    @Override
    public void shareItem(User user, SecuredItem itemToShare, String sharedUserMail) throws AlfrescoServiceException {
        if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
            nodeSharedWithConsumerCache.invalidate(itemToShare);
        }

        Map<String, String> shareParams = new HashMap<>();
        shareParams.put("email", sharedUserMail);
        shareParams.put("nodeRef", itemToShare.getNodeRef());
        shareParams.put("koyaPermission", KoyaPermissionConsumer.CLIENT.toString());

        user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_SHARESINGLE,
                shareParams, String.class);
    }

    /**
     * Undo shares to sepcified user.
     *
     * @param user
     */
    @Override
    public void unShareItem(User user, SecuredItem itemToUnShare, String unsharedUserMail) throws AlfrescoServiceException {
        if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
            nodeSharedWithConsumerCache.invalidate(itemToUnShare);
        }

        Map<String, String> unshareParams = new HashMap<>();
        unshareParams.put("email", unsharedUserMail);
        unshareParams.put("nodeRef", itemToUnShare.getNodeRef());
        unshareParams.put("koyaPermission", KoyaPermissionConsumer.CLIENT.toString());

        user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_UNSHARESINGLE,
                unshareParams, String.class);
    }

    /**
     * Show Users who can publicly access to given element.
     *
     * @param user
     * @param item
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    @Override
    public List<User> sharedUsers(User user, SecuredItem item) throws AlfrescoServiceException {

        return fromJSON(new TypeReference<List<User>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SHAREDUSERS, String.class, item.getNodeRef()));

    }

    /**
     * Get all securedItems shared for specified user on a company.
     *
     * @param userLogged
     * @param userToGetShares
     * @param c
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<SecuredItem> sharedItems(User userLogged, User userToGetShares, Company c) throws AlfrescoServiceException {

        return fromJSON(new TypeReference<List<SecuredItem>>() {
        }, userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SHAREDITEMS,
                String.class, userToGetShares.getUserName(), c.getName()));

    }

    /**
     * Checks if item has any share with consumer permission
     *
     * @param item
     * @return
     */
    @Override
    public Boolean isSharedWithConsumerPermission(SubSpace item) {

        if (item == null) {
            return Boolean.FALSE;
        }
        Boolean shared;
        if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
            shared = nodeSharedWithConsumerCache.getIfPresent(item);
            if (shared != null) {
                return shared;
            }
        }
        shared = getTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_ISSHAREDWITHCONSUMER,
                Boolean.class, item.getNodeRef());
        if (nodeSharedWithConsumerCacheConfig.getEnabled()) {
            nodeSharedWithConsumerCache.put(item, shared);
        }
        return shared;

    }

}
