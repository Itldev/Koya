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
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.IOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * Content Adding webscript.
 *
 *
 */
public class AddContent extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(AddContent.class);
    private KoyaContentService koyaContentService;

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    /**
     *
     * @param req
     * @param res
     * @throws IOException
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        ItlAlfrescoServiceWrapper wrapper = new ItlAlfrescoServiceWrapper();

        try {
            ObjectMapper mapper = new ObjectMapper();

            try {
                Directory dir = mapper.readValue(req.getContent().getReader(), Directory.class);

                NodeRef parent = new NodeRef(req.getServiceMatch().getTemplateVars().get("parentNodeRef"));
                Directory dirCreated = koyaContentService.createDir(dir.getName(), parent);
                wrapper.addItem(dirCreated);
                wrapper.setStatusOK();
            } catch (IOException ex) {
                wrapper.setStatusFail(ex.toString());
                wrapper.setErrorCode(KoyaErrorCodes.CONTENT_CREATION_INVALID_TYPE);
            }
        } catch (Exception ex) {
            wrapper.setStatusFail(ex.toString());
            wrapper.setErrorCode(KoyaErrorCodes.UNHANDLED);
        }

        res.setContentType("application/json");

        res.getWriter().write(wrapper.getAsJSON());
        logger.trace(wrapper.getAsJSON());
    }

}
