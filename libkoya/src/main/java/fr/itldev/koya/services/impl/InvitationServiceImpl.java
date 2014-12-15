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

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.InviteWrapper;
import fr.itldev.koya.services.InvitationService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import fr.itldev.koya.services.impl.util.CacheConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.InitializingBean;

public class InvitationServiceImpl extends AlfrescoRestService
        implements InvitationService, InitializingBean {

    private static final String REST_GET_INVITATION = "/s/fr/itldev/koya/invitation/invitation/{userName}/{companyName}";
    private static final String REST_POST_INVITATION = "/s/fr/itldev/koya/invitation/sendmail";
    private static final String REST_POST_VALIDUSERBYINVITE = "/s/fr/itldev/koya/invitation/validate";
    private static final String REST_POST_INVITEUSER = "/s/fr/itldev/koya/invitation/invite";

    private Cache<String, Map<String, String>> invitationsCache;
    private CacheConfig invitationsCacheConfig;

    public void setInvitationsCacheConfig(CacheConfig invitationsCacheConfig) {
        this.invitationsCacheConfig = invitationsCacheConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (invitationsCacheConfig == null) {
            invitationsCacheConfig = CacheConfig.noCache();
        }
        invitationsCacheConfig.debugLogConfig("invitationsCache");

        if (invitationsCacheConfig.getEnabled()) {
            invitationsCache = CacheBuilder.newBuilder()
                    .maximumSize(invitationsCacheConfig.getMaxSize())
                    .expireAfterWrite(invitationsCacheConfig.getExpireAfterWriteSeconds(),
                            TimeUnit.SECONDS)
                    .build();
        }
    }

    /**
     * Invite user identified by email on company with rolename granted.
     *
     * @param userLogged
     * @param c
     * @param userEmail
     * @param roleName
     * @throws AlfrescoServiceException
     */
    @Override
    public void inviteUser(User userLogged, Company c, String userEmail,
            String roleName) throws AlfrescoServiceException {

        if (invitationsCacheConfig.getEnabled()) {
            invitationsCache.invalidate(userEmail);
        }

        InviteWrapper iw = new InviteWrapper();
        iw.setCompanyName(c.getName());
        iw.setEmail(userEmail);
        iw.setRoleName(roleName);

        userLogged.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_INVITEUSER, iw,
                String.class);
    }

    /**
     * Validate invitation giving user modifications;
     *
     * @param user
     * @param inviteId
     * @param inviteTicket
     * @throws AlfrescoServiceException
     */
    @Override
    public void validateInvitation(User user, String inviteId, String inviteTicket) throws AlfrescoServiceException {

        Map<String, String> params = new HashMap<>();
        params.put("inviteId", inviteId);
        params.put("inviteTicket", inviteTicket);
        params.put("password", user.getPassword());
        params.put("lastName", user.getName());
        params.put("firstName", user.getFirstName());

        User u = fromJSON(new TypeReference<User>() {
        }, getTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_VALIDUSERBYINVITE,
                params, String.class));

        if (invitationsCacheConfig.getEnabled()) {
            invitationsCache.invalidate(u.getEmail());
        }
    }

    /**
     * Get user's invitation on company if exists.
     *
     * @param user
     * @param c
     * @param userToGetInvitaion
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Map<String, String> getInvitation(User user, Company c, User userToGetInvitaion) throws AlfrescoServiceException {
        Map m;
        if (invitationsCacheConfig.getEnabled()) {
            m = invitationsCache.getIfPresent(userToGetInvitaion.getEmail());
            if (m.isEmpty()) {
                return null;
            } else {
                return m;
            }
        }

        m = fromJSON(new TypeReference<Map>() {
        }, user.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_INVITATION,
                String.class, userToGetInvitaion.getUserName(), c.getName()
        ));

        if (invitationsCacheConfig.getEnabled()) {
            Map<String, String> value;
            if (m == null) {
                value = new HashMap<>();
            } else {
                value = m;
            }
            invitationsCache.put(userToGetInvitaion.getEmail(), value);
        }
        return m;

    }

    /**
     * Send invitation mail again to invitee based on invitationId
     *
     * @param user
     * @param inviteId
     * @throws AlfrescoServiceException
     */
    @Override
    public void reSendInviteMail(User user, String inviteId) throws AlfrescoServiceException {
        user.getRestTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_INVITATION, inviteId, String.class);
    }
}
