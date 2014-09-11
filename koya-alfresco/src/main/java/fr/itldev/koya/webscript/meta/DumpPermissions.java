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
package fr.itldev.koya.webscript.meta;

import fr.itldev.koya.alfservice.DebuggerService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * Display all given company suspaces permissions as log
 *
 *
 */
public class DumpPermissions extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(this.getClass());

    private DebuggerService debuggerService;
    private SpaceService spaceService;

    public void setDebuggerService(DebuggerService debuggerService) {
        this.debuggerService = debuggerService;
    }

    public void setSpaceService(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @Override

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        String companyName = (String) urlParams.get(KoyaWebscript.WSCONST_COMPANYNAME);
        try {
            
            debuggerService.dumpUsersPermissions(companyName);
            for (Space s : spaceService.list(companyName, Integer.MAX_VALUE)) {
                debuggerService.dumpSubSpacesPermissions(s);
                logger.debug("---------------------------------------------------");
            }
        } catch (KoyaServiceException ex) {
            logger.error(ex.toString());
        }
        res.setContentType("application/json");
        res.getWriter().write("");
    }

}
