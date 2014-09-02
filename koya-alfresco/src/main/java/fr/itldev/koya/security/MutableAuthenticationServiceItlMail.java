/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.security;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationServiceImpl;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class MutableAuthenticationServiceItlMail extends MutableAuthenticationServiceImpl implements MutableAuthenticationService {

    private Logger logger = Logger.getLogger(this.getClass());

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void authenticate(String userName, char[] password) throws AuthenticationException {

        final String uName = userName;

        User u = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< User>() {
            @Override
            public User doWork() throws Exception {

                try {
                    return userService.getUser(uName);
                } catch (KoyaServiceException kex) {
                    //no error if user id not found
                }
                return null;
            }
        });

        if (u != null) {
            super.authenticate(u.getUserName(), password);
        } else {
            super.authenticate(userName, password);
        }

    }

}
