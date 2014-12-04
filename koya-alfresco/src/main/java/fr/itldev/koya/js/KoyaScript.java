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
package fr.itldev.koya.js;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.KoyaNotificationService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.alfservice.security.SubSpaceAclService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.permissions.KoyaPermissionConsumer;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;

/**
 *
 *
 */
public class KoyaScript extends BaseProcessorExtension {

    private SubSpaceAclService subSpaceAclService;
    private KoyaNodeService koyaNodeService;
    private UserService userService;
    private CompanyService companyService;
    private KoyaNotificationService koyaNotificationService;
    private NodeService nodeService;
    private RuleService ruleService;

    private ServiceRegistry serviceRegistry;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSubSpaceAclService(SubSpaceAclService subSpaceAclService) {
        this.subSpaceAclService = subSpaceAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setKoyaNotificationService(KoyaNotificationService koyaNotificationService) {
        this.koyaNotificationService = koyaNotificationService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    //</editor-fold>
    /**
     * List users who can access a node.
     *
     * @param n
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<ScriptNode> listConsumersAccess(ScriptNode n) throws KoyaServiceException {
        List<ScriptNode> users = new ArrayList<>();
        SecuredItem s = koyaNodeService.getSecuredItem(n.getNodeRef());
        for (User u : subSpaceAclService.listUsers(s, KoyaPermissionConsumer.getAll())) {
            users.add(new ScriptNode(u.getNodeRefasObject(), serviceRegistry));
        }

        return users;
    }

    /**
     *
     * @param n
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<ScriptNode> listUserNodes(ScriptNode n) throws KoyaServiceException {
        List<ScriptNode> sharedElements = new ArrayList<>();

        for (Company c : companyService.list()) {
            for (SecuredItem s : subSpaceAclService.listSecuredItems(c,
                    userService.buildUser(n.getNodeRef()),
                    KoyaPermission.getAll())) {
                sharedElements.add(new ScriptNode(s.getNodeRefasObject(), serviceRegistry));
            }
        }
        return sharedElements;
    }

    /**
     * Create notification rule for company if such a rules doesn't exists
     *
     * @param n
     * @throws java.lang.Exception
     */
    public void createNotificationRule(ScriptNode n) throws Exception {

        Company c = koyaNodeService.getSecuredItem(n.getNodeRef(), Company.class);

        //checks if company rule exists.
        final NodeRef docLibNodeRef = nodeService.getChildByName(
                c.getNodeRefasObject(), ContentModel.ASSOC_CONTAINS, "documentLibrary");

        List<Rule> rules = ruleService.getRules(docLibNodeRef, true, RuleType.INBOUND);

        for (Rule r : rules) {
            if (r.getTitle().equals("Koya notification rule")) {
                throw new Exception("Rule alredy exists fro this company");
            }
        }

        //create company rule
        koyaNotificationService.createCompanyNotificationRule(c);
    }

}
