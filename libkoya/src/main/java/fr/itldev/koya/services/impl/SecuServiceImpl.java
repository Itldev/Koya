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
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.impl.UserRole;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.SecuService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public class SecuServiceImpl extends AlfrescoRestService implements SecuService {

    private static final String REST_GET_AVAILABLEROLES = "/s/fr/itldev/koya/company/roles/{companyName}";
    private static final String REST_GET_USERROLE = "/s/fr/itldev/koya/user/role/{companyName}/{userName}";
    private static final String REST_GET_SETUSERROLE = "/s/fr/itldev/koya/user/setrole/{companyName}/{userName}/{roleName}";

    @Override
    public List<User> usersGrantedDirect(User user, SecuredItem item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<User> usersGrantedInherit(User user, SecuredItem item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<UserRole> listAvailableRoles(User userLogged, Company c) throws AlfrescoServiceException {

        ItlAlfrescoServiceWrapper ret = userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_AVAILABLEROLES, ItlAlfrescoServiceWrapper.class, c.getName());

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    @Override
    public UserRole getUserRole(User userLogged, Company c, User userToGetRole) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_USERROLE,
                ItlAlfrescoServiceWrapper.class, c.getName(), userToGetRole.getUserName());

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return (UserRole) ret.getItems().get(0);
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    @Override
    public void setUserRole(User userLogged, Company c, String userNameSetRole, String roleName) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SETUSERROLE,
                ItlAlfrescoServiceWrapper.class, c.getName(),
                userNameSetRole, roleName);

        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

}
