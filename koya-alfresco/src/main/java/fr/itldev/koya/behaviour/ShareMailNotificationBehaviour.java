package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.policies.SharePolicies;
import java.util.List;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShareMailNotificationBehaviour implements SharePolicies.AfterSharePolicy {

    protected static Log logger = LogFactory.getLog(ShareMailNotificationBehaviour.class);

    protected PolicyComponent policyComponent;
    protected KoyaMailService koyaMailService;
    protected KoyaNodeService koyaNodeService;
    protected UserService userService;
    protected CompanyAclService companyAclService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

    }

    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail,
            User inviter, Boolean sharedByImporter) {
        try {
            Company c = koyaNodeService.getFirstParentOfType(nodeRef, Company.class);
            User u = userService.getUser(userMail);
            List<Invitation> invitations = companyAclService.getPendingInvite(c.getName(), null, u.getUserName());

            /**
             * Ininbits mail sending if any invitation or sharing automaticly
             * set by importer
             */
            if (invitations.isEmpty() && !sharedByImporter) {
                koyaMailService.sendShareNotifMail(inviter, userMail, nodeRef);
            } else {
                //Nothing to do : invitation already sent 
            }
        } catch (KoyaServiceException ex) {
            logger.fatal("Error while sending share notification mail : " + ex.toString());
        }
    }

}
