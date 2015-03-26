/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.itldev.koya.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.TempFileProvider;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.utils.Zips;

public class UnzipActionExecuter extends ActionExecuterAbstractBase {

    Logger logger = Logger.getLogger(UnzipActionExecuter.class);

    public static final String NAME = "koyaUnzip";
    public static final String PARAM_ENCODING = "encoding";
    public static final String PARAM_DESTINATION_FOLDER = "destination";

    private static final String TEMP_FILE_PREFIX = "koya";
    private static final String TEMP_FILE_SUFFIX_ZIP = ".zip";

    private NodeService nodeService;
    private ContentService contentService;

    private KoyaContentService koyaContentService;
    
    private String defaultZipCharset;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }
    
    public void setDefaultZipCharset(String defaultZipCharset) {
		this.defaultZipCharset = defaultZipCharset;
	}

	/**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef,
     *      org.alfresco.repo.ref.NodeRef)
     */
    @Override
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
        if (this.nodeService.exists(actionedUponNodeRef) == true) {
            // The node being passed in should be an Alfresco content package
            ContentReader reader = this.contentService.getReader(
                    actionedUponNodeRef, ContentModel.PROP_CONTENT);
            if (reader != null) {
                File tempFile = null;
                File tempDir = null;
                try {
                    tempFile = TempFileProvider.createTempFile(
                            TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ZIP);
                    reader.getContent(tempFile);

                    // build a temp dir name based on the ID of the noderef we
                    // are importing
                    // also use the long life temp folder as large ZIP files can
                    // take a while
                    File alfTempDir = TempFileProvider
                            .getLongLifeTempDir("unzip");
                    tempDir = new File(alfTempDir.getPath()
                            + File.separatorChar + actionedUponNodeRef.getId());

                    if (Zips.unzip(tempFile.getAbsolutePath(),
                            tempDir.getAbsolutePath(),defaultZipCharset)) {

                        NodeRef importDest = (NodeRef) ruleAction
                                .getParameterValue(PARAM_DESTINATION_FOLDER);

                        importDirectory(tempDir.getPath(), importDest);

                    }

                } catch (KoyaServiceException | ContentIOException ex) {
                    throw new AlfrescoRuntimeException(
                            "Failed to import ZIP file. " + ex.getMessage(), ex);
                } finally {
                    // now the import is done, delete the temporary file
                    if (tempDir != null) {
                        deleteDir(tempDir);
                    }
                    if (tempFile != null) {
                        tempFile.delete();
                    }
                }
            }

            try {
                nodeService.deleteNode(actionedUponNodeRef);
            } catch (Exception ex) {
                throw new AlfrescoRuntimeException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Recursively import a directory structure into the specified root node
     *
     * @param dir
     *            The directory of files and folders to import
     * @param root
     *            The root node to import into
     */
    private void importDirectory(String directory, NodeRef root)
            throws KoyaServiceException {
        // Path topdir = Paths.get(dir);
    	String currentPath="";
        try (DirectoryStream<Path> directoryStream = Files
                .newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                String fileName = path.getFileName().toString();
                currentPath = path.toString();
                logger.trace("nex :" + fileName);

                if (Files.isRegularFile(path)) {
                    koyaContentService.createContentNode(root, fileName,
                            Files.newInputStream(path));

                } else {
                    // create a folder node
                    Directory d = koyaContentService.createDir(fileName, root);
                    // logger.debug(file.getPath());

                    logger.trace(path);
                    // recurcive call to import folder contents
                    importDirectory(path.toString(), d.getNodeRefasObject());
                }

            }
        } catch (IOException ex) {
        	logger.error("Import IOException on "+currentPath + " : "+ex.toString() );
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER,
                DataTypeDefinition.NODE_REF, true,
                getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
    }

    /**
     * Recursively delete a dir of files and directories
     *
     * @param dir
     *            directory to delete
     */
    private static void deleteDir(File dir) {
        if (dir != null) {
            File elenco = new File(dir.getPath());

            // listFiles can return null if the path is invalid i.e. already
            // been deleted,
            // therefore check for null before using in loop
            File[] files = elenco.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    } else {
                        deleteDir(file);
                    }
                }
            }

            // delete provided directory
            dir.delete();
        }
    }

}
