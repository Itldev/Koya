package fr.itldev.koya.alfservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.CompanyProperties;
import fr.itldev.koya.model.impl.Contact;
import fr.itldev.koya.model.impl.ContactItem;
import fr.itldev.koya.model.impl.GeoPos;

public class CompanyPropertiesService {

    private static final String XPATH_DEFAULTCOMPANY_LOGO = "//app:company_home/app:dictionary/cm:koya/cm:default-company/cm:company.properties/cm:logo.png";
    private static final String COMPANYPROPERTIES_FILE_NAME = "company.properties";
    protected static final String KOYA_CONFIG = "koya-config";

    private static final Set<QName> QNAMEFILTER_CONTACTITEM = new HashSet<QName>() {
        {
            add(KoyaModel.TYPE_CONTACTITEM);
        }
    };

    private static final Set<QName> QNAMEFILTER_CONTACT = new HashSet<QName>() {
        {
            add(KoyaModel.TYPE_CONTACT);
        }
    };

    private NodeRef defaultLogo;

    private final Logger logger = Logger
            .getLogger(CompanyPropertiesService.class);

    protected KoyaNodeService koyaNodeService;
    protected SearchService searchService;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected UserService userService;
    protected FileFolderService fileFolderService;
    protected NodeService unprotNodeService;

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setUnprotNodeService(NodeService unprotNodeService) {
        this.unprotNodeService = unprotNodeService;
    }

    public NodeRef getLogo(String companyShortName) throws KoyaServiceException {
        return getLogo(koyaNodeService.companyBuilder(companyShortName));
    }

    /**
     * 
     * @param c
     * @return
     */
    public NodeRef getLogo(Company c) {
        // search in company koya-config dir a logo

        NodeRef companyPropertiesNodeRef = (NodeRef) nodeService.getProperty(
                c.getNodeRef(), KoyaModel.PROP_COMPANYHOME);

        List<AssociationRef> logos = nodeService.getTargetAssocs(
                companyPropertiesNodeRef, KoyaModel.ASSOC_LOGO_COMPANY);
        if (logos.size() == 1) {
            // retrieve company property current logo
            return logos.get(0).getTargetRef();
        } else {
            //
            if (logos.size() > 1) {
                logger.warn("logo config error : many logo association for company "
                        + c.getName());
            }
        }
        return getDefaultLogo();
    }

    private NodeRef getDefaultLogo() {
        if (defaultLogo == null) {
            ResultSet rs = null;
            try {
                rs = searchService
                        .query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                SearchService.LANGUAGE_XPATH,
                                XPATH_DEFAULTCOMPANY_LOGO);
                if (rs.getNumberFound() == 1) {
                    defaultLogo = rs.getNodeRef(0);
                } else {
                    logger.warn("No company logo or koya default logo found : please check repository defaults settings");

                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
        return defaultLogo;
    }

    /**
     * Replaces current company logo with uploaded one Files have no name to
     * avoid duplicates.filenames are stored in title. Updates company
     * 
     * @throws KoyaServiceException
     */
    public Map<String, String> uploadNewLogo(NodeRef companyPropertiesNode,
            String fileName,
            org.springframework.extensions.surf.util.Content content)
            throws KoyaServiceException {

        // TODO check duplicate content

        /**
         * Create Node
         */
        final NodeRef createdNode;
        try {
            final Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_TITLE, fileName);
            ChildAssociationRef car = nodeService.createNode(
                    companyPropertiesNode, ContentModel.ASSOC_CONTAINS, QName
                            .createQName(
                                    NamespaceService.CONTENT_MODEL_1_0_URI,
                                    fileName),// TODO changer name
                    ContentModel.TYPE_CONTENT, properties);
            createdNode = car.getChildRef();
        } catch (DuplicateChildNodeNameException dcne) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.FILE_UPLOAD_NAME_EXISTS, fileName);
        }

        /**
         * Add Content to created node
         * 
         */
        ContentWriter writer = this.contentService.getWriter(createdNode,
                ContentModel.PROP_CONTENT, true);
        if (content.getMimetype() != null) {
            writer.setMimetype(content.getMimetype());
        } else {
            writer.guessMimetype(fileName);
        }
        if (content.getEncoding() != null) {
            writer.setEncoding(content.getEncoding());
        }
        writer.guessEncoding();
        writer.putContent(content.getInputStream());

