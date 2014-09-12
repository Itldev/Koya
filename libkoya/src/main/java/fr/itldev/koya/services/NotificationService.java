package fr.itldev.koya.services;

import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface NotificationService {

    /**
     * List activities for the current user
     *
     * @param user
     * @return List of notifications for the user.
     * @throws AlfrescoServiceException
     */
    List<Notification> list(User user) throws AlfrescoServiceException;
    
}
