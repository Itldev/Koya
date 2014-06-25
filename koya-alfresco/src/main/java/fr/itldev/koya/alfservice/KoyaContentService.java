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
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Koya Specific documents and directories Service.
 */
public class KoyaContentService {

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    private KoyaNodeService koyaNodeService;

    protected DictionaryService dictionaryService;
    protected ContentService contentService;
    protected NamespaceService namespaceService;
    protected FileFolderService fileFolderService;
    protected KoyaAclService koyaAclService;
    protected ActionService actionService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setKoyaAclService(KoyaAclService koyaAclService) {
        this.koyaAclService = koyaAclService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    // </editor-fold>
    public Directory createDir(String name, NodeRef parent) throws KoyaServiceException {

        if (!(nodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_DOSSIER)
                || nodeService.getType(parent).equals(ContentModel.TYPE_FOLDER))) {
            throw new KoyaServiceException(KoyaErrorCodes.DIR_CREATION_INVALID_PARENT_TYPE);
        }

        FileInfo fInfo;
        try {
            fInfo = fileFolderService.create(parent, name, ContentModel.TYPE_FOLDER);
        } catch (FileExistsException fex) {
            throw new KoyaServiceException(KoyaErrorCodes.DIR_CREATION_NAME_EXISTS);
        }

        return koyaNodeService.nodeDirBuilder(fInfo.getNodeRef());
    }

    public Content move(NodeRef toMove, NodeRef dest) throws KoyaServiceException {

        String newName = (String) nodeService.getProperty(toMove, ContentModel.PROP_NAME);

        FileInfo fInfo;
        try {
            fInfo = fileFolderService.move(toMove, dest, newName);
        } catch (FileExistsException fex) {
            throw new KoyaServiceException(KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
        } catch (FileNotFoundException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.MOVE_SOURCE_NOT_FOUND);
        }

        return koyaNodeService.nodeContentBuilder(fInfo.getNodeRef());
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
     * @param folderOnly
     * @return
     */
    public List<Content> list(NodeRef parent, Integer depth, Boolean... folderOnly) {

        List<Content> contents = new ArrayList<>();

        if (depth <= 0) {
            return contents;//return empty list if max depth < = 0 : ie max depth reached
        }

        List<FileInfo> childList;
        if (folderOnly.length > 0 && folderOnly[0]) {
            childList = fileFolderService.listFolders(parent);
        } else {
            childList = fileFolderService.list(parent);
        }

        for (final FileInfo fi : childList) {
            if (koyaNodeService.nodeIsFolder(fi.getNodeRef())) {
                Directory dir = koyaNodeService.nodeDirBuilder(fi.getNodeRef());
                dir.setChildren(list(fi.getNodeRef(), depth - 1, folderOnly));
                contents.add(dir);
            } else {
                contents.add(koyaNodeService.nodeDocumentBuilder(fi.getNodeRef()));
            }
        }
        return contents;

    }

    public File zip(List<String> nodeRefs) {
        File tmpZipFile = null;
        try {
            tmpZipFile = TempFileProvider.createTempFile("tmpDL", ".zip");
            FileOutputStream fos = new FileOutputStream(tmpZipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());
            BufferedOutputStream buff = new BufferedOutputStream(checksum);
            ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(buff);
            // NOTE: This encoding allows us to workaround bug...
            //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
            zipStream.setEncoding("UTF-8");

            zipStream.setMethod(ZipArchiveOutputStream.DEFLATED);
            zipStream.setLevel(Deflater.BEST_COMPRESSION);

            zipStream.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
            zipStream.setUseLanguageEncodingFlag(true);
            zipStream.setFallbackToUTF8(true);

            try {
                for (String nodeRef : nodeRefs) {
                    addToZip(new NodeRef(nodeRef), zipStream, "");
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } finally {
                zipStream.close();
                buff.close();
                checksum.close();
                fos.close();

            }
        } catch (IOException | WebScriptException e) {
            logger.error(e.getMessage(), e);
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }

        return tmpZipFile;
    }

    /**
     * Extract selected zipfile and delete it if succeed.
     *
     * use 'import' Action :
     * https://svn.alfresco.com/repos/alfresco-open-mirror/alfresco/HEAD/root/projects/repository/source/java/org/alfresco/repo/action/executer/ImporterActionExecuter.java
     *
     * @param zipFile
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void importZip(NodeRef zipFile) throws KoyaServiceException {

        /**
         * Checks if nodeRef is a zip file
         */
        try {
            ContentReader reader = contentService.getReader(zipFile, ContentModel.PROP_CONTENT);
            if (!MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())) {
                throw new KoyaServiceException(KoyaErrorCodes.CONTENT_IS_NOT_ZIP);
            }
        } catch (InvalidNodeRefException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODEREF, ex);
        }

