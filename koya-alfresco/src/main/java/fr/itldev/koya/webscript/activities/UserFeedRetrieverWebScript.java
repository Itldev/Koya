package fr.itldev.koya.webscript.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.util.JSONtoFmModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Java-backed WebScript to retrieve Activity User Feed
 */
public class UserFeedRetrieverWebScript extends DeclarativeWebScript {

    private static final Log logger = LogFactory.getLog(UserFeedRetrieverWebScript.class);

    // URL request parameter names
    public static final String PARAM_SITE_ID = "s";
    public static final String PARAM_EXCLUDE_THIS_USER = "exclUser";
    public static final String PARAM_EXCLUDE_OTHER_USERS = "exclOthers";
    public static final String PARAM_ONLY_FOLLOWING = "following";
    public static final String PARAM_ACTIVITY_FILTER = "activityFilter";
    public static final String PARAM_MIN_FEED_ID = "minFeedId";

    private ActivityService activityService;
    private SubscriptionService subscriptionService;

    private boolean userNamesAreCaseSensitive = false;

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive) {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status) {
        // retrieve requested format
        String format = req.getFormat();
        if (format == null || format.length() == 0) {
            format = getDescription().getDefaultFormat();
        }

        // process extension
        String extensionPath = req.getExtensionPath();
        String[] extParts = extensionPath == null ? new String[1] : extensionPath.split("/");

        String feedUserId = null;
        if (extParts.length == 1) {
            feedUserId = extParts[0];
        } else if (extParts.length > 1) {
            throw new AlfrescoRuntimeException("Unexpected extension: " + extensionPath);
        }

        // process arguments
        String siteId = req.getParameter(PARAM_SITE_ID); // optional
        String exclThisUserStr = req.getParameter(PARAM_EXCLUDE_THIS_USER); // optional
        String exclOtherUsersStr = req.getParameter(PARAM_EXCLUDE_OTHER_USERS); // optional
        String onlyFollowingStr = req.getParameter(PARAM_ONLY_FOLLOWING); // optional
        String activityFilterStr = req.getParameter(PARAM_ACTIVITY_FILTER); // optional
        String minFeedIdStr = req.getParameter(PARAM_MIN_FEED_ID);

        if (siteId != null && siteId.trim().isEmpty()) {
            siteId = null;
        }

        boolean exclThisUser = false;
        if ((exclThisUserStr != null) && (exclThisUserStr.equalsIgnoreCase("true") || exclThisUserStr.equalsIgnoreCase("t"))) {
            exclThisUser = true;
        }

        boolean exclOtherUsers = false;
        if ((exclOtherUsersStr != null) && (exclOtherUsersStr.equalsIgnoreCase("true") || exclOtherUsersStr.equalsIgnoreCase("t"))) {
            exclOtherUsers = true;
        }

        Set<String> userFilter = null;
        if ((onlyFollowingStr != null) && (onlyFollowingStr.equalsIgnoreCase("true") || onlyFollowingStr.equalsIgnoreCase("t"))) {
            userFilter = new HashSet<String>();
            if (subscriptionService.isActive()) {
                PagingFollowingResults following = subscriptionService.getFollowing(AuthenticationUtil.getRunAsUser(), new PagingRequest(-1, null));
                if (following.getPage() != null) {
                    for (String userName : following.getPage()) {
                        userFilter.add(this.userNamesAreCaseSensitive ? userName : userName.toLowerCase());
                    }
                }
            }
        }

        Set<String> activityFilter = null;
        if (activityFilterStr != null) {
            activityFilter = new HashSet<String>();
            String[] activities = activityFilterStr.split(",");
            for (String s : activities) {
                if (s.trim().length() > 0) {
                    activityFilter.add(s.trim());
                }
            }
            if (activityFilter.size() == 0) {
                activityFilter = null;
            }
        }

        if ((feedUserId == null) || (feedUserId.length() == 0)) {
            feedUserId = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        Integer minFeedId = Integer.valueOf(-1);
        if (minFeedIdStr != null && !minFeedIdStr.trim().isEmpty()) {
            try {
                minFeedId = Integer.valueOf(minFeedIdStr);
            } catch (NumberFormatException nfe) {
            }
        }

        // map feed collection format to feed entry format (if not the same), eg.
        //     atomfeed -> atomentry
        //     atom     -> atomentry
        if (format.equals("atomfeed") || format.equals("atom")) {
            format = "atomentry";
        }

        Map<String, Object> model = new HashMap<String, Object>();

        try {

            List<ActivityFeedEntity> activityFeeds = activityService.getUserFeedEntries(feedUserId, siteId, exclThisUser, exclOtherUsers, userFilter, activityFilter, minFeedId);

            List<String> feedEntries = new ArrayList<>();
            try {
                if (activityFeeds != null) {
                    for (ActivityFeedEntity activityFeed : activityFeeds) {
                        feedEntries.add(activityFeed.getJSONString());
                    }
                }
                if (format.equals(FeedTaskProcessor.FEED_FORMAT_JSON)) {
                    model.put("feedEntries", feedEntries);
                    model.put("siteId", siteId);
                } else {
                    List<Map<String, Object>> activityFeedModels = new ArrayList<Map<String, Object>>();

                    for (String feedEntry : feedEntries) {
                        activityFeedModels.add(JSONtoFmModel.convertJSONObjectToMap(feedEntry));
                    }

                    model.put("feedEntries", activityFeedModels);
                    model.put("feedUserId", feedUserId);
                }
            } catch (JSONException je) {
                throw new AlfrescoRuntimeException("Unable to get user feed entries: " + je.getMessage());
            }
        } catch (AccessDeniedException ade) {
            status.setCode(Status.STATUS_UNAUTHORIZED);
            logger.warn("Unable to get user feed entries for '" + feedUserId + "' - currently logged in as '" + AuthenticationUtil.getFullyAuthenticatedUser() + "'");
            return null;
        }

        return model;
    }
}
