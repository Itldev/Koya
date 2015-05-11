package fr.itldev.koya.action.notification;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.NotificationType;
import fr.itldev.koya.model.impl.User;

public class ShareSpaceActivityPoster extends KoyaActivityPoster {

	public static final String NAME = "shareSpaceActivityPoster";
	private final Logger logger = Logger.getLogger(this.getClass());

	private SiteService siteService;
	private UserService userService;
	private CompanyAclService companyAclService;

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setCompanyAclService(CompanyAclService companyAclService) {
		this.companyAclService = companyAclService;
	}

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		try {

			String inviteeEmail = (String) action
					.getParameterValue(KoyaActivityPoster.SHARE_USER_MAILINVITEE);
			logger.error("Share space with " + inviteeEmail);

			User user = userService.getUser(inviteeEmail);
			if (user.isEnabled() != null && user.isEnabled()) {
				String siteShortName = siteService.getSiteShortName(actionedUponNodeRef);

				List<Invitation> invitations = companyAclService
						.getPendingInvite(siteShortName, null,
								user.getUserName());

				if (invitations.isEmpty()) {
					// Posting the according activity

					activityService.postActivity(NotificationType.KOYA_SHARED,
							siteShortName, "koya",
							getShareActivityData(user, actionedUponNodeRef), user.getUserName());
				}
			}
		} catch (KoyaServiceException ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

}
