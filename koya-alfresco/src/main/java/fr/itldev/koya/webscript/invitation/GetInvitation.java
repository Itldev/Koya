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
package fr.itldev.koya.webscript.invitation;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.invitation.Invitation;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Checks if user has a pending invitaion for specified company
 *
 *
 */
public class GetInvitation extends AbstractWebScript {

    private KoyaNodeService koyaNodeService;
    private UserService userService;
    private CompanyAclService companyAclService;

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

        String userName = (String) urlParams.get(KoyaWebscript.WSCONST_USERNAME);
        String companyName = (String) urlParams.get(KoyaWebscript.WSCONST_COMPANYNAME);
        String response = "";

        try {
            User u = userService.getUserByUsername(userName);
            Company c = koyaNodeService.companyBuilder(companyName);

            List<Invitation> invitations = companyAclService.getPendingInvite(c.getName(), null, u.getUserName());
            if (invitations != null && invitations.size() == 1) {

                Map<String, String> invit = new HashMap<>();
                invit.put("inviteId", invitations.get(0).getInviteId());
             //   invit.put("createdAt", invitations.get(0).getCreatedAt());
                //TODO number of sended mail for this invitation

                response = KoyaWebscript.getObjectAsJson(invit);
            }

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }

}
