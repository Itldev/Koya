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

import fr.itldev.koya.model.Permissions;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserConnection;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.model.json.InviteWrapper;
import fr.itldev.koya.services.SecuService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import java.util.List;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

public class SecuServiceImpl extends AlfrescoRestService implements SecuService {

    private static final String REST_GET_AVAILABLEROLES = "/s/fr/itldev/koya/company/roles/{companyName}";
    private static final String REST_GET_USERROLE = "/s/fr/itldev/koya/user/role/{companyName}/{userName}";
    private static final String REST_GET_SETUSERROLE = "/s/fr/itldev/koya/user/setrole/{companyName}/{userName}/{roleName}";
    private static final String REST_GET_INVITEUSER = "/s/fr/itldev/koya/user/invite";
    private static final String REST_GET_LISTUSERCONNECTIONS = "/s/fr/itldev/koya/user/listconnect/{userName}?"
            + "companiesFilter={companiesFilter}&maxResults={maxResults}";
    private static final String REST_GET_REVOKEUSERACCESS = "/s/fr/itldev/koya/user/revoke/{companyName}/{userName}";
    private static final String REST_GET_ISCOMPANYMANAGER = "/s/fr/itldev/koya/company/ismanager/{companyName}";
    private static final String REST_GET_PERMISSIONS = "/s/fr/itldev/koya/global/secu/permissions/{nodeRef}";

    @Override
    public List<UserRole> listAvailableRoles(User userLogged, Company c) throws AlfrescoServiceException {
        return fromJSON(new TypeReference<List<UserRole>>() {
        }, userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_AVAILABLEROLES, String.class, c.getName()));
    }

    @Override
    public UserRole getUserRole(User userLogged, Company c, User userToGetRole) throws AlfrescoServiceException {
        return userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_USERROLE,
                UserRole.class, c.getName(), userToGetRole.getUserName());
    }

    @Override
    public void setUserRole(User userLogged, Company c, String userNameSetRole,
            String roleName) throws AlfrescoServiceException {
        userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SETUSERROLE,
                String.class, c.getName(),
                userNameSetRole, roleName);
    }

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
                getAlfrescoServerUrl() + REST_GET_INVITEUSER, iw,
                String.class);
    }

    /**
     *
     * @param userLogged
     * @param userToGetConnections
     * @param companyFilter
     * @param maxResults
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<UserConnection> listUserConnections(User userLogged,
            User userToGetConnections, List<Company> companyFilter,
            Integer maxResults) throws AlfrescoServiceException {

        String companiesFilter = "";
        String maxRes = "";

        if (companyFilter != null && companyFilter.size() > 0) {
            String sep = "";
            for (Company c : companyFilter) {
                companiesFilter += sep + c.getName();
                sep = ",";
            }
        }
        if (maxResults != null && maxResults > 0) {
            maxRes = maxResults.toString();
        }

        return fromJSON(new TypeReference<List<UserConnection>>() {
        }, userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_LISTUSERCONNECTIONS, String.class,
                userToGetConnections.getUserName(), companiesFilter, maxRes));

    }

    /**
     * revoke all user Acces on specified company.
     *
     * @param userLogged
     * @param c
     * @param u
     * @throws AlfrescoServiceException
     */
    @Override
    public void revokeAccess(User userLogged, Company c, User u) throws AlfrescoServiceException {
        userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_REVOKEUSERACCESS,
                String.class, c.getName(), u.getUserName());
    }

    /**
     * Checks if user logged is company manager.
     *
     * @param userLogged
     * @param c
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Boolean isCompanyManager(User userLogged, Company c) throws AlfrescoServiceException {

        return userLogged.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_ISCOMPANYMANAGER,
                        Boolean.class, c.getName(), c.getName());

    }

    /**
     * Get permissions on defined secured Item
     *
     * @param user
     * @param s
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public Permissions getPermissions(User user, SecuredItem s) throws AlfrescoServiceException {
        if (s == null) {
            return null;
        }
        return user.getRestTemplate().
                getForObject(getAlfrescoServerUrl() + REST_GET_PERMISSIONS,
                        Permissions.class, s.getNodeRef());

    }

}
