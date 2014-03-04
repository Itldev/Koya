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
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.KoyaModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Spaces Handling service
 */
public class SpaceService {

    public static final String DOCLIB_NAME = "documentLibrary";

    private Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    private SearchService searchService;
    private KoyaNodeService koyaNodeService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    // </editor-fold>
    /**
     * Space creation in a valid Container : Space or Company
     *
     * @param name
     * @param parent
     * @param prop
     * @param userName
     * @return
     * @throws KoyaServiceException
     */
    public Space create(String name, NodeRef parent, Map<String, String> prop, String userName) throws KoyaServiceException {

        //Space must have a name
        if (name == null || name.isEmpty()) {
            throw new KoyaServiceException();
        }

        NodeRef nrParent = null;

        if (nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_SPACE)) {
            //if parent is a space, select his node
            nrParent = parent;
        } else if (nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_COMPANY)) {
            //if it's a company, select documentLibrary's node
            nrParent = getDocLibNodeRef(parent);
        } else {
            throw new KoyaServiceException();//TODO invalid parent type
        }

        //TODO check name unicity
        //build node properties
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);

        ChildAssociationRef car = nodeService.createNode(nrParent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                KoyaModel.QNAME_KOYA_SPACE,
                properties);
        koyaNodeService.setActiveStatus(car.getChildRef(), Boolean.TRUE);

        return koyaNodeService.nodeSpaceBuilder(car.getChildRef(), userName);
    }

    /**
     * Returns Company Spaces flat list.
     *
     * @param companyShortName
     * @param userName
     * @return
     */
    public List<Space> list(String companyShortName, String userName) {
        List<Space> espaces = new ArrayList<>();

        String listEspacesQuery = "PATH:\"/app:company_home/st:sites/cm:" + companyShortName + "/cm:documentLibrary//*\"";
        listEspacesQuery += " AND TYPE:\"" + KoyaModel.TYPESHORTPREFIX_KOYA_SPACE + "\"";

        ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, listEspacesQuery);

        for (ResultSetRow r : rs) {
            espaces.add(koyaNodeService.nodeSpaceBuilder(r.getNodeRef(), userName));
        }
        return espaces;
    }

    /**
     * delete a Space
     *
     * @param e
     */
    public void del(Space e) {
        NodeRef n = new NodeRef(e.getNodeRef());
        //TODO call global del method
    }

    /**
     * move a Space
     *
     * @param e
     */
    public void move(Space e) {
        //TODO call golbal move method
    }

    /**
     *
     * =============== private Methods =================
     *
     */
  

    /**
     *
     * Returns Company "documentLibrary" NodeRef (root spaces parent).
     *
     * @param s
     * @return
     * @throws KoyaServiceException
     */
    private NodeRef getDocLibNodeRef(NodeRef companyNodeRef) throws KoyaServiceException {
        //TODO cache noderef / companies

        for (ChildAssociationRef car : nodeService.getChildAssocs(companyNodeRef)) {
            if (nodeService.getProperty(car.getChildRef(), ContentModel.PROP_NAME).equals(DOCLIB_NAME)) {
                return car.getChildRef();
            }
        }

        throw new KoyaServiceException();//TODO erreur aucun doc lib
    }
}
