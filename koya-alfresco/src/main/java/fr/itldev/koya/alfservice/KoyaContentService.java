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

import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Directory;
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
 * Koya Specific documents and directories Service.
 */
public class KoyaContentService {

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
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

        if (nodeService.getType(toMove).equals(ContentModel.TYPE_FOLDER)) {//TODO folder ou element héritant de folder !!! cf méthode pyramides dictionnary
            return nodeDirBuilder(toMove);
        } else {
            return nodeDocumentBuilder(toMove);
        }
    }

    /**
     * List Content recursive from parent noderef.
     *
     * //TODO limit by depth
     *
     *
     * @param parent
     * @return
     */
    public List<Content> list(NodeRef parent) {

        List<Content> contenus = new ArrayList<>();

        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            NodeRef childNr = car.getChildRef();
            //
            if (nodeService.getType(childNr).equals(ContentModel.TYPE_FOLDER)) {//TODO folder ou element héritant de folder !!! cf méthode pyramides dictionnary
                contenus.add(nodeDirBuilder(childNr));
                contenus.addAll(list(childNr));
            } else {
                contenus.add(nodeDocumentBuilder(childNr));
            }

        }
        return contenus;
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
