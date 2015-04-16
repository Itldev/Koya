package fr.itldev.koya.action;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.itldev.koya.action.importXml.model.ContentXml;
import fr.itldev.koya.action.importXml.model.ContentsXmlWrapper;
import fr.itldev.koya.action.importXml.model.DossierXml;
import fr.itldev.koya.action.importXml.model.DossiersXmlWrapper;
import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SubSpaceCollaboratorsAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.utils.Zips;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;

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
    private BehaviourFilter policyBehaviourFilter;

    private String defaultZipCharset;
    private String failoverZipCharset;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setDossierService(DossierService dossierService) {
        this.dossierService = dossierService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setSubSpaceCollaboratorsAclService(
            SubSpaceCollaboratorsAclService subSpaceCollaboratorsAclService) {
        this.subSpaceCollaboratorsAclService = subSpaceCollaboratorsAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setKoyaContentService(KoyaContentService koyaContentService) {
        this.koyaContentService = koyaContentService;
    }

    public void setAuthenticationService(
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public void setDefaultZipCharset(String defaultZipCharset) {
        this.defaultZipCharset = defaultZipCharset;
    }

    public void setFailoverZipCharset(String failoverZipCharset) {
        this.failoverZipCharset = failoverZipCharset;
    }

    // </editor-fold>
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
        // try {
        String username = authenticationService.getCurrentUserName();
        File tempFile = null;
        File tempDir = null;
        if (!this.nodeService.exists(actionedUponNodeRef)) {
            return;
        }
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

        SiteInfo siteInfo = siteService.getSite(actionedUponNodeRef);
        NodeRef documentLibrary = siteService.getContainer(
                siteInfo.getShortName(), SiteService.DOCUMENT_LIBRARY);
        Map<String, Dossier> mapCacheDossier = new HashMap<String, Dossier>();
        Set<Dossier> modifiedDossiers = new HashSet<>();
        try {
            Company company = koyaNodeService.getSecuredItem(
                    siteInfo.getNodeRef(), Company.class);
            String companyName = company.getName();
            logger.debug(getLogPrefix(companyName, username)
                    + "Import Xml Started");

            // The node being passed in should be an Alfresco content
            // package
            ContentReader reader = this.contentService.getReader(
                    actionedUponNodeRef, ContentModel.PROP_CONTENT);
            if (reader != null) {
                // perform an dossiers import of a standard ZIP file

                // unfortunately a ZIP file can not be read directly from an
                // input stream so we have to create a temporary file first
                tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX,
                        TEMP_FILE_SUFFIX_ZIP);
                reader.getContent(tempFile);

                // build a temp dir name based on the ID of the noderef we are
                // importing also use the long life temp folder as large ZIP
                // files can take a while
                File alfTempDir = TempFileProvider.getLongLifeTempDir("import");
                tempDir = new File(alfTempDir.getPath() + File.separatorChar
                        + actionedUponNodeRef.getId());

                // TODO: improve this code to directly pipe the zip stream
                // output into the repo objects -
                // to remove the need to expand to the filesystem first?
                logger.debug(getLogPrefix(companyName, username)
                        + "Extracting "
                        + nodeService.getProperty(actionedUponNodeRef,
                                ContentModel.PROP_NAME) + " ("
                        + tempFile.getAbsolutePath() + ")" + " to "
                        + tempDir.getAbsolutePath());

                Zips.unzip(tempFile.getAbsolutePath(),
                        tempDir.getAbsolutePath(), defaultZipCharset,
                        failoverZipCharset);

                importDossier(tempDir, company, documentLibrary,
                        mapCacheDossier, modifiedDossiers);

                // Importing new content into the dossiers
                File fileContentZip = new File(tempDir, FILE_CONTENT_ZIP);
                if (fileContentZip.exists()) {
                    // Extract content zip file
                    File contentsDir = new File(tempDir, FOLDER_CONTENTS);

                    logger.debug(getLogPrefix(companyName, username)
                            + "Extracting " + fileContentZip.getName() + " to "
                            + contentsDir.getAbsolutePath());

                    Zips.unzip(fileContentZip.getAbsolutePath(),
                            contentsDir.getAbsolutePath(), defaultZipCharset,
                            failoverZipCharset);

                    importContent(contentsDir, company, mapCacheDossier,
                            modifiedDossiers);
                }
            }
        } catch (InvalidNodeRefException | InvalidTypeException
                | ContentIOException ex) {
            throw new AlfrescoRuntimeException(getLogPrefix(null, username)
                    + " " + ex.getMessage(), ex);
        } finally {
            deleteDir(tempDir);

            // now the import is done, delete the temporary file
            if (tempFile != null) {
                tempFile.delete();
            }
        }

        for (Dossier d : modifiedDossiers) {
            dossierService.addOrUpdateLastModifiedDate(d.getNodeRefasObject());
        }

    }

    private void importDossier(File tempDir, Company company,
            NodeRef documentLibrary, Map<String, Dossier> mapCacheDossier,
            Set<Dossier> modifiedDossiers) {
        String userName = authenticationService.getCurrentUserName();
        String companyName = company.getName();

        // Reading the new dossiers to create
        File fileDossiersXml = new File(tempDir, FILE_DOSSIERS_XML);
        if (fileDossiersXml.exists()) {
            // Create new dossiers
            List<DossierXml> dossierXmls = null;
            try {
                logger.debug(getLogPrefix(companyName, userName)
                        + "Unmarshalling " + fileDossiersXml.getName());
                JAXBContext context = JAXBContext.newInstance(
                        DossiersXmlWrapper.class, DossierXml.class);
                DossiersXmlWrapper dossiersXmlWrapper = (DossiersXmlWrapper) context
                        .createUnmarshaller().unmarshal(fileDossiersXml);
                if (dossiersXmlWrapper != null) {
                    dossierXmls = dossiersXmlWrapper.getDossiers();
                }

            } catch (JAXBException ex) {
                throw new AlfrescoRuntimeException(getLogPrefix(companyName,
                        userName) + "Error unmarshalling dossiers metadata.",
                        ex);
            }

            if (dossierXmls != null) {
                logger.debug(getLogPrefix(companyName, userName)
                        + dossierXmls.size() + " dossiers found");
                int countDossiersCreated = 0;
                int countDossiersDuplicate = 0;
                int countDossiersError = 0;
                for (final DossierXml dossierXml : dossierXmls) {

                    if (dossierXml.getReference() == null) {
                        continue;
                    }

                    String space = dossierXml.getSpace();
                    if (space == null || space.isEmpty()) {
                        space = "defaultSpace";
                    }
                    String dossierTitle = dossierXml.getReference() + " - "
                            + dossierXml.getName();

                    NodeRef spaceNodeRef = nodeService
                            .getChildByName(documentLibrary,
                                    ContentModel.ASSOC_CONTAINS, space);

                    logger.trace(getLogPrefix(companyName, userName)
                            + "Creating dossier " + dossierTitle + " into "
                            + spaceNodeRef);

                    Dossier d = null;
                    boolean newDossier = false;

                    try {
                        d = dossierService.getDossier(company,
                                dossierXml.getReference());
                        if (!d.getTitle().equals(dossierTitle)) {
                            koyaNodeService.rename(d.getNodeRefasObject(),
                                    dossierXml.getReference() + " - "
                                            + dossierXml.getName());
                            d = koyaNodeService.getSecuredItem(
                                    d.getNodeRefasObject(), Dossier.class);
                        }
                    } catch (KoyaServiceException kse) {
                        if (KoyaErrorCodes.NO_SUCH_DOSSIER_REFERENCE.equals(kse
                                .getErrorCode())) {
                            d = dossierService.create(dossierXml.getReference()
                                    + " - " + dossierXml.getName(),
                                    spaceNodeRef, new HashMap<QName, String>() {
                                        {
                                            put(KoyaModel.PROP_REFERENCE,
                                                    dossierXml.getReference());
                                        }
                                    });
                            newDossier = true;
                            modifiedDossiers.add(d);
                        } else {
                            logger.error(
                                    getLogPrefix(companyName, userName)
                                            + "Cannot create dossier "
                                            + dossierXml.getReference(), kse);
                            countDossiersError++;
                            continue;

                        }
                    }

                    mapCacheDossier.put(dossierXml.getReference(), d);
                    countDossiersCreated++;

                    logger.trace(getLogPrefix(companyName, userName)
                            + "Get dossier responsibles");
                    addKoyaPermissionCollaborator(company, d,
                            dossierXml.getResponsibles(),
                            KoyaPermissionCollaborator.RESPONSIBLE, newDossier);

                    logger.trace(getLogPrefix(companyName, userName)
                            + "get dossier members");
                    addKoyaPermissionCollaborator(company, d,
                            dossierXml.getMembers(),
                            KoyaPermissionCollaborator.MEMBER, newDossier);

                }
                logger.debug(getLogPrefix(companyName, userName)
                        + countDossiersCreated + " dossiers created");
                logger.debug(getLogPrefix(companyName, userName)
                        + countDossiersDuplicate
                        + " dossiers allready existing");
                logger.debug(getLogPrefix(companyName, userName)
                        + countDossiersError + " dossiers not created (error)");
            }
        }
    }

    private void importContent(File contentsDir, Company company,
            Map<String, Dossier> mapCacheDossier, Set<Dossier> modifiedDossiers) {

        String userName = authenticationService.getCurrentUserName();
        String companyName = company.getName();

        // Reading contents files descriptor
        File fileContentXml = new File(contentsDir.getPath(), FILE_CONTENT_XML);
        if (!fileContentXml.exists()) {
            throw new AlfrescoRuntimeException(getLogPrefix(companyName,
                    userName) + "No files content metadata file found.");
        }
        // Create new content
        List<ContentXml> contentXmls = null;
        try {
            logger.debug(getLogPrefix(companyName, userName) + "Unmarshalling "
                    + fileContentXml.getName());

            JAXBContext context = JAXBContext.newInstance(
                    ContentsXmlWrapper.class, ContentXml.class);

            ContentsXmlWrapper contentsXmlWrapper = (ContentsXmlWrapper) context
                    .createUnmarshaller().unmarshal(fileContentXml);

            if (contentsXmlWrapper != null) {
                contentXmls = contentsXmlWrapper.getContentXmls();
            }

        } catch (JAXBException ex) {
            throw new AlfrescoRuntimeException(getLogPrefix(companyName,
                    userName) + "Error unmarshalling dossiers metadata.", ex);
        }

        if (contentXmls == null) {
            return;
        }
        logger.debug(getLogPrefix(companyName, userName) + contentXmls.size()
                + " contents found");

        int countContentAdded = 0;
        int countContentDuplicate = 0;
        int countContentError = 0;
        int countContentFileNotFound = 0;
        int countContentDossierNotFound = 0;
        int countPathCreation = 0;

        for (ContentXml contentXml : contentXmls) {
            String filename = null;
            String title = null;
            try {
                Dossier dossier = mapCacheDossier.get(contentXml
                        .getDossierReference());
                if (dossier == null) {
                    dossier = dossierService.getDossier(company,
                            contentXml.getDossierReference());
                    mapCacheDossier.put(contentXml.getDossierReference(),
                            dossier);
                }
                String path = contentXml.getPath();

                boolean pathCreated = false;
                NodeRef dirNodeRef;
                if (path == null || path.isEmpty()) {
                    dirNodeRef = dossier.getNodeRefasObject();
                } else {
                    dirNodeRef = koyaContentService.makeFolders(
                            dossier.getNodeRefasObject(),
                            Arrays.asList(path.split(File.separator)))
                            .getNodeRefasObject();
                    pathCreated = true;
                }

                filename = contentXml.getFilename();
                if (filename != null && !filename.trim().isEmpty()) {
                    int extIdx = filename.lastIndexOf(".");
                    title = contentXml.getName()
                            + (extIdx != -1 ? filename.substring(extIdx) : "");
                    logger.trace(getLogPrefix(companyName, userName)
                            + "Adding " + filename + " to " + path + " as "
                            + title);
                    if (nodeService.getChildByName(dirNodeRef,
                            ContentModel.ASSOC_CONTAINS, filename) == null) {
                        koyaContentService.createContentNode(dirNodeRef, title,
                                filename, new FileInputStream(new File(
                                        contentsDir, filename)));
                        modifiedDossiers.add(dossier);
                    }
                    countContentAdded++;

                } else if(pathCreated) {
                    countPathCreation++;
                }
            } catch (FileNotFoundException ex) {
                logger.error(ex.getMessage(), ex);
                countContentFileNotFound++;
            } catch (KoyaServiceException ex) {
                // TODO Dossier not found or
                // multiple dossiers found
                logger.error(
                        getLogPrefix(companyName, userName) + ex.getMessage(),
                        ex);
                Integer errorCode = ex.getErrorCode();

                if (errorCode.equals(KoyaErrorCodes.NO_SUCH_DOSSIER_REFERENCE)
                        || errorCode
                                .equals(KoyaErrorCodes.MANY_DOSSIERS_REFERENCE)) {
                    countContentDossierNotFound++;
                } else if (errorCode
                        .equals(KoyaErrorCodes.FILE_UPLOAD_NAME_EXISTS)) {
                    logger.error(getLogPrefix(companyName, userName) + "File "
                            + filename + " - " + title + " already exist");
                    countContentDuplicate++;
                } else {
                    countContentError++;

                }
            }
        }

        logger.debug(getLogPrefix(companyName, userName) + countContentAdded
                + " contents added");
        logger.debug(getLogPrefix(companyName, userName)
                + countContentDuplicate + " contents duplicates name");
        logger.debug(getLogPrefix(companyName, userName)
                + countContentFileNotFound + " files not found");
        logger.debug(getLogPrefix(companyName, userName)
                + countContentDossierNotFound + "contents' dossiers not found");
        logger.debug(getLogPrefix(companyName, userName) + countContentError
                + " contents not added (error)");
        logger.debug(getLogPrefix(companyName, userName) + countPathCreation
                + " empty path");

    }

    private void addKoyaPermissionCollaborator(Company c, Dossier d,
            List<String> usersMail,
            final KoyaPermissionCollaborator permissionCollaborator,
            boolean newDossier) throws KoyaServiceException {
        if (!newDossier) {
            // Removing not responsible anymore
            List<User> currentUsers = subSpaceCollaboratorsAclService
                    .listUsers(d, new ArrayList<KoyaPermission>() {
                        {
                            add(permissionCollaborator);
                        }
                    });

            for (User u : currentUsers) {
                if (!usersMail.contains(u.getEmail())) {
                    subSpaceCollaboratorsAclService.unShareSecuredItem(d,
                            u.getEmail(), permissionCollaborator);
                }
            }
        }

        for (String userMail : usersMail) {
            User u = userService.getUserByEmailFailOver(userMail);
            if (u != null) {
                logger.trace(getLogPrefix(c.getName(), null) + "Adding "
                        + userMail + " as responsible");
                try {
                    if (societePermissions.contains(SitePermission
                            .valueOf(siteService.getMembersRole(c.getName(),
                                    u.getUserName())))) {
                        subSpaceCollaboratorsAclService.shareSecuredItem(
                                (SubSpace) koyaNodeService.getSecuredItem(d
                                        .getNodeRefasObject()), userService
                                        .getUserByUsername(u.getUserName())
                                        .getEmail(), permissionCollaborator,
                                true);
                    }
                } catch (KoyaServiceException ex) {
                    logger.error(getLogPrefix(c.getName(), null)
                            + "Error adding " + userMail
                            + " AS collaborator to dossier " + d.getTitle(), ex);
                }
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }

    /**
     * Recursively delete a dir of files and directories
     *
     * @param dir
     *            directory to delete
     */
    private void deleteDir(File dir) {
        if (dir == null) {
            return;
        }
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

    private String getLogPrefix(String companyName, String username) {
        return "[" + username + "]"
                + ((companyName != null) ? "[" + companyName + "]" : "");
    }
}
