package fr.itldev.koya.alfservice.security;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.security.AccessPermission;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * This service handle permissions settings for company CONSUMER roles
 * 
 */
public class SpaceConsumersAclService extends SpaceAclService {

	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 * This method si used to share an item (only Dossiers in this first
	 * implementation) with Consumers (ie company Clients)
	 * 
	 * @param space
	 * @param userMail
	 * @param perm
	 * @throws KoyaServiceException
	 */
	@Override
	protected NominatedInvitation shareKoyaNodeImpl(final Space space,
			String userMail, KoyaPermission perm) throws KoyaServiceException {

		if (!Dossier.class.isAssignableFrom(space.getClass())) {
			throw new KoyaServiceException(KoyaErrorCodes.SECU_UNSHARABLE_TYPE);
		}

		if (!KoyaPermissionConsumer.getAll().contains(perm)) {
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_UNSETTABLE_PERMISSION);
		}

		// Get company the shared Node belongs To
		Company company = koyaNodeService.getFirstParentOfType(
				space.getNodeRef(), Company.class);

		SitePermission userPermissionInCompany = companyAclService
				.getSitePermission(company, userMail);

		NominatedInvitation invitation = null;
		// If user can't access specified company then invite him even if he
		// already exists in alfresco
		if (userPermissionInCompany == null) {
			logger.info("[Invite] : {'invitee':'" + userMail + "','company':'"
					+ company + "','permission':'" + SitePermission.CONSUMER
					+ "}");

			invitation = companyAclService.inviteMember(company, userMail,
					SitePermission.CONSUMER, space);

			userPermissionInCompany = companyAclService.getSitePermission(
					company, userMail);
		}

		final User u = userService.getUserByEmailFailOver(userMail);

		/**
		 * User should not already have any permission on node
		 * 
		 */
		AccessPermission existingPermission = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<AccessPermission>() {
					@Override
					public AccessPermission doWork() throws Exception {
						for (AccessPermission ap : permissionService
								.getAllSetPermissions(space.getNodeRef())) {
							if (ap.getAuthority().equals(u.getUserName())) {
								return ap;
							}
						}
						return null;
					}
				});
						
		if(existingPermission != null){
			throw new KoyaServiceException(KoyaErrorCodes.SECU_USER_ALREADY_HAVE_PERMISSION_ON_SPACE);
		}
		

		// Now user should exist for company as a site Consumer member
		if (userPermissionInCompany.equals(SitePermission.CONSUMER)) {

			grantSpacePermission(space, u.getUserName(),
					KoyaPermissionConsumer.CLIENT);

		} else {
			logger.error("Consumer Share not available for "
					+ userPermissionInCompany.toString() + " users");
			throw new KoyaServiceException(
					KoyaErrorCodes.SECU_USER_MUSTBE_CONSUMER_TO_APPLY_PERMISSION);
		}

		return invitation;
	}

}
