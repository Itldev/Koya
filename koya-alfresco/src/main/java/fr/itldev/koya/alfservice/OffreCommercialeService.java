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

import fr.itldev.koya.model.impl.OffreCommerciale;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

/**
 *
 * Les Société sont les sites ayant l'aspect itlsd:activable
 *
 */
public class OffreCommercialeService {

    private final Logger logger = Logger.getLogger(OffreCommercialeService.class);

    private NodeService nodeService;
    private KoyaNodeService koyaNodeService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    // </editor-fold>
    public List<OffreCommerciale> listerOffresCommerciales() {
        return null;

    }

    public OffreCommerciale getOffreCommerciale(String nomOffre) {
        return null;

    }

}
