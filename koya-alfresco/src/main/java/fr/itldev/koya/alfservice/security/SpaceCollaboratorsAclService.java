package fr.itldev.koya.alfservice.security;

import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * This service handle permissions settings for company COLLABORATOR roles
 * 
 */
public class SpaceCollaboratorsAclService extends SpaceAclService {

	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 * This method si used to share an item (only Dossiers in this first
	 * implementation) with Collaborators (ie company Clients)
	 * 
	 * @param space
	 * @param userMail
	 * @param perm
	 * @return
	 * @throws KoyaServiceException
	 */
	@Override
	protected NominatedInvitation shareKoyaNodeImpl(Space space, String userMail,
			KoyaPermission perm) throws KoyaServiceException {

		if (!Dossier.class.isAssignableFrom(space.getClass())) {
			throw new KoyaServiceException(KoyaErrorCodes.SECU_UNSHARABLE_TYPE);
		}

		if (!KoyaPermissionCollaborator.getAll().contains(perm)) {
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_UNSETTABLE_PERMISSION);
		}

		logger.debug("Grant Collaborator permission  " + perm + " to user "
				+ userMail + " on " + space.getName() + "("
				+ space.getClass().getSimpleName() + ")");

		// Get company the shared Node belongs To
		Company company = koyaNodeService.getFirstParentOfType(
				space.getNodeRef(), Company.class);
		SitePermission userPermissionInCompany = companyAclService
				.getSitePermission(company, userMail);

		if (userPermissionInCompany == null) {
			// user must already be a company member to get
			// KoyaPermissionCollaborator permission type
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_USER_MUSTBE_COLLABORATOR_OR_ADMIN_TO_APPLY_PERMISSION);
		}

		User u = userService.getUserByEmailFailOver(userMail);

		// user should exist for company as a site Collaborator or site manager
		// member
		if (userPermissionInCompany.equals(SitePermission.COLLABORATOR)
				|| userPermissionInCompany.equals(SitePermission.MANAGER)) {
			grantSpacePermission(space, u.getUserName(), perm);
		} else {
			logger.error("Collaborator Share not available for "
					+ userPermissionInCompany.toString() + " users");
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_USER_MUSTBE_COLLABORATOR_OR_ADMIN_TO_APPLY_PERMISSION);
		}
		return null;
	}
}
