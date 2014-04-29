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

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.services.impl.AlfrescoRestService;
import java.net.HttpURLConnection;
import java.net.URL;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO9075;

/**
 *
 */
public class ModelService extends AlfrescoRestService {

    private static final String SPACE_TEMPLATE_PATH = "/app:company_home/app:dictionary/app:koya_space_templates";
    private static final String REST_GET_DOC_LIB_LIST = "/slingshot/doclib/containers/";
    
    // Dependencies
    private NodeService nodeService;
    private CopyService copyService;
    private SearchService searchService;
    private SiteService siteService;
    
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
                if(docLib == null)
                    docLib = siteService.createContainer(siteShortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
//
//                    siteService.createContainer(siteShortName, "documentLibrary"templateName, null, null)

                rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                        SearchService.LANGUAGE_XPATH,
                        SPACE_TEMPLATE_PATH + "/cm:" + ISO9075.encode((templateName == null) ? "default" : templateName));

                NodeRef template = null;
                if (rs.length() == 1) {
                    template = rs.getNodeRef(0);

//                //copie des templates
                    for (ChildAssociationRef associationRef : nodeService.getChildAssocs(template)) {
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
     * Templates existance check method
     */
    public void checkOrInitKoyaModels() {

    }

}
