package fr.itldev.koya.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
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
import fr.itldev.koya.alfservice.security.SpaceCollaboratorsAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.utils.Zips;

public class DossierImportActionExecuter extends ActionExecuterAbstractBase {

	private Log logger = LogFactory.getLog(DossierImportActionExecuter.class);

	public static final String NAME = "koyaDossierImport";

	private SiteService siteService;
	private NodeService nodeService;
	private ContentService contentService;
	private DossierService dossierService;
	private UserService userService;
	private SpaceCollaboratorsAclService spaceCollaboratorsAclService;
	private KoyaNodeService koyaNodeService;
	private KoyaContentService koyaContentService;
	private AuthenticationService authenticationService;
	private BehaviourFilter policyBehaviourFilter;
	private FileFolderService fileFolderService;

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

	public void setSpaceCollaboratorsAclService(
			SpaceCollaboratorsAclService spaceCollaboratorsAclService) {
		this.spaceCollaboratorsAclService = spaceCollaboratorsAclService;
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

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
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

	private static final String PROCESSED_ARCHIVE_DIRECTORY = "processed";

	private static final List<SitePermission> societePermissions = new ArrayList<SitePermission>() {
		{
			add(SitePermission.MANAGER);
			add(SitePermission.COLLABORATOR);
		}
	};

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		String username = authenticationService.getCurrentUserName();
		File tempFile = null;
		File tempDir = null;
		if (!this.nodeService.exists(actionedUponNodeRef)) {
			return;
		}
		policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

		String zipName = (String) nodeService.getProperty(actionedUponNodeRef,
				ContentModel.PROP_NAME);

		if (!zipName.toLowerCase().endsWith(".zip")) {
			return; // Not a zip file.
		}

		StringBuffer sbLog = new StringBuffer();
		ImportDossierStat dossierStat = new ImportDossierStat();
		ImportContentStat contentStat = new ImportContentStat();
		SiteInfo siteInfo = siteService.getSite(actionedUponNodeRef);
		NodeRef documentLibrary = siteService.getContainer(
				siteInfo.getShortName(), SiteService.DOCUMENT_LIBRARY);
		Map<String, Dossier> mapCacheDossier = new HashMap<String, Dossier>();
		Set<Dossier> modifiedDossiers = new HashSet<>();
		try {
			Company company = koyaNodeService.getKoyaNode(
					siteInfo.getNodeRef(), Company.class);
			String companyName = company.getName();
			logger.debug(getLogPrefix(companyName, username)
					+ "Import Xml Started");
			sbLog.append("Import start at " + new Date() + "\n");

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
						+ "Extracting " + zipName + " ("
						+ tempFile.getAbsolutePath() + ")" + " to "
						+ tempDir.getAbsolutePath());

				Zips.unzip(tempFile.getAbsolutePath(),
						tempDir.getAbsolutePath(), defaultZipCharset,
						failoverZipCharset, sbLog);

				dossierStat = importDossier(tempDir, company, documentLibrary,
						mapCacheDossier, modifiedDossiers, sbLog);

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
							failoverZipCharset, sbLog);

