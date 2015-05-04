package fr.itldev.koya.services.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Notification;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.NotificationService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;

public class NotificationServiceImpl extends AlfrescoRestService implements
		NotificationService {
	private static final String REST_GET_ACTIVITIES = "/s/fr/itldev/koya/activities/feed/user?format=json";

	@Override
	public List<Notification> list(User user, Company company,
			final Boolean excludeUser, Boolean excludeOthers, Long minFeedId,
			List<String> activityFilter) throws AlfrescoServiceException {

		String activitiesUrl = getAlfrescoServerUrl() + REST_GET_ACTIVITIES;

		if (company != null) {
			activitiesUrl += "&s=" + company.getName();
		}
		if (excludeUser != null) {
			activitiesUrl += "&exclUser=" + excludeUser;
		}
		if (excludeOthers != null) {
			activitiesUrl += "&exclOthers=" + excludeOthers;
		}
		if (minFeedId != null) {
			activitiesUrl += "&minFeedId=" + minFeedId;
		}
		if (activityFilter != null && !activityFilter.isEmpty()) {
			activitiesUrl += "&activityFilter="
					+ StringUtils.join(activityFilter, ",");
		}

		return fromJSON(new TypeReference<List<Notification>>() {
		}, user.getRestTemplate().getForObject(activitiesUrl, String.class));
	}

}
