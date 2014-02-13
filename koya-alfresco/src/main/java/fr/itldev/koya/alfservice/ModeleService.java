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

package fr.itldev.koya.alfservice;

import fr.itldev.koya.model.impl.Societe;
import org.alfresco.service.cmr.repository.NodeService;

/**
 *
 */
public class ModeleService {

    private NodeService nodeService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    // </editor-fold>

    /**
     * Méthode de copie du template de société afin de rendre un site existant
     * utilisable comme une société par le client.
     *
     * Par défaut, tous les sites sont des sociétés : si elles n'en ont pas les
     * attrinuts, il sont crés automtiquement.
     *
     *
     * TODO : Quid process de création ???? a impl dans societeService
     *
     * @param societe
     */
    public void verifInitSociete(Societe societe) {

        //copie des templates
    }

    /**
     * Méthode de vérification de la présence des modeles dans les dossiers
     * appropriés.
     *
     * Si ces modèles n'existent pas,il serons crées.
     */
    public void checkOrInitSdModeles() {

    }

}
