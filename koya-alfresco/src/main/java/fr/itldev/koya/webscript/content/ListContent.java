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

package fr.itldev.koya.webscript.content;

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * Dossier Listing Webscript
 *
 *
 *
 */
public class ListContent extends AbstractWebScript {

    private static final Integer DEFAULT_MAX_DEPTH = 50;

    private KoyaContentService koyaContentService;

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);

        NodeRef parent = new NodeRef((String) urlParamsMap.get(KoyaWebscript.WSCONST_NODEREF));
        Integer depth;

        if (urlParamsMap.containsKey(KoyaWebscript.WSCONST_MAXDEPTH)) {
            depth = new Integer((String) urlParamsMap.get(KoyaWebscript.WSCONST_MAXDEPTH));
        } else {
            depth = DEFAULT_MAX_DEPTH;
        }
        Boolean onlyFolders = ((String) urlParamsMap.get(KoyaWebscript.WSCONST_ONLYFOLDERS)).equals("true");

        String response = KoyaWebscript.getObjectAsJson(koyaContentService.list(parent, depth, onlyFolders));
        res.setContentType("application/json");
        res.getWriter().write(response);
    }

}
