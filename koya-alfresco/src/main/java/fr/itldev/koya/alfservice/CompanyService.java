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
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.CompanyProperties;
import fr.itldev.koya.model.impl.Contact;
import fr.itldev.koya.model.impl.ContactItem;
import fr.itldev.koya.model.impl.GeoPos;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.impl.SalesOffer;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
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

    private static final String PREFS_FILE_NAME = "preferences.properties";
    private static final String LOGO_FILE_NAME_PREFIX = "logo";
    private static final String COMPANYPROPERTIES_FILE_NAME = "company.properties";

    private final Logger logger = Logger.getLogger(CompanyService.class);
    // service beans
    protected SiteService siteService;
    protected NodeService nodeService;
    protected NodeService unprotNodeService;
    protected KoyaNodeService koyaNodeService;
    protected ModelService modelService;
    protected AuthenticationService authenticationService;
    protected FileFolderService fileFolderService;

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

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    // </editor-fold>
    public Company create(String title, SalesOffer sO, String spaceTemplate) throws KoyaServiceException {

        if (title == null || title.isEmpty()) {
            throw new KoyaServiceException(KoyaErrorCodes.COMPANY_EMPTY_TITLE);
        }

        String shortName = getShortNameFromTitle(title);
        SiteInfo sInfo = siteService.createSite(SITE_PRESET, shortName, title, DESC + " - " + shortName, SiteVisibility.PRIVATE);
        Company created = koyaNodeService.getSecuredItem(sInfo.getNodeRef(), Company.class);

        //Creating koya-config directory
        NodeRef koyaConfig = getKoyaConfigNodeRef(sInfo.getNodeRef(), true);

        modelService.companyInitTemplate(shortName, spaceTemplate);

        modelService.companyInitImports(shortName);

        //TODO copy config files to the koy-config directory
        return created;
    }

    /**
     * List users available companies.
     *
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<Company> list() throws KoyaServiceException {
        //TODO limit user's available sites only
        List<Company> socs = new ArrayList<>();

        for (SiteInfo s : siteService.listSites(authenticationService.getCurrentUserName())) {
            socs.add(koyaNodeService.getSecuredItem(s.getNodeRef(), Company.class));
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
    public Preferences getPreferences(String companyName) throws KoyaServiceException {
        Preferences prefs = new Preferences();
        Company c = koyaNodeService.getSecuredItem(siteService.getSite(companyName).getNodeRef(), Company.class);

        Properties compProperties;
        try {
            //TODO use lucene path to retrieve node reference.
            compProperties = koyaNodeService.readPropertiesFileContent(getCompanyConfigFile(c, PREFS_FILE_NAME));
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
    public String getPreference(String companyName, String preferenceKey) throws KoyaServiceException {

        Company c = koyaNodeService.getSecuredItem(siteService.getSite(companyName).getNodeRef(), Company.class);

        Properties compProperties;
        try {
            //TODO use lucene path to retrieve node reference.
            compProperties = koyaNodeService.readPropertiesFileContent(getCompanyConfigFile(c, PREFS_FILE_NAME));
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
    public void commitPreferences(String companyName, Preferences prefsToCommit) throws KoyaServiceException {

        Company c = koyaNodeService.getSecuredItem(siteService.getSite(companyName).getNodeRef(), Company.class);
        NodeRef prefFileNodeRef = getCompanyConfigFile(c, PREFS_FILE_NAME);

        if (prefFileNodeRef != null) {
            fileFolderService.delete(prefFileNodeRef);
        }

        //create preferences file
        NodeRef koyaConfigNr = getKoyaConfigNodeRef(c.getNodeRefasObject(), false);
        prefFileNodeRef = fileFolderService.create(koyaConfigNr, PREFS_FILE_NAME, ContentModel.TYPE_CONTENT).getNodeRef();

        //write preferences file new version
        ContentWriter writer = fileFolderService.getWriter(prefFileNodeRef);

        //placeholder = write comment first line
        // writer.putContent(" ");
        for (String key : prefsToCommit.keySet()) {
            writer.putContent(key + "=" + prefsToCommit.get(key));
        }
    }

    /**
     * ================= Properties Methods ==============================
     */
    /**
     * Get Company properties.
     *
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public CompanyProperties getProperties(String companyName) throws KoyaServiceException {

        Company c = koyaNodeService.getSecuredItem(siteService.getSite(companyName).getNodeRef(), Company.class);
        CompanyProperties cp = new CompanyProperties(companyName);

        NodeRef companyPropertiesNodeRef = getCompanyConfigFile(c, COMPANYPROPERTIES_FILE_NAME);

        if (companyPropertiesNodeRef == null) {
            FileInfo fi = fileFolderService.create(getKoyaConfigNodeRef(c.getNodeRefasObject(), true),
                    COMPANYPROPERTIES_FILE_NAME, KoyaModel.TYPE_COMPANYPROPERTIES);
            companyPropertiesNodeRef = fi.getNodeRef();
        }

        if (companyPropertiesNodeRef != null) {
            //if geographic aspect is set,  get latitude and longitude
            if (nodeService.hasAspect(companyPropertiesNodeRef, ContentModel.ASPECT_GEOGRAPHIC)) {
                cp.setGeoPos(new GeoPos(
                        (Double) nodeService.getProperty(companyPropertiesNodeRef, ContentModel.PROP_LATITUDE),
                        (Double) nodeService.getProperty(companyPropertiesNodeRef, ContentModel.PROP_LONGITUDE)));
            }

            //address, description,legal informations - company properties attributes
            cp.setCompanyAddress((String) nodeService.getProperty(companyPropertiesNodeRef, KoyaModel.PROP_ADDRESS));
            cp.setDescription((String) nodeService.getProperty(companyPropertiesNodeRef, KoyaModel.PROP_DESCRIPTION));
            cp.setLegalInformations((String) nodeService.getProperty(companyPropertiesNodeRef, KoyaModel.PROP_LEGALINFOS));

            for (ChildAssociationRef car : nodeService.getChildAssocs(companyPropertiesNodeRef)) {

                NodeRef currentNodeRef = car.getChildRef();
                QName currentNodeRefType = nodeService.getType(currentNodeRef);
                String currentNodeRefName = (String) nodeService.getProperty(currentNodeRef, ContentModel.PROP_NAME);

                if (currentNodeRefName.startsWith(LOGO_FILE_NAME_PREFIX)) {
                    /**
                     * Each file wich name starts with logo is considered as
                     * company logo.
                     *
                     * If contact or contactItem name, starts with 'logo', it
                     * won't be used as it should be
                     */
                    cp.setLogoNodeRef(car.getChildRef().toString());
                } else if (currentNodeRefType.equals(KoyaModel.TYPE_CONTACTITEM)) {
                    try {
                        ContactItem companyCi = new ContactItem();

                        companyCi.setValue((String) nodeService.getProperty(currentNodeRef, KoyaModel.PROP_CONTACTITEM_VALUE));
                        companyCi.setType(Integer.valueOf((String) nodeService.getProperty(currentNodeRef, KoyaModel.PROP_CONTACTITEM_TYPE)));
                        cp.getContactItems().add(companyCi);
                    } catch (NumberFormatException | InvalidNodeRefException ex) {
                        //silent exception catching : if error occurs no contact created
                    }

                } else if (currentNodeRefType.equals(KoyaModel.TYPE_CONTACT)) {
                    Contact contact = new Contact();

                    contact.setTitle((String) nodeService.getProperty(currentNodeRef, KoyaModel.PROP_CONTACT_TITLE));

                    //========= get associated user informations
                    List<AssociationRef> assocRefs = nodeService.getTargetAssocs(currentNodeRef, KoyaModel.ASSOC_CONTACT_USER);
                    //there must be exactly one associated user
                    if (assocRefs.size() != 1) {
                        logger.error("Error retrieving contact asociated user : skip");
                        break;
                    }
                    NodeRef userNodeRef = assocRefs.get(0).getTargetRef();

                    contact.setUserNodeRef(userNodeRef.toString());
                    contact.setFirstName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_FIRSTNAME));
                    contact.setLastName((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_LASTNAME));

                    ContactItem contactMail = new ContactItem();
                    contactMail.setType(ContactItem.TYPE_MAIL);
                    contactMail.setValue((String) nodeService.getProperty(userNodeRef, ContentModel.PROP_EMAIL));

                    contact.getContactItems().add(contactMail);

                    // ========= get additionnal contacts =======
                    for (ChildAssociationRef carContact : nodeService.getChildAssocs(currentNodeRef)) {
                        NodeRef currentContactNodeRef = carContact.getChildRef();
                        if (nodeService.getType(currentContactNodeRef).equals(KoyaModel.TYPE_CONTACTITEM)) {
                            try {

                                ContactItem contactCi = new ContactItem();
                                contactCi.setValue((String) nodeService.getProperty(currentContactNodeRef, KoyaModel.PROP_CONTACTITEM_VALUE));
                                contactCi.setType(Integer.valueOf((String) nodeService.getProperty(currentContactNodeRef, KoyaModel.PROP_CONTACTITEM_TYPE)));
                                contact.getContactItems().add(contactCi);
                            } catch (NumberFormatException | InvalidNodeRefException ex) {
                                //silent exception catching : if error occurs no contact created
                            }
                        }
                    }

                    cp.getContacts().add(contact);
                }

            }
        }

        return cp;
    }

    /**
     *
     * @param companyName
     * @param compProperties
     * @throws KoyaServiceException
     */
    public void commitProperties(String companyName, CompanyProperties compProperties) throws KoyaServiceException {
        Company c = koyaNodeService.getSecuredItem(siteService.getSite(companyName).getNodeRef(), Company.class);
        //TODO implement this method
    }

    /**
     * =========================================================================
     */
    /**
     * Site name normalisation method
     *
     * @return
     */
    private String getShortNameFromTitle(String title) {

        String shortTitle = koyaNodeService.getUniqueValidFileNameFromTitle(title);

        shortTitle = shortTitle.replaceAll("[àáâãäå]", "a")
                .replaceAll("æ", "ae")
                .replaceAll("ç", "c")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("ñ", "n")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("œ", "oe")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ýÿ]", "y")
                .replaceAll("&", "and")
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

        return nodeService.getType(n).equals(KoyaModel.TYPE_COMPANY)
                && nodeService.hasAspect(n, KoyaModel.ASPECT_ACTIVABLE);
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

            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
            props.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
            nodeService.addAspect(ref.getChildRef(), ContentModel.ASPECT_INDEX_CONTROL, props);

        }
        return koyaConfigRef;
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
        NodeRef prefFileNodeRef = null;
        try {
            prefFileNodeRef = nodeService.getChildByName(
                    getKoyaConfigNodeRef(c.getNodeRefasObject(), false), ContentModel.ASSOC_CONTAINS, fileName);
        } catch (Exception ex) {
        }
        return prefFileNodeRef;
    }
}
