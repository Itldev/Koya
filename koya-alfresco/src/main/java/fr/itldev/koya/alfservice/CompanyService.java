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
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.model.impl.Company;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.script.Site;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 *
 * Koya companies are site whith activable aspect
 *
 */
public class CompanyService {

    private static final String SITE_PRESET = "site-dashboard";
    private static final String DESC = "Koya Company";
    private final Logger logger = Logger.getLogger(CompanyService.class);

    private SiteService siteService;
    private NodeService nodeService;
    private KoyaNodeService koyaNodeService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    // </editor-fold>
    public Company create(String title, SalesOffer sO) throws KoyaServiceException {

        if (title == null || title.isEmpty()) {
            throw new KoyaServiceException();//TODO préciser l'exc
        }

        String shortName = getShortNameFromTitle(title);
        SiteInfo sInfo = siteService.createSite(SITE_PRESET, shortName, title, DESC + " - " + shortName, SiteVisibility.PUBLIC);
        Company created = new Company(sInfo);
        koyaNodeService.setActiveStatus(sInfo.getNodeRef(), Boolean.TRUE);

        //TODO copy tree from template
        /**
         *
         */
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, "documentLibrary");
        ChildAssociationRef car = nodeService.createNode(created.getNodeRefasObject(),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "documentLibrary"),
                ContentModel.TYPE_FOLDER,
                properties);

        /**
         *
         */
        return created;
    }

    /**
     * List users available companies.
     *
     * @param userName
     * @return
     */
    public List<Company> list(String userName) {
        //TODO limit user's available sites only
        //TODO active sites only
        List<Company> socs = new ArrayList<>();

        for (SiteInfo s : siteService.listSites("", SITE_PRESET)) {
            if (isKoyaCompany(s.getNodeRef())) {
                socs.add(koyaNodeService.siteCompanyBuilder(s, userName));
            }
        }
        return socs;
    }
    
    public SiteInfo getSiteInfo(String shortName){
        return siteService.getSite(shortName);
    }
    
     public SiteInfo getSiteInfo(NodeRef nodeSite){
        return siteService.getSite(nodeSite);
    }

    public List<SalesOffer> listSalesOffer() {
        return null;

    }

    public SalesOffer getSalesOffer(String offerName) {
        return null;

    }

    /**
     * Site name normalisation method
     *
     * @return
     */
    private String getShortNameFromTitle(String title) {
        //TODO use regex
        String shortTitle = title.replace(" ", "-");

        /**
         * shortTitle is unique even if title alredy exists.
         */
        String buildTitle = shortTitle;
        int incr = 1;
        while (siteService.getSite(buildTitle) != null) {
            buildTitle = shortTitle + "-" + incr;
            incr++;
        }
        return buildTitle;
    }

    /**
     * Koya company is a site with activable aspect.
     *
     * @param n
     * @return
     */
    public Boolean isKoyaCompany(NodeRef n) {

        return nodeService.getType(n).equals(KoyaModel.QNAME_KOYA_COMPANY)
                && nodeService.hasAspect(n, KoyaModel.QNAME_KOYA_ACTIVABLE);
    }

}
