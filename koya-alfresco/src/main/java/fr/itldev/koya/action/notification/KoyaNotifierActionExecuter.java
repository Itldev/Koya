package fr.itldev.koya.action.notification;

import fr.itldev.koya.alfservice.KoyaNotificationService;
import java.util.List;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.ParameterDefinition;


public abstract class KoyaNotifierActionExecuter extends ActionExecuterAbstractBase {

    protected KoyaNotificationService koyaNotificationService;

    public void setKoyaNotificationService(KoyaNotificationService koyaNotificationService) {
        this.koyaNotificationService = koyaNotificationService;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }

}
