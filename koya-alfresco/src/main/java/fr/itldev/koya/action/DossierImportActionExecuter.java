package fr.itldev.koya.action;

import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.importXml.model.ContentXml;
import fr.itldev.koya.alfservice.importXml.model.ContentsXmlWrapper;
import fr.itldev.koya.alfservice.importXml.model.DossierXml;
import fr.itldev.koya.alfservice.importXml.model.DossiersXmlWrapper;
import fr.itldev.koya.alfservice.security.SubSpaceCollaboratorsAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DossierImportActionExecuter extends ActionExecuterAbstractBase {

    private Log logger = LogFactory.getLog(DossierImportActionExecuter.class);

    public static final String NAME = "koyaDossierImport";

    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;
    private DossierService dossierService;
    private UserService userService;
    private SubSpaceCollaboratorsAclService subSpaceCollaboratorsAclService;
    private KoyaNodeService koyaNodeService;
    private KoyaContentService koyaContentService;
    private AuthenticationService authenticationService;
    private RuntimeExec unzipCommand;

    private static String VAR_ZIPFILE = "zipFile";
    private static String VAR_DESTDIR = "destDir";

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public DossierService getDossierService() {
        return dossierService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public SubSpaceCollaboratorsAclService getSubSpaceCollaboratorsAclService() {
        return subSpaceCollaboratorsAclService;
    }

    public void setSubSpaceCollaboratorsAclService(SubSpaceCollaboratorsAclService subSpaceCollaboratorsAclService) {
        this.subSpaceCollaboratorsAclService = subSpaceCollaboratorsAclService;
    }

    public KoyaNodeService getKoyaNodeService() {
        return koyaNodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public KoyaContentService getKoyaContentService() {
        return koyaContentService;
    }

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setUnzipCommand(RuntimeExec unzipCommand) {
        this.unzipCommand = unzipCommand;
    }

    // </editor-fold>
    private static final int BUFFER_SIZE = 16384;

    private static final String TEMP_FILE_PREFIX = "koya";
    private static final String TEMP_FILE_SUFFIX_ZIP = ".zip";
    private static final String FILE_DOSSIERS_XML = "dossiers_import.xml";

    private static final String FILE_CONTENT_ZIP = "Documents.zip";
    private static final String FILE_CONTENT_XML = "documents_import.xml";
    private static final String FOLDER_CONTENTS = "contents";

    private static final List<SitePermission> societePermissions = new ArrayList<SitePermission>() {
        {
            add(SitePermission.MANAGER);
            add(SitePermission.COLLABORATOR);
        }
    };

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        String username = authenticationService.getCurrentUserName();
        if (this.nodeService.exists(actionedUponNodeRef) == true) {
            SiteInfo siteInfo = siteService.getSite(actionedUponNodeRef);
            NodeRef documentLibrary = siteService.getContainer(siteInfo.getShortName(), SiteService.DOCUMENT_LIBRARY);
            Map<String, Dossier> mapCacheDossier = new HashMap<String, Dossier>();
            try {
                Company company = koyaNodeService.getSecuredItem(siteInfo.getNodeRef(), Company.class);
                String companyName = company.getName();
                logger.debug(getLogPrefix(companyName, username) + "Import Xml Started");

                // The node being passed in should be an Alfresco content package
                ContentReader reader = this.contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
                if (reader != null) {
                    // perform an dossiers import of a standard ZIP file
                    File tempFile = null;
                    try {
                        // unfortunately a ZIP file can not be read directly from an input stream so we have to create
                        // a temporary file first
                        tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ZIP);
                        reader.getContent(tempFile);

                        // build a temp dir name based on the ID of the noderef we are importing
                        // also use the long life temp folder as large ZIP files can take a while
                        File alfTempDir = TempFileProvider.getLongLifeTempDir("import");
                        File tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
                        try {
                            // TODO: improve this code to directly pipe the zip stream output into the repo objects - 
                            //       to remove the need to expand to the filesystem first?
                            logger.debug(getLogPrefix(companyName, username) + "Extracting " + tempFile.getAbsolutePath());

                            Map<String, String> unzipImportParam = new HashMap<>(2);
                            unzipImportParam.put(VAR_ZIPFILE, tempFile.getAbsolutePath());
                            unzipImportParam.put(VAR_DESTDIR, tempDir.getAbsolutePath());

                            unzipCommand.execute(unzipImportParam);

                            //Reading the new dossiers to create
                            File fileDossiersXml = new File(tempDir, FILE_DOSSIERS_XML);
                            if (fileDossiersXml.exists()) {
                                //Create new dossiers
                                List<DossierXml> dossierXmls = null;
                                try {
                                    logger.debug(getLogPrefix(companyName, username) + "Unmarshalling " + fileDossiersXml.getName());
                                    JAXBContext context = JAXBContext.newInstance(DossiersXmlWrapper.class, DossierXml.class);
                                    DossiersXmlWrapper dossiersXmlWrapper = (DossiersXmlWrapper) context.createUnmarshaller().unmarshal(fileDossiersXml);
                                    if (dossiersXmlWrapper != null) {
                                        dossierXmls = dossiersXmlWrapper.getDossiers();
                                    }

                                } catch (JAXBException ex) {
                                    throw new AlfrescoRuntimeException(getLogPrefix(companyName, username) + "Error unmarshalling dossiers metadata.", ex);
                                }

                                if (dossierXmls != null) {
                                    logger.debug(getLogPrefix(companyName, username) + dossierXmls.size() + " dossiers found");
                                    int countDossiersCreated = 0;
                                    int countDossiersDuplicate = 0;
                                    int countDossiersError = 0;
                                    for (final DossierXml dossierXml : dossierXmls) {

                                        if (dossierXml.getReference() != null) {
                                            String space = dossierXml.getSpace();
                                            if (space == null || space.isEmpty()) {
                                                space = "defaultSpace";
                                            }

                                            NodeRef spaceNodeRef = nodeService.getChildByName(documentLibrary, ContentModel.ASSOC_CONTAINS, space);
                                            logger.trace(getLogPrefix(companyName, username) + "Creating dossier " + dossierXml.getReference() + " - " + dossierXml.getName() + " into " + spaceNodeRef);

                                            Dossier d = null;
                                            try {
                                                d = dossierService.create(dossierXml.getReference() + " - " + dossierXml.getName(), spaceNodeRef, new HashMap<QName, String>() {
                                                    {
                                                        put(KoyaModel.PROP_REFERENCE, dossierXml.getReference());
                                                    }
                                                });

                                                mapCacheDossier.put(dossierXml.getReference(), d);
                                                countDossiersCreated++;
                                            } catch (KoyaServiceException ex) {
                                                if (KoyaErrorCodes.DOSSIER_NAME_EXISTS.equals(ex.getErrorCode())) {
                                                    logger.error(getLogPrefix(companyName, username) + "Dossiers " + dossierXml.getReference() + " already exists.");
                                                    countDossiersDuplicate++;
                                                } else {
                                                    logger.error(getLogPrefix(companyName, username) + "Cannot create dossier " + dossierXml.getReference(), ex);
                                                    countDossiersError++;
                                                }
                                                continue;
                                            }

                                            logger.trace(getLogPrefix(companyName, username) + "Get dossier responsibles");
                                            for (String respMail : dossierXml.getResponsibles()) {
                                                User respUser = userService.getUserByEmailFailOver(respMail);
                                                if (respUser != null) {
                                                    logger.trace(getLogPrefix(companyName, username) + "Adding " + respMail + " as responsible");
                                                    try {
                                                        addKoyaPermissionCollaborator(company, d, respUser, KoyaPermissionCollaborator.RESPONSIBLE);
                                                    } catch (KoyaServiceException ex) {
                                                        logger.error(getLogPrefix(companyName, username) + "Error adding " + respMail + " AS reponsible to dossier " + dossierXml.getReference(), ex);
                                                    }
                                                }
                                                dossierXml.getMembers().remove(respMail);
                                            }

                                            logger.trace(getLogPrefix(companyName, username) + "get dossier members");
                                            for (String memberMail : dossierXml.getMembers()) {
                                                User memberUser = userService.getUserByEmailFailOver(memberMail);
                                                if (memberUser != null) {
                                                    logger.trace(getLogPrefix(companyName, username) + "Adding " + memberMail + " as collaborator");
                                                    try {
                                                        addKoyaPermissionCollaborator(company, d, memberUser, KoyaPermissionCollaborator.MEMBER);
                                                    } catch (KoyaServiceException ex) {
                                                        logger.error(getLogPrefix(companyName, username) + "Error adding " + memberMail + " AS collaborator to dossier " + dossierXml.getReference(), ex);
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    logger.debug(getLogPrefix(companyName, username) + countDossiersCreated + " dossiers created");
                                    logger.debug(getLogPrefix(companyName, username) + countDossiersDuplicate + " dossiers allready existing");
                                    logger.debug(getLogPrefix(companyName, username) + countDossiersError + " dossiers not created (error)");
                                }
                            }

                            //Importing new content into the dossiers
                            File fileContentZip = new File(tempDir, FILE_CONTENT_ZIP);
                            if (fileContentZip.exists()) {
                                //Extract content zip file
                                File contentsDir = new File(tempDir, FOLDER_CONTENTS);


                                logger.debug(getLogPrefix(companyName, username) + "Extracting " + fileContentZip.getName());

                                Map<String, String> unzipDocumentsParam = new HashMap<>(2);
                                unzipDocumentsParam.put(VAR_ZIPFILE, fileContentZip.getAbsolutePath());
                                unzipDocumentsParam.put(VAR_DESTDIR, contentsDir.getAbsolutePath());

                                unzipCommand.execute(unzipDocumentsParam);

                                //Reading contents files descriptor
                                File fileContentXml = new File(contentsDir.getPath(), FILE_CONTENT_XML);
                                if (!fileContentXml.exists()) {
                                    throw new AlfrescoRuntimeException(getLogPrefix(companyName, username) + "No files content metadata file found.");
                                }
                                //Create new content
                                List<ContentXml> contentXmls = null;
                                try {
                                    logger.debug(getLogPrefix(companyName, username) + "Unmarshalling " + fileContentXml.getName());
                                    JAXBContext context = JAXBContext.newInstance(ContentsXmlWrapper.class, ContentXml.class);
                                    ContentsXmlWrapper contentsXmlWrapper = (ContentsXmlWrapper) context.createUnmarshaller().unmarshal(fileContentXml);
                                    if (contentsXmlWrapper != null) {
                                        contentXmls = contentsXmlWrapper.getContentXmls();
                                    }

                                } catch (JAXBException ex) {
                                    throw new AlfrescoRuntimeException(getLogPrefix(companyName, username) + "Error unmarshalling dossiers metadata.", ex);
                                }

                                if (contentXmls != null) {
                                    logger.debug(getLogPrefix(companyName, username) + contentXmls.size() + " contents found");

                                    int countContentAdded = 0;
                                    int countContentDuplicate = 0;
                                    int countContentError = 0;
                                    int countContentFileNotFound = 0;
                                    int countContentDossierNotFound = 0;

                                    for (ContentXml contentXml : contentXmls) {
                                        try {
                                            Dossier dossier = mapCacheDossier.get(contentXml.getDossierReference());
                                            if (dossier == null) {
                                                dossier = dossierService.getDossier(company, contentXml.getDossierReference());
                                                mapCacheDossier.put(contentXml.getDossierReference(), dossier);
                                            }
                                            String path = contentXml.getPath();

                                            NodeRef dirNodeRef;
                                            if (path == null || path.isEmpty()) {
                                                dirNodeRef = dossier.getNodeRefasObject();
                                            } else {
                                                dirNodeRef = koyaContentService.makeFolders(dossier.getNodeRefasObject(), Arrays.asList(path.split(File.separator))).getNodeRefasObject();
                                            }

                                            String filename = contentXml.getFilename();
                                            int extIdx = filename.lastIndexOf(".");
                                            String name = contentXml.getName() + (extIdx != -1 ? filename.substring(extIdx) : "");

                                            try {
                                                logger.trace(getLogPrefix(companyName, username) + "Adding " + filename + " to " + path + " as " + name);

                                                koyaContentService.createContentNode(dirNodeRef, name, filename, null, null,
                                                        new FileInputStream(new File(contentsDir, filename)));
                                                countContentAdded++;
                                            } catch (FileNotFoundException ex) {
                                                logger.error(ex.getMessage(), ex);
                                                countContentFileNotFound++;
                                            } catch (KoyaServiceException ex) {
                                                //TODO Do something about duplicate - update/rename
                                                if (KoyaErrorCodes.FILE_UPLOAD_NAME_EXISTS.equals(ex.getErrorCode())) {
                                                    //TODO Do something about duplicate - update/rename/ignore
                                                    logger.error(getLogPrefix(companyName, username) + "File " + filename + " - " + name + " already exist");
                                                    countContentDuplicate++;
                                                } else {
                                                    logger.error(getLogPrefix(companyName, username) + ex.getMessage(), ex);
                                                    countContentError++;
                                                }
                                            }
                                        } catch (KoyaServiceException ex) {
                                            //TODO Dossier not found or multiple dossiers found
                                            logger.error(getLogPrefix(companyName, username) + ex.getMessage(), ex);
                                            countContentDossierNotFound++;
                                        }
                                    }
                                    logger.debug(getLogPrefix(companyName, username) + countContentAdded + " contents added");
                                    logger.debug(getLogPrefix(companyName, username) + countContentDuplicate + " contents duplicates name");
                                    logger.debug(getLogPrefix(companyName, username) + countContentFileNotFound + " file not found");
                                    logger.debug(getLogPrefix(companyName, username) + countContentDossierNotFound + " dossiers not found");
                                    logger.debug(getLogPrefix(companyName, username) + countContentError + " contents not added (error)");
                                }
                            }

                        } finally {
                            deleteDir(tempDir);
                        }
                    } catch (Exception ioErr) {
                        throw new AlfrescoRuntimeException("Failed to import ZIP file.", ioErr);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (tempFile != null) {
                            tempFile.delete();
                        }
                    }
                }
            } catch (KoyaServiceException ex) {
                throw new AlfrescoRuntimeException(getLogPrefix(null, username) + "Company not found.", ex);
            }

        }
    }

    private void addKoyaPermissionCollaborator(Company c, Dossier d, User u, KoyaPermissionCollaborator permissionCollaborator) throws KoyaServiceException {
        if (societePermissions.contains(SitePermission.valueOf(siteService.getMembersRole(c.getName(), u.getUserName())))) {
            subSpaceCollaboratorsAclService.shareSecuredItem( 
                    (SubSpace) koyaNodeService.getSecuredItem(d.getNodeRefasObject()),
                    userService.getUserByUsername(u.getUserName()).getEmail(),
                    permissionCollaborator, "", "", "", true);
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }


    /**
     * Recursively delete a dir of files and directories
     *
     * @param dir directory to delete
     */
    private void deleteDir(File dir) {
        if (dir != null) {
            File elenco = new File(dir.getPath());

            // listFiles can return null if the path is invalid i.e. already been deleted,
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

    private String getLogPrefix(String companyName, String username) {
        return "[" + username + "]" + ((companyName != null) ? "[" + companyName + "]" : "");
    }
}
