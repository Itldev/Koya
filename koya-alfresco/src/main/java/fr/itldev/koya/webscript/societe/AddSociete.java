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

package fr.itldev.koya.webscript.societe;

import fr.itldev.koya.alfservice.SocieteService;
import fr.itldev.koya.model.impl.OffreCommerciale;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;

/**
 *
 * Webscript d'ajout de Société
 *
 * Il n'existe qu'au titre de PoC car il n'est utilisé que par les TU de la
 * librairie cliente afin de pouvoir tester les fonctionnalitées annexes.
 *
 *
 */
public class AddSociete extends KoyaWebscript {

    private static final String ARG_NOM = "nom";
    private static final String ARG_OFFRECOM = "offrecom";

    private SocieteService societeService;

    public void setSocieteService(SocieteService societeService) {
        this.societeService = societeService;
    }

    @Override
    public ItlAlfrescoServiceWrapper sdExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {

        String nomSociete = urlParams.get(ARG_NOM);
        /**
         *
         * TODO gestion de l'offre commerciale et de son existance
         *
         */
        OffreCommerciale oc = societeService.getOffreCommerciale(urlParams.get(ARG_OFFRECOM));

        wrapper.addItem(societeService.creer(nomSociete, oc));
        return wrapper;
    }

}
