package fr.itldev.koya.webscript.user;

import fr.itldev.koya.alfservice.EmailNotificationService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.BooleanWrapper;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;
import org.apache.log4j.Logger;
/**
 * get the notification status of the user
 *
 */
public class NotificationStatus extends KoyaWebscript {
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    private static final String WSCONST_ENABLE = "enable";
    EmailNotificationService emailNotificationService;
    
    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        
        String strEnable = (String) urlParams.get(WSCONST_ENABLE);
        String username = (String) urlParams.get(WSCONST_USERNAME);
        
        try {
            if (strEnable != null) {
                emailNotificationService.addRemoveUser(username, Boolean.valueOf(strEnable));
            }
            wrapper.addItem(new BooleanWrapper(!emailNotificationService.getEmailNotificationRule(username).isEmpty()));
            
        } catch (KoyaServiceException ex) {
            wrapper.setStatusFail(ex.toString());
            wrapper.setErrorCode(ex.getErrorCode());
        }
        
        return wrapper;
    }
      
    
    public void setEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }
    
}
