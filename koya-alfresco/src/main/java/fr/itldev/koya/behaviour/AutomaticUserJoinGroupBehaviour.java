package fr.itldev.koya.behaviour;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import com.google.common.base.Stopwatch;

/**
 * 
 * If feature is active and current user doesn't belongs to companyName, then
 * user is automaticly granted with groupName role in companyName.
 * 
 * 
 * This feature is useful in case of automatic ldap Sync to give new users the
 * default role on a default company
 * 
 */
public class AutomaticUserJoinGroupBehaviour implements NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy {
	private final Logger logger = Logger.getLogger(this.getClass());

	private static String[] EXCLUDED_USERNAMES = { "admin", "System", "guest" };

	private PolicyComponent policyComponent;
	private NodeService nodeService;

	private SiteService siteService;
	private boolean active;
	private String companyName;
	private String groupName;

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void init() {
		// Create behaviours

		this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
				ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateProperties",
						Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		if (active) {
			Stopwatch timer = new Stopwatch().start();
			try {
				final NodeRef person = childAssocRef.getChildRef();
				joinGroup((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME));
			} catch (Exception e) {

			}
			timer.stop();
			logger.error("onCreateNode > " + timer.elapsedMillis());
		}
	}

	@Override
	public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
		if (active) {
			Stopwatch timer = new Stopwatch().start();
			try {
				joinGroup((String) after.get(ContentModel.PROP_USERNAME));
			} catch (Exception e) {

			}
			timer.stop();
			logger.error("onUpdateProperties > " + timer.elapsedMillis());
		}
	}

	public void joinGroup(final String username) {

		if (Arrays.asList(EXCLUDED_USERNAMES).contains(username)) {
			return;
		}

		// search company and group
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			public Void doWork() throws Exception {
				SiteInfo sInfo = siteService.getSite(companyName);

				if (sInfo == null) {
					return null;
				}

				if (!siteService.isMember(companyName, username)) {

					logger.info(username + " > setMembership company " + companyName + " -> "
							+ groupName);

					siteService.setMembership(companyName, username, groupName);
				}
				return null;
			}
		});

	}

}
