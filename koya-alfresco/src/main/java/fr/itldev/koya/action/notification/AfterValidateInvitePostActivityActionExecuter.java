package fr.itldev.koya.action.notification;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.NotificationType;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;

/**
 * Post share activities on defined company all availables dossiers after
 * validating invitation
 */
public class AfterValidateInvitePostActivityActionExecuter extends
		ActionExecuterAbstractBase {

	private final Logger logger = Logger.getLogger(this.getClass());

	public static final String NAME = "afterValidateInvitePostActivity";

	protected SpaceService spaceService;
	protected DossierService dossierService;
	protected KoyaNodeService koyaNodeService;
	protected ActivityService activityService;
	protected UserService userService;
	protected AuthenticationService authenticationService;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">
	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setDossierService(DossierService dossierService) {
		this.dossierService = dossierService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	// </editor-fold>
	@Override
	protected void executeImpl(Action action, final NodeRef actionedUponNodeRef) {

		User user = userService.getUserByUsername(authenticationService
				.getCurrentUserName());

		try {
			Company c = koyaNodeService.getKoyaNode(actionedUponNodeRef,
					Company.class);

			for (Space s : spaceService.list(c.getName(), Integer.MAX_VALUE)) {
				for (Dossier d : dossierService.list(s.getNodeRef())) {
					activityService.postActivity(NotificationType.KOYA_SHARED,
							c.getName(), "koya",
							getActivityData(user, d.getNodeRef()),
							user.getUserName());
				}
			}
		} catch (KoyaServiceException ex) {
			logger.error("Error while posting shared activity " + ex.toString());
		}

	}

	/**
	 * Helper method to get the activity data for a user
	 * 
	 * @param userName
	 *            user name
	 * @param role
	 *            role
	 * @return
	 */
	private String getActivityData(User user, NodeRef nodeRef)
			throws KoyaServiceException {
		String memberFN = user.getFirstName();
		String memberLN = user.getName();
		String userMail = user.getEmail();

		JSONObject activityData = new JSONObject();
		activityData.put("memberUserName", userMail);
		activityData.put("memberFirstName", memberFN);
		activityData.put("memberLastName", memberLN);
		activityData.put("title",
				(memberFN + " " + memberLN + " (" + userMail + ")").trim());
		activityData.put("nodeRef", nodeRef.toString());
		return activityData.toString();
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {	
	}

}
