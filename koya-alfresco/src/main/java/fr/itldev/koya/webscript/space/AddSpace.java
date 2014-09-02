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

package fr.itldev.koya.webscript.space;

import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 */
public class AddSpace extends AbstractWebScript {

    private SpaceService spaceService;

    public void setSpaceService(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);
        Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);
        String name = (String) jsonPostMap.get(KoyaWebscript.WSCONST_NAME);

        NodeRef parent = new NodeRef((String) urlParamsMap.get(KoyaWebscript.WSCONST_PARENTNODEREF));

        String response;

        try {
            response = KoyaWebscript.getObjectAsJson(spaceService.create(name, parent, null));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