        /**
         * Update logo reference
         */

        List<AssociationRef> logos = nodeService.getTargetAssocs(
                companyPropertiesNode, KoyaModel.ASSOC_LOGO_COMPANY);

        if (logos.size() == 0) {
            // create association
            nodeService.createAssociation(companyPropertiesNode, createdNode,
                    KoyaModel.ASSOC_LOGO_COMPANY);
        } else if (logos.size() == 1) {
            // update association
            nodeService.setAssociations(companyPropertiesNode,
                    KoyaModel.ASSOC_LOGO_COMPANY, new ArrayList<NodeRef>() {
                        {
                            add(createdNode);
                        }
                    });
        } else {
            // shouldn't happen
            logger.warn("many logo association found on company properties node "
                    + companyPropertiesNode);
        }

        Map<String, String> retMap = new HashMap<>();
        retMap.put("filename", fileName);
        retMap.put("originalFilename", fileName);
        retMap.put("rename", Boolean.FALSE.toString());
        retMap.put("size", Long.toString(writer.getSize()));
        return retMap;

    }

    public CompanyProperties getProperties(String shortName)
            throws KoyaServiceException {
        return getProperties(koyaNodeService.companyBuilder(shortName));
    }

    /**
     * Get Company properties.
     * 
     * @param companyName
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public CompanyProperties getProperties(Company c)
            throws KoyaServiceException {

        NodeRef companyPropertiesNodeRef = (NodeRef) nodeService.getProperty(
                c.getNodeRef(), KoyaModel.PROP_COMPANYHOME);

        CompanyProperties cp = new CompanyProperties(c.getName(),
                companyPropertiesNodeRef);
        if (companyPropertiesNodeRef == null) {
            this.createCompanyConfigFile(c);
        }

        if (companyPropertiesNodeRef != null) {

            cp.setNodeRef(companyPropertiesNodeRef);
            cp.setTitle(c.getTitle());

            // address, description,legal informations - company properties
            // attributes
            cp.setAddress((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_ADDRESS));
            cp.setAddress2((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_ADDRESS2));
            cp.setZipCode((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_ZIPCODE));
            cp.setCity((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_CITY));

            cp.setDescription((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_DESCRIPTION));
            cp.setLegalInformations((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_LEGALINFOS));

            cp.setMailHeaderText((String) nodeService.getProperty(
                    companyPropertiesNodeRef, KoyaModel.PROP_MAILHEADERTEXT));

            // if geographic aspect is set, get latitude and longitude
            if (nodeService.hasAspect(companyPropertiesNodeRef,
                    ContentModel.ASPECT_GEOGRAPHIC)) {
                cp.setGeoPos(new GeoPos((Double) nodeService.getProperty(
                        companyPropertiesNodeRef, ContentModel.PROP_LATITUDE),
                        (Double) nodeService.getProperty(
                                companyPropertiesNodeRef,
                                ContentModel.PROP_LONGITUDE)));
            }

            // load contact items
            cp.setContactItems(loadContactItems(companyPropertiesNodeRef));
            // load contacts
            cp.setContacts(loadContacts(companyPropertiesNodeRef));
        }
        return cp;
    }

    private List<ContactItem> loadContactItems(NodeRef parent) {
        List<ContactItem> contactItems = new ArrayList<ContactItem>();
        // TODO cibler recherche par type
        for (ChildAssociationRef car : nodeService.getChildAssocs(parent,
                QNAMEFILTER_CONTACTITEM)) {
            NodeRef currentNodeRef = car.getChildRef();

            try {
                ContactItem companyCi = new ContactItem(currentNodeRef);

                companyCi.setValue((String) nodeService.getProperty(
                        currentNodeRef, KoyaModel.PROP_CONTACTITEM_VALUE));
                companyCi.setType(Integer.valueOf((String) nodeService
                        .getProperty(currentNodeRef,
                                KoyaModel.PROP_CONTACTITEM_TYPE)));
                contactItems.add(companyCi);
            } catch (NumberFormatException | InvalidNodeRefException ex) {
                // silent exception catching : if error occurs no
                // contact created
            }
        }
        return contactItems;
    }

    public List<Contact> loadContacts(NodeRef parent) {
        List<Contact> contacts = new ArrayList<Contact>();

        for (ChildAssociationRef car : nodeService.getChildAssocs(parent,
                QNAMEFILTER_CONTACT)) {

            NodeRef contactNodeRef = car.getChildRef();

            Contact contact = new Contact(contactNodeRef);

            contact.setTitle((String) nodeService.getProperty(contactNodeRef,
                    ContentModel.PROP_TITLE));

            // ========= get associated user informations
            List<AssociationRef> assocRefs = nodeService.getTargetAssocs(
                    contactNodeRef, KoyaModel.ASSOC_CONTACT_USER);

            // there must be exactly one associated user //TODO ensure unicity
            // in the model
            if (assocRefs.size() != 1) {
                logger.error("Error retrieving contact asociated user : skip"
                        + assocRefs.size());
                break;
            }

            contact.setUser(userService.buildUser(assocRefs.get(0)
                    .getTargetRef()));

            contact.setContactItems(loadContactItems(contactNodeRef));
            contacts.add(contact);
        }

        return contacts;
    }

    /**
     * 
     * @param companyName
     * @param compProperties
     * @throws KoyaServiceException
     */
    public void commitProperties(String companyName, CompanyProperties cp)
            throws KoyaServiceException {

        Company c = koyaNodeService.companyBuilder(companyName);

        NodeRef companyPropertiesNodeRef = (NodeRef) nodeService.getProperty(
                c.getNodeRef(), KoyaModel.PROP_COMPANYHOME);

        // Modify company Title
        nodeService.setProperty(c.getNodeRef(),
                ContentModel.PROP_TITLE, cp.getTitle());

        /**
         * Merge commited CompanyProperties argument with
         * companyPropertiesNodeRef
         */

        nodeService.setProperty(companyPropertiesNodeRef,
                KoyaModel.PROP_ADDRESS, cp.getAddress());
        nodeService.setProperty(companyPropertiesNodeRef,
                KoyaModel.PROP_ADDRESS2, cp.getAddress2());
        nodeService.setProperty(companyPropertiesNodeRef,
                KoyaModel.PROP_ZIPCODE, cp.getZipCode());
        nodeService.setProperty(companyPropertiesNodeRef, KoyaModel.PROP_CITY,
                cp.getCity());
        nodeService.setProperty(companyPropertiesNodeRef,
                KoyaModel.PROP_DESCRIPTION, cp.getDescription());
        nodeService.setProperty(companyPropertiesNodeRef,
                KoyaModel.PROP_LEGALINFOS, cp.getLegalInformations());
        nodeService.setProperty(companyPropertiesNodeRef,
                KoyaModel.PROP_MAILHEADERTEXT, cp.getMailHeaderText());

        // merge geopos object
        if (cp.getGeoPos() != null) {
            // TODO geopos remove policy
            if (!nodeService.hasAspect(companyPropertiesNodeRef,
                    ContentModel.ASPECT_GEOGRAPHIC)) {
                nodeService.addAspect(companyPropertiesNodeRef,
                        ContentModel.ASPECT_GEOGRAPHIC, null);
            }
            nodeService.setProperty(companyPropertiesNodeRef,
                    ContentModel.PROP_LATITUDE, cp.getGeoPos().getLatitude());
            nodeService.setProperty(companyPropertiesNodeRef,
                    ContentModel.PROP_LONGITUDE, cp.getGeoPos().getLongitude());
        }

        mergeContactItems(companyPropertiesNodeRef, cp.getContactItems());
        mergeContacts(companyPropertiesNodeRef, cp.getContacts());
    }

    private void mergeContactItems(NodeRef parentNode,
            List<ContactItem> contactItems) {
        // merge contact item
        List<ContactItem> existingContactItems = new ArrayList<ContactItem>();
        for (ChildAssociationRef car : nodeService.getChildAssocs(parentNode,
                QNAMEFILTER_CONTACTITEM)) {
            existingContactItems.add(new ContactItem(car.getChildRef()));
        }
        // cp.getContactItems() --> contacts items to modify or create
        for (final ContactItem ci : contactItems) {
            // update existing node
            if (ci.getNodeRef() != null) {
                existingContactItems.remove(ci);
                // update contactitem
                nodeService.setProperty(ci.getNodeRef(),
                        KoyaModel.PROP_CONTACTITEM_TYPE, ci.getType());
                nodeService.setProperty(ci.getNodeRef(),
                        KoyaModel.PROP_CONTACTITEM_VALUE, ci.getValue());
            } else {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>() {
                    {
                        put(KoyaModel.PROP_CONTACTITEM_TYPE, ci.getType());
                        put(KoyaModel.PROP_CONTACTITEM_VALUE, ci.getValue());
                    }
                };
                // create new node
                nodeService.createNode(parentNode, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(
                                NamespaceService.CONTENT_MODEL_1_0_URI,
                                ci.getValue()), KoyaModel.TYPE_CONTACTITEM,
                        props);

            }

        }
        // delete removed contact items (ie exists on disk but not in commited
        // object)
        for (ContactItem cDel : existingContactItems) {
            nodeService.deleteNode(cDel.getNodeRef());
        }

    }

    private void mergeContacts(NodeRef parentNode, List<Contact> contacts) {
        // merge contact item
        List<Contact> existingContacts = new ArrayList<Contact>();
        for (ChildAssociationRef car : nodeService.getChildAssocs(parentNode,
                QNAMEFILTER_CONTACT)) {
            existingContacts.add(new Contact(car.getChildRef()));
        }

        for (final Contact c : contacts) {

            // associated user
            String personName = (String) nodeService.getProperty(c.getUser()
                    .getNodeRef(), ContentModel.PROP_USERNAME);

            // update existing node
            if (c.getNodeRef() != null) {
                existingContacts.remove(c);

                List<AssociationRef> assocRefs = nodeService.getTargetAssocs(
                        c.getNodeRef(), KoyaModel.ASSOC_CONTACT_USER);

                if (assocRefs.size() == 0) {
                    // create association
                    nodeService
                            .createAssociation(c.getNodeRef(), c.getUser()
                                    .getNodeRef(),
                                    KoyaModel.ASSOC_CONTACT_USER);
                } else if (assocRefs.size() == 1) {
                    // update association
                    nodeService.setAssociations(c.getNodeRef(),
                            KoyaModel.ASSOC_CONTACT_USER,
                            new ArrayList<NodeRef>() {
                                {
                                    add(c.getUser().getNodeRef());
                                }
                            });
                } else {
                    // shouldn't happen
                    logger.warn("Error retrieving contact asociated user : skip "
                            + c.getNodeRef());
                    break;
                }              
                // update contact items values
                mergeContactItems(c.getNodeRef(), c.getContactItems());

            } else {

                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_TITLE, (String) nodeService
                        .getProperty(c.getUser().getNodeRef(),
                                ContentModel.PROP_EMAIL));

                // create new contact
                NodeRef contactCreated = nodeService.createNode(
                        parentNode,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(
                                NamespaceService.CONTENT_MODEL_1_0_URI,
                                personName), KoyaModel.TYPE_CONTACT, props)
                        .getChildRef();
                nodeService.createAssociation(contactCreated, c.getUser()
                        .getNodeRef(), KoyaModel.ASSOC_CONTACT_USER);
                // update contact items values
                mergeContactItems(contactCreated, c.getContactItems());
            }
        }
        
        // delete removed contacts (ie exists on disk but not in commited
        // object)
        for (Contact cDel : existingContacts) {
            nodeService.deleteNode(cDel.getNodeRef());
        }

    }

    public NodeRef createCompanyConfigFile(Company c) {
        // TODO Read From company Home variable
        FileInfo fi = fileFolderService.create(
                getKoyaConfigNodeRef(c.getNodeRef(), true),
                COMPANYPROPERTIES_FILE_NAME, KoyaModel.TYPE_COMPANYPROPERTIES);
        return fi.getNodeRef();
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
    protected NodeRef getKoyaConfigNodeRef(final NodeRef rootRef,
            final boolean create) {
        NodeRef koyaConfigRef = this.unprotNodeService.getChildByName(rootRef,
                ContentModel.ASSOC_CONTAINS, KOYA_CONFIG);
        if (create && koyaConfigRef == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("'koya-config' folder not found under path, creating...");
            }
            QName assocQName = QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI, KOYA_CONFIG);
            Map<QName, Serializable> properties = new HashMap<>(1, 1.0f);
            properties.put(ContentModel.PROP_NAME, (Serializable) KOYA_CONFIG);
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
