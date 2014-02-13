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
import fr.itldev.koya.model.Contenu;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

/**
 *
 * Webscript d'ajout de Contenu
 *
 * Le contenu est ajouté en fonction du type passé en paramètre.
 *
 */
public class AddContenu extends KoyaWebscript {

    private static final String ARG_TYPECLASS = "typeClass";

    //TODO a faire avec les noms issus de la classe mais pour le moment elle est unknown ???
    private static final String TYPECLASS_REPERTOIRE = "Repertoire";

    private final Logger logger = Logger.getLogger(AddContenu.class);
    /*services*/
    private ContenuService contenuService;

    public void setContenuService(ContenuService contenuService) {
        this.contenuService = contenuService;
    }

    @Override
    public ItlAlfrescoServiceWrapper sdExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {

        NodeRef parent = new NodeRef((String) jsonPostMap.get("parentNodeRef"));
        String nom = (String) jsonPostMap.get("nom");

        Contenu c = null;

        if (TYPECLASS_REPERTOIRE.equals(urlParams.get(ARG_TYPECLASS))) {
            c = contenuService.creerRepertoire(nom, parent);
        } else {
            throw new Exception("Type de contenu invalide");
        }

        wrapper.addItem(c);
        return wrapper;
    }

}
