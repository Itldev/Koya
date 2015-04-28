package fr.itldev.koya.behaviour.security;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.ModelService;
import fr.itldev.koya.alfservice.security.SpaceCollaboratorsAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;

/**
 *
 *
 */
public class SetOwnerAsResponsibleOnCreate implements
		NodeServicePolicies.OnCreateNodePolicy {

	private final Logger logger = Logger.getLogger(this.getClass());
	private Behaviour onCreateNode;
	private PolicyComponent policyComponent;
	private KoyaNodeService koyaNodeService;
	private NodeService nodeService;
	private SiteService siteService;

	private SpaceCollaboratorsAclService spaceCollaboratorsAclService;
	private ModelService modelService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setSpaceCollaboratorsAclService(
			SpaceCollaboratorsAclService spaceCollaboratorsAclService) {
		this.spaceCollaboratorsAclService = spaceCollaboratorsAclService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void init() {
		// Create behaviours
		this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
				Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		this.policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnCreateNodePolicy.QNAME,
				KoyaModel.TYPE_DOSSIER, this.onCreateNode);

		this.policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnCreateNodePolicy.QNAME,
				KoyaModel.TYPE_SPACE, this.onCreateNode);

	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		try {
			String creator = (String) nodeService.getProperty(
					childAssocRef.getChildRef(), ContentModel.PROP_CREATOR);

			if (creator != null
					&& !creator.equals(modelService
							.getCompanyImporterUsername(siteService
									.getSiteShortName(childAssocRef
											.getChildRef())))) {
				spaceCollaboratorsAclService.grantSpacePermission(
						koyaNodeService.getKoyaNode(
								childAssocRef.getChildRef(), Space.class),
						creator, KoyaPermissionCollaborator.RESPONSIBLE);
			} else {
				// TODO set default responsible
			}
		} catch (KoyaServiceException ex) {
			logger.error(ex.toString());
		}
	}
}
