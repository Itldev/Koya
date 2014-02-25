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

package fr.itldev.koya.webscript.salesoffer;

import fr.itldev.koya.alfservice.SalesOfferService;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.HashMap;
import java.util.Map;

/**
 * List available Sales Offer
 *
 */
public class ListOffer extends KoyaWebscript {

    private SalesOfferService salesOfferService;

    public void setSalesOfferService(SalesOfferService salesOfferService) {
        this.salesOfferService = salesOfferService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        //TODO implementation ... Stub impl
        Map<String, String> dataOffre1 = new HashMap<>();
        Map<String, String> dataOffre2 = new HashMap<>();

        dataOffre1.put("template", "tpl");
        dataOffre1.put("nodeRef", "xxxxnr1");
        dataOffre1.put("name", "offre1");
        dataOffre1.put("active", "true");

        dataOffre2.put("template", "tpl2");
        dataOffre2.put("nodeRef", "xxxxnr2");
        dataOffre2.put("name", "offre2");
        dataOffre2.put("active", "true");

        wrapper.addItem(new SalesOffer(dataOffre1));
        wrapper.addItem(new SalesOffer(dataOffre2));

        return wrapper;
    }

}
