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
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.log4j.Logger;

/**
 *
 * Content Adding webscript.
 *
 *
 */
public class AddContent extends KoyaWebscript {

    private static final String ARG_TYPECLASS = "typeClass";

    //TODO a faire avec les noms issus de la classe mais pour le moment elle est unknown ???
    private static final String TYPECLASS_DIRECTORY = "Directory";

    private final Logger logger = Logger.getLogger(AddContent.class);
    private KoyaContentService koyaContentService;
    private AuthenticationService authenticationService;

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {

        NodeRef parent = new NodeRef((String) jsonPostMap.get("parentNodeRef"));
        String name = (String) jsonPostMap.get("name");

        Content c = null;

        if (TYPECLASS_DIRECTORY.equals(urlParams.get(ARG_TYPECLASS))) {
            c = koyaContentService.createDir(name, parent,authenticationService.getCurrentUserName());
        } else {
            throw new Exception("Invalid Content type : '" + urlParams.get(ARG_TYPECLASS) + "'");
        }

        wrapper.addItem(c);
        return wrapper;
    }

}
