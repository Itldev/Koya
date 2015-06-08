package fr.itldev.koya.activities.feed;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.activities.feed.RepoCtx;
import org.alfresco.repo.activities.feed.local.LocalFeedTaskProcessor;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.NotificationType;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.model.permissions.SitePermission;

/**
 * Koya Local Feed task processor
 * 
 * 
 * 
 * 
 */
public class KoyaLocalFeedTaskProcessor extends LocalFeedTaskProcessor {
	private static final Log logger = LogFactory
			.getLog(KoyaLocalFeedTaskProcessor.class);
	private SiteService siteService;
	private PermissionService permissionService;
	private KoyaMailService koyaMailService;
	private KoyaNodeService koyaNodeService;
	private NodeService nodeService;

	@Override
	public void setPermissionService(PermissionService permissionService) {
		super.setPermissionService(permissionService);
		this.permissionService = permissionService;
	}

	@Override
	public void setSiteService(SiteService siteService) {
		super.setSiteService(siteService);
		this.siteService = siteService;
	}

	@Override
	public void setNodeService(NodeService nodeService) {
		super.setNodeService(nodeService);
		this.nodeService = nodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setKoyaMailService(KoyaMailService koyaMailService) {
		this.koyaMailService = koyaMailService;
	}

	public void process(int jobTaskNode, long minSeq, long maxSeq, RepoCtx ctx)
			throws Exception {
		long startTime = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			logger.debug("Process: jobTaskNode '" + jobTaskNode
					+ "' from seq '" + minSeq + "' to seq '" + maxSeq
					+ "' on this node from grid job.");
		}

		ActivityPostEntity selector = new ActivityPostEntity();
		selector.setJobTaskNode(jobTaskNode);
		selector.setMinId(minSeq);
		selector.setMaxId(maxSeq);
		selector.setAppTool(NotificationType.KOYA_APPTOOL);
		selector.setStatus(ActivityPostEntity.STATUS.POSTED.toString());

		List<ActivityPostEntity> activityPosts = null;
		int totalGenerated = 0;

		try {
			activityPosts = selectPosts(selector);

			if (logger.isDebugEnabled()) {
				logger.debug("Process: " + activityPosts.size()
						+ " activity posts");
			}

			// for each activity post ...
			for (final ActivityPostEntity activityPost : activityPosts) {

				// Get recipients of this post
				Set<String> recipients = getRecipients(activityPost);

				if (logger.isDebugEnabled()) {
					logger.debug("Activity " + activityPost.getActivityType()
							+ ">>> " + recipients);
				}

				try {
					startTransaction();

					for (String recipient : recipients) {
						ActivityFeedEntity feed = new ActivityFeedEntity();

						// Generate activity feed summary
						feed.setFeedUserId(recipient);
						feed.setPostUserId(activityPost.getUserId());
						feed.setActivityType(activityPost.getActivityType());

						String activitySummary = null;
						// allows JSON to simply pass straight through

						activitySummary = activityPost.getActivityData();
						feed.setActivitySummary(activitySummary);
						feed.setSiteNetwork(activityPost.getSiteNetwork());
						feed.setAppTool(activityPost.getAppTool());
						feed.setPostDate(activityPost.getPostDate());
						feed.setPostId(activityPost.getId());
						feed.setFeedDate(new Date());

						// Insert activity feed
						insertFeedEntry(feed);
						totalGenerated++;

					}

					updatePostStatus(activityPost.getId(),
							ActivityPostEntity.STATUS.PROCESSED);

					commitTransaction();

					if (logger.isDebugEnabled()) {
						logger.debug("Processed: " + recipients.size()
								+ " connections for activity post "
								+ activityPost.getId() + ")");
					}
				} finally {
					endTransaction();
				}

				// TODO do it in a new transaction
				// Send email alerts
				if (Arrays.asList(SHARING_ACTIVITIES).contains(
						activityPost.getActivityType())) {
					// TODO Alert for company users
					sendShareNotificationMail(activityPost);
				}

			}
		} catch (SQLException se) {
			logger.error(se);
			throw se;
		} finally {
			int postCnt = activityPosts == null ? 0 : activityPosts.size();

			// TODO i18n info message
			StringBuilder sb = new StringBuilder();
			sb.append("Generated ").append(totalGenerated)
					.append(" activity feed entr")
					.append(totalGenerated == 1 ? "y" : "ies");
			sb.append(" for ").append(postCnt).append(" activity post")
					.append(postCnt != 1 ? "s" : "").append(" (in ")
					.append(System.currentTimeMillis() - startTime)
					.append(" msecs)");
			logger.info(sb.toString());
		}
	}

