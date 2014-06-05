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
package fr.itldev.koya.webscript.content;

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

/**
 *
 * Dossier Listing Webscript
 *
 *
 *
 */
public class ListContent extends KoyaWebscript {

    private static final String URL_PARAM_MAXDEPTH = "maxdepth";
    private static final Integer DEFAULT_MAX_DEPTH = 50;

    private final Logger logger = Logger.getLogger(ListContent.class);

    /*services*/
    private KoyaContentService koyaContentService;

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        NodeRef parent = new NodeRef((String) jsonPostMap.get("nodeRef"));

        Integer depth;

        if (urlParams.containsKey(URL_PARAM_MAXDEPTH)) {
            depth = new Integer((String) urlParams.get(URL_PARAM_MAXDEPTH));
        } else {
            depth = DEFAULT_MAX_DEPTH;
        }

        Boolean onlyFolders = ((String) urlParams.get(WSCONST_ONLYFOLDERS)).equals("true");

        wrapper.addItems(koyaContentService.list(parent, depth, onlyFolders));
        return wrapper;
    }

}