        try {
            Map<String, Serializable> paramsImport = new HashMap<>();
            paramsImport.put("encoding", "UTF-8");
            paramsImport.put("destination", nodeService.getPrimaryParent(zipFile).getParentRef());
            Action importZip = actionService.createAction("import", paramsImport);
            /**
             * Process must be executed synchronously in order to delete
             * original zip
             *
             * We could also have written a new action that exctracts AND delete
             * zip.
             */
            importZip.setExecuteAsynchronously(false);
            actionService.executeAction(importZip, zipFile);      
        } catch (Exception ex) {
            throw new KoyaServiceException(KoyaErrorCodes.ZIP_EXTRACTION_PROCESS_ERROR, ex);
        }

        try {
            fileFolderService.delete(zipFile);
        } catch (Exception ex) {
            throw new KoyaServiceException(KoyaErrorCodes.FILE_DELETE_ERROR, ex);
        }
    }

    private void addToZip(NodeRef node, ZipArchiveOutputStream out, String path) throws IOException {
        QName nodeQnameType = this.nodeService.getType(node);

        // Special case : links
        if (this.dictionaryService.isSubClass(nodeQnameType, ApplicationModel.TYPE_FILELINK)) {
            NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(node, ContentModel.PROP_LINK_DESTINATION);
            if (linkDestinationNode == null) {
                return;
            }

            // Duplicate entry: check if link is not in the same space of the link destination
            if (nodeService.getPrimaryParent(node).getParentRef().equals(nodeService.getPrimaryParent(linkDestinationNode).getParentRef())) {
                return;
            }

            nodeQnameType = this.nodeService.getType(linkDestinationNode);
            node = linkDestinationNode;
        }

        String nodeName = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
//        nodeName = noaccent ? unAccent(nodeName) : nodeName;

        if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
            ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
            if (reader != null) {
                InputStream is = reader.getContentInputStream();

                String filename = path.isEmpty() ? nodeName : path + '/' + nodeName;

                ZipArchiveEntry entry = new ZipArchiveEntry(filename);
                entry.setTime(((Date) nodeService.getProperty(node, ContentModel.PROP_MODIFIED)).getTime());

                entry.setSize(reader.getSize());
                out.putArchiveEntry(entry);
                try {
                    byte buffer[] = new byte[8192];
                    while (true) {
                        int nRead = is.read(buffer, 0, buffer.length);
                        if (nRead <= 0) {
                            break;
                        }

                        out.write(buffer, 0, nRead);
                    }

                } catch (Exception exception) {
                    logger.error(exception.getMessage(), exception);
                } finally {
                    is.close();
                    out.closeArchiveEntry();
                }
            } else {
                logger.warn("Could not read : " + nodeName + "content");
            }
        } else if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_FOLDER)
                && !this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_SYSTEM_FOLDER)) {
            List<ChildAssociationRef> children = nodeService
                    .getChildAssocs(node);
            if (children.isEmpty()) {
                String folderPath = path.isEmpty() ? nodeName + '/' : path + '/' + nodeName + '/';
                out.putArchiveEntry(new ZipArchiveEntry(folderPath));
                out.closeArchiveEntry();
            } else {
                for (ChildAssociationRef childAssoc : children) {
                    NodeRef childNodeRef = childAssoc.getChildRef();

                    addToZip(childNodeRef, out, path.isEmpty() ? nodeName : path + '/' + nodeName);
                }
            }
        } else {
            logger.info("Unmanaged type: "
                    + nodeQnameType.getPrefixedQName(this.namespaceService)
                    + ", filename: " + nodeName);
        }
    }

}
