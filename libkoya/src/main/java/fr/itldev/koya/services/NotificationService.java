package fr.itldev.koya.services;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface NotificationService {

    /**
     * List activities for the current user
     *
     * @param user Current logged user
     * @param company Company to limit notification to
     * @param excludeUser Exclude user Notifications
     * @param excludeOthers Exclude others notifications
     * @param minFeedId Minimum Feed Id to return (inclusive)
     * @param activityFilter ActivityType filter. Separate by ','
     *
     * @return List of notifications for the user.
     *
     * @throws AlfrescoServiceException
     */
    List<Notification> list(User user, Company company, Boolean excludeUser,
            Boolean excludeOthers, Integer minFeedId,
            List<String> activityFilter) throws AlfrescoServiceException;

}
