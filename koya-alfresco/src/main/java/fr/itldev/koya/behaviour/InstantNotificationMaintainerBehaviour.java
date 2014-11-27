/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.behaviour;

import fr.itldev.koya.alfservice.EmailNotificationService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.policies.SharePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InstantNotificationMaintainerBehaviour implements SharePolicies.AfterSharePolicy, SharePolicies.AfterUnsharePolicy {

    /**
     *
     * TODO refine notifications against Permission :
     *
     */
    protected static Log logger = LogFactory.getLog(InstantNotificationMaintainerBehaviour.class);

    private PolicyComponent policyComponent;
    protected EmailNotificationService emailNotificationService;
    protected UserService userService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(SharePolicies.AfterSharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterShareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(SharePolicies.AfterUnsharePolicy.QNAME, KoyaModel.TYPE_DOSSIER,
                new JavaBehaviour(this, "afterUnshareItem", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void afterShareItem(NodeRef nodeRef, String userMail, Invitation invitation, User inviter, Boolean sharedByImporter, String directAccessUrl) {
        try {
            User u = userService.getUserByEmailFailOver(userMail);

            if (emailNotificationService.isUserNotified(u.getUserName())) {
                emailNotificationService.addRemoveUser(nodeRef, u.getUserName(), true);
            }
        } catch (KoyaServiceException kse) {
            logger.error(kse.getMessage(), kse);
        }
    }

    @Override
    public void afterUnshareItem(NodeRef nodeRef, String userMail, User inviter) {
        try {
            User u = userService.getUserByEmailFailOver(userMail);

            if (emailNotificationService.isUserNotified(u.getUserName())) {
                emailNotificationService.addRemoveUser(nodeRef, u.getUserName(), false);
            }
        } catch (KoyaServiceException kse) {
            logger.error(kse.getMessage(), kse);
        }
    }

}
