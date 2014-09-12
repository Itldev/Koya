package fr.itldev.koya.services.impl;

import fr.itldev.koya.services.NotificationService;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;
import org.codehaus.jackson.type.TypeReference;

public class NotificationServiceImpl extends AlfrescoRestService implements NotificationService {

    private static final String REST_GET_ACTIVITIES = "/s/api/activities/feed/user?format=json";

    /**
     * List activities for the current user
     *
     * @param user
     * @param space
     * @throws AlfrescoServiceException
     */
    @Override
    public List<Notification> list(User user) throws AlfrescoServiceException {

        return fromJSON(new TypeReference<List<Notification>>() {
        }, user.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_ACTIVITIES, String.class));

    }
}
