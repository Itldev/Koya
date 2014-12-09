package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.NotificationType;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.policies.SharePolicies;
import java.util.List;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.invitation.Invitation;
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
    private CompanyAclService companyAclService;

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

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(SharePolicies.AfterUnsharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterUnshareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail, User inviter, Boolean sharedByImporter) {
        try {
            User user = userService.getUser(userMail);
            if (user.isEnabled() != null && user.isEnabled()) {
                String siteShortName = siteService.getSiteShortName(nodeRef);

                List<Invitation> invitations = companyAclService.getPendingInvite(siteShortName, null, user.getUserName());

                if (invitations.isEmpty()) {
                        //Posting the according activity

                    //TODO call action or use condition
                    activityService.postActivity(NotificationType.KOYA_SHARED,
                            siteShortName, "koya", getActivityData(user, nodeRef), user.getUserName());
                }
            }
        } catch (KoyaServiceException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void afterUnshareItem(NodeRef nodeRef, String userMail, User inviter) {
        try {
            User user = userService.getUser(userMail);
            if (user.isEnabled() != null && user.isEnabled()) {
                String siteShortName = siteService.getSiteShortName(nodeRef);

                List<Invitation> invitations = companyAclService.getPendingInvite(siteShortName, null, user.getUserName());

                if (invitations.isEmpty()) {
                    //TODO call action
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

}
