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

package fr.itldev.koya.action;

import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.log4j.Logger;

/**
 * This action cleans all permission references to user defined by username in
 * company.
 *
 */
public class CleanUserPermissionsActionExecuter extends ActionExecuterAbstractBase {

    private Logger logger = Logger.getLogger(this.getClass());

    public static final String PARAM_USERNAME = "userName";

    private NodeService nodeService;
    protected PermissionService permissionService;
    protected SearchService searchService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

        String userName = (String) ruleAction.getParameterValue(PARAM_USERNAME);

        if (!nodeService.getType(actionedUponNodeRef).equals(SiteModel.TYPE_SITE)) {
            //if node is not a site then abort action
            return;
        }
        delPermissionsDossiers(actionedUponNodeRef, userName);

    }

    private void delPermissionsDossiers(NodeRef companyNodeRef, String userName) {
        String siteName = (String) nodeService.getProperty(companyNodeRef, ContentModel.PROP_NAME);
        String luceneRequest = "TYPE:\"koya:dossier\" AND PATH:\"/app:company_home/st:sites/cm:" + siteName + "//*\"";

        ResultSet rs = null;
        try {
            rs = searchService.query(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_LUCENE, luceneRequest);
            for (ResultSetRow r : rs) {
                try {
                    permissionService.deletePermission(r.getNodeRef(), userName, PermissionService.READ);
                } catch (Exception ex) {
                    logger.error("error revoke permission on " + nodeService.getProperty(r.getNodeRef(), ContentModel.PROP_NAME) + " -- " + ex.getMessage());
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private void delPermissionsRecursive(NodeRef n, String userName) {

        try {
            permissionService.deletePermission(n, userName, PermissionService.READ);
        } catch (Exception ex) {
            logger.error("err delete on " + nodeService.getProperty(n, ContentModel.PROP_NAME) + " -- " + ex.getMessage());
        }

        for (ChildAssociationRef car : nodeService.getChildAssocs(n)) {
            delPermissionsRecursive(car.getChildRef(), userName);
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

        paramList.add(new ParameterDefinitionImpl(PARAM_USERNAME, DataTypeDefinition.NODE_REF,
                true, getParamDisplayLabel(PARAM_USERNAME)));
    }

}
