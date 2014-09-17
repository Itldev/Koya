package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
<<<<<<< HEAD
=======
import fr.itldev.koya.model.NotificationType;
>>>>>>> Post activity for share and unshare of dossiers
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.policies.SharePolicies;
import java.util.List;
import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

public class ShareNotificationBehaviour implements SharePolicies.AfterSharePolicy, SharePolicies.AfterUnsharePolicy {

    protected static Log logger = LogFactory.getLog(ShareNotificationBehaviour.class);

    private ActivityService activityService;
    private SiteService siteService;
    private UserService userService;
    private PolicyComponent policyComponent;
    private InvitationService invitationService;

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setInvitationService(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(SharePolicies.AfterUnsharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterUnshareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail, Invitation invitation, User inviter) {
        if (invitation == null) {
            try {
                User user = userService.getUser(userMail);
                if (user.isEnabled() != null && user.isEnabled()) {
                    String siteShortName = siteService.getSiteShortName(nodeRef);

                    List<Invitation> invitations = getPendingInvite(siteShortName, null, user.getUserName());

                    if (invitations.isEmpty()) {
                        //Posting the according activity
                        activityService.postActivity(NotificationType.KOYA_SHARED, siteShortName, "koya", getActivityData(user, nodeRef), user.getUserName());

                        /**
                         * TODO send koya specific sharing alert
                         */
                    }
                }
            } catch (KoyaServiceException ex) {
                logger.error(ex.getMessage(), ex);
            }

            logger.error("TODO send subspace sharing alert by email");
        } else {
            //Nothing to do : invitation sent 
        }

    }

    @Override
    public void afterUnshareItem(NodeRef nodeRef, String userMail, User inviter) {
        try {
            User user = userService.getUser(userMail);
            if (user.isEnabled() != null && user.isEnabled()) {
                String siteShortName = siteService.getSiteShortName(nodeRef);

                List<Invitation> invitations = getPendingInvite(siteShortName, null, user.getUserName());

                if (invitations.isEmpty()) {
                    //Posting the according activity
                    activityService.postActivity(NotificationType.KOYA_UNSHARED, siteShortName, "koya", getActivityData(user, nodeRef), user.getUserName());

                }
            }
        } catch (KoyaServiceException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Helper method to get the activity data for a user
     *
     * @param userName user name
     * @param role role
     * @return
     */
    private String getActivityData(User user, NodeRef nodeRef) throws KoyaServiceException {
        String memberFN = user.getFirstName();
        String memberLN = user.getName();
        String userMail = user.getEmail();

        JSONObject activityData = new JSONObject();
        activityData.put("memberUserName", user.getEmail());
        activityData.put("memberFirstName", memberFN);
        activityData.put("memberLastName", memberLN);
        activityData.put("title", (memberFN + " " + memberLN + " ("
                + userMail + ")").trim());
        activityData.put("nodeRef", nodeRef.toString());
        return activityData.toString();
    }

    /**
     * Helper method to get the pending invite
     *
     */
    private List<Invitation> getPendingInvite(String companyId, String inviterId, String inviteeId) {
        InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
        criteria.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);
        criteria.setResourceType(Invitation.ResourceType.WEB_SITE);

        if (inviterId != null) {
            criteria.setInviter(inviterId);
        }
        if (inviteeId != null) {
            criteria.setInvitee(inviteeId);
        }
        if (companyId != null) {
            criteria.setResourceName(companyId);
        }

        return invitationService.searchInvitation(criteria);

    }
}
