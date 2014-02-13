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

package fr.itldev.koya.webscript.contenu;

import fr.itldev.koya.alfservice.ContenuService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

/**
 *
 * Webscript de listing du contenu d'un dossier
 *
 *
 * TODO option de récursivité qui permet d'obtenir tous les contenu ou le
 * contenu descendant direct
 *
 *
 * Dans la pratique, ce WS rique d'etre peu utilisée car ces appels serons
 * directement fait en JS depuis l'interface vers les services natifs alfresco
 *
 */
public class ListContenu extends KoyaWebscript {

    private final Logger logger = Logger.getLogger(ListContenu.class);

    /*services*/
    private ContenuService contenuService;

    public void setContenuService(ContenuService contenuService) {
        this.contenuService = contenuService;
    }

    @Override
    public ItlAlfrescoServiceWrapper sdExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        NodeRef parent = new NodeRef((String) jsonPostMap.get("nodeRef"));
        wrapper.addItems(contenuService.lister(parent));
        return wrapper;
    }

}
