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
package fr.itldev.koya.alfservice;

import fr.itldev.koya.action.notification.NewContentNotifierActionExecuter;
import fr.itldev.koya.alfservice.security.CompanyAclService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.activities.feed.FeedNotifier;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class KoyaNotificationService {

    protected static Log logger = LogFactory.getLog(FeedNotifier.class);

    protected ActionService actionService;
    protected RuleService ruleService;
    protected UserService userService;
    protected NodeService nodeService;
    protected KoyaMailService koyaMailService;
    protected KoyaNodeService koyaNodeService;
    protected CompanyAclService companyAclService;
    protected SubSpaceAclService subSpaceAclService;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setCompanyAclService(CompanyAclService companyAclService) {
        this.companyAclService = companyAclService;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    //</editor-fold>
    /**
     * Checks if user is notified on specified node.
     *
     * User must have a role on item.
     *
     * @param u
     * @param s
     * @return
     * @throws KoyaServiceException
     */
    public boolean isUserNotified(User u, SecuredItem s) throws KoyaServiceException {

        //s not used while notifications applies on all repository aspect implementation
        /**
         * TODO change model to define notified users associated to users
         * permissions. define notifiable aspect that carries notified users
         * list. check the notifiables parents chain to trigger notication.
         *
         */
        /**
         * check for global notification setting TODO remove
         */
        if (!nodeService.hasAspect(u.getNodeRefasObject(), KoyaModel.ASPECT_USERNOTIFIED)) {
            return false;
        }

        /**
         * checks if user have responsability on secured item
         */
        //  return subSpaceAclService.listUsers(s, new ArrayList<KoyaPermission>(0)).contains(u);        
        return true;
    }

    public void addNotification(User u, SecuredItem s) {
        nodeService.addAspect(u.getNodeRefasObject(), KoyaModel.ASPECT_USERNOTIFIED, null);
    }

    public void removeNotification(User u, SecuredItem s) {
        nodeService.removeAspect(u.getNodeRefasObject(), KoyaModel.ASPECT_USERNOTIFIED);
    }

    /**
     * notification rule is applied by company.
     *
     * @param c
     */
    public void createCompanyNotificationRule(Company c) {

        /**
         * Action applies on company documentLibrary node.
         */
        final NodeRef docLibNodeRef = nodeService.getChildByName(
                c.getNodeRefasObject(), ContentModel.ASSOC_CONTAINS, "documentLibrary");

        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);
        rule.setTitle("Koya notification rule");
        rule.applyToChildren(true); // cascade subfolders.
        rule.setExecuteAsynchronously(true);

        CompositeAction compositeAction = actionService.createCompositeAction();
        rule.setAction(compositeAction);

        ActionCondition actionCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        Map<String, Serializable> conditionParameters = new HashMap<>(1);
        conditionParameters.put(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT); // setting subtypes to CONTENT
        actionCondition.setParameterValues(conditionParameters);
        compositeAction.addActionCondition(actionCondition);

        
        /**
         * TODO insert additionnal layer this process 'events' and decide notifications triggers (mail, webui, sms, etc ....)
         * 
         * 
         * Easiest way is to call back a notifcationservice method that collects events, process it (eg for sending one mail grouping events)
         * and dispatch to notificationsExecuters
         * 
         * 
         */
        Action action = actionService.createAction(NewContentNotifierActionExecuter.NAME);     
        action.setExecuteAsynchronously(true);
        Map<String, Serializable> ruleParameters = new HashMap<>(1);       
        action.setParameterValues(ruleParameters);
        
        
        compositeAction.addAction(action);
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork() {
            @Override
            public Object doWork() throws Exception {
                ruleService.saveRule(docLibNodeRef, rule); // Save the rule to your nodeRef
                return null;
            }
        });

    }
    

}
