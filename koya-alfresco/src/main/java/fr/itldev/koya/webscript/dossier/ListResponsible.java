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

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SubSpaceCollaboratorsAclService;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * Get All persons in charge of specified dossier.
 *
 */
public class ListResponsible extends AbstractWebScript {

    private SubSpaceCollaboratorsAclService SubSpaceCollaboratorsAclService;
    private KoyaNodeService koyaNodeService;

    public void setSubSpaceCollaboratorsAclService(SubSpaceCollaboratorsAclService SubSpaceCollaboratorsAclService) {
        this.SubSpaceCollaboratorsAclService = SubSpaceCollaboratorsAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
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
        String response;
        try {
            response = KoyaWebscript.getObjectAsJson(
                    SubSpaceCollaboratorsAclService.listUsers(
                            koyaNodeService.nodeRef2SecuredItem(nodeRef), permissions));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }

}
