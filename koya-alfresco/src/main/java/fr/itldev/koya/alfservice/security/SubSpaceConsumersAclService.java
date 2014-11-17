package fr.itldev.koya.alfservice.security;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.interfaces.SubSpace;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.invitation.Invitation;
import org.apache.log4j.Logger;

/**
 * This service handle permissions settings for company CONSUMER roles
 *
 */
public class SubSpaceConsumersAclService extends SubSpaceAclService {

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * This method si used to share an item (only Dossiers in this first
     * implementation) with Consumers (ie company Clients)
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
    protected Invitation shareSecuredItemImpl(final SubSpace subSpace, String userMail, KoyaPermission perm,
            String serverPath, String acceptUrl, String rejectUrl) throws KoyaServiceException {

        if (!Dossier.class.isAssignableFrom(subSpace.getClass())) {
            throw new KoyaServiceException(KoyaErrorCodes.SECU_UNSHARABLE_TYPE);
        }

        if (!KoyaPermissionConsumer.getAll().contains(perm)) {
            throw new KoyaServiceException(KoyaErrorCodes.SECU_UNSETTABLE_PERMISSION);
        }
      
        //Get company the shared Node belongs To
        Company company = koyaNodeService.getFirstParentOfType(subSpace.getNodeRefasObject(),Company.class);
        SitePermission userPermissionInCompany = companyAclService.getSitePermission(company, userMail);
        
        Invitation invitation = null;
        //If user can't access specified company then invite him even if he alredy exists in alfresco
        if (userPermissionInCompany == null) {
            invitation = companyAclService.inviteMember(company, userMail, SitePermission.CONSUMER,
                    serverPath, acceptUrl, rejectUrl);
            userPermissionInCompany = companyAclService.getSitePermission(company, userMail);
        }

        final User u = userService.getUserByEmailFailOver(userMail);

        //Now user should exist for company as a site Consumer member
        if (userPermissionInCompany.equals(SitePermission.CONSUMER)) {
            //exec as system user if current user is member of dossier -> get temporarly more permissions
            //TODO check user role (is KoyaMember on shared dossier) before run
               AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork() {
                    @Override
                    public Object doWork() throws Exception {
                         grantSubSpacePermission(subSpace, u.getUserName(), KoyaPermissionConsumer.CLIENT);
                      return null;
                    }
                });            
            return invitation;
        } else {
            logger.error("Consumer Share not available for " + userPermissionInCompany.toString() + " users");
            throw new KoyaServiceException(KoyaErrorCodes.SECU_USER_MUSTBE_CONSUMER_TO_APPLY_PERMISSION);
        }
    }

}
