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

package fr.itldev.koya.webscript.offrecommerciale;

import fr.itldev.koya.alfservice.OffreCommercialeService;
import fr.itldev.koya.model.impl.OffreCommerciale;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.HashMap;
import java.util.Map;

/**
 * Liste les Offres Commerciales Disponibles pour les Sociétés
 *
 */
public class ListOffres extends KoyaWebscript {

    private OffreCommercialeService offreCommercialeService;

    public void setOffreCommercialeService(OffreCommercialeService offreCommercialeService) {
        this.offreCommercialeService = offreCommercialeService;
    }

    @Override
    public ItlAlfrescoServiceWrapper sdExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        //TODO implementation concrete ... eci est un bouchon
        Map<String, String> dataOffre1 = new HashMap<>();
        Map<String, String> dataOffre2 = new HashMap<>();

        dataOffre1.put("template", "tpl");
        dataOffre1.put("nodeRef", "xxxxnr1");
        dataOffre1.put("nom", "offre1");
        dataOffre1.put("active", "true");

        dataOffre2.put("template", "tpl2");
        dataOffre2.put("nodeRef", "xxxxnr2");
        dataOffre2.put("nom", "offre2");
        dataOffre2.put("active", "true");

        wrapper.addItem(new OffreCommerciale(dataOffre1));
        wrapper.addItem(new OffreCommerciale(dataOffre2));

        return wrapper;
    }

}
