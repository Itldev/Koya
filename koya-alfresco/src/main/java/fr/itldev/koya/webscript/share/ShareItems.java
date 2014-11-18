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
package fr.itldev.koya.webscript.share;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SubSpaceConsumersAclService;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.json.SharingWrapper;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Share secured items webscript.
 *
 */
public class ShareItems extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(this.getClass());

    private SubSpaceConsumersAclService subSpaceConsumersAclService;
    private KoyaNodeService koyaNodeService;

    public void setSubSpaceConsumersAclService(SubSpaceConsumersAclService subSpaceConsumersAclService) {
        this.subSpaceConsumersAclService = subSpaceConsumersAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    /**
     *
     * @param req
     * @param res
     * @throws IOException
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            SharingWrapper sw = mapper.readValue(req.getContent().getReader(), SharingWrapper.class);

            //extract shared elements
            for (String n : sw.getSharedNodeRefs()) {

                SecuredItem si = koyaNodeService.getSecuredItem(koyaNodeService.getNodeRef(n));

                if (SubSpace.class.isAssignableFrom(si.getClass())) {
                    SubSpace subSpace = (SubSpace) si;

                    for (String userMail : sw.getSharingUsersMails()) { //TODO limiter aux subspaces dans le wrapper unique
                        if (!sw.isResetSharings()) {
                            subSpaceConsumersAclService.shareSecuredItem(subSpace, userMail,
                                    KoyaPermissionConsumer.CLIENT,
                                    sw.getServerPath(), sw.getAcceptUrl(), sw.getRejectUrl(), false);
                        } else {
                            subSpaceConsumersAclService.unShareSecuredItem(subSpace, userMail,
                                    KoyaPermissionConsumer.CLIENT);
                        }
                    }
                }
            }

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write("");
    }

}
