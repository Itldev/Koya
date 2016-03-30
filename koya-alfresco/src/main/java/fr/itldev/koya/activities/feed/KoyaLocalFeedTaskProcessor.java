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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.activities.feed.RepoCtx;
import org.alfresco.repo.activities.feed.local.LocalFeedTaskProcessor;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.model.KoyaActivityType;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
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

	private final static Pattern MEMBERUSERNAME_PATTERN = Pattern
			.compile(".*memberUserName\\\":\\\"([^\\\"]+)\\\".*");

	private SiteService siteService;
	private KoyaMailService koyaMailService;
	private KoyaNodeService koyaNodeService;
	private NodeService nodeService;
	private UserService userService;
	private SpaceAclService spaceAclService;

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

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
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
		selector.setAppTool(KoyaActivityType.KOYA_APPTOOL);
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

				String activitySummary = null;
				// allows JSON to simply pass straight through
				activitySummary = activityPost.getActivityData();

				/**
				 * get user joined recipient email from username and add it to
				 * activitySummary
				 */

				if (activityPost.getActivityType().equals(
						ActivityType.SITE_USER_JOINED)) {

					activitySummary = addMemberEmailForCompanyMemberShipActivities(
							activitySummary, activityPost.getUserId());

				}

				if (activityPost.getActivityType().equals(
						ActivityType.SITE_USER_REMOVED)) {
					try {

						// extract user removed userName from activitySummary
						Matcher m = MEMBERUSERNAME_PATTERN
								.matcher(activitySummary);

						if (m.find()) {
							activitySummary = addMemberEmailForCompanyMemberShipActivities(
									activitySummary, m.group(1));
						}

					} catch (Exception e) {
						// silently continue
					}

				}

				try {
					startTransaction();

					for (String recipient : recipients) {
						ActivityFeedEntity feed = new ActivityFeedEntity();

						// Generate activity feed summary
						feed.setFeedUserId(recipient);
						feed.setPostUserId(activityPost.getUserId());
						feed.setActivityType(activityPost.getActivityType());
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
				
				if ((activityPost.getActivityType().equals(KoyaActivityType.KOYA_CONSUMERUPLOAD))) {
					// TODO Alert for company users
					sendConsumerUploadNotificationMail(activityPost,recipients);
				}
				
				// Send mail for ansync dl file available
				if ((activityPost.getActivityType().equals(KoyaActivityType.KOYA_DLFILEAVAILABLE))) {		
					sendDlNotificationMail(activityPost);					
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

	private String addMemberEmailForCompanyMemberShipActivities(
			String activitySummary, final String memberUserName) {
		User u = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<User>() {
					@Override
					public User doWork() throws Exception {
						return userService.getUserByUsername(memberUserName);
					}
				});
		// add user email to activitySummary
		if (activitySummary.endsWith("}")) {
			activitySummary = activitySummary.substring(0,
					activitySummary.length() - 1)
					+ ",\"memberEmail\":\"" + u.getEmail() + "\"}";
		}
		return activitySummary;
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
								logger.warn("Failed to send share alert mail : "
										+ e.toString());
							}
							return null;
						}
					});

		}
	}
	
	private void sendConsumerUploadNotificationMail(final ActivityPostEntity activityPost,final Set<String> recipients) {
		AuthenticationUtil
		.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@SuppressWarnings("unchecked")
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
					NodeRef docNodeRef =  new NodeRef(
							activityPostData.get("nodeRef")
							.toString());
				

					if (recipients.isEmpty()) {
						logger.warn("No responsible for client document add.  Dossier " + spaceNodeRef.toString());			
					}else{
						koyaMailService.sendClientUploadAlertMail(recipients,activityPost.getUserId(),docNodeRef,spaceNodeRef);
					}
					
				} catch (Exception e) {
					logger.warn("Failed to send consumer upload alert mail : "
							+ e.toString());
				}
				return null;
			}
		});
	}
	
	private void sendDlNotificationMail(final ActivityPostEntity activityPost) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				Map<String, Object> activityPostData = null;
				try {
					activityPostData = new ObjectMapper().readValue(activityPost.getActivityData(),
							Map.class);
					NodeRef dlFileNodeRef = new NodeRef(activityPostData.get("nodeRef").toString());
					String fileName = activityPostData.get("fileName").toString();
					koyaMailService.sendDlFileAvailableAlertMail(activityPost.getUserId(),
							dlFileNodeRef,fileName);
				} catch (Exception e) {
					logger.warn("Failed to send dl File Available alert mail : " + e.toString());
				}
				return null;
			}
		});

	}

	private static String[] SHARING_ACTIVITIES = {
			KoyaActivityType.KOYA_SPACESHARED,
			KoyaActivityType.KOYA_SPACEUNUNSHARED };

	private static String[] FILEFOLDER_ACTIVITIES = { ActivityType.FILE_ADDED,
			ActivityType.FOLDER_ADDED, ActivityType.FILES_ADDED,
			ActivityType.FOLDERS_ADDED, ActivityType.FILE_DELETED,
			ActivityType.FOLDER_DELETED, ActivityType.FILE_UPDATED,
			KoyaActivityType.FOLDER_UPDATED };

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

			List<KoyaPermission> rolesSelector = new ArrayList<KoyaPermission>();
			rolesSelector.add(KoyaPermissionCollaborator.RESPONSIBLE);
			rolesSelector.add(KoyaPermissionCollaborator.MEMBER);

			Set<String> users = spaceAclService.listUsersAuthorities(
					spaceNodeRef, rolesSelector);

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

			// exclude import user
			if (!activityPost.getUserId().equals(
					activityPost.getSiteNetwork() + "_import")) {
				return listCompanyMembers(activityPost.getSiteNetwork(),
						SitePermission.MANAGER);
			} else {
				return new HashSet<>();
			}

		}
		
		/**
		 * Select user for Download File Available Activities syndication
		 */
		if(activityPost.getActivityType().equals(KoyaActivityType.KOYA_DLFILEAVAILABLE)){
			HashSet<String> userNotified = new HashSet<>();		
			userNotified.add(activityPost.getUserId()) ;
			return userNotified;
		}
		
		/**
		 * Select users for Space public upload Activities syndication
		 * 
		 */
		if(activityPost.getActivityType().equals(KoyaActivityType.KOYA_CONSUMERUPLOAD)){
			spaceNodeRef = new NodeRef(activityPostData.get("spaceNodeRef")
					.toString());
			
			List<KoyaPermission> rolesSelector = new ArrayList<KoyaPermission>();
			rolesSelector.add(KoyaPermissionCollaborator.RESPONSIBLE);
			rolesSelector.add(KoyaPermissionCollaborator.MEMBER);			
			Set<String> users = spaceAclService.listUsersAuthorities(
					spaceNodeRef, rolesSelector);
			/**
			 * TODO strategy for empty notifiable users list.
			 */
			
			return users;
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

			if (activityPost.getActivityType().equals(
					ActivityType.FOLDER_DELETED)
					&& s.getClass().equals(Space.class)) {
				/**
				 * Folder deleted is a DOssier >
				 */

				// return list of site manager
				// return listCompanyMembers(activityPost.getSiteNetwork(),
				// SitePermission.MANAGER);
				// TODO return list of responsibles or members on delete node
				return new HashSet<String>();

			} else {

				List<KoyaPermission> rolesSelector = new ArrayList<KoyaPermission>();
				rolesSelector.add(KoyaPermissionCollaborator.RESPONSIBLE);
				rolesSelector.add(KoyaPermissionCollaborator.MEMBER);
				rolesSelector.add(KoyaPermissionConsumer.CLIENT);
				

				return spaceAclService.listUsersAuthorities(s.getNodeRef(),
						rolesSelector);

			}
		}
		return new HashSet<String>();

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
