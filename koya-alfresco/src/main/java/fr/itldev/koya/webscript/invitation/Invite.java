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
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.InviteWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Invite new or user user to a company with defined role.
 *
 *
 * TODO use alfresco standard invite webscript
 *
 *
 */
public class Invite extends AbstractWebScript {

    private Logger logger = Logger.getLogger(this.getClass());

    private CompanyAclService companyAclService;
    private KoyaNodeService koyaNodeService;

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String response;
        try {
            ObjectMapper mapper = new ObjectMapper();
            InviteWrapper iw = mapper.readValue(req.getContent().getReader(), InviteWrapper.class);
            NominatedInvitation invitation = (NominatedInvitation) companyAclService.inviteMember(koyaNodeService.companyBuilder(iw.getCompanyName()),
                    iw.getEmail(), SitePermission.valueOf(iw.getRoleName()));

            Map<String, String> jsonInvitation = new HashMap<>();
            jsonInvitation.put("inviteId", invitation.getInviteId());
            jsonInvitation.put("ticket", invitation.getTicket());

            response = KoyaWebscript.getObjectAsJson(jsonInvitation);
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
