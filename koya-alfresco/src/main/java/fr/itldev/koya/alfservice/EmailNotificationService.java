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

import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.activities.feed.FeedNotifier;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class EmailNotificationService {

    protected static Log logger = LogFactory.getLog(FeedNotifier.class);

    private final static String INSTANT_NOTIFICATION_SUBJECT = "koya.instant-notification.subject";

    protected ActionService actionService;
    protected RuleService ruleService;
    protected RepositoryLocation feedEmailTemplateLocation;
    protected TransactionService transactionService;
    protected SubSpaceAclService subSpaceAclService;
    protected UserService userService;
    protected KoyaMailService koyaMailService;

    private static final String RULE_NAME_EMAIL = "email_notification_rule";

    private static final List<QName> TYPEFILTER_DOSSIER = Collections.unmodifiableList(new ArrayList<QName>() {
        {
            add(KoyaModel.TYPE_DOSSIER);
        }
    });

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

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    public void setFeedEmailTemplateLocation(RepositoryLocation feedEmailTemplateLocation) {
        this.feedEmailTemplateLocation = feedEmailTemplateLocation;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setKoyaMailService(KoyaMailService koyaMailService) {
        this.koyaMailService = koyaMailService;
    }

    //</editor-fold>
    /**
     * Checks if user belongs to any notification rule on the repository. If
     * true he's a notified user.
     *
     *
     * @param username
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public boolean isUserNotified(String username) throws KoyaServiceException {
        //TODO : use a EHcache   
        //@Cacheable(cacheName = "userNotifyCache", cacheNull = false, keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator")

        User u = userService.getUserByUsername(username);

        /**
         * Get list of secured Items a user has a Koya Role
         *
         * this excludes admin users that have a read permission on each node by
         * default.
         *
         */
        List<SecuredItem> readableDossiers
                = subSpaceAclService.getUsersSecuredItemWithKoyaPermissions(u, TYPEFILTER_DOSSIER, null);

        for (SecuredItem item : readableDossiers) {
            logger.debug("getEmailNotificationRule SecuredItem: " + item.getName());

            NodeRef nodeRef = item.getNodeRefasObject();
            List<Rule> rules = ruleService.getRules(nodeRef, false, RuleType.INBOUND);

            for (Rule r : rules) {
                if (RULE_NAME_EMAIL.equals(r.getTitle())
                        && ((ArrayList<String>) ((CompositeAction) r.getAction()).getAction(0).getParameterValue(MailActionExecuter.PARAM_TO_MANY)).contains(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addRemoveUser(String username, boolean add) throws KoyaServiceException {

        User u = userService.getUserByUsername(username);

        /**
         * Get list of secured Items a user has a Koya Role
         *
         * this excludes admin users that have a read permission on each node by
         * default.
         *
         */
        List<SecuredItem> readableDossiers
                = subSpaceAclService.getUsersSecuredItemWithKoyaPermissions(u, TYPEFILTER_DOSSIER, null);

        for (SecuredItem item : readableDossiers) {
            logger.debug("addRemoveUser SecuredItem: " + item.getName());

            final NodeRef nodeRef = item.getNodeRefasObject();

            addRemoveUser(nodeRef, username, add);
        }
    }

    public void addRemoveUser(final NodeRef nodeRef, String username, boolean add) throws KoyaServiceException {
        List<Rule> rules = ruleService.getRules(nodeRef, true, RuleType.INBOUND);

        boolean ruleFound = false;
        for (final Rule r : rules) {
            if (RULE_NAME_EMAIL.equals(r.getTitle())) {

                final Action a = ((CompositeAction) r.getAction()).getAction(0);
                final ArrayList<String> usernames = (a.getParameterValue(MailActionExecuter.PARAM_TO_MANY) != null)
                        ? (ArrayList<String>) a.getParameterValue(MailActionExecuter.PARAM_TO_MANY) : new ArrayList();

                if (add && !usernames.contains(username)) {
                    //Add user to notification
                    usernames.add(username);
                } else if (!add && usernames.contains(username)) {
                    //Remove user
                    usernames.remove(username);

                }

                AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork() {
                    @Override
                    public Object doWork() throws Exception {
                        transactionService.getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                                    @Override
                                    public Object execute() {
                                        if (usernames.isEmpty()) {

                                            ruleService.removeRule(nodeRef, r);
                                        } else {
                                            a.setParameterValue(MailActionExecuter.PARAM_TO_MANY, usernames);

                                            ruleService.saveRule(nodeRef, r);
                                        }
                                        return null;
                                    }
                                });
                        return null;
                    }
                });
                ruleFound = true;
                break;
            }
        }
        if (add && !ruleFound) {
            createRule(nodeRef, username);
        }
    }

    private void createRule(final NodeRef nodeRef, String username) throws KoyaServiceException {
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);
        rule.setTitle(RULE_NAME_EMAIL);
        rule.applyToChildren(true); // set this to true if you want to cascade to sub folders

        CompositeAction compositeAction = actionService.createCompositeAction();
        rule.setAction(compositeAction);

        ActionCondition actionCondition = actionService.createActionCondition(IsSubTypeEvaluator.NAME);

        Map<String, Serializable> conditionParameters = new HashMap<>(1);
        conditionParameters.put(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT); // setting subtypes to CONTENT
        actionCondition.setParameterValues(conditionParameters);

        compositeAction.addActionCondition(actionCondition);

        Action action = actionService.createAction(MailActionExecuter.NAME);	     // Send mail Action            
        action.setExecuteAsynchronously(true);

        Map<String, Serializable> ruleParameters = new HashMap<>(1);

        ruleParameters.put(MailActionExecuter.PARAM_TO_MANY, new ArrayList(Arrays.asList(username)));

        Properties i18n = koyaMailService.getI18nSubjectProperties();

        /**
         * TODO get mail subject on mail sending not on rule creation
         *
         * if mail subject change between rule creation (at first users added)
         * and mail alert, le subject will be the old one.
         *
         */
        ruleParameters.put(MailActionExecuter.PARAM_SUBJECT, i18n.getProperty(INSTANT_NOTIFICATION_SUBJECT));
        ruleParameters.put(MailActionExecuter.PARAM_TEMPLATE, koyaMailService.getFileTemplateRef(feedEmailTemplateLocation));

        action.setParameterValues(ruleParameters);

        compositeAction.addAction(action);
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork() {
            @Override
            public Object doWork() throws Exception {
                ruleService.saveRule(nodeRef, rule); // Save the rule to your nodeRef
                return null;
            }
        });
    }

}