	private void sendShareNotificationMail(final ActivityPostEntity activityPost) {
		// external user sharing notification mail
		if (listCompanyMembers(activityPost.getSiteNetwork(),
				SitePermission.CONSUMER).contains(activityPost.getUserId())) {

			AuthenticationUtil
					.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
						@Override
						public Void doWork() throws Exception {
							Map<String, Object> activityPostData = null;
							try {
								activityPostData = new ObjectMapper()
										.readValue(
												activityPost.getActivityData(),
												Map.class);

								NodeRef spaceNodeRef = new NodeRef(
										activityPostData.get("spaceNodeRef")
												.toString());
								// TODO inviter parameter
								koyaMailService.sendShareAlertMail(
										activityPost.getUserId(), null,
										spaceNodeRef);
							} catch (Exception e) {
								logger.warn("Failed to send alert mail : "
										+ e.toString());
							}
							return null;
						}
					});

		}
	}

	private static String[] SHARING_ACTIVITIES = {
			NotificationType.KOYA_SPACESHARED,
			NotificationType.KOYA_SPACEUNUNSHARED };

	private static String[] FILEFOLDER_ACTIVITIES = { ActivityType.FILE_ADDED,
			ActivityType.FOLDER_ADDED, ActivityType.FILES_ADDED,
			ActivityType.FOLDERS_ADDED, ActivityType.FILE_DELETED,
			ActivityType.FOLDER_DELETED, ActivityType.FILE_UPDATED };

	private static String[] COMPANYMEMBERSHIP_ACTIVITIES = {
			ActivityType.SITE_USER_JOINED, ActivityType.SITE_USER_REMOVED };

	/**
	 * 
	 * Activity user filering method
	 * 
	 * TODO process all
	 * 
	 * 
	 */
	private Set<String> getRecipients(final ActivityPostEntity activityPost) {

		NodeRef spaceNodeRef;
		/**
		 * Get nodeRef from activityPost.getActivityData()
		 */

		// ObjectMapper activityPostData = ;
		Map<String, Object> activityPostData = null;
		try {
			activityPostData = new ObjectMapper().readValue(
					activityPost.getActivityData(), Map.class);
		} catch (IOException e) {
		}

		/**
		 * Select users for Space Sharing Activities syndication
		 */
		if (Arrays.asList(SHARING_ACTIVITIES).contains(
				activityPost.getActivityType())) {
			spaceNodeRef = new NodeRef(activityPostData.get("spaceNodeRef")
					.toString());
			// return list of members or responsibles of space
			// + user share destination whatever his role
			Set<String> users = listUsersWithPermission(spaceNodeRef,
					KoyaPermissionCollaborator.RESPONSIBLE,
					KoyaPermissionCollaborator.MEMBER);
			users.add(activityPost.getUserId());
			return users;
		}

		/**
		 * Select users for Files and Folders Activities syndication
		 * 
		 */
		if (Arrays.asList(FILEFOLDER_ACTIVITIES).contains(
				activityPost.getActivityType())) {
			return getFileFolderActivityRecipents(activityPostData,
					activityPost);
		}

		/**
		 * Select users for Company Membership Activities syndication
		 * 
		 */
		if (Arrays.asList(COMPANYMEMBERSHIP_ACTIVITIES).contains(
				activityPost.getActivityType())) {
			return listCompanyMembers(activityPost.getSiteNetwork(),
					SitePermission.MANAGER);
		}

		logger.warn("Unhandled Activity type : "
				+ activityPost.getActivityType());

		return new HashSet<>();
	}

	private Set<String> getFileFolderActivityRecipents(
			final Map<String, Object> activityPostData,
			ActivityPostEntity activityPost) {
		final String[] nodesCandidatesKeys = { "nodeRef", "parentNodeRef" };

		Space s = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Space>() {
					@Override
					public Space doWork() throws Exception {
						for (String nodeRefCandidate : nodesCandidatesKeys) {
							try {
								NodeRef candidate = new NodeRef(
										activityPostData.get(nodeRefCandidate)
												.toString());

								if (nodeService.exists(candidate)) {
									KoyaNode kn = koyaNodeService
											.getKoyaNode(candidate);
									if (kn.getClass().isAssignableFrom(
											Dossier.class)) {
										return (Space) kn;
									}
									return koyaNodeService
											.getFirstParentOfType(candidate,
													Dossier.class);
								}
							} catch (Exception e) {

							}

						}
						return null;
					}
				});

		if (s != null) {

			if (activityPost.getActivityType().equals(ActivityType.FOLDER_DELETED) &&
					s.getClass().equals(Space.class)) {
				/**
				 * Folder deleted is a DOssier > 
				 */
				
				// return list of site manager
//				return listCompanyMembers(activityPost.getSiteNetwork(),
//						SitePermission.MANAGER);
				//TODO return list of responsibles or members on delete node
				return new HashSet<String>();
				
				} else {
				// return list of members or responsibles of space
				return listUsersWithPermission(s.getNodeRef(),
						KoyaPermissionCollaborator.RESPONSIBLE,
						KoyaPermissionCollaborator.MEMBER,
						KoyaPermissionConsumer.CLIENT,
						KoyaPermissionConsumer.CLIENTCONTRIBUTOR,
						KoyaPermissionConsumer.PARTNER);
			}
		}
		return new HashSet<String>();

	}

	public Set<String> listUsersWithPermission(final NodeRef n,
			KoyaPermission... permissions) {

		final List<KoyaPermission> perms = Arrays.asList(permissions);

		return AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Set<String>>() {
					@Override
					public Set<String> doWork() throws Exception {
						Set<String> usersId = new HashSet<>();
						for (AccessPermission ap : permissionService
								.getAllSetPermissions(n)) {
							try {
								if (perms.contains(KoyaPermission.valueOf(ap
										.getPermission()))) {
									usersId.add(ap.getAuthority());
								}
							} catch (IllegalArgumentException iex) {

							}

						}
						return usersId;
					}
				});
	}

	public Set<String> listCompanyMembers(final String companyName,
			final SitePermission permission) {

		return AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Set<String>>() {
					@Override
					public Set<String> doWork() throws Exception {

						try {
							return siteService.listMembers(companyName, "",
									permission.toString(), -1).keySet();
						} catch (SiteDoesNotExistException ex) {
							// silently catch site doesn't exists exception
							return new HashSet<>();
						}
					}
				});
	}

}
