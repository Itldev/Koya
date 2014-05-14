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
package fr.itldev.koya.webscript.user;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;

/**
 * change user Own password
 *
 * TODO allow admin (or company admin) to change password without given previous one.
 *
 * TODO encrypt password with user key?
 *
 *
 */
public class ChangePassword extends KoyaWebscript {

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        String oldpwd = (String) urlParams.get("oldpwd");
        String newpwd = (String) urlParams.get("newpwd");
        userService.changePassword(oldpwd, newpwd);
        return wrapper;
    }
}
