package fr.itldev.koya.behaviour.security;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Space;

/**
 * Apply default ACL on objects of type Space or Dossier on node creation.
 * 
 * Creates Node Specific Groups and set Permissions.
 * 
 * Add Creator Authority to Responsibles Group if not belongs blacklist nor
 * importer user
 * 
 */
public class ApplyDefaultAclOnCreate implements NodeServicePolicies.OnCreateNodePolicy {

	private final Logger logger = Logger.getLogger(this.getClass());
	private Behaviour onCreateNode;
	private PolicyComponent policyComponent;
	private SpaceAclService spaceAclService;
	private KoyaNodeService koyaNodeService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void init() {
		// Create behaviours
		this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
				Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
				KoyaModel.TYPE_DOSSIER, this.onCreateNode);

		this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
				KoyaModel.TYPE_SPACE, this.onCreateNode);

	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		Space space;
		try {
			space = koyaNodeService.getKoyaNode(childAssocRef.getChildRef(), Space.class);
		} catch (KoyaServiceException ex) {
			logger.error("Error Applying default permissions on node creation : " + ex.toString());
			return;
		}

		spaceAclService.initSpaceAcl(space);

	}

}
