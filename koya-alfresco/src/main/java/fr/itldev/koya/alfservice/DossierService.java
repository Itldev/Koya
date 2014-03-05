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
package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Dossier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
 * Dossiers Handling Service
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
     * @param name
     * @param parent
     * @param prop
     * @return
     * @throws KoyaServiceException
     */
    public Dossier create(String name, NodeRef parent, Map<String, String> prop, String userName) throws KoyaServiceException {

        //Dossier must have a name
        if (name == null || name.isEmpty()) {
            throw new KoyaServiceException();
        }

        //parent must be a Space
        if (!nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_SPACE)) {
            throw new KoyaServiceException();
        }
        //checks if dossier's name already exists
        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            if (nodeService.getProperty(car.getChildRef(), ContentModel.PROP_NAME).equals(name)) {
                throw new KoyaServiceException();
            }
        }

        //build node properties
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);

        ChildAssociationRef childAssociationRef = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                KoyaModel.QNAME_KOYA_DOSSIER,
                properties);
        nodeService.addAspect(childAssociationRef.getChildRef(), KoyaModel.QNAME_KOYA_ACTIVABLE, null);

        return koyaNodeService.nodeDossierBuilder(childAssociationRef.getChildRef(), userName);
    }

    /**
     *
     * @param parent
     * @return
     * @throws KoyaServiceException
     */
    public List<Dossier> list(NodeRef parent, String userName) throws KoyaServiceException {
        List<Dossier> dossiers = new ArrayList<>();

        for (ChildAssociationRef child : nodeService.getChildAssocs(parent)) {
            if (nodeService.getType(child.getChildRef()).equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
                dossiers.add(koyaNodeService.nodeDossierBuilder(child.getChildRef(), userName));
            }
        }
        return dossiers;
    }

    /**
     *
     * @param parent
     */
    public void delete(NodeRef parent) {
        //TODO 
    }

   
}