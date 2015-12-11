package fr.itldev.koya.action.notification;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaActivityPoster;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaNode;
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

	protected KoyaNodeService koyaNodeService;
	protected UserService userService;
	protected KoyaActivityPoster koyaActivityPoster;
	protected AuthenticationService authenticationService;

	// <editor-fold defaultstate="collapsed" desc="Getters/Setters">

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setKoyaActivityPoster(KoyaActivityPoster koyaActivityPoster) {
		this.koyaActivityPoster = koyaActivityPoster;
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

			// list user shares to post space shared activity
			for (Space space : userService.getSharedKoyaNodes(
					authenticationService.getCurrentUserName(), c)) {
				koyaActivityPoster.postSpaceShared(user.getUserName(), "", space);
				// inviter is ommited
			}
		} catch (KoyaServiceException ex) {
			logger.error("Error while posting shared activity " + ex.toString());
		}

	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	}

}
