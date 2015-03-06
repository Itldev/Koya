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

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * 
 * Koya companies are site whith activable aspect
 * 
 */
public class CompanyService {

    private static final String SITE_PRESET = "site-dashboard";

    private static final String PREFS_FILE_NAME = "preferences.properties";
    private static final String DESC = "Koya Company";



    private final Logger logger = Logger.getLogger(CompanyService.class);
    // service beans
    protected SiteService siteService;
    protected NodeService nodeService;
    protected NodeService unprotNodeService;
    protected KoyaNodeService koyaNodeService;
    protected ModelService modelService;
    protected AuthenticationService authenticationService;
    protected FileFolderService fileFolderService;
    protected KoyaNotificationService koyaNotificationService;
    protected SearchService searchService;
    protected CompanyPropertiesService companyPropertiesService;
    

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

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public void setAuthenticationService(
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setKoyaNotificationService(
            KoyaNotificationService koyaNotificationService) {
        this.koyaNotificationService = koyaNotificationService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
   
    public void setCompanyPropertiesService(
            CompanyPropertiesService companyPropertiesService) {
        this.companyPropertiesService = companyPropertiesService;
    }

    // </editor-fold>
    public Company create(String compTitle, SalesOffer sO, String spaceTemplate)
            throws KoyaServiceException {

        if (compTitle == null || compTitle.isEmpty()) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_EMPTY_TITLE);
        }
        String title;
        try {
            title = URLDecoder.decode(compTitle, "UTF-8");
        } catch (Exception ex) {
            title = compTitle;
            logger.warn("not able to decode UTF-8 URL encoding company title : using input company title without modification");
        }

        String shortName = getShortNameFromTitle(title);

        SiteInfo sInfo = siteService.createSite(SITE_PRESET, shortName, title,
                DESC + " - " + shortName, SiteVisibility.PRIVATE);
        Company created = koyaNodeService.getSecuredItem(sInfo.getNodeRef(),
                Company.class);

        // Creating koya-config directory
        // todo difference between company config (ie company hime) and koya
        // site config (default site)
        getKoyaConfigNodeRef(sInfo.getNodeRef(), true);

        // Creating koya:companyProperties node (COMPANYPROPERTIES_FILE_NAME) =
        // companyHome
        // in site koyaConfig folder (default location)
        // todo define outside site company home
        NodeRef companyPropertiesNode = companyPropertiesService.createCompanyConfigFile(created);
        
        // put companySite Aspect with default company.properties reference.
        Map<QName, Serializable> props = new HashMap<>();
        props.put(KoyaModel.PROP_COMPANYHOME, companyPropertiesNode);
        nodeService.addAspect(created.getNodeRefasObject(),
                KoyaModel.ASPECT_COMPANYSITE, props);

        modelService.companyInitTemplate(shortName, spaceTemplate);

        modelService.companyInitImports(shortName);

        // add users notification rule on documentLibraryNode
        koyaNotificationService.createCompanyNotificationRule(created);

        // TODO copy config files to the koya-config directory
        return created;
    }

    public Company getCompany(String shortName) throws KoyaServiceException {
        return koyaNodeService.getSecuredItem(getSiteInfo(shortName)
                .getNodeRef(), Company.class);
    }

