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

import fr.itldev.koya.alfservice.KoyaNotificationService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * get the notification status of the user
 *
 */
public class NotificationStatus extends AbstractWebScript {

    private KoyaNotificationService koyaNotificationService;
    private UserService userService;

    public void setKoyaNotificationService(KoyaNotificationService koyaNotificationService) {
        this.koyaNotificationService = koyaNotificationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        //passer obligatoirement la cible de notifs
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        String strEnable = (String) urlParams.get(KoyaWebscript.WSCONST_ENABLE);
        String username = (String) urlParams.get(KoyaWebscript.WSCONST_USERNAME);
        String response;

        /**
         * TODO process secureditem parameter
         */
        SecuredItem node = null;
        try {
            User u = userService.getUserByUsername(username);
            if (strEnable != null) {
                if (Boolean.valueOf(strEnable)) {
                    koyaNotificationService.addNotification(u, node);
                } else {
                    koyaNotificationService.removeNotification(u, node);
                }
            }
            response = KoyaWebscript.getObjectAsJson(koyaNotificationService.isUserNotified(u, node));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
