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
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
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
    private FileFolderService fileFolderService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
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
    public Dossier create(String name, NodeRef parent, Map<String, String> prop) throws KoyaServiceException {

        //Dossier must have a name
        if (name == null || name.isEmpty()) {
            throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_EMPTY_NAME);
        }

        //parent must be a Space
        if (!nodeService.getType(parent).equals(KoyaModel.TYPE_SPACE)) {
            throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_NOT_IN_SPACE);
        }
        //checks if dossier's name already exists
        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            if (nodeService.getProperty(car.getChildRef(), ContentModel.PROP_NAME).equals(name)) {
                throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_NAME_EXISTS);
            }
        }

        //build node properties
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);

        ChildAssociationRef car = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                KoyaModel.TYPE_DOSSIER,
                properties);
        koyaNodeService.setActiveStatus(car.getChildRef(), Boolean.TRUE);

        return koyaNodeService.nodeDossierBuilder(car.getChildRef());
    }

    /**
     *
     * @param parent
     * @return
     * @throws KoyaServiceException
     */
    public List<Dossier> list(NodeRef parent) throws KoyaServiceException {
        List<Dossier> dossiers = new ArrayList<>();

        for (final FileInfo fi : fileFolderService.listFolders(parent)) {
            if (fi.getType().equals(KoyaModel.TYPE_DOSSIER)) {
                Dossier dossier = koyaNodeService.nodeDossierBuilder(fi.getNodeRef());
                dossiers.add(dossier);
            }
        }
        return dossiers;
    }

}
