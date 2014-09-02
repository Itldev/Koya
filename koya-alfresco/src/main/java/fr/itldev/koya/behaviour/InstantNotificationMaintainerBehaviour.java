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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.EmailNotificationService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.policies.SharePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author nico
 */
public class InstantNotificationMaintainerBehaviour implements SharePolicies.AfterSharePolicy, SharePolicies.AfterUnsharePolicy {
    
    protected static Log logger = LogFactory.getLog(InstantNotificationMaintainerBehaviour.class);
    
    private PolicyComponent policyComponent;
    protected EmailNotificationService emailNotificationService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }
    
    public void setEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }
    
    public void init() {
        // Create behaviours
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterUnsharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterUnshareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }
    
    @Override
    public void afterShareItem(NodeRef nodeRef, String username) {
        try {
            if (emailNotificationService.isUserNotified(username)) {
                emailNotificationService.addRemoveUser(nodeRef, username, true);
            }
        } catch (KoyaServiceException kse) {
            logger.error(kse.getMessage(), kse);
        }
    }
    
    @Override
    public void afterUnshareItem(NodeRef nodeRef, String username) {
        try {
            if (emailNotificationService.isUserNotified(username)) {
                emailNotificationService.addRemoveUser(nodeRef, username, false);
            }
        } catch (KoyaServiceException kse) {
            logger.error(kse.getMessage(), kse);
        }
    }
    
}
