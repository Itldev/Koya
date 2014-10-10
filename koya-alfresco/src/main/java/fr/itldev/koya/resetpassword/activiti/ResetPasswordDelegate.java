package fr.itldev.koya.resetpassword.activiti;

import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.impl.User;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class ResetPasswordDelegate extends BaseJavaDelegate {
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    private UserService userService;
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        Map<String, Object> executionVariables = execution.getVariables();
        
        ActivitiScriptNode asn = (ActivitiScriptNode) executionVariables.get(ResetPasswordModel.wfVarUser);
        User user = userService.buildUser(asn.getNodeRef());
        
        logger.error("reset password user " + user.getEmail()
                + " with new password : " + executionVariables.get(ResetPasswordModel.wfVarNewPassword));
        
        logger.error(execution.getCurrentActivityId());
        
        for (String k : executionVariables.keySet()) {
            try {
                if (k.startsWith("koya")) {
                    logger.error(">>>>>>>" + k + "=" + executionVariables.get(k));
                }
            } catch (Exception e) {
                
            }
        }
    }
    
}
