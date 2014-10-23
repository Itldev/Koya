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

import fr.itldev.koya.action.DossierImportActionExecuter;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.permissions.SitePermission;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.services.impl.AlfrescoRestService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO9075;
import org.alfresco.util.PropertyMap;

/**
 *
 */
public class ModelService extends AlfrescoRestService {

    private static final String SPACE_TEMPLATE_PATH = "/app:company_home/app:dictionary/app:koya_space_templates";
    private static final String REST_GET_DOC_LIB_LIST = "/slingshot/doclib/containers/";
    private static final String IMPORT_FOLDER_NAME = "import";

    // Dependencies
    private NodeService nodeService;
    private CopyService copyService;
    private SearchService searchService;
    private SiteService siteService;
    protected PermissionService permissionService;
    protected PersonService personService;
    protected MutableAuthenticationService authenticationService;
    protected ActionService actionService;
    protected RuleService ruleService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCopyService(CopyService copyService) {
        this.copyService = copyService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    // </editor-fold>
    /**
     *
     * @param siteShortName
     * @param templateName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     *
     * @retun company doclib noderef
     */
    public NodeRef companyInitTemplate(String siteShortName, String templateName) throws KoyaServiceException {
        NodeRef docLib = null;

        ResultSet rs = null;
        try {
            SiteInfo siteInfo = siteService.getSite(siteShortName);

            if (siteInfo != null) {
                NodeRef companyNodeRef = siteInfo.getNodeRef();

                docLib = nodeService.getChildByName(companyNodeRef, ContentModel.ASSOC_CONTAINS, "documentLibrary");
                if (docLib == null) {
                    docLib = siteService.createContainer(siteShortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
                }

                rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                        SearchService.LANGUAGE_XPATH,
                        SPACE_TEMPLATE_PATH + "/cm:" + ISO9075.encode((templateName == null) ? "default" : templateName));

                NodeRef template = null;
                if (rs.length() == 1) {
                    template = rs.getNodeRef(0);
                    /**
                     * default space created by template copying
                     */
                    for (ChildAssociationRef associationRef : nodeService.getChildAssocs(template)) {
                        //  spaceService.createFromTemplate(associationRef.getChildRef(), docLib, null);

                        copyService.copyAndRename(associationRef.getChildRef(),
                                docLib,
                                associationRef.getTypeQName(),
                                associationRef.getQName(),
                                true);

                    }

                } else {
                    throw new KoyaServiceException(KoyaErrorCodes.SPACE_TEMPLATE_NOT_FOUND);
                }
            } else {
                throw new KoyaServiceException(KoyaErrorCodes.COMPANY_SITE_NOT_FOUND);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return docLib;
    }

    /**
     * Creates company default import user (<companyName>_import) and import
     * folder giving user permissions.
     *
     * @param siteShortName
     * @return
     */
    public NodeRef companyInitImports(String siteShortName) {
        SiteInfo siteInfo = siteService.getSite(siteShortName);

        /**
         * Create importer user
         */
        String userName = siteShortName + "_" + IMPORT_FOLDER_NAME;

        PropertyMap propsUser = new PropertyMap();
        propsUser.put(ContentModel.PROP_USERNAME, userName);
        propsUser.put(ContentModel.PROP_FIRSTNAME, userName);
        propsUser.put(ContentModel.PROP_LASTNAME, userName);
        propsUser.put(ContentModel.PROP_EMAIL, userName + "@alfresco.com");

        //initialize importer user with generated password : admin has to modify it
        authenticationService.createAuthentication(userName, GUID.generate().toCharArray());
        personService.createPerson(propsUser);

        /**
         * give conbtributor role to this user
         */
        siteService.setMembership(siteShortName, userName, SitePermission.CONTRIBUTOR.toString());

        /**
         * create import directory with default permissions
         */
        //build node properties
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, IMPORT_FOLDER_NAME);
        properties.put(ContentModel.PROP_TITLE, IMPORT_FOLDER_NAME);

        ChildAssociationRef car = nodeService.createNode(siteInfo.getNodeRef(), ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, IMPORT_FOLDER_NAME),
                ContentModel.TYPE_FOLDER,
                properties);

        Rule importRule = new Rule();
        importRule.setRuleTypes(new ArrayList<String>() {
            {
                add(RuleType.INBOUND);
                add(RuleType.UPDATE);
            }
        });
        importRule.applyToChildren(false);
        importRule.setTitle("Import Koya Zip File");

        CompositeAction compositeAction = actionService.createCompositeAction();
        importRule.setAction(compositeAction);
        importRule.setExecuteAsynchronously(true);

        Action action = actionService.createAction(DossierImportActionExecuter.NAME);
        action.setExecuteAsynchronously(true);

        compositeAction.addAction(action);

        ruleService.saveRule(car.getChildRef(), importRule);

        return car.getChildRef();
    }

}
