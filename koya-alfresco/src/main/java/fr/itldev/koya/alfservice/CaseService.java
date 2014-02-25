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
import fr.itldev.koya.model.impl.Case;
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
 * Cases Handling Service
 *
 *
 */
public class CaseService {

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
    public Case create(String name, NodeRef parent, Map<String, String> prop) throws KoyaServiceException {

        //Case must have a name
        if (name == null || name.isEmpty()) {
            throw new KoyaServiceException();
        }

        //parent must be a Space
        if (!nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_SPACE)) {
            throw new KoyaServiceException();
        }
        //checks if case name already exists
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
                KoyaModel.QNAME_KOYA_CASE,
                properties);
        nodeService.addAspect(childAssociationRef.getChildRef(), KoyaModel.QNAME_KOYA_ACTIVABLE, null);

        return nodeCaseBuilder(childAssociationRef.getChildRef());
    }

    /**
     *
     * @param parent
     * @return
     * @throws KoyaServiceException
     */
    public List<Case> list(NodeRef parent) throws KoyaServiceException {
        List<Case> dossiers = new ArrayList<>();

        for (ChildAssociationRef child : nodeService.getChildAssocs(parent)) {
            if (nodeService.getType(child.getChildRef()).equals(KoyaModel.QNAME_KOYA_CASE)) {
                dossiers.add(nodeCaseBuilder(child.getChildRef()));
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

    /**
     *
     * =============== private Methods =================
     *
     */
    /**
     *
     * @param caseNodeRef
     * @return
     */
    private Case nodeCaseBuilder(NodeRef caseNodeRef) {
        Case d = new Case();

        d.setNodeRef(caseNodeRef.toString());
        d.setName((String) nodeService.getProperty(caseNodeRef, ContentModel.PROP_NAME));
       
        NodeRef directParent = nodeService.getPrimaryParent(caseNodeRef).getParentRef();
        NodeRef realParent = null;
        if (nodeService.getType(directParent).equals(KoyaModel.QNAME_KOYA_SPACE)) {
            realParent = directParent;
        } else {
            logger.warn("Erreur de hiérachie des parent d'espace");
            //TODO exception      
        }
        d.setParentNodeRefasObject(realParent);
        
        d.setActive(koyaNodeService.isActive(caseNodeRef));

        return d;
    }
}
