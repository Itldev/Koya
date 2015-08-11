package fr.itldev.koya.alfservice;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.ActivityPoster;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;

import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaActivityType;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;

public class KoyaActivityPoster implements InitializingBean {

	public static final char PathSeperatorChar = '/';

	private static Log logger = LogFactory.getLog(ActivityPoster.class);

	private ActivityService activityService;
	private SiteService siteService;
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private HiddenAspect hiddenAspect;
	private KoyaNodeService koyaNodeService;
	private UserService userService;
	private CompanyAclService companyAclService;

	public KoyaActivityPoster() {
	}

	public void setHiddenAspect(HiddenAspect hiddenAspect) {
		this.hiddenAspect = hiddenAspect;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	public void afterPropertiesSet() throws Exception {
		PropertyCheck.mandatory(this, "activityService", activityService);
		PropertyCheck.mandatory(this, "siteService", siteService);
		PropertyCheck.mandatory(this, "nodeService", nodeService);
		PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
		PropertyCheck.mandatory(this, "koyaNodeService", koyaNodeService);
		PropertyCheck.mandatory(this, "userService", userService);
		PropertyCheck.mandatory(this, "companyAclService", companyAclService);
	}

	public void postSpaceShared(String inviteeEmail, String inviterUserName,
			Space sharedSpace) {
		try {

			User user = userService.getUser(inviteeEmail);
			if (user != null && user.isEnabled() != null && user.isEnabled()) {
				// TODO test if user still exists : treat invitation deletion
				// case
				String siteShortName = siteService.getSiteShortName(sharedSpace
						.getNodeRef());

				List<Invitation> invitations = companyAclService
						.getPendingInvite(siteShortName, null,
								user.getUserName());

				if (invitations.isEmpty()) {
					activityService.postActivity(
							KoyaActivityType.KOYA_SPACESHARED,
							siteShortName,
							KoyaActivityType.KOYA_APPTOOL,
							getShareActivityData(user, inviterUserName,
									sharedSpace), user.getUserName());

				}
			}
		} catch (KoyaServiceException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void postSpaceUnshared(String revokedinviteeEmail,
			String revokerUserName, Space unsharedSpace) {
		try {

			User user = userService.getUser(revokedinviteeEmail);
			if (user.isEnabled() != null && user.isEnabled()) {
				// TODO test if user still exists : treat invitation deletion
				// case
				String siteShortName = siteService
						.getSiteShortName(unsharedSpace.getNodeRef());

				List<Invitation> invitations = companyAclService
						.getPendingInvite(siteShortName, null,
								user.getUserName());

				if (invitations.isEmpty()) {
					// TODO call action
					// Posting the according activity
					activityService.postActivity(
							KoyaActivityType.KOYA_SPACEUNUNSHARED,
							siteShortName,
							KoyaActivityType.KOYA_APPTOOL,
							getShareActivityData(user, revokerUserName,
									unsharedSpace), user.getUserName());

				}
			}
		} catch (KoyaServiceException ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	public void postFileFolderAdded(NodeRef nodeRef) {

		if (!hiddenAspect.hasHiddenAspect(nodeRef)) {
			SiteInfo siteInfo = siteService.getSite(nodeRef);
			String siteId = (siteInfo != null ? siteInfo.getShortName() : null);

			if (siteId != null && !siteId.equals("")) {
				// post only for nodes within sites
				NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef)
						.getParentRef();

				String path = null;
				boolean isFolder = isFolder(nodeRef);
				String name = (String) nodeService.getProperty(nodeRef,
						ContentModel.PROP_NAME);

				if (isFolder) {
					NodeRef documentLibrary = siteService.getContainer(siteId,
							SiteService.DOCUMENT_LIBRARY);
					path = "/";
					try {
						path = getPathFromNode(documentLibrary, nodeRef);
					} catch (FileNotFoundException error) {
						if (logger.isDebugEnabled()) {
							logger.debug("No " + SiteService.DOCUMENT_LIBRARY
									+ " container found.");
						}
					}
				}

				Company c = null;
				Dossier d = null;
				try {
					c = koyaNodeService.getFirstParentOfType(nodeRef,
							Company.class);
				} catch (KoyaServiceException kse) {
				}

				try {
					d = koyaNodeService.getFirstParentOfType(nodeRef,
							Dossier.class);
				} catch (KoyaServiceException kse) {
				}

				postFileFolderActivity((isFolder ? ActivityType.FOLDER_ADDED
						: ActivityType.FILE_ADDED), path, parentNodeRef,
						nodeRef, siteId, name, c, d);
			}
		}
	}

	public void postFileFolderUpdated(boolean isFolder, NodeRef nodeRef) {
		if (!hiddenAspect.hasHiddenAspect(nodeRef)) {
			SiteInfo siteInfo = siteService.getSite(nodeRef);
			String siteId = (siteInfo != null ? siteInfo.getShortName() : null);
			if (siteId != null && !siteId.equals("")) {
				// post only for nodes within sites
				String fileName = (String) nodeService.getProperty(nodeRef,
						ContentModel.PROP_NAME);

				Company c = null;
				Dossier d = null;
				try {
					c = koyaNodeService.getFirstParentOfType(nodeRef,
							Company.class);
				} catch (KoyaServiceException kse) {
				}

				try {
					d = koyaNodeService.getFirstParentOfType(nodeRef,
							Dossier.class);
				} catch (KoyaServiceException kse) {
				}

				// add new event folder-updated
				postFileFolderActivity(
						isFolder ? "org.alfresco.documentlibrary.folder-updated"
								: ActivityType.FILE_UPDATED, null, null,
						nodeRef, siteId, fileName, c, d);
			}
		}
	}

	public void postFileFolderDeleted(KoyaActivityInfo activityInfo) {
		if (activityInfo.getSiteId() != null) {
			// post only for nodes within sites
			postFileFolderActivity(
					(activityInfo.isFolder() ? ActivityType.FOLDER_DELETED
							: ActivityType.FILE_DELETED),
					activityInfo.getParentPath(),
					activityInfo.getParentNodeRef(), activityInfo.getNodeRef(),
					activityInfo.getSiteId(), activityInfo.getFileName(),
					activityInfo.getParentCompany(),
					activityInfo.getParentDossier());
		}
	}

	public KoyaActivityInfo getActivityInfo(NodeRef nodeRef) {
		SiteInfo siteInfo = siteService.getSite(nodeRef);
		String siteId = (siteInfo != null ? siteInfo.getShortName() : null);
		if (siteId != null && !siteId.equals("")) {
			NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef)
					.getParentRef();
			FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);
			String name = fileInfo.getName();
			boolean isFolder = fileInfo.isFolder();

			NodeRef documentLibrary = siteService.getContainer(siteId,
					SiteService.DOCUMENT_LIBRARY);
			String parentPath = "/";
			try {
				parentPath = getPathFromNode(documentLibrary, parentNodeRef);
			} catch (FileNotFoundException error) {
				if (logger.isDebugEnabled()) {
					logger.debug("No " + SiteService.DOCUMENT_LIBRARY
							+ " container found.");
				}
			}

			Company c = null;
			try {
				c = koyaNodeService
						.getFirstParentOfType(nodeRef, Company.class);
			} catch (KoyaServiceException kse) {
			}

			Dossier d = null;
			try {
				d = koyaNodeService
						.getFirstParentOfType(nodeRef, Dossier.class);
			} catch (KoyaServiceException kse) {
			}

			return new KoyaActivityInfo(nodeRef, parentPath, parentNodeRef,
					siteId, name, isFolder, c, d);
		} else {
			return null;
		}
	}

	/*
	 * ===== Private Utils ======
	 */

	private void postFileFolderActivity(String activityType, String path,
			NodeRef parentNodeRef, NodeRef nodeRef, String siteId, String name,
			Company parentCompany, Dossier parentDossier) {

		JSONObject json = createActivityJSON(path, parentNodeRef, nodeRef,
				name, parentCompany, parentDossier);

		activityService.postActivity(activityType, siteId,
				KoyaActivityType.KOYA_APPTOOL, json.toString());
	}

	/**
	 * Create JSON suitable for create, modify or delete activity posts. Returns
	 * a new JSONObject containing appropriate key/value pairs.
	 * 
	 * @param tenantDomain
	 * @param nodeRef
	 * @param fileName
	 * @throws WebDAVServerException
	 * @return JSONObject
	 */
	private JSONObject createActivityJSON(String path, NodeRef parentNodeRef,
			NodeRef nodeRef, String fileName, Company parentCompany,
			Dossier parentDossier) {
		JSONObject json = new JSONObject();
		try {
			json.put("nodeRef", nodeRef);

			if (parentNodeRef != null) {
				// Used for deleted files.
				json.put("parentNodeRef", parentNodeRef);
			}

			if (path != null) {
				// Used for deleted files and folders (added or deleted)
				json.put("page", "documentlibrary?path=" + path);
			} else {
				// Used for added or modified files.
				json.put("page", "document-details?nodeRef=" + nodeRef);
			}
			json.put("title", fileName);

			if (parentCompany != null) {
				json.put("koyaParentCompanyNodeRef", parentCompany.getNodeRef()
						.toString());
				json.put("koyaParentCompanyTitle", parentCompany.getTitle());
			}

			if (parentDossier != null) {
				json.put("koyaParentDossierNodeRef", parentDossier.getNodeRef()
						.toString());
				json.put("koyaParentDossierTitle", parentDossier.getTitle());
			}

		} catch (JSONException error) {
			throw new AlfrescoRuntimeException("", error);
		}

		return json;
	}

	protected String getShareActivityData(User invitee, String inviter,
			Space space) throws KoyaServiceException {
		try {

			JSONObject activityData = new JSONObject();
			activityData.put("email", invitee.getEmail());
			activityData.put("spaceTitle", space.getTitle());

			activityData.put("spaceNodeRef", space.getNodeRef());
			activityData.put("inviter", inviter);
			return activityData.toString();
		} catch (JSONException jsonEx) {
			throw new KoyaServiceException(0);// TODO define error code
		}

	}

	private boolean isFolder(NodeRef nodeRef) {
		QName typeQName = nodeService.getType(nodeRef);
		FileFolderServiceType type = fileFolderService.getType(typeQName);
		boolean isFolder = type.equals(FileFolderServiceType.FOLDER);
		return isFolder;
	}

	private final String getPathFromNode(NodeRef rootNodeRef, NodeRef nodeRef)
			throws FileNotFoundException {
		// Check if the nodes are valid, or equal
		if (rootNodeRef == null || nodeRef == null)
			throw new IllegalArgumentException(
					"Invalid node(s) in getPathFromNode call");

		// short cut if the path node is the root node
		if (rootNodeRef.equals(nodeRef))
			return "";

		// get the path elements
		List<FileInfo> pathInfos = fileFolderService.getNamePath(rootNodeRef,
				nodeRef);

		// build the path string
		StringBuilder sb = new StringBuilder(pathInfos.size() * 20);
		for (FileInfo fileInfo : pathInfos) {
			sb.append(PathSeperatorChar);
			sb.append(fileInfo.getName());
		}
		// done
		if (logger.isDebugEnabled()) {
			logger.debug("Build name path for node: \n" + "   root: "
					+ rootNodeRef + "\n" + "   target: " + nodeRef + "\n"
					+ "   path: " + sb);
		}
		return sb.toString();
	}

	public static class KoyaActivityInfo {
		private NodeRef nodeRef;
		private String parentPath;
		private NodeRef parentNodeRef;
		private String siteId;
		private String fileName;
		private boolean isFolder;
		private Company parentCompany;
		private Dossier parentDossier;

		public KoyaActivityInfo(NodeRef nodeRef, String parentPath,
				NodeRef parentNodeRef, String siteId, String fileName,
				boolean isFolder, Company parentCompany, Dossier parentDossier) {
			super();
			this.nodeRef = nodeRef;
			this.parentPath = parentPath;
			this.parentNodeRef = parentNodeRef;
			this.siteId = siteId;
			this.fileName = fileName;
			this.isFolder = isFolder;
			this.parentCompany = parentCompany;
			this.parentDossier = parentDossier;
		}

		public NodeRef getNodeRef() {
			return nodeRef;
		}

		public String getParentPath() {
			return parentPath;
		}

		public NodeRef getParentNodeRef() {
			return parentNodeRef;
		}

		public String getSiteId() {
			return siteId;
		}

		public String getFileName() {
			return fileName;
		}

		public boolean isFolder() {
			return isFolder;
		}

		public Company getParentCompany() {
			return parentCompany;
		}

		public Dossier getParentDossier() {
			return parentDossier;
		}
	}

}
