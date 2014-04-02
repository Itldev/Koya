package fr.itldev.koya.alfservice;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;

public class KoyaAclService {

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Grant Access for user with specified mail to item.
     *
     * If users doesn't exists, runs creation process.
     *
     *
     * @param userLog
     * @param sharedItem
     * @param userMail
     */
    public void grantAccess(User userLog, SecuredItem sharedItem, String userMail) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param userLog
     * @param sharedItem
     * @param userMail
     */
    public void revokeAccess(User userLog, SecuredItem sharedItem, String userMail) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
