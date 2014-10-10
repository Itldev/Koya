package fr.itldev.koya.resetpassword.activiti;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.impl.User;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class ResetPasswordDelegate extends BaseJavaDelegate {

    private Logger logger = Logger.getLogger(this.getClass());

    private UserService userService;
    private MutableAuthenticationService authenticationService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Map<String, Object> executionVariables = execution.getVariables();
        String newPassword = (String) executionVariables.get(ResetPasswordModel.wfVarNewPassword);
        ActivitiScriptNode asn = (ActivitiScriptNode) executionVariables.get(ResetPasswordModel.wfVarUser);
        User user = userService.buildUser(asn.getNodeRef());
        authenticationService.setAuthentication(user.getUserName(), newPassword.toCharArray());
        logger.debug("reset password user : " + user.getEmail());

    }

}
