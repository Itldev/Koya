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

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Directory;

/**
 * Koya Specific documents and directories Service.
 */
public class KoyaContentService {

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    private KoyaNodeService koyaNodeService;
    protected ContentService contentService;
    protected FileFolderService fileFolderService;
    private KoyaActivityPoster activityPoster;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setActivityPoster(KoyaActivityPoster activityPoster) {
        this.activityPoster = activityPoster;
    }

    // </editor-fold>
    public Directory createDir(String title, NodeRef parent)
            throws KoyaServiceException {

        if (!(nodeService.getType(parent).equals(KoyaModel.TYPE_DOSSIER) || nodeService
                .getType(parent).equals(ContentModel.TYPE_FOLDER))) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.DIR_CREATION_INVALID_PARENT_TYPE);
        }

        String name = koyaNodeService.getUniqueValidFileNameFromTitle(title);

        // build node properties
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);

        try {
            ChildAssociationRef car = nodeService.createNode(parent,
                    ContentModel.ASSOC_CONTAINS, QName.createQName(
                            NamespaceService.CONTENT_MODEL_1_0_URI, name),
                    ContentModel.TYPE_FOLDER, properties);

            NodeRef dirNodeRef = car.getChildRef();

            activityPoster.postFileFolderAdded(dirNodeRef);

            return koyaNodeService.getKoyaNode(dirNodeRef, Directory.class);
        } catch (DuplicateChildNodeNameException dcne) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
        }

    }

    /**
     * Checks for the presence of, and creates as necessary, the folder
     * structure in the provided path.
     * <p>
     * An empty path list is not allowed as it would be impossible to
     * necessarily return file info for the parent node - it might not be a
     * folder node.
     *
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElements
     *            the folder name path to create - may not be empty
     *
     * @return Returns the info of the last folder in the path.
     */
    public Directory makeFolders(NodeRef parentNodeRef,
            List<String> pathElements) throws KoyaServiceException {
        if (!(nodeService.getType(parentNodeRef).equals(KoyaModel.TYPE_DOSSIER) || nodeService
                .getType(parentNodeRef).equals(ContentModel.TYPE_FOLDER))) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.DIR_CREATION_INVALID_PARENT_TYPE);
        }

        if (pathElements != null && pathElements.size() == 0) {
            throw new IllegalArgumentException("Path element list is empty");
        }
        NodeRef currentParentRef = parentNodeRef;
        // just loop and create if necessary
        for (final String pathElement : pathElements) {
            // ignoring empty path part
            if (pathElement != null && !pathElement.isEmpty()) {
                // does it exist?
                // Navigation should not check permissions
                NodeRef nodeRef = AuthenticationUtil
                        .runAsSystem(new SearchAsSystem(
                                fileFolderService,
                                currentParentRef,
                                koyaNodeService
                                        .getUniqueValidFileNameFromTitle(pathElement)));

                if (nodeRef == null) {
                    try {
                        // not present - make it
                        // If this uses the public service it will check create
                        // permissions
                        Directory directory = createDir(pathElement,
                                currentParentRef);
                        currentParentRef = directory.getNodeRef();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    } finally {

                    }
                } else {
                    // it exists
                    currentParentRef = nodeRef;
                }
            }
        }

        return koyaNodeService.getKoyaNode(currentParentRef, Directory.class);
    }

    private static class SearchAsSystem implements
            AuthenticationUtil.RunAsWork<NodeRef> {

        FileFolderService service;
        NodeRef node;
        String name;

        SearchAsSystem(FileFolderService service, NodeRef node, String name) {
            this.service = service;
            this.node = node;
            this.name = name;
        }

        public NodeRef doWork() throws Exception {
            return service.searchSimple(node, name);
        }
    }

    public Pair<NodeRef,Map<String, String>> createContentNode(NodeRef parent,
            String fileName,
            org.springframework.extensions.surf.util.Content content)
            throws KoyaServiceException {

        return createContentNode(parent, fileName, null, content.getMimetype(),
                content.getEncoding(), content.getInputStream(),true);
    }

    public Pair<NodeRef,Map<String, String>> createContentNode(NodeRef parent,
            String fileName, InputStream contentInputStream)
            throws KoyaServiceException {
        return createContentNode(parent, fileName, null, contentInputStream);
    }

    public Pair<NodeRef,Map<String, String>> createContentNode(NodeRef parent,
            String fileName, String name, InputStream contentInputStream)
            throws KoyaServiceException {
        return createContentNode(parent, fileName, name, null, null,
                contentInputStream,true);
    }

    public Pair<NodeRef,Map<String, String>> createContentNode(NodeRef parent,
            String fileName, String name, String mimetype, String encoding,
            InputStream contentInputStream,Boolean postActivity) throws KoyaServiceException {
        Boolean rename = false;
        if (name == null) {
            name = koyaNodeService.getUniqueValidFileNameFromTitle(fileName);

            rename = !fileName.equals(name);
        }

        /**
         * CREATE NODE
         */
        NodeRef createdNode;
        try {
            final Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);
            properties.put(ContentModel.PROP_TITLE, fileName);
            ChildAssociationRef car = nodeService.createNode(parent,
                    ContentModel.ASSOC_CONTAINS, QName.createQName(
                            NamespaceService.CONTENT_MODEL_1_0_URI, name),
                    ContentModel.TYPE_CONTENT, properties);

            createdNode = car.getChildRef();

            
        } catch (DuplicateChildNodeNameException ex) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.FILE_UPLOAD_NAME_EXISTS, fileName);
        } catch (IllegalArgumentException ex) {
            logger.error(fileName);
            throw ex;
        }

        /**
         * ADD CONTENT TO CREATED NODE
         *
         */
        ContentWriter writer = this.contentService.getWriter(createdNode,
                ContentModel.PROP_CONTENT, true);
        if (mimetype != null) {
            writer.setMimetype(mimetype);
        } else {
            writer.guessMimetype(fileName);
        }
        if (encoding != null) {
            writer.setEncoding(encoding);
        }
        writer.guessEncoding();
        writer.putContent(contentInputStream);

        Map<String, String> retMap = new HashMap<>();

        retMap.put("filename", name);
        retMap.put("originalFilename", fileName);
        retMap.put("rename", rename.toString());
        retMap.put("size", Long.toString(writer.getSize()));
		retMap.put("nodeRef", createdNode.toString());

		if(postActivity){
			activityPoster.postFileFolderAdded(createdNode);
		}
        return new Pair<NodeRef, Map<String,String>>(createdNode, retMap);
    }

}
