package fr.itldev.koya.activities.feed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.feed.ActivitiesFeedModelBuilder;
import org.alfresco.repo.activities.feed.EmailUserNotifier;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.json.JSONException;

import fr.itldev.koya.alfservice.KoyaMailService;

public class KoyaEmailUserNotifier extends EmailUserNotifier {

	protected KoyaMailService koyaMailService;

	public void setKoyaMailService(KoyaMailService koyaMailService) {
		this.koyaMailService = koyaMailService;
	}

	public Pair<Integer, Long> notifyUser(final NodeRef personNodeRef,
			String subject, Object[] subjectParams,
			Map<String, String> siteNames, String shareUrl,
			int repeatIntervalMins, String templateNodeRef) {
		Map<QName, Serializable> personProps = nodeService
				.getProperties(personNodeRef);

		String feedUserId = (String) personProps
				.get(ContentModel.PROP_USERNAME);

		if (skipUser(personNodeRef)) {
			// skip
			return null;
		}

		// where did we get up to ?
		Long feedDBID = getFeedId(personNodeRef);

		// own + others (note: template can be changed to filter out user's own
		// activities if needed)
		if (logger.isDebugEnabled()) {
			logger.debug("Get user feed entries: " + feedUserId + ", "
					+ feedDBID);
		}
		List<ActivityFeedEntity> feedEntries = activityService
				.getUserFeedEntries(feedUserId, null, false, false, null, null,
						feedDBID);

		if (feedEntries.size() > 0) {

			Map<String, ActivitiesFeedModelBuilder> companyModelBuilder = new HashMap<>();

			for (ActivityFeedEntity feedEntry : feedEntries) {

				String companyName = feedEntry.getSiteNetwork();
				ActivitiesFeedModelBuilder modelBuilder;
				// select company model builder. Creates if not exists

				if (!companyModelBuilder.containsKey(companyName)) {
					try {
						companyModelBuilder.put(companyName,
								activitiesFeedModelBuilderFactory.getObject());
					} catch (Exception error) {
						logger.warn("Unable to create model builder for company '"
								+ companyName + "' : " + error.getMessage());
						return null;
					}
				}
				modelBuilder = companyModelBuilder.get(companyName);

				try {
					modelBuilder.addAcctivitiyFeedEntry(feedEntry);

					// desactivé
					//
					// String siteId = feedEntry.getSiteNetwork();
					// // addSiteName(siteId, siteNames);
				} catch (JSONException je) {
					// skip this feed entry
					logger.warn("Skip feed entry for user (" + feedUserId
							+ "): " + je.getMessage());
					continue;
				}
			}

			long maxFeedId = 0;
			int activityCount = 0;
			for (String companyName : companyModelBuilder.keySet()) {
				ActivitiesFeedModelBuilder modelBuilder = companyModelBuilder
						.get(companyName);

				int localActivityCount = modelBuilder.activityCount();
				activityCount += localActivityCount;
				if (localActivityCount > 0) {
					Map<String, Object> model = modelBuilder.buildModel();

					model.put("siteTitles", siteNames);
					model.put("repeatIntervalMins", repeatIntervalMins);
					model.put("feedItemsMax", activityService.getMaxFeedItems());

					// add Share info to model
					model.put(TemplateService.KEY_PRODUCT_NAME,
							ModelUtil.getProductName(repoAdminService));

					Map<String, Serializable> personPrefixProps = new HashMap<String, Serializable>(
							personProps.size());
					for (QName propQName : personProps.keySet()) {
						try {
							String propPrefix = propQName
									.toPrefixString(namespaceService);
							personPrefixProps.put(propPrefix,
									personProps.get(propQName));
						} catch (NamespaceException ne) {
							// ignore properties that do not have a registered
							// namespace
							logger.warn("Ignoring property '" + propQName
									+ "' as it's namespace is not registered");
						}
					}

					model.put("personProps", personPrefixProps);

					if (modelBuilder.getMaxFeedId() > maxFeedId) {
						maxFeedId = modelBuilder.getMaxFeedId();
					}
					
					// send with koyaMail
					koyaMailService.sendUserNotifMail(personNodeRef, model,
							templateNodeRef, companyName);

				}

				return new Pair<Integer, Long>(activityCount, maxFeedId);
			}
		}

		return null;
	}

}
