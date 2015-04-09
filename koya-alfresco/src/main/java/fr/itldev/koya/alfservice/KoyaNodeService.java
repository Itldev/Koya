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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.nodelocator.AncestorNodeLocator;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.utils.SecuredItemBuilder;

/**
 * Koya Nodes Util Service.
 */
public class KoyaNodeService {

    private static final String FAVOURITES_PREF_FOLDERS = "org.alfresco.share.folders.favourites";
    private static final String FAVOURITES_PREF_DOCS = "org.alfresco.share.documents.favourites";
    private static final String FAVOURITES_PREF_COMPANIES = "org.alfresco.share.sites.favourites";
    public static final String DOCLIB_NAME = "documentLibrary";
    private Logger logger = Logger.getLogger(KoyaNodeService.class);
    private NodeService nodeService;
    private NodeService unsecuredNodeService;
    private FavouritesService favouritesService;
    private PreferenceService preferenceService;
    private FileFolderService fileFolderService;
    private AuthenticationService authenticationService;
    private ActivityService activityService;
    private SiteService siteService;// TODO remove this dependancy
    private AncestorNodeLocator ancestorNodeLocator;
    protected SearchService searchService;
    protected NamespaceService namespaceService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setUnsecuredNodeService(NodeService unsecuredNodeService) {
        this.unsecuredNodeService = unsecuredNodeService;
    }

    public void setFavouritesService(FavouritesService favouritesService) {
        this.favouritesService = favouritesService;
    }

