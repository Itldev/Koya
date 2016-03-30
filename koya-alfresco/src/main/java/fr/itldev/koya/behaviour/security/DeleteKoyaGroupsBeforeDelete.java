package fr.itldev.koya.behaviour.security;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Space;

public class DeleteKoyaGroupsBeforeDelete implements NodeServicePolicies.BeforeDeleteNodePolicy {

	private final Logger logger = Logger.getLogger(this.getClass());
	private Behaviour beforeDeleteNode;
	private PolicyComponent policyComponent;
	private SpaceAclService spaceAclService;
	private KoyaNodeService koyaNodeService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void init() {
		this.beforeDeleteNode = new JavaBehaviour(this, "beforeDeleteNode",
				Behaviour.NotificationFrequency.FIRST_EVENT);
		this.policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
				KoyaModel.TYPE_DOSSIER, this.beforeDeleteNode);

		this.policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
				KoyaModel.TYPE_SPACE, this.beforeDeleteNode);
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		try {
			Space s = koyaNodeService.getKoyaNode(nodeRef, Space.class);
			spaceAclService.removeAllKoyaGroups(s);
		} catch (KoyaServiceException ex) {
			logger.error("before delete node error : " + ex.toString());
		} catch (UnknownAuthorityException e) {
			logger.error("Unknown authority trying to del group : " + e.toString());
		}

	}

}
