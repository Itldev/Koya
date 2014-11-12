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

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.InviteWrapper;
import fr.itldev.koya.services.InvitationService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.Map;
import org.codehaus.jackson.type.TypeReference;

public class InvitationServiceImpl extends AlfrescoRestService implements InvitationService {

    private static final String REST_GET_INVITATION = "/s/fr/itldev/koya/invitation/invitation/{userName}/{companyName}";
    private static final String REST_POST_INVITATION = "/s/fr/itldev/koya/invitation/sendmail";
    private static final String REST_POST_VALIDUSERBYINVITE = "/s/fr/itldev/koya/invitation/validate/{inviteId}/{inviteTicket}/{password}";
    private static final String REST_POST_INVITEUSER = "/s/fr/itldev/koya/invitation/invite";

    /**
     * Invite user identified by email on company with rolename granted.
     *
     * @param userLogged
     * @param c
     * @param userEmail
     * @param roleName
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     * @throws AlfrescoServiceException
     */
    @Override
    public void inviteUser(User userLogged, Company c, String userEmail, String roleName,
            String serverPath, String acceptUrl, String rejectUrl) throws AlfrescoServiceException {

        InviteWrapper iw = new InviteWrapper();
        iw.setCompanyName(c.getName());
        iw.setEmail(userEmail);
        iw.setRoleName(roleName);
        iw.setAcceptUrl(acceptUrl);
        iw.setServerPath(serverPath);
        iw.setRejectUrl(rejectUrl);

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
        getTemplate().postForObject(getAlfrescoServerUrl() + REST_POST_VALIDUSERBYINVITE,
                user, String.class, inviteId, inviteTicket, user.getPassword());

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

        return fromJSON(new TypeReference<Map>() {
        }, user.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_INVITATION,
                String.class, userToGetInvitaion.getUserName(), c.getName()
        ));

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
