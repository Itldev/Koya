package fr.itldev.koya.action.notification;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

/**
 *
 */
public class NewContentNotifierActionExecuter extends KoyaNotifierActionExecuter {

    private final Logger logger = Logger.getLogger(this.getClass());

    public static final String NAME = "newContentNotifier";

    protected NodeService nodeService;
    protected KoyaMailService koyaMailService;
    protected KoyaNodeService koyaNodeService;
    protected CompanyAclService companyAclService;
    protected SubSpaceAclService subSpaceAclService;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    //</editor-fold>
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        Dossier d = null;
        Company c = null;

        //gets company users involved
        try {

            d = koyaNodeService.getFirstParentOfType(actionedUponNodeRef, Dossier.class);
            c = koyaNodeService.getFirstParentOfType(actionedUponNodeRef, Company.class);
        } catch (KoyaServiceException ex) {
            logger.error("error while determinating nodeRef Koya Typed parents");
        }
        String modifierUserName = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_MODIFIER);

        for (User u : companyAclService.listMembersValidated(c.getName(), null)) {
            /**
             * Exec if user has notify aspect and has responsabilty on current
             * dossier and is not owner
             *
             * TODO user or admin configurable conditions
             *
             */

            if (nodeService.hasAspect(u.getNodeRef(), KoyaModel.ASPECT_USERNOTIFIED)
                    && subSpaceAclService.listUsers(d, null).contains(u)
                    && !u.getUserName().equals(modifierUserName)) {
                try {                
                	logger.trace("send to "+u.getEmail()+"for node "+actionedUponNodeRef);
                    koyaMailService.sendNewContentNotificationMail(u, actionedUponNodeRef);
                } catch (KoyaServiceException ex) {
                    logger.debug("Notification : Sending new content mail to " + u.getEmail() + " - " + ex.toString());
                }
            }
        }

    }

}
