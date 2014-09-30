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
package fr.itldev.koya.webscript.global;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.DiskSizeWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import static fr.itldev.koya.webscript.dossier.ListResponsible.permissions;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 *
 */
public class DiskSize extends AbstractWebScript {
    /*services*/

    private static final String URL_PARAM_NODEREF = "nodeRef";
    private KoyaNodeService koyaNodeService;

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    protected Map<String, String> getUrlParamsMap(WebScriptRequest req) {
        Map<String, String> params = new HashMap<>();
        params.putAll(req.getServiceMatch().getTemplateVars());
        for (String k : req.getParameterNames()) {
            params.put(k, req.getParameter(k));
        }
        return params;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        /**
         * TODO do not use wrapper
         */
        DiskSizeWrapper wrapper = new DiskSizeWrapper();

        String response;
        try {

            NodeRef nodeRef = koyaNodeService.getNodeRef((String) getUrlParamsMap(req).get(URL_PARAM_NODEREF));
            wrapper.setSize(koyaNodeService.getByteSize(nodeRef));
            response = KoyaWebscript.getObjectAsJson(wrapper);

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }

        res.setContentType("application/json");
        res.getWriter().write(response);
    }

}