					contentStat = importContent(contentsDir, company,
							mapCacheDossier, modifiedDossiers, sbLog);
				}
			}
		} catch (InvalidNodeRefException | InvalidTypeException
				| ContentIOException ex) {
			throw new AlfrescoRuntimeException(getLogPrefix(null, username)
					+ " " + ex.getMessage(), ex);
		} finally {
			NodeRef importDirNodeRef = nodeService.getPrimaryParent(
					actionedUponNodeRef).getParentRef();

			// moving imported zip archive
			NodeRef processedNodeRef = fileFolderService.searchSimple(
					importDirNodeRef, PROCESSED_ARCHIVE_DIRECTORY);
			if (processedNodeRef == null) {
				processedNodeRef = fileFolderService.create(importDirNodeRef,
						PROCESSED_ARCHIVE_DIRECTORY, ContentModel.TYPE_FOLDER)
						.getNodeRef();
			}
			try {
				fileFolderService.move(actionedUponNodeRef, processedNodeRef,
						null);
			} catch (FileExistsException ex) {
				int i = 0;
				String zipFileName = zipName.substring(0,
						zipName.indexOf(".zip"));

				while (fileFolderService.searchSimple(processedNodeRef,
						zipFileName + "-" + (++i) + ".zip") != null) {
				}
				try {
					fileFolderService.move(actionedUponNodeRef,
							processedNodeRef, zipFileName + "-" + i + ".zip");
				} catch (FileExistsException
						| org.alfresco.service.cmr.model.FileNotFoundException ex1) {
					sbLog.append("\nCan't move zip archive to "
							+ PROCESSED_ARCHIVE_DIRECTORY);
					sbLog.append(ex.getMessage());
					logger.error(ex.getMessage(), ex);
				}

			} catch (org.alfresco.service.cmr.model.FileNotFoundException ex) {
				sbLog.append("\nCan't move zip archive to "
						+ PROCESSED_ARCHIVE_DIRECTORY);
				sbLog.append(ex.getMessage());
				logger.error(ex.getMessage(), ex);
			}

			// Writing down the log file
			String logFilename = zipName + ".log";
			NodeRef logNodeRef = fileFolderService.searchSimple(
					importDirNodeRef, logFilename);

			ContentWriter logWriter;
			if (logNodeRef != null) {
				logWriter = fileFolderService.getWriter(logNodeRef);

			} else {
				FileInfo logInfo = fileFolderService.create(importDirNodeRef,
						logFilename, ContentModel.TYPE_CONTENT);
				logWriter = fileFolderService.getWriter(logInfo.getNodeRef());
			}

			// logWriter.putContent(sbLog.toString());
			FileChannel fileChannel = logWriter.getFileChannel(false);
			ByteBuffer bf = ByteBuffer
					.wrap(("=======================\n\n\n" + sbLog.toString())
							.getBytes());
			try {
				fileChannel.position(logWriter.getSize());

				fileChannel.write(bf);
				fileChannel.force(false);

				fileChannel.close();
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}

			deleteDir(tempDir);

			// now the import is done, delete the temporary file
			if (tempFile != null) {
				tempFile.delete();
			}

			logger.info("importZipArchive=" + zipName + ", " + dossierStat
					+ ", " + contentStat);
		}

		for (Dossier d : modifiedDossiers) {
			dossierService.updateLastModificationDate(d);
		}

	}

	private ImportDossierStat importDossier(File tempDir, Company company,
			NodeRef documentLibrary, Map<String, Dossier> mapCacheDossier,
			Set<Dossier> modifiedDossiers, final StringBuffer sbLog) {
		String userName = authenticationService.getCurrentUserName();
		String companyName = company.getName();

		ImportDossierStat dossierStat = new ImportDossierStat();

		// Reading the new dossiers to create
		File fileDossiersXml = new File(tempDir, FILE_DOSSIERS_XML);
		if (fileDossiersXml.exists()) {
			// Create new dossiers
			List<DossierXml> dossierXmls = null;
			try {
				logger.debug(getLogPrefix(companyName, userName)
						+ "Unmarshalling " + fileDossiersXml.getName());
				sbLog.append("\n\nUnmarshalling " + fileDossiersXml.getName());

				JAXBContext context = JAXBContext.newInstance(
						DossiersXmlWrapper.class, DossierXml.class);
				DossiersXmlWrapper dossiersXmlWrapper = (DossiersXmlWrapper) context
						.createUnmarshaller().unmarshal(fileDossiersXml);
				if (dossiersXmlWrapper != null) {
					dossierXmls = dossiersXmlWrapper.getDossiers();
				}

			} catch (JAXBException ex) {
				sbLog.append("Error unmarshalling dossiers metadata. "
						+ ex.getMessage());
				throw new AlfrescoRuntimeException(getLogPrefix(companyName,
						userName) + "Error unmarshalling dossiers metadata.",
						ex);
			}

			if (dossierXmls != null) {
				dossierStat.countDossier = dossierXmls.size();
				logger.debug(getLogPrefix(companyName, userName)
						+ dossierXmls.size() + " dossiers found");
				sbLog.append("\n" + dossierStat.countDossier
						+ " dossiers found");

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
							sbLog.append("\nUpdating dossier " + d.getTitle()
									+ " to " + dossierTitle);
							koyaNodeService.rename(d.getNodeRef(),
									dossierXml.getReference() + " - "
											+ dossierXml.getName());
							d = koyaNodeService.getKoyaNode(d.getNodeRef(),
									Dossier.class);

						} else {
							sbLog.append("\nFound dossier " + d.getTitle());
						}
					} catch (KoyaServiceException kse) {
						if (KoyaErrorCodes.NO_SUCH_DOSSIER_REFERENCE.equals(kse
								.getErrorCode())) {
							sbLog.append("\nCreating dossier "
									+ dossierXml.getReference() + " - "
									+ dossierXml.getName());

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
							sbLog.append("\nCannot create dossier "
									+ dossierXml.getReference() + " : "
									+ kse.getMessage());
							dossierStat.countDossiersError++;
							continue;

						}
					}

					mapCacheDossier.put(dossierXml.getReference(), d);
					dossierStat.countDossiersCreated++;

					logger.trace(getLogPrefix(companyName, userName)
							+ "Get dossier responsibles");
					addKoyaPermissionCollaborator(company, d,
							dossierXml.getResponsibles(),
							KoyaPermissionCollaborator.RESPONSIBLE, newDossier,
							sbLog);

					logger.trace(getLogPrefix(companyName, userName)
							+ "get dossier members");
					addKoyaPermissionCollaborator(company, d,
							dossierXml.getMembers(),
							KoyaPermissionCollaborator.MEMBER, newDossier,
							sbLog);

				}
				logger.debug(getLogPrefix(companyName, userName)
						+ dossierStat.countDossiersCreated
						+ " dossiers created");
				sbLog.append("\n\n" + dossierStat.countDossiersCreated
						+ " dossiers created");
				logger.debug(getLogPrefix(companyName, userName)
						+ dossierStat.countDossiersDuplicate
						+ " dossiers allready existing");
				sbLog.append("\n" + dossierStat.countDossiersDuplicate
						+ " dossiers allready existing");
				logger.debug(getLogPrefix(companyName, userName)
						+ dossierStat.countDossiersError
						+ " dossiers not created (error)");
				sbLog.append("\n" + dossierStat.countDossiersError
						+ " dossiers not created (error)");

			}
		}
		return dossierStat;

	}

	private class ImportDossierStat {
		int countDossier = 0;
		int countDossiersCreated = 0;
		int countDossiersDuplicate = 0;
		int countDossiersError = 0;

		@Override
		public String toString() {
			return "ImportDossierStat{" + "countDossier=" + countDossier
					+ ", countDossiersCreated=" + countDossiersCreated
					+ ", countDossiersDuplicate=" + countDossiersDuplicate
					+ ", countDossiersError=" + countDossiersError + '}';
		}
	}

	private ImportContentStat importContent(File contentsDir, Company company,
			Map<String, Dossier> mapCacheDossier,
			Set<Dossier> modifiedDossiers, final StringBuffer sbLog) {
		ImportContentStat contentStat = new ImportContentStat();

		String userName = authenticationService.getCurrentUserName();
		String companyName = company.getName();

		// Reading contents files descriptor
		File fileContentXml = new File(contentsDir.getPath(), FILE_CONTENT_XML);
		if (!fileContentXml.exists()) {
			sbLog.append("\nNo files content metadata file found.");
			throw new AlfrescoRuntimeException(getLogPrefix(companyName,
					userName) + "No files content metadata file found.");
		}
		// Create new content
		List<ContentXml> contentXmls = null;
		try {
			logger.debug(getLogPrefix(companyName, userName) + "Unmarshalling "
					+ fileContentXml.getName());
			sbLog.append("\n\nUnmarshalling " + fileContentXml.getName());
			JAXBContext context = JAXBContext.newInstance(
					ContentsXmlWrapper.class, ContentXml.class);

			ContentsXmlWrapper contentsXmlWrapper = (ContentsXmlWrapper) context
					.createUnmarshaller().unmarshal(fileContentXml);

			if (contentsXmlWrapper != null) {
				contentXmls = contentsXmlWrapper.getContentXmls();
			}

		} catch (JAXBException ex) {
			sbLog.append("\nError unmarshalling dossiers metadata."
					+ ex.getMessage());
			throw new AlfrescoRuntimeException(getLogPrefix(companyName,
					userName) + "Error unmarshalling dossiers metadata.", ex);
		}

		if (contentXmls == null) {
			return contentStat;
		}

		contentStat.countContent = contentXmls.size();
		logger.debug(getLogPrefix(companyName, userName) + contentXmls.size()
				+ " contents found");
		sbLog.append("\n\n" + contentStat.countContent + " contents found");

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
					dirNodeRef = dossier.getNodeRef();
				} else {
					dirNodeRef = koyaContentService.makeFolders(
							dossier.getNodeRef(),
							Arrays.asList(path.split(File.separator)))
							.getNodeRef();
					pathCreated = true;
				}

				filename = contentXml.getFilename();
				if (filename != null && !filename.trim().isEmpty()) {
					int extIdx = filename.lastIndexOf(".");
					title = contentXml.getName()
							+ (extIdx != -1 ? filename.substring(extIdx) : "");
					logger.trace(getLogPrefix(companyName, userName)
							+ "Adding " + filename + " to /" + path + " as "
							+ title);
					sbLog.append("\nAdding " + filename + " to /"
							+ dossier.getTitle() + "/" + path);
					if (nodeService.getChildByName(dirNodeRef,
							ContentModel.ASSOC_CONTAINS, filename) == null) {
						koyaContentService.createContentNode(dirNodeRef, title,
								filename, new FileInputStream(new File(
										contentsDir, filename)));
						modifiedDossiers.add(dossier);

						contentStat.countContentAdded++;
					} else {
						logger.error(getLogPrefix(companyName, userName)
								+ "File " + filename + " - " + title
								+ " already exist in /" + dossier.getTitle()
								+ "/" + path);
						sbLog.append("\nFile " + filename + " - " + title
								+ " already exist in /" + dossier.getTitle()
								+ "/" + path);

						contentStat.countContentError++;
					}
				} else if (pathCreated) {
					contentStat.countPathCreation++;
				}
			} catch (FileNotFoundException ex) {
				logger.error(ex.getMessage(), ex);
				sbLog.append("\n" + filename + " not found");
				contentStat.countContentFileNotFound++;
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
					sbLog.append("\nDossier "
							+ contentXml.getDossierReference() + " not found");
					contentStat.countContentDossierNotFound++;
				} else if (errorCode
						.equals(KoyaErrorCodes.FILE_UPLOAD_NAME_EXISTS)) {
					logger.error(getLogPrefix(companyName, userName) + "File "
							+ filename + " - " + title + " already exist");
					sbLog.append("\nFile " + filename + " - " + title
							+ " already exist in dossier");
					contentStat.countContentDuplicate++;
				} else {

				}
			}
		}

		logger.debug(getLogPrefix(companyName, userName)
				+ contentStat.countContentAdded + " contents added");
		sbLog.append("\n" + contentStat.countContentAdded + " contents added");
		logger.debug(getLogPrefix(companyName, userName)
				+ contentStat.countContentDuplicate
				+ " contents duplicates name");
		sbLog.append("\n" + contentStat.countContentDuplicate
				+ " contents duplicates name");
		logger.debug(getLogPrefix(companyName, userName)
				+ contentStat.countContentFileNotFound + " files not found");
		sbLog.append("\n" + contentStat.countContentFileNotFound
				+ " files not found");
		logger.debug(getLogPrefix(companyName, userName)
				+ contentStat.countContentDossierNotFound
				+ "contents' dossiers not found");
		sbLog.append("\n" + contentStat.countContentDossierNotFound
				+ "contents' dossiers not found");
		logger.debug(getLogPrefix(companyName, userName)
				+ contentStat.countContentError + " contents not added (error)");
		sbLog.append("\n" + contentStat.countContentError
				+ " contents not added (error)");
		logger.debug(getLogPrefix(companyName, userName)
				+ contentStat.countPathCreation + " empty path");
		sbLog.append("\n" + contentStat.countPathCreation + " empty path");

		return contentStat;

	}

	private class ImportContentStat {
		int countContent = 0;
		int countContentAdded = 0;
		int countContentDuplicate = 0;
		int countContentError = 0;
		int countContentFileNotFound = 0;
		int countContentDossierNotFound = 0;
		int countPathCreation = 0;

		@Override
		public String toString() {
			return "ImportContentStat{" + "countContent=" + countContent
					+ ", countContentAdded=" + countContentAdded
					+ ", countContentDuplicate=" + countContentDuplicate
					+ ", countContentError=" + countContentError
					+ ", countContentFileNotFound=" + countContentFileNotFound
					+ ", countContentDossierNotFound="
					+ countContentDossierNotFound + ", countPathCreation="
					+ countPathCreation + '}';
		}

	}

	private void addKoyaPermissionCollaborator(Company c, Dossier d,
			List<String> usersMail,
			final KoyaPermissionCollaborator permissionCollaborator,
			boolean newDossier, final StringBuffer sbLog) {
		if (!newDossier) {
			// Removing not responsible anymore
			List<User> currentUsers = spaceCollaboratorsAclService
					.listUsers(d, new ArrayList<KoyaPermission>() {
						{
							add(permissionCollaborator);
						}
					});

			for (User u : currentUsers) {
				if (!usersMail.contains(u.getEmail())) {
					spaceCollaboratorsAclService.unShareKoyaNode(d,
							u.getEmail(), permissionCollaborator);
				}
			}
		}

		for (String userMail : usersMail) {
			User u = userService.getUserByEmailFailOver(userMail);
			if (u != null) {

				try {
					if (societePermissions.contains(SitePermission
							.valueOf(siteService.getMembersRole(c.getName(),
									u.getUserName())))) {
						logger.trace(getLogPrefix(c.getName(), null)
								+ "Adding " + userMail + " as "
								+ permissionCollaborator);
						sbLog.append("\nAdding " + userMail + " as "
								+ permissionCollaborator);
						spaceCollaboratorsAclService.shareKoyaNode(
								(Space) koyaNodeService.getKoyaNode(d
										.getNodeRef()), userService
										.getUserByUsername(u.getUserName())
										.getEmail(), permissionCollaborator,
								true);
					}
				} catch (KoyaServiceException ex) {
					logger.error(
							getLogPrefix(c.getName(), null) + "Error adding "
									+ userMail + " AS "
									+ permissionCollaborator + " to dossier "
									+ d.getTitle(), ex);
					sbLog.append("\nError adding " + userMail + " AS "
							+ permissionCollaborator + " to dossier "
							+ d.getTitle());
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
