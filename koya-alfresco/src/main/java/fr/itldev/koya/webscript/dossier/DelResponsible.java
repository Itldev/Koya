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
package fr.itldev.koya.webscript.dossier;

import com.ibm.icu.impl.USerializedSet;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SubSpaceCollaboratorsAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * Del one or many persons in charge of specified dossier.
 *
 */
public class DelResponsible extends AbstractWebScript {

    private SubSpaceCollaboratorsAclService SubSpaceCollaboratorsAclService;
    private KoyaNodeService koyaNodeService;
    private UserService userService;

    public void setSubSpaceCollaboratorsAclService(SubSpaceCollaboratorsAclService SubSpaceCollaboratorsAclService) {
        this.SubSpaceCollaboratorsAclService = SubSpaceCollaboratorsAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public static List<KoyaPermission> permissions = Collections.unmodifiableList(new ArrayList() {
        {
            add(KoyaPermissionCollaborator.RESPONSIBLE);
        }
    });

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        NodeRef nodeRef = new NodeRef((String) urlParams.get(KoyaWebscript.WSCONST_NODEREF));
        String userNames = (String) urlParams.get(KoyaWebscript.WSCONST_USERNAMES);

        try {
            //seperate comma separedted usernames list
            String[] uNames = userNames.split(",");
            if (uNames.length == 1) {
                User u = userService.getUserByUsername(uNames[0]);
                SubSpaceCollaboratorsAclService.unShareSecuredItem(
                        (SubSpace) koyaNodeService.nodeRef2SecuredItem(nodeRef),
                        u.getEmail(), KoyaPermissionCollaborator.RESPONSIBLE);
                SubSpaceCollaboratorsAclService.unShareSecuredItem(
                        (SubSpace) koyaNodeService.nodeRef2SecuredItem(nodeRef),
                        u.getEmail(), KoyaPermissionCollaborator.MEMBER);
            } else {
                throw new WebScriptException("KoyaError bad mail resp length");
            }

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write("");
    }

}
