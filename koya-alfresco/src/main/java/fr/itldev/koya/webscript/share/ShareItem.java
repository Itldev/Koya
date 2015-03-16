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
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Share secured items webscript.
 *
 */
public class ShareItem extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(this.getClass());

    private SubSpaceConsumersAclService subSpaceConsumersAclService;
    private KoyaNodeService koyaNodeService;

    public void setSubSpaceConsumersAclService(
            SubSpaceConsumersAclService subSpaceConsumersAclService) {
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
    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        Map<String, Object> params = KoyaWebscript.getJsonMap(req);
        try {
            NodeRef n = koyaNodeService.getNodeRef((String) params
                    .get(KoyaWebscript.WSCONST_NODEREF));

            SubSpace s;
            SecuredItem si = koyaNodeService.getSecuredItem(n);
            if (SubSpace.class.isAssignableFrom(si.getClass())) {
                s = (SubSpace) si;
            } else {
                throw new KoyaServiceException(
                        KoyaErrorCodes.INVALID_SECUREDITEM_NODEREF);
            }

            subSpaceConsumersAclService.shareSecuredItem(
                    (SubSpace) koyaNodeService.getSecuredItem(n),
                    (String) params.get(KoyaWebscript.WSCONST_EMAIL),
                    KoyaPermission.valueOf((String) params
                            .get(KoyaWebscript.WSCONST_KOYAPERMISSION)), false);

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : "
                    + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write("");
    }

}
