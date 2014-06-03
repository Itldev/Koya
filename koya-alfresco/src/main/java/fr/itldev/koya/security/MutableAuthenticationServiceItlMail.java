package fr.itldev.koya.security;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.User;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationServiceImpl;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class MutableAuthenticationServiceItlMail extends MutableAuthenticationServiceImpl implements MutableAuthenticationService {

    private Logger logger = Logger.getLogger(this.getClass());

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void authenticate(String userName, char[] password) throws AuthenticationException {

        final String uName = userName;

        User u = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< User>() {
            @Override
            public User doWork() throws Exception {

                try {
                    return userService.getUser(uName);
                } catch (KoyaServiceException kex) {
                    //no error if user id not found
                }
                return null;
            }
        });

        if (u != null) {
            super.authenticate(u.getUserName(), password);
        } else {
            super.authenticate(userName, password);
        }

    }

}
