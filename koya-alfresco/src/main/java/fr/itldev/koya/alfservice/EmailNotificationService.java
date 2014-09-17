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
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.activities.feed.FeedNotifier;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
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
    protected ActionService actionService;
    protected RuleService ruleService;
    protected RepositoryLocation feedEmailTemplateLocation;
    private NamespaceService namespaceService;
    protected FileFolderService fileFolderService;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected TransactionService transactionService;
    protected SubSpaceAclService subSpaceAclService;
    protected UserService userService;

    private static final String RULE_NAME_EMAIL = "email_notification_rule";
    private static final String MSG_EMAIL_SUBJECT = "activities.feed.notifier.email.subject";

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

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    //</editor-fold>
    public boolean isUserNotified(String username) throws KoyaServiceException {
        //TODO : use an aspect on user to save the value
        return !getEmailNotificationRule(username).isEmpty();
    }

    public List<Rule> getEmailNotificationRule(String username) throws KoyaServiceException {
        List<Rule> res = new ArrayList<>();

        User u = userService.getUserByUsername(username);

        /**
         * Get list of all Read availables secured items for users.
         *
         * Limited to
         */
        List<SecuredItem> readableDossiers = subSpaceAclService.getReadableSecuredItem(u, TYPEFILTER_DOSSIER);

        for (SecuredItem item : readableDossiers) {
            logger.debug("getEmailNotificationRule SecuredItem: " + item.getName());

            NodeRef nodeRef = item.getNodeRefasObject();
            List<Rule> rules = ruleService.getRules(nodeRef, false, RuleType.INBOUND);

            for (Rule r : rules) {
                if (RULE_NAME_EMAIL.equals(r.getTitle())
                        && ((ArrayList<String>) ((CompositeAction) r.getAction()).getAction(0).getParameterValue(MailActionExecuter.PARAM_TO_MANY)).contains(username)) {
                    res.add(r);
                }
            }
        }

        return res;
    }

    public void addRemoveUser(String username, boolean add) throws KoyaServiceException {

        User u = userService.getUserByUsername(username);

        /**
         * Get list of all Read availables secured items for users.
         *
         * Limited to Dossiers
         */
        List<SecuredItem> readableDossiers = subSpaceAclService.getReadableSecuredItem(u, TYPEFILTER_DOSSIER);

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

    private void createRule(final NodeRef nodeRef, String username) {
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
        ruleParameters.put(MailActionExecuter.PARAM_SUBJECT, "Nouvel élément ajouté");
        ruleParameters.put(MailActionExecuter.PARAM_TEMPLATE, getEmailTemplateRef());

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

    protected String getEmailTemplateRef() {
        String locationType = feedEmailTemplateLocation.getQueryLanguage();

        if (locationType.equals(SearchService.LANGUAGE_XPATH)) {
            StoreRef store = feedEmailTemplateLocation.getStoreRef();
            String xpath = feedEmailTemplateLocation.getPath();

            try {
                if (!feedEmailTemplateLocation.getQueryLanguage().equals(SearchService.LANGUAGE_XPATH)) {
                    logger.error("Cannot find the activities email template - repository location query language is not 'xpath': " + feedEmailTemplateLocation.getQueryLanguage());
                    return null;
                }

                List<NodeRef> nodeRefs = searchService.selectNodes(nodeService.getRootNode(store), xpath, null, namespaceService, false);
                if (nodeRefs.size() != 1) {
                    logger.error("Cannot find the activities email template: " + xpath);
                    return null;
                }

                return fileFolderService.getLocalizedSibling(nodeRefs.get(0)).toString();
            } catch (SearcherException e) {
                logger.error("Cannot find the email template!", e);
            }

            return null;
        } else if (locationType.equals(RepositoryLocation.LANGUAGE_CLASSPATH)) {
            return feedEmailTemplateLocation.getPath();
        } else {
            logger.error("Unsupported location type: " + locationType);
            return null;
        }
    }
}
