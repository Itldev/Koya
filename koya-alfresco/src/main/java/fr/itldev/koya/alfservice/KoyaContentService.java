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
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Directory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Koya Specific documents and directories Service.
 */
public class KoyaContentService {

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    KoyaNodeService koyaNodeService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public Directory createDir(String name, NodeRef parent, String userName) {

        //TODO parent must be a dir or a dossier
        //TODO check dir name unicity
        //node properties building
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        //TODO other properties

        ChildAssociationRef car = nodeService.createNode(parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_FOLDER,
                properties);

        return koyaNodeService.nodeDirBuilder(car.getChildRef(), userName);
    }

    public Content move(NodeRef toMove, NodeRef dest, String userName) {

        //TODO security check before moving OR exception catching ?
        String name = (String) nodeService.getProperty(toMove, ContentModel.PROP_NAME);

        nodeService.moveNode(toMove, dest, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));

        return koyaNodeService.nodeContentBuilder(toMove, userName);
    }

    /**
     * List Content recursive from parent noderef.
     *
     * depth limit
     *
     * - 0 : no limit
     *
     * - n : limit to n levels
     *
     * @param parent
     * @param depth
     * @param userName
     * @return
     */
    public List<Content> list(NodeRef parent, Integer depth, String userName) {

        List<Content> contents = new ArrayList<>();

        if (depth <= 0) {
            return contents;//return empty list if max depth < = 0 : ie max depth reached
        }
        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            NodeRef childNr = car.getChildRef();
            //
            if (koyaNodeService.nodeIsFolder(childNr)) {
                Directory dir = koyaNodeService.nodeDirBuilder(childNr, userName);
                dir.setChildren(list(childNr, depth - 1, userName));
                contents.add(dir);
            } else {
                contents.add(koyaNodeService.nodeDocumentBuilder(childNr, userName));
            }
        }
        return contents;
    }

    /**
     * Returns node (assumed is a content) parent
     *
     * @param currentNode
     * @param userName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem getParent(NodeRef currentNode, String userName) throws KoyaServiceException {

        NodeRef parentNr = nodeService.getPrimaryParent(currentNode).getParentRef();

        if (nodeService.getType(parentNr).equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
            return koyaNodeService.nodeDossierBuilder(parentNr, userName);
        } else if (nodeIsChildOfDossier(parentNr)) {
            return koyaNodeService.nodeContentBuilder(parentNr, userName);
        } else {
            throw new KoyaServiceException("Invalid Content node passed to get parent.");
        }

    }

    /**
     *
     * @param nodeRef
     * @return
     */
    private Boolean nodeIsChildOfDossier(NodeRef nodeRef) {

        try {
            NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();

            if (nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
                return true;
            } else {
                return nodeIsChildOfDossier(parent);
            }
        } catch (InvalidNodeRefException e) {
            return false;
        }
    }

}