    /**
     * List users available companies.
     * 
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<Company> list() throws KoyaServiceException {
        // TODO limit user's available sites only
        List<Company> socs = new ArrayList<>();

        for (SiteInfo s : siteService.listSites(authenticationService
                .getCurrentUserName())) {

            if (nodeService.hasAspect(s.getNodeRef(),
                    KoyaModel.ASPECT_COMPANYSITE)) {
                socs.add(koyaNodeService.getSecuredItem(s.getNodeRef(),
                        Company.class));
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
     * ================= Preferences Methods ===========================
     */
    /**
     * Get company preferences from properties file.
     * 
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Preferences getPreferences(String companyName)
            throws KoyaServiceException {
        Preferences prefs = new Preferences();
        Company c = koyaNodeService.getSecuredItem(
                siteService.getSite(companyName).getNodeRef(), Company.class);

        Properties compProperties;
        try {
            // TODO use lucene path to retrieve node reference.
            compProperties = koyaNodeService
                    .readPropertiesFileContent(getCompanyConfigFile(c,
                            PREFS_FILE_NAME));
        } catch (NullPointerException nex) {
            return prefs;
        }

        for (Object k : compProperties.keySet()) {
            prefs.put((String) k, compProperties.get(k));
        }

        return prefs;
    }

    /**
     * Get single preference identified by a key for a company
     * 
     * @param companyName
     * @param preferenceKey
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public String getPreference(String companyName, String preferenceKey)
            throws KoyaServiceException {

        Company c = koyaNodeService.getSecuredItem(
                siteService.getSite(companyName).getNodeRef(), Company.class);

        Properties compProperties;
        try {
            // TODO use lucene path to retrieve node reference.
            compProperties = koyaNodeService
                    .readPropertiesFileContent(getCompanyConfigFile(c,
                            PREFS_FILE_NAME));
        } catch (NullPointerException nex) {
            return "";
        }
        try {
            return compProperties.getProperty(preferenceKey);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Writes current company object preferences as preferences file.
     * 
     * @param companyName
     * @param prefsToCommit
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void commitPreferences(String companyName, Preferences prefsToCommit)
            throws KoyaServiceException {

        Company c = koyaNodeService.getSecuredItem(
                siteService.getSite(companyName).getNodeRef(), Company.class);
        NodeRef prefFileNodeRef = getCompanyConfigFile(c, PREFS_FILE_NAME);

        if (prefFileNodeRef != null) {
            fileFolderService.delete(prefFileNodeRef);
        }

        // create preferences file
        NodeRef koyaConfigNr = getKoyaConfigNodeRef(c.getNodeRefasObject(),
                false);
        prefFileNodeRef = fileFolderService.create(koyaConfigNr,
                PREFS_FILE_NAME, ContentModel.TYPE_CONTENT).getNodeRef();

        // write preferences file new version
        ContentWriter writer = fileFolderService.getWriter(prefFileNodeRef);

        // placeholder = write comment first line
        // writer.putContent(" ");
        for (String key : prefsToCommit.keySet()) {
            writer.putContent(key + "=" + prefsToCommit.get(key));
        }
    }

    /**
     * ================= Properties Methods ==============================
     */

    
    /**
     * =========================================================================
     */
    /**
     * Site name normalisation method
     * 
     * @return
     */
    private String getShortNameFromTitle(String title) {

        String shortTitle = title.replaceAll("[àáâãäå]", "a")
                .replaceAll("æ", "ae").replaceAll("ç", "c")
                .replaceAll("[èéêë]", "e").replaceAll("[ìíîï]", "i")
                .replaceAll("ñ", "n").replaceAll("[òóôõö]", "o")
                .replaceAll("œ", "oe").replaceAll("[ùúûü]", "u")
                .replaceAll("[ýÿ]", "y").replaceAll("&", "and")
                .replaceAll("\\s+", "-").toLowerCase();

        // TODO check invalid characters like €

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

        return nodeService.getType(n).equals(KoyaModel.TYPE_COMPANY)
                && nodeService.hasAspect(n, KoyaModel.ASPECT_ACTIVABLE);
    }

   
    /**
     * Returns company specific file : file stored in 'koya-config' company
     * directory.
     * 
     * retuns null if file doesn't exists.
     * 
     * @param c
     * @return
     */
    private NodeRef getCompanyConfigFile(Company c, String fileName) {
        // TODO Read From company Home variable
        NodeRef prefFileNodeRef = null;
        try {

            prefFileNodeRef = nodeService.getChildByName(
                    getKoyaConfigNodeRef(c.getNodeRefasObject(), false),
                    ContentModel.ASSOC_CONTAINS, fileName);
        } catch (Exception ex) {
        }
        return prefFileNodeRef;
    }
    
    /**
     * Return the "koya-config" noderef under the given root. Optionally create
     * the folder if it does not exist yet. NOTE: must only be set to create if
     * within a WRITE transaction context.
     * <p>
     * Adds the "isIndexed = false" property to the koya-config folder node.
     * 
     * @param rootRef
     *            Root node reference where the "koya-config" folder should live
     * @param create
     *            True to create the folder if missing, false otherwise
     * 
     * @return koya-config folder ref if found, null otherwise if not creating
     */
    @Deprecated
    protected NodeRef getKoyaConfigNodeRef(final NodeRef rootRef,
            final boolean create) {
        NodeRef koyaConfigRef = this.unprotNodeService.getChildByName(rootRef,
                ContentModel.ASSOC_CONTAINS, CompanyPropertiesService.KOYA_CONFIG);
        if (create && koyaConfigRef == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("'koya-config' folder not found under path, creating...");
            }
            QName assocQName = QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI,  CompanyPropertiesService.KOYA_CONFIG);
            Map<QName, Serializable> properties = new HashMap<>(1, 1.0f);
            properties.put(ContentModel.PROP_NAME, (Serializable)  CompanyPropertiesService.KOYA_CONFIG);
            ChildAssociationRef ref = this.unprotNodeService.createNode(
                    rootRef, ContentModel.ASSOC_CONTAINS, assocQName,
                    ContentModel.TYPE_FOLDER, properties);
            koyaConfigRef = ref.getChildRef();
            // koya-config needs to be hidden - applies index control aspect as
            // part of the hidden aspect

            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
            props.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
            nodeService.addAspect(ref.getChildRef(),
                    ContentModel.ASPECT_INDEX_CONTROL, props);

        }
        return koyaConfigRef;
    }


 

}
