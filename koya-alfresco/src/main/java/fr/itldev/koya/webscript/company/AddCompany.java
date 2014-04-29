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
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;

/**
 *
 * Add Company.
 *
 * Il n'existe qu'au titre de PoC car il n'est utilisé que par les TU de la
 * librairie cliente afin de pouvoir tester les fonctionnalitées annexes.
 *
 *
 */
public class AddCompany extends KoyaWebscript {

    private static final String ARG_NAME = "name";
    private static final String ARG_TEMPLATE = "template";
    private static final String ARG_SALESOFFER = "salesoffer";

    private CompanyService companyService;

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {

        String name = urlParams.get(ARG_NAME);
        String template = urlParams.get(ARG_TEMPLATE);
        /**
         *
         * TODO gestion de l'offre commerciale et de son existance
         *
         */
        SalesOffer so = companyService.getSalesOffer(urlParams.get(ARG_SALESOFFER));

        wrapper.addItem(companyService.create(name, so, template));
        return wrapper;
    }

}
