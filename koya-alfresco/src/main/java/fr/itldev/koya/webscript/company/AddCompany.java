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
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 * Add Company.
 *
 */
public class AddCompany extends AbstractWebScript {

    private CompanyService companyService;

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, Object> jsonParams = KoyaWebscript.getJsonMap(req);

        String name = (String) jsonParams.get(KoyaWebscript.WSCONST_TITLE);
        String spaceTemplate = (String) jsonParams.get(KoyaWebscript.WSCONST_SPACETEMPLATE);
        String salesOffer = (String) jsonParams.get(KoyaWebscript.WSCONST_SALESOFFER);

        String response;

        try {
            SalesOffer so = companyService.getSalesOffer(salesOffer);
            response = KoyaWebscript.getObjectAsJson(companyService.create(name, so, spaceTemplate));
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(response);
    }
}
