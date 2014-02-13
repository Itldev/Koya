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

import fr.itldev.koya.model.KoyaModel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Service général relatifs aux noeuds pris en charge par koya
 */
public class KoyaNodeService {

    private NodeService nodeService;

    /**
     * Méthode qui retourne un objet typé koya à partir d'un NodeRef
     *
     * @param n
     * @return
     */
    public Object getTypedSdObject(NodeRef n) {
        //TODO

        return null;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setActifStatus(NodeRef n, Boolean activeValue) {

        //TODO vérifier le champ d'application des noeuds activables (ou limiter dans le modele ??)
        if (nodeService.hasAspect(n, KoyaModel.QNAME_ITL_SDCONTENEURACTIF)) {
            //si le noeud existe déja, on met la valeur à jour
            nodeService.setProperty(n, KoyaModel.QNAME_PROPASPECT_ITL_ISACTIF, activeValue);
        } else {
            //sinon on ajoute l'aspect avec la valeur correcte
            Map<QName, Serializable> props = new HashMap<>();
            props.put(KoyaModel.QNAME_PROPASPECT_ITL_ISACTIF, activeValue);
            nodeService.addAspect(n, KoyaModel.QNAME_ITL_SDCONTENEURACTIF, props);
        }
    }

    /**
     * Un noeud est actif s'il contient l'aspect
     * SdModel.QNAME_ITL_SDCONTENEURACTIF ET que sa valeur est vraie.
     *
     * @param n
     * @return
     */
    public Boolean isActif(NodeRef n) {
        return nodeService.hasAspect(n, KoyaModel.QNAME_ITL_SDCONTENEURACTIF)
                && (Boolean) nodeService.getProperty(n, KoyaModel.QNAME_PROPASPECT_ITL_ISACTIF);
    }

    /**
     * Un site suivi dossier est un site ayant l'aspect sdConteneurActif
     *
     * @param n
     * @return
     */
    public Boolean isSdSite(NodeRef n) {

        //TODO verifier que le noeud en question est un site !!!!
        return nodeService.hasAspect(n, KoyaModel.QNAME_ITL_SDCONTENEURACTIF);
    }

}
