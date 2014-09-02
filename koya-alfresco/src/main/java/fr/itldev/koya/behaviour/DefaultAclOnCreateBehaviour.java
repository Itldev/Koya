/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.KoyaAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import java.util.logging.Level;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.log4j.Logger;

/**
 * Apply default ACL on objects of type Space or Dossier on node creation
 *
 */
public class DefaultAclOnCreateBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    private final Logger logger = Logger.getLogger(this.getClass());
    private Behaviour onCreateNode;
    private PolicyComponent policyComponent;
    private KoyaAclService koyaAclService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setKoyaAclService(KoyaAclService koyaAclService) {
        this.koyaAclService = koyaAclService;
    }

    public void init() {
        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                this.onCreateNode);

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, KoyaModel.TYPE_SPACE,
                this.onCreateNode);

    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        try {
            koyaAclService.setSpaceDossierDefaultAccess(childAssocRef.getChildRef());
        } catch (KoyaServiceException ex) {
            logger.error(ex.toString());
        }
    }
}
