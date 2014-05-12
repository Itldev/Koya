package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.SharingWrapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class KoyaShareService {

    private Logger logger = Logger.getLogger(KoyaShareService.class);

    private UserService userService;
    private KoyaNodeService koyaNodeService;
    private KoyaAclService koyaAclService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setKoyaAclService(KoyaAclService koyaAclService) {
        this.koyaAclService = koyaAclService;
    }

    //</editor-fold>
    public void shareItems(SharingWrapper sharingWrapper) throws KoyaServiceException {

        List<SecuredItem> sharedItems = new ArrayList<>();
        //extract shared elements and define sharing configuration
        for (String n : sharingWrapper.getSharedNodeRefs()) {
            try {
                sharedItems.add(koyaNodeService.nodeRef2SecuredItem(n));
            } catch (KoyaServiceException kex) {
                logger.error("Error creating element for sharing : " + kex.toString());
            }
        }

        //share elements to users specified by email
        for (String userMail : sharingWrapper.getSharingUsersMails()) {
            User u = null;

            try {
                u = userService.getUser(userMail);
            } catch (KoyaServiceException kex) {
                //do nothing if exception thrown
            }

            if (u != null) {
                shareSecuredItemsWithExistingUser(sharedItems, u);
            } else {
                shareSecuredItemsWithNonExistingUser(sharedItems, userMail);
            }
        }
    }

    private void shareSecuredItemsWithExistingUser(List<SecuredItem> sharedItems, User userToShareWith) throws KoyaServiceException {
        logger.error("share " + sharedItems.size() + " elements to existing : " + userToShareWith.getEmail());

        //give permissions to user on nodes
        for (SecuredItem si : sharedItems) {
            koyaAclService.setReadAccess(userToShareWith.getLogin(), si, Boolean.TRUE, Boolean.TRUE);
        }

        //send email
        //  emailService.importMessage(null, null);
    }

    private void shareSecuredItemsWithNonExistingUser(List<SecuredItem> sharedItems, String newUserMail) {
        logger.error("create user : " + newUserMail + " and share " + sharedItems.size() + " elements");

        //create user 
        //give permissions
        //send email
    }

}
