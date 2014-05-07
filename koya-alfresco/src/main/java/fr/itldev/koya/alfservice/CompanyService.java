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
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.HiddenAspect;
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
    private static final String KOYA_CONFIG = "koya-config";

    private final Logger logger = Logger.getLogger(CompanyService.class);

    // service beans
    protected SiteService siteService;
    protected NodeService nodeService;
    protected NodeService unprotNodeService;
    protected KoyaNodeService koyaNodeService;
    protected HiddenAspect hiddenAspect;
    protected ModelService modelService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setUnprotectedNodeService(NodeService unprotNodeService) {
        this.unprotNodeService = unprotNodeService;
    }

    public void setHiddenAspect(HiddenAspect hiddenAspect) {
        this.hiddenAspect = hiddenAspect;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    // </editor-fold>
    public Company create(String title, SalesOffer sO, String template) throws KoyaServiceException {

        if (title == null || title.isEmpty()) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_EMPTY_TITLE);
        }

        String shortName = getShortNameFromTitle(title);
        SiteInfo sInfo = siteService.createSite(SITE_PRESET, shortName, title, DESC + " - " + shortName, SiteVisibility.PUBLIC);
        Company created = new Company(sInfo);
        koyaNodeService.setActiveStatus(sInfo.getNodeRef(), Boolean.TRUE);

        //Creating koya-config directory
        NodeRef koyaConfig = getKoyaConfigNodeRef(sInfo.getNodeRef(), true);

        modelService.companyInitTemplate(shortName, template);

        //TODO copy config files to the koy-config directory
        return created;
    }

    /**
     * List users available companies.
     *    
     * @return
     */
    public List<Company> list() {
        //TODO limit user's available sites only
        //TODO active sites only
        List<Company> socs = new ArrayList<>();

        for (SiteInfo s : siteService.listSites("", SITE_PRESET)) {
            if (koyaNodeService.isKoyaCompany(s.getNodeRef())) {
                socs.add(koyaNodeService.siteCompanyBuilder(s));
            }
        }
        return socs;
    }

    public SiteInfo getSiteInfo(String shortName) {
        return siteService.getSite(shortName);
    }

    public SiteInfo getSiteInfo(NodeRef nodeSite) {
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
        String shortTitle = title.replaceAll("[^0-9a-zA-Z\\-\\s]", "")
                .replaceAll("\\s+", "-").toLowerCase();

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

    /**
     * Return the "koya-config" noderef under the given root. No attempt will be
     * made to create the node if it does not exist yet.
     *
     * @param rootRef Root node reference where the "koya-config" folder should
     * live
     *
     * @return koya-config folder ref if found, null otherwise
     */
    private NodeRef getKoyaConfigNodeRef(final NodeRef rootRef) {
        return getKoyaConfigNodeRef(rootRef, false);
    }

    /**
     * Return the "koya-config" noderef under the given root. Optionally create
     * the folder if it does not exist yet. NOTE: must only be set to create if
     * within a WRITE transaction context.
     * <p>
     * Adds the "isIndexed = false" property to the koya-config folder node.
     *
     * @param rootRef Root node reference where the "koya-config" folder should
     * live
     * @param create True to create the folder if missing, false otherwise
     *
     * @return koya-config folder ref if found, null otherwise if not creating
     */
    protected NodeRef getKoyaConfigNodeRef(final NodeRef rootRef, final boolean create) {
        NodeRef koyaConfigRef = this.unprotNodeService.getChildByName(
                rootRef, ContentModel.ASSOC_CONTAINS, KOYA_CONFIG);
        if (create && koyaConfigRef == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("'koya-config' folder not found under path, creating...");
            }
            QName assocQName = QName.createQName(DESC).createQName(NamespaceService.CONTENT_MODEL_1_0_URI, KOYA_CONFIG);
            Map<QName, Serializable> properties = new HashMap<>(1, 1.0f);
            properties.put(ContentModel.PROP_NAME, (Serializable) KOYA_CONFIG);
            ChildAssociationRef ref = this.unprotNodeService.createNode(
                    rootRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER, properties);
            koyaConfigRef = ref.getChildRef();
            // koya-config needs to be hidden - applies index control aspect as part of the hidden aspect
            hiddenAspect.hideNode(ref.getChildRef(), false, false, false);
        }
        return koyaConfigRef;
    }
}
