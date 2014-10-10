package fr.itldev.koya.resetpassword.activiti;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.model.impl.User;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class SendResetTicketDelegate extends BaseJavaDelegate {

    private KoyaMailService koyaMailService;
    private UserService userService;

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        Map<String, Object> executionVariables = execution.getVariables();

        ActivitiScriptNode asn = (ActivitiScriptNode) executionVariables.get(ResetPasswordModel.wfVarUser);

        User user = userService.buildUser(asn.getNodeRef());

        String resetId = ActivitiConstants.ENGINE_ID + "$" + execution.getProcessInstanceId();
        String resetUrl = (String) executionVariables.get(ResetPasswordModel.wfVarResetUrl);
        String resetTicket = (String) executionVariables.get(ResetPasswordModel.wfVarResetTicket);

        String url = resetUrl + "?resetId=" + resetId + "&resetTicket=" + resetTicket + "&userEmail=" + user.getEmail();

        logger.debug("resetUrl=" + url);

        koyaMailService.sendResetRequestMail(user.getEmail(), url);
    }

}
