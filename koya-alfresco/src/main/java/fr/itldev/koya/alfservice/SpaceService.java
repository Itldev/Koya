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
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Spaces Handling service
 */
public class SpaceService {

    public static final String DOCLIB_NAME = "documentLibrary";

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    private SearchService searchService;
    private KoyaNodeService koyaNodeService;
    private CompanyService companyService;
    private KoyaAclService koyaAclService;
    private FileFolderService fileFolderService;

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

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setKoyaAclService(KoyaAclService koyaAclService) {
        this.koyaAclService = koyaAclService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    // </editor-fold>
    /**
     * Space creation in a valid Container : Space or Company
     *
     * @param name
     * @param parent
     * @param prop
     * @return
     * @throws KoyaServiceException
     */
    public Space create(String name, NodeRef parent, Map<String, String> prop) throws KoyaServiceException {

        //Space must have a name
        if (name == null || name.isEmpty()) {
            throw new KoyaServiceException(KoyaErrorCodes.SPACE_EMPTY_NAME);
        }

        NodeRef nrParent = null;

        if (nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_SPACE)) {
            //if parent is a space, select his node
            nrParent = parent;
        } else if (nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_COMPANY)) {
            //if it's a company, select documentLibrary's node
            nrParent = getDocLibNodeRef(parent);
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.SPACE_INVALID_PARENT);
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
               
        return koyaNodeService.nodeSpaceBuilder(car.getChildRef());
    }

    /**
     * Returns Company Spaces recursive list.
     *
     * @param companyShortName
     * @param depth
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<Space> list(String companyShortName, Integer depth) throws KoyaServiceException {
        NodeRef nodeDocLib = getDocLibNodeRef(companyService.getSiteInfo(companyShortName).getNodeRef());
        return listRecursive(nodeDocLib, depth);
    }

    /**
     * private recursive spaces list builder method.
     *
     * @param rootNodeRef
     * @param depth
     * @return
     * @throws KoyaServiceException
     */
    private List<Space> listRecursive(NodeRef rootNodeRef, Integer depth) throws KoyaServiceException {
        List<Space> spaces = new ArrayList<>();
        if (depth <= 0) {
            return spaces;//return empty list if max depth < = 0 : ie max depth reached
        }

        for (FileInfo fi : fileFolderService.listFolders(rootNodeRef)) {
            NodeRef childNr = fi.getNodeRef();
            if (nodeService.getType(childNr).equals(KoyaModel.QNAME_KOYA_SPACE)) {
                Space space = koyaNodeService.nodeSpaceBuilder(childNr);
                space.setChildSpaces(listRecursive(childNr, depth - 1));
                spaces.add(space);
            }
        }
        return spaces;
    }

    /**
     * move a space
     *
     * @param toMove
     * @param dest
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Space move(NodeRef toMove, NodeRef dest) throws KoyaServiceException {

        String name = (String) nodeService.getProperty(toMove, ContentModel.PROP_NAME);

        if (nodeService.getType(dest).equals(KoyaModel.QNAME_KOYA_COMPANY)) {
            dest = getDocLibNodeRef(dest);
        }
        logger.error("move " + name + " to " + (String) nodeService.getProperty(dest, ContentModel.PROP_NAME));

        nodeService.moveNode(toMove, dest, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));
        return koyaNodeService.nodeSpaceBuilder(toMove);
        //TODO call global move method
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

        throw new KoyaServiceException(KoyaErrorCodes.SPACE_DOCLIB_NODE_NOT_FOUND);
    }
}
