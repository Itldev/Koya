package fr.itldev.koya.alfservice.security;

import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import org.alfresco.service.cmr.invitation.Invitation;
import org.apache.log4j.Logger;

/**
 * This service handle permissions settings for company COLLABORATOR roles
 *
 */
public class SubSpaceCollaboratorsAclService extends SubSpaceAclService {

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * This method si used to share an item (only Dossiers in this first
     * implementation) with Collaborators (ie company Clients)
     *
     * @param subSpace
     * @param userMail
     * @param perm
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     * @return
     * @throws KoyaServiceException
     */
    @Override
    protected Invitation shareSecuredItemImpl(SubSpace subSpace, String userMail, KoyaPermission perm,
            String serverPath, String acceptUrl, String rejectUrl) throws KoyaServiceException {

        if (!Dossier.class.isAssignableFrom(subSpace.getClass())) {
            throw new KoyaServiceException(KoyaErrorCodes.SECU_UNSHARABLE_TYPE);
        }

        if (!KoyaPermissionCollaborator.getAll().contains(perm)) {
            throw new KoyaServiceException(KoyaErrorCodes.SECU_UNSETTABLE_PERMISSION);
        }

        logger.debug("Grant Collaborator permission  " + perm + " to user "
                + userMail + " on " + subSpace.getName() + "(" + subSpace.getClass().getSimpleName() + ")");

        //Get company the shared Node belongs To
        Company company = koyaNodeService.getCompany(subSpace.getNodeRefasObject());
        SitePermission userPermissionInCompany = companyAclService.getSitePermission(company, userMail);
               
        if (userPermissionInCompany == null) {
            //user must already be a company member to get KoyaPermissionCollaborator permission type
            throw new KoyaServiceException(KoyaErrorCodes.SECU_USER_MUSTBE_COLLABORATOR_OR_ADMIN_TO_APPLY_PERMISSION);
        }

        User u = userService.getUserByEmailFailOver(userMail);

        // user should exist for company as a site Collaborator or site manager member
        if (userPermissionInCompany.equals(SitePermission.COLLABORATOR)
                || userPermissionInCompany.equals(SitePermission.MANAGER)) {
            grantSubSpacePermission(subSpace, u.getUserName(), perm);
            return null;
        } else {
            logger.error("Collaborator Share not available for " + userPermissionInCompany.toString() + " users");
            throw new KoyaServiceException(KoyaErrorCodes.SECU_USER_MUSTBE_COLLABORATOR_OR_ADMIN_TO_APPLY_PERMISSION);
        }
    }
}
