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
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Directory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
    private DictionaryService dictionaryService;
    private DossierService dossierService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public Directory createDir(String name, NodeRef parent) {

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

        return nodeDirBuilder(car.getChildRef());
    }

    public Content move(NodeRef toMove, NodeRef dest) {

        //TODO security check before moving OR exception catching ?
        String name = (String) nodeService.getProperty(toMove, ContentModel.PROP_NAME);

        nodeService.moveNode(toMove, dest, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));

        return nodeContentBuilder(toMove);
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
     * @return
     */
    public List<Content> list(NodeRef parent, Integer depth) {

        List<Content> contents = new ArrayList<>();

        if (depth <= 0) {
            return contents;//return empty list if max depth < = 0 : ie max depth reached
        }
        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            NodeRef childNr = car.getChildRef();
            //
            if (nodeIsFolder(childNr)) {
                Directory dir = nodeDirBuilder(childNr);
                dir.setChildren(list(childNr, depth - 1));
                contents.add(dir);
            } else {
                contents.add(nodeDocumentBuilder(childNr));
            }
        }
        return contents;
    }

    /**
     * Returns node (assumed is a content) parent
     *
     * @param currentNode
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem getParent(NodeRef currentNode) throws KoyaServiceException {

        NodeRef parentNr = nodeService.getPrimaryParent(currentNode).getParentRef();

        if (nodeService.getType(parentNr).equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
            return dossierService.nodeDossierBuilder(parentNr);
        } else if (nodeIsChildOfDossier(parentNr)) {
            return nodeContentBuilder(parentNr);
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

    /**
     * Return true if node is type ContentModel.TYPE_FOLDER or subtype.
     *
     *
     * @param nodeRef
     * @return
     */
    private Boolean nodeIsFolder(NodeRef nodeRef) {
        QName qNameType = nodeService.getType(nodeRef);
        return Boolean.valueOf(qNameType.equals(ContentModel.TYPE_FOLDER)
                || (dictionaryService.isSubClass(qNameType, ContentModel.TYPE_FOLDER)));
//        return Boolean.valueOf((dictionaryService.isSubClass(qNameType, ContentModel.TYPE_FOLDER)
//                && !dictionaryService.isSubClass(qNameType, ContentModel.TYPE_SYSTEM_FOLDER)));        
    }

    /**
     * Builds Content according to the type.
     *
     * @param nodeRef
     * @return
     */
    private Content nodeContentBuilder(NodeRef nodeRef) {
        if (nodeIsFolder(nodeRef)) {
            return nodeDirBuilder(nodeRef);
        } else {
            return nodeDocumentBuilder(nodeRef);
        }
    }

    /**
     *
     * @param dirNodeRef
     * @return
     */
    private Directory nodeDirBuilder(NodeRef dirNodeRef) {
        Directory r = new Directory();
        r.setNodeRef(dirNodeRef.toString());
        r.setName((String) nodeService.getProperty(dirNodeRef, ContentModel.PROP_NAME));
        r.setParentNodeRefasObject(nodeService.getPrimaryParent(dirNodeRef).getParentRef());
        return r;
    }

    /**
     *
     * @param docNodeRef
     * @return
     */
    private Document nodeDocumentBuilder(NodeRef docNodeRef) {
        Document d = new Document();
        d.setNodeRef(docNodeRef.toString());
        d.setName((String) nodeService.getProperty(docNodeRef, ContentModel.PROP_NAME));
        d.setParentNodeRefasObject(nodeService.getPrimaryParent(docNodeRef).getParentRef());
        return d;
    }

}