    public void setPreferenceService(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setAuthenticationService(
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAncestorNodeLocator(AncestorNodeLocator ancestorNodeLocator) {
        this.ancestorNodeLocator = ancestorNodeLocator;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    // </editor-fold>
    private SecuredItemBuilder builder;

    public void init() {
        builder = new SecuredItemBuilder(nodeService, this);
    }

    /**
     * ====== Favourites Handling methods ======.
     *
     */
    /**
     *
     * @return
     */
    public List<SecuredItem> getFavourites() {

        List<SecuredItem> favourites = new ArrayList<>();

        Map<String, Serializable> prefs = preferenceService
                .getPreferences(authenticationService.getCurrentUserName());

        // favourites folders
        String foldersFavourites = (String) prefs.get(FAVOURITES_PREF_FOLDERS);

        if (foldersFavourites != null && !foldersFavourites.isEmpty()) {
            for (String favStr : foldersFavourites.split(",")) {
                try {
                    NodeRef n = getNodeRef(favStr);
                    favourites.add(getSecuredItem(n));
                } catch (KoyaServiceException | NullPointerException e) {
                    logger.error("Ignored Favourite nodeRef (FOLDERS) "
                            + favStr + " : " + e.getMessage());
                }
            }
        }
        // favourites documents
        String docsFavourites = (String) prefs.get(FAVOURITES_PREF_DOCS);
        if (docsFavourites != null && !docsFavourites.isEmpty()) {
            for (String favStr : docsFavourites.split(",")) {
                try {
                    NodeRef n = getNodeRef(favStr);
                    favourites.add(getSecuredItem(n));
                } catch (KoyaServiceException | NullPointerException e) {
                    logger.error("Ignored Favourite nodeRef (DOCS) " + favStr
                            + " : " + e.getMessage());
                }
            }
        }

        // favourites companies
        for (String k : prefs.keySet()) {
            if (k.startsWith(FAVOURITES_PREF_COMPANIES)
                    && ((Boolean) prefs.get(k)).equals(Boolean.TRUE)) {

                String compName = k.substring(FAVOURITES_PREF_COMPANIES
                        .length() + 1);
                try {
                    favourites.add(companyBuilder(compName));
                } catch (KoyaServiceException | NullPointerException e) {
                    logger.error("Ignored Favourite nodeRef (COMPANY) " + k
                            + " : " + e.getMessage());
                }

            }
        }

        // TODO do it with favourites service ?
        // PagingResults<PersonFavourite> favsPaged =
        // favouritesService.getPagedFavourites(userName, null, null, null);
        return favourites;
    }

    /**
     * Change users favourite status for a node.
     *
     * @param item
     * @param status
     */
    public void setFavouriteStatus(NodeRef item, Boolean status) {
        if (status) {
            favouritesService.addFavourite(
                    authenticationService.getCurrentUserName(), item);
        } else {
            favouritesService.removeFavourite(
                    authenticationService.getCurrentUserName(), item);
        }
    }

    /**
     * Checks if node n is a favourite for current user
     *
     * @param n
     * @return
     */
    public Boolean isFavourite(NodeRef n) {
        return favouritesService.isFavourite(
                authenticationService.getCurrentUserName(), n);
    }

    /**
     *
     * ===== byte size methods =====.
     *
     */
    /**
     * Byte size getting method.
     *
     * returns Element size for Object.
     *
     * return recursive size for Containers Objects.
     *
     * @param n
     * @return
     */
    public Long getByteSize(NodeRef n) {

        try {
            FileInfo fi = fileFolderService.getFileInfo(n);

            if (!fi.isFolder()) {
                return fi.getContentData().getSize();
            } else {// return recursive size
                long size = 0;
                for (ChildAssociationRef car : nodeService.getChildAssocs(n)) {
                    size += getByteSize(car.getChildRef());
                }
                return size;
            }
        } catch (InvalidNodeRefException | NullPointerException e) {
            return (long) 0;
        }
    }

    /**
     *
     * ==== Objects Builder Methods =====.
     *
     */
    /**
     * NodeRef builder : should be used instead of NodeRef instanciation.
     *
     * @param strNodeRef
     * @return
     * @throws KoyaServiceException
     */
    public NodeRef getNodeRef(String strNodeRef) throws KoyaServiceException {
        try {
            return new NodeRef(strNodeRef);
        } catch (InvalidNodeRefException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODEREF);
        }
    }

    /**
     *
     * Get typed SecuredItem from Noderef
     *
     * @param nodeRef
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem getSecuredItem(NodeRef nodeRef)
            throws KoyaServiceException {

        SecuredItem si;
        if (nodeService.exists(nodeRef)) {
            si = builder.build(nodeRef);
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODEREF,
                    nodeRef.toString());
        }
        return si;
    }

    /**
     * Get typed Secured Item.
     *
     * @param <T>
     * @param nodeRef
     * @param type
     * @return
     * @throws KoyaServiceException
     */
    @SuppressWarnings("unchecked")
    public <T> T getSecuredItem(NodeRef nodeRef, Class<T> type)
            throws KoyaServiceException {
        SecuredItem s = getSecuredItem(nodeRef);
        assert type.isAssignableFrom(s.getClass());
        return (T) s;
    }

    /**
     * Used for compatibility
     *
     * @param companyName
     * @return
     * @throws KoyaServiceException
     * @deprecated
     */
    @Deprecated
    public Company companyBuilder(String companyName)
            throws KoyaServiceException {
        return getSecuredItem(siteService.getSite(companyName).getNodeRef(),
                Company.class);
    }

    /**
     *
     * ==== Global secured item methods =====.
     *
     */
    /**
     *
     * @param n
     * @param newTitle
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem rename(NodeRef n, String newTitle)
            throws KoyaServiceException {
        // todo check new name validity

        try {
            String name = getUniqueValidFileNameFromTitle(newTitle);

            nodeService.setProperty(n, ContentModel.PROP_NAME, name);
            nodeService.setProperty(n, ContentModel.PROP_TITLE, newTitle);

        } catch (DuplicateChildNodeNameException dex) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.DUPLICATE_CHILD_RENAME, dex);
        }
        return getSecuredItem(n);
    }

    /**
     *
     * @param n
     * @throws KoyaServiceException
     */
    public void delete(NodeRef n) throws KoyaServiceException {
        NodeRef parentNodeRef = nodeService.getPrimaryParent(n).getParentRef();
        QName nodeRefType = nodeService.getType(n);
        String siteId = siteService.getSiteShortName(n);
        String title = (String) nodeService.getProperty(n,
                ContentModel.PROP_NAME);

        /**
         * Delete from favourites
         *
         * TODO asynchronous process
         */
        if (isFavourite(n)) {
            setFavouriteStatus(n, Boolean.FALSE);
        }

        nodeService.deleteNode(n);
        if (nodeRefType.equals(ContentModel.TYPE_CONTENT)) {
            activityService.postActivity(ActivityType.FILE_DELETED, siteId,
                    "koya", n, title, nodeRefType, parentNodeRef);
        }
    }

    /**
     * @param toMove
     * @param dest
     * @return
     * @throws KoyaServiceException
     */
    public SecuredItem move(NodeRef toMove, NodeRef dest)
            throws KoyaServiceException {
        FileInfo fInfo;
        try {
            fInfo = fileFolderService.move(toMove, dest, (String) nodeService
                    .getProperty(toMove, ContentModel.PROP_NAME));
        } catch (FileExistsException fex) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
        } catch (FileNotFoundException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.MOVE_SOURCE_NOT_FOUND);
        } catch (CyclicChildRelationshipException ccre) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.MOVE_CYCLIC_RELATIONSHIP_DETECTED);
        }
        // TODO update KoyaNodes cache
        return getSecuredItem(fInfo.getNodeRef());
    }

    /**
     *
     * @param toCopy
     * @param dest
     * @return
     * @throws KoyaServiceException
     */
    public SecuredItem copy(NodeRef toCopy, NodeRef dest)
            throws KoyaServiceException {
        FileInfo fInfo;
        try {
            fInfo = fileFolderService.copy(toCopy, dest, (String) nodeService
                    .getProperty(toCopy, ContentModel.PROP_NAME));
        } catch (FileExistsException fex) {
            /**
             * change errors
             */
            throw new KoyaServiceException(
                    KoyaErrorCodes.MOVE_DESTINATION_NAME_ALREADY_EXISTS);
        } catch (FileNotFoundException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.MOVE_SOURCE_NOT_FOUND);
        }
        return getSecuredItem(fInfo.getNodeRef());
    }

    /**
     *
     * ===== Type checking methods ======
     *
     */
    /**
     *
     * @param n
     * @param type
     * @return
     */
    public Boolean isKoyaType(NodeRef n, Class<? extends SecuredItem> type) {
        try {
            return type.isAssignableFrom(getSecuredItem(n).getClass());
        } catch (KoyaServiceException kex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends SecuredItem> T getFirstParentOfType(NodeRef n,
            Class<? extends SecuredItem> type) throws KoyaServiceException {
        if (n == null) {
            return null;
        }
        QName qName = KoyaModel.CLASS_TO_QNAME.get(type);

        if (qName == null) {
            /**
             * TODO throw KoyaError
             */
            logger.error("Unsupported type class (" + type.getSimpleName()
                    + ") for Qname conversion");
        }

        Map<String, Serializable> params = new HashMap<>(1);
        params.put(AncestorNodeLocator.TYPE_KEY,
                KoyaModel.TYPES_SHORT_PREFIX.get(qName));

        NodeRef nTyped = ancestorNodeLocator.getNode(n, params);

        if (nTyped != null) {
            return (T) getSecuredItem(nTyped, type);
        }
        return null;
    }

    /**
     * Returns node parent if exists.
     *
     * unsecured method
     *
     * @param currentNode
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem getParent(NodeRef currentNode)
            throws KoyaServiceException {

        if (isKoyaType(currentNode, Company.class)) {
            return null;// can't get company parent
        }

        NodeRef parentNr = unsecuredNodeService.getPrimaryParent(currentNode)
                .getParentRef();

        try {
            return getSecuredItem(parentNr);
        } catch (KoyaServiceException e) {

            if (unsecuredNodeService.getProperty(parentNr,
                    ContentModel.PROP_NAME).equals(DOCLIB_NAME)) {
                try {
                    return getSecuredItem(unsecuredNodeService
                            .getPrimaryParent(parentNr).getParentRef());
                } catch (KoyaServiceException e2) {
                    throw new KoyaServiceException(
                            KoyaErrorCodes.INVALID_NODE_HIERACHY);
                }
            } else {
                throw new KoyaServiceException(
                        KoyaErrorCodes.INVALID_NODE_HIERACHY);
            }
        }
    }

    public static final Integer NB_ANCESTOR_INFINTE = -1;

    /**
     * Return parent nodes list with maximum nbAncestor elements.
     *
     * if NbAncestor
     *
     * @param n
     * @param nbAncestor
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<SecuredItem> getParentsList(NodeRef n, Integer nbAncestor)
            throws KoyaServiceException {
        List<SecuredItem> parents = new ArrayList<>();
        SecuredItem parent = getParent(n);

        if (parent == null) {
            /**
             * End condition
             */
            return new ArrayList<>();
        } else {
            parents.add(parent);
            if (nbAncestor > 1 || nbAncestor <= NB_ANCESTOR_INFINTE) {
                parents.addAll(getParentsList(parent.getNodeRefasObject(),
                        --nbAncestor));
            }
        }

        return parents;
    }

    /**
     * count direct children of given type
     *
     *
     * @param parent
     * @param qNameFilter
     * @return
     * @throws KoyaServiceException
     */
    public Integer countChildren(NodeRef parent, Set<QName> qNameFilter)
            throws KoyaServiceException {

        if (qNameFilter != null && !qNameFilter.isEmpty()) {
            return nodeService.getChildAssocs(parent, qNameFilter).size();
        } else {
            return nodeService.getChildAssocs(parent).size();
        }
    }

    /**
     *
     * @param parent
     * @param skipCount
     * @param maxItems
     * @param onlyFolders
     * @return
     * @throws KoyaServiceException
     */
    public List<SecuredItem> listChildrenPaginated(NodeRef parent,
            final Integer skipCount, final Integer maxItems, final Integer depth,
            final boolean onlyFolders)
            throws KoyaServiceException {

        Integer skip = skipCount;
        Integer max = maxItems;
        if (skipCount == null) {
            skip = Integer.valueOf(0);
        }
        if (maxItems == null) {
            max = Integer.MAX_VALUE;
        }
        List<Pair<QName, Boolean>> sortProps = new ArrayList() {
            {
                add(new Pair<>(
                        GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false));
                add(new Pair<>(ContentModel.PROP_TITLE, true));
            }
        };

        PagingResults<FileInfo> results = fileFolderService.list(parent,
                !onlyFolders, true, null, sortProps,
                new PagingRequest(skip, max)
        );

        List children = results.getPage();

        /**
         * Transform List<FileInfo> as List<SecuredItem>
         */
        CollectionUtils.transform(children, new Transformer() {
            @Override
            public Object transform(Object input) {
                try {
                    FileInfo fi = (FileInfo) input;
                    SecuredItem si = getSecuredItem(fi.getNodeRef());
                    try {
                        if (depth != null && depth > 0) {
                            Directory dir = (Directory) si;
                            List children = listChildrenPaginated(fi.getNodeRef(), skipCount, maxItems, depth - 1, onlyFolders);

                            dir.setChildren(children);
                        }
                    } catch (ClassCastException cce) {
                        //Faster than instanceOf
                    }
                    return si;
                } catch (KoyaServiceException ex) {
                    return null;
                }
            }
        });
        return children;
    }

    public Properties readPropertiesFileContent(NodeRef fileNr) {
        Properties props = new Properties();
        if (fileNr != null) {
            ContentReader contentReader = fileFolderService.getReader(fileNr);
            try {
                props.load(contentReader.getContentInputStream());
            } catch (IOException | ContentIOException ex) {
                // silent exception catching
            }
        }
        return props;
    }

    public NodeRef xPath2NodeRef(String xpath) throws KoyaServiceException {

        RepositoryLocation templateLoc = new RepositoryLocation();// defaultQuery
        // language
        // = xpath
        templateLoc.setPath(xpath);
        StoreRef store = templateLoc.getStoreRef();

        try {
            List<NodeRef> nodeRefs = searchService.selectNodes(
                    nodeService.getRootNode(store), xpath, null,
                    namespaceService, false);
            if (nodeRefs.size() != 1) {
                throw new KoyaServiceException(
                        KoyaErrorCodes.INVALID_XPATH_NODE, nodeRefs.size()
                        + " nodes match search");
            }
            return nodeRefs.get(0);
        } catch (SearcherException e) {
            throw new KoyaServiceException(
                    KoyaErrorCodes.CANNOT_FIND_XPATH_NODE, e);
        }
    }

    /**
     * Name encoding method from title String.
     *
     * @param title
     * @return
     */
    public String getUniqueValidFileNameFromTitle(String title) {

        title = title.replaceAll("([\\\"\\\\*\\\\\\>\\<\\?\\/\\:\\|]+)", "");
        String oldTitle;
        do {
            oldTitle = title;
            title = title.replaceAll("([\\.]+$)|([ ]+$)", "");
        } while (!oldTitle.equals(title));

        return title;
    }
}
