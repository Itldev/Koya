package fr.itldev.koya.webscript.user;

import fr.itldev.koya.alfservice.EmailNotificationService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * get the notification status of the user
 *
 */
public class NotificationStatus extends AbstractWebScript {

    private EmailNotificationService emailNotificationService;

    public void setEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);
        String strEnable = (String) urlParams.get(KoyaWebscript.WSCONST_ENABLE);
        String username = (String) urlParams.get(KoyaWebscript.WSCONST_USERNAME);
        String response;
        try {
            if (strEnable != null) {
                emailNotificationService.addRemoveUser(username, Boolean.valueOf(strEnable));
            }
            response = KoyaWebscript.getObjectAsJson(!emailNotificationService.getEmailNotificationRule(username).isEmpty());
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write(response);
    }
}
