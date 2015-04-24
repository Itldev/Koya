package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.services.NotificationService;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.type.TypeReference;

public class NotificationServiceImpl extends AlfrescoRestService implements NotificationService {

    private static final String REST_GET_ACTIVITIES = "/s/fr/itldev/koya/activities/feed/user?s={siteId?}"
            + "&exclUser={false?}"
            + "&exclOthers={false?}"
            + "&minFeedId={minFeedId?}"
            + "&activityFilter={activityFilter?}"
            + "&format=json";


    @Override
    public List<Notification> list(User user, Company company,
            final Boolean excludeUser,
            Boolean excludeOthers, Integer minFeedId,
            List<String> activityFilter) throws AlfrescoServiceException {

        Map<String, Object> urlVariable = new HashMap<>();
        if (company != null) {
            urlVariable.put("s", company.getName());
        }
        if (excludeUser != null) {
            urlVariable.put("excludeUser", excludeUser);
        }
        if (excludeOthers != null) {
            urlVariable.put("excludeOthers", excludeOthers);
        }
        if (minFeedId != null) {
            urlVariable.put("minFeedId", minFeedId);
        }
        if (activityFilter != null) {
            urlVariable.put("activityFilter", activityFilter);
        }

        return fromJSON(new TypeReference<List<Notification>>() {
        }, user.getRestTemplate().getForObject(getAlfrescoServerUrl() + REST_GET_ACTIVITIES, String.class, urlVariable));
    }

}
