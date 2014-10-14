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
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Count direct Children of given parent node
 *
 */
public class CountChildren extends AbstractWebScript {

    private KoyaNodeService koyaNodeService;

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

        String response;

        try {
            NodeRef parent = koyaNodeService.getNodeRef((String) urlParams.get(KoyaWebscript.WSCONST_PARENTNODEREF));
            ObjectMapper mapper = new ObjectMapper();
            Set qNameFilter = mapper.readValue(req.getContent().getReader(), new TypeReference<Set>() {
            });

            /**
             * Transform Set Of Map<String,String> as Set<Qname>
             */
            CollectionUtils.transform(qNameFilter, new Transformer() {
                @Override
                public Object transform(Object input) {
                    Map<String, String> m = (Map<String, String>) input;
                    return QName.createQName(m.get("namespaceURI"), m.get("localName"));
                }
            });

            response = KoyaWebscript.getObjectAsJson(koyaNodeService.countChildren(parent, qNameFilter));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
