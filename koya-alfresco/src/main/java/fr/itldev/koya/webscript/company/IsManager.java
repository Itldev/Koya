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
package fr.itldev.koya.webscript.company;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Checks if currently logged user has manager role on specified company.
 *
 */
public class IsManager extends AbstractWebScript {

    private CompanyService companyService;

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        String companyName = (String) urlParams.get(KoyaWebscript.WSCONST_COMPANYNAME);

        String response;

        try {
            response = KoyaWebscript.getObjectAsJson(companyService.isCompanyManager(companyName));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }

        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
