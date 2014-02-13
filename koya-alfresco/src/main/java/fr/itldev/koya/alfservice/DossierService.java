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

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Dossier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Service de gestion des dossiers
 *
 *
 */
public class DossierService {

    private final Logger logger = Logger.getLogger(this.getClass());

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
    /**
     *
     * @param nom
     * @param parent
     * @param prop
     * @return
     * @throws KoyaServiceException
     */
    public Dossier creer(String nom, NodeRef parent, Map<String, String> prop) throws KoyaServiceException {

        //le dossier doit avoir un nom
        if (nom == null || nom.isEmpty()) {
            throw new KoyaServiceException();
        }

        //le parent doir etre de type espace 
        if (!nodeService.getType(parent).equals(KoyaModel.QNAME_ITL_ESPACE)) {
            throw new KoyaServiceException();
        }
        //verifier qu'il n'existe pas déja un dossier ayant ce nom 
        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            if (nodeService.getProperty(car.getChildRef(), ContentModel.PROP_NAME).equals(nom)) {
                throw new KoyaServiceException();
            }
        }

        //construction des propriétes du noeud.
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, nom);

        ChildAssociationRef childAssociationRef = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nom),
                KoyaModel.QNAME_ITL_DOSSIER,
                properties);
        nodeService.addAspect(childAssociationRef.getChildRef(), KoyaModel.QNAME_ITL_SDCONTENEURACTIF, null);

        return nodeDossierBuilder(childAssociationRef.getChildRef());
    }

    /**
     *
     * @param parent
     * @return
     * @throws KoyaServiceException
     */
    public List<Dossier> lister(NodeRef parent) throws KoyaServiceException {
        List<Dossier> dossiers = new ArrayList<>();

        for (ChildAssociationRef child : nodeService.getChildAssocs(parent)) {
            if (nodeService.getType(child.getChildRef()).equals(KoyaModel.QNAME_ITL_DOSSIER)) {
                dossiers.add(nodeDossierBuilder(child.getChildRef()));
            }
        }
        return dossiers;
    }

    /**
     *
     * @param parent
     */
    public void supprimer(NodeRef parent) {
        //TODO 
    }

    /**
     *
     * =============== Méthodes privées =================
     *
     */
    /**
     *
     * @param dossierNodeRef
     * @return
     */
    private Dossier nodeDossierBuilder(NodeRef dossierNodeRef) {
        Dossier d = new Dossier();

        d.setNodeRef(dossierNodeRef.toString());
        d.setNom((String) nodeService.getProperty(dossierNodeRef, ContentModel.PROP_NAME));

        //défintion du noderef parent . TODO voir intéret de cette vérification
        NodeRef directParent = nodeService.getPrimaryParent(dossierNodeRef).getParentRef();
        NodeRef realParent = null;
        if (nodeService.getType(directParent).equals(KoyaModel.QNAME_ITL_ESPACE)) {
            realParent = directParent;
        } else {
            logger.warn("Erreur de hiérachie des parent d'espace");
            //TODO lever un exception        
        }
        d.setParentNodeRefasObject(realParent);

        //status activité
        d.setActive(koyaNodeService.isActif(dossierNodeRef));

        return d;
    }
}
