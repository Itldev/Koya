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
import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.nodelocator.AncestorNodeLocator;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

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
    private DictionaryService dictionaryService;
    private CompanyService companyService;
    private FileFolderService fileFolderService;
    private AuthenticationService authenticationService;
    private KoyaShareService koyaShareService;
    private KoyaAclService koyaAclService;
    private AncestorNodeLocator ancestorNodeLocator;
    
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

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setKoyaShareService(KoyaShareService koyaShareService) {
        this.koyaShareService = koyaShareService;
    }

    public void setKoyaAclService(KoyaAclService koyaAclService) {
        this.koyaAclService = koyaAclService;
    }

    // </editor-fold>
    /**
     * ===== Activity Handling methods ==========.
     *
     */
    /**
     *
     * @param n
     * @param activeValue
     */
    public void setActiveStatus(NodeRef n, Boolean activeValue) {

        //TODO limit actions to activable nodes (check model)
        if (nodeService.hasAspect(n, KoyaModel.QNAME_KOYA_ACTIVABLE)) {
            //if node exists with activable aspect, update value.
            nodeService.setProperty(n, KoyaModel.QNAME_PROPASPECT_KOYA_ISACTIVE, activeValue);
        } else {
            //add aspect with value
            Map<QName, Serializable> props = new HashMap<>();
            props.put(KoyaModel.QNAME_PROPASPECT_KOYA_ISACTIVE, activeValue);
            nodeService.addAspect(n, KoyaModel.QNAME_KOYA_ACTIVABLE, props);
        }
    }

    /**
     *
     * Active Node has aspect KoyaModel.QNAME_KOYA_ACTIVABLE AND active property
     * is true.
     *
     * @param n
     * @return
     */
    public Boolean isActive(NodeRef n) {
        return unsecuredNodeService.hasAspect(n, KoyaModel.QNAME_KOYA_ACTIVABLE)
                && (Boolean) unsecuredNodeService.getProperty(n, KoyaModel.QNAME_PROPASPECT_KOYA_ISACTIVE);
    }

    /**
     * ====== Favourites Handling methods ======.
     *
     * TODO add a ehcache cache
     */
    /**
     *
     * @return @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<SecuredItem> getFavourites() throws KoyaServiceException {

        List<SecuredItem> favourites = new ArrayList<>();

        Map<String, Serializable> prefs = preferenceService.getPreferences(authenticationService.getCurrentUserName());

        //favourites folders
        String foldersFavourites = (String) prefs.get(FAVOURITES_PREF_FOLDERS);

        if (foldersFavourites != null && !foldersFavourites.isEmpty()) {
            for (String favStr : foldersFavourites.split(",")) {
                try {
                    NodeRef n = new NodeRef(favStr);
                    //Build object according to type                
                    QName typeFav = nodeService.getType(n);

                    if (typeFav.equals(KoyaModel.QNAME_KOYA_SPACE)) {
                        favourites.add(nodeSpaceBuilder(n));
                    } else if (typeFav.equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
                        favourites.add(nodeDossierBuilder(n));
                    } else {
                        favourites.add(nodeDirBuilder(n));
                    }

                } catch (InvalidNodeRefException e) {
                }
            }
        }
        //favourites documents
        String docsFavourites = (String) prefs.get(FAVOURITES_PREF_DOCS);
        if (docsFavourites != null && !docsFavourites.isEmpty()) {
            for (String favStr : docsFavourites.split(",")) {
                try {
                    NodeRef n = new NodeRef(favStr);
                    favourites.add(nodeDocumentBuilder(n));
                } catch (InvalidNodeRefException e) {
                }
            }
        }

        //favourites companies
        for (String k : prefs.keySet()) {
            if (k.startsWith(FAVOURITES_PREF_COMPANIES) && ((Boolean) prefs.get(k)).equals(Boolean.TRUE)) {

                String compName = k.substring(FAVOURITES_PREF_COMPANIES.length() + 1);
                try {
                    SiteInfo s = companyService.getSiteInfo(compName);
                    favourites.add(siteCompanyBuilder(s));
                } catch (Exception e) {//In case of non existant company
                }

            }
        }

        //TODO do it with favourites service ?
        //  PagingResults<PersonFavourite> favsPaged = favouritesService.getPagedFavourites(userName, null, null, null);
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
            favouritesService.addFavourite(authenticationService.getCurrentUserName(), item);
        } else {
            favouritesService.removeFavourite(authenticationService.getCurrentUserName(), item);
        }
    }

    /**
     * Checks if node n is a favourite for current user
     *
     * @param n
     * @return
     */
    public Boolean isFavourite(NodeRef n) {
        return favouritesService.isFavourite(authenticationService.getCurrentUserName(), n);
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
     * return recussive size for Containers Objects.
     *
     * @param n
     * @return
     */
    public Long getByteSize(NodeRef n) {

        try {
            FileInfo fi = fileFolderService.getFileInfo(n);

            if (!fi.isFolder()) {
                return fi.getContentData().getSize();
            } else {//return recusive size
                long size = 0;
                for (ChildAssociationRef car : nodeService.getChildAssocs(n)) {
                    size += getByteSize(car.getChildRef());
                }
                return size;
            }
        } catch (InvalidNodeRefException e) {
            return (long) 0;
        }
    }

    /**
     *
     * ==== Objects Builder Methods =====.
     *
     */
    /**
     *
     * Get typed SecuredItem from Noderef String.
     *
     * @param strNodeRef
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem nodeRef2SecuredItem(String strNodeRef) throws KoyaServiceException {
        NodeRef nr = null;
        try {
            nr = new NodeRef(strNodeRef);
        } catch (InvalidNodeRefException ex) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODEREF);
        }
        return nodeRef2SecuredItem(nr);
    }

    /**
     *
     * Get typed SecuredItem from Noderef
     *
     * @param nodeRef
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public SecuredItem nodeRef2SecuredItem(NodeRef nodeRef) throws KoyaServiceException {
        SecuredItem si = null;
        QName type = nodeService.getType(nodeRef);
        if (type.equals(KoyaModel.QNAME_KOYA_COMPANY)) {
            si = nodeCompanyBuilder(nodeRef);
        } else if (type.equals(KoyaModel.QNAME_KOYA_SPACE)) {
            si = nodeSpaceBuilder(nodeRef);
        } else if (type.equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
            si = nodeDossierBuilder(nodeRef);
        } else if (type.equals(ContentModel.TYPE_FOLDER) && nodeIsChildOfDossier(nodeRef)) {
            si = nodeDirBuilder(nodeRef);
        } else if (type.equals(ContentModel.TYPE_CONTENT) && nodeIsChildOfDossier(nodeRef)) {
            si = nodeDocumentBuilder(nodeRef);
        } else {
            logger.warn("Invalid noderef type (" + type + ") given for sharing  : ignored");
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_SECUREDITEM_NODEREF);
        }
        return si;
    }

    /**
     *
     * @param s
     * @return
     */
    public Company siteCompanyBuilder(SiteInfo s) {
        Company c = new Company(s);
        c.setUserFavourite(isFavourite(c.getNodeRefasObject()));
        c.setShared(koyaShareService.listUsersAccessShare(c).size() > 0);
        c.setPermissions(koyaAclService.getPermissions(c.getNodeRefasObject()));
        return c;
    }

    /**
     *
     * @param n
     * @return
     */
    public Company nodeCompanyBuilder(NodeRef n) {
        return siteCompanyBuilder(companyService.getSiteInfo(n));
    }

    /**
     *
     * @param spaceNodeRef
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Space nodeSpaceBuilder(final NodeRef spaceNodeRef) throws KoyaServiceException {
        Space e = new Space();

        /**
         * General attributes
         */
        e.setNodeRef(spaceNodeRef.toString());
        e.setName((String) unsecuredNodeService.getProperty(spaceNodeRef, ContentModel.PROP_NAME));
        e.setActive(isActive(spaceNodeRef));
        e.setShared(AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                return koyaShareService.listUsersAccessShare(spaceNodeRef).size() > 0;
            }
        }));

        /**
         * User context attributes
         */
        e.setUserFavourite(isFavourite(spaceNodeRef));
        e.setPermissions(koyaAclService.getPermissions(spaceNodeRef));

        return e;
    }

    /**
     *
     * @param dossierNodeRef
     * @return
     */
    public Dossier nodeDossierBuilder(final NodeRef dossierNodeRef) {
        Dossier c = new Dossier();

        /**
         * General attributes
         */
        c.setNodeRef(dossierNodeRef.toString());
        c.setName((String) unsecuredNodeService.getProperty(dossierNodeRef, ContentModel.PROP_NAME));
        c.setActive(isActive(dossierNodeRef));
        c.setLastModifiedDate((Date) unsecuredNodeService.getProperty(dossierNodeRef, ContentModel.PROP_MODIFIED));
        c.setShared(AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                return koyaShareService.listUsersAccessShare(dossierNodeRef).size() > 0;
            }
        }));

        /**
         * User context attributes
         */
        c.setUserFavourite(isFavourite(dossierNodeRef));
        c.setPermissions(koyaAclService.getPermissions(dossierNodeRef));

        return c;
    }

    /**
     * Builds Content according to the type.
     *
     * @param nodeRef
     * @return
     */
    public Content nodeContentBuilder(NodeRef nodeRef) {
        if (nodeIsFolder(nodeRef)) {
            return nodeDirBuilder(nodeRef);
        } else {
            return nodeDocumentBuilder(nodeRef);
        }
    }

    /**
     * Directory building method
     * 
     * unsecured method
     *
     * @param dirNodeRef
     * @return
     */
    public Directory nodeDirBuilder(NodeRef dirNodeRef) {
        Directory r = new Directory();
        r.setNodeRef(dirNodeRef.toString());
        r.setName((String) unsecuredNodeService.getProperty(dirNodeRef, ContentModel.PROP_NAME));

        r.setUserFavourite(isFavourite(dirNodeRef));

        //not used
        r.setShared(Boolean.FALSE);

        r.setPermissions(koyaAclService.getPermissions(dirNodeRef));

        return r;
    }

    /**
     * Document building method
     * 
     * 
     * unsecured method
     * 
     *
     * @param docNodeRef
     * @return
     */
    public Document nodeDocumentBuilder(final NodeRef docNodeRef) {
        Document d = new Document();
        d.setNodeRef(docNodeRef.toString());
        d.setName((String) unsecuredNodeService.getProperty(docNodeRef, ContentModel.PROP_NAME));
        d.setUserFavourite(isFavourite(docNodeRef));
        d.setByteSize(AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< Long>() {
            @Override
            public Long doWork() throws Exception {
                return getByteSize(docNodeRef);
            }
        }));

        ContentData contentData = (ContentData) unsecuredNodeService.getProperty(docNodeRef, ContentModel.PROP_CONTENT);
        d.setMimeType(contentData.getMimetype());
        //not used
        d.setShared(Boolean.FALSE);

        d.setPermissions(koyaAclService.getPermissions(docNodeRef));

        return d;
    }

    /**
     *
     * ===== Type checking methods ======
     *
     */
    /**
     * Return true if node is type ContentModel.TYPE_FOLDER or subtype.
     *
     * unsecured method
     *
     *
     * @param nodeRef
     * @return
     */
    public Boolean nodeIsFolder(NodeRef nodeRef) {
        QName qNameType = unsecuredNodeService.getType(nodeRef);
        return qNameType.equals(ContentModel.TYPE_FOLDER)
                || (dictionaryService.isSubClass(qNameType, ContentModel.TYPE_FOLDER));
    }

    /**
     * Koya company is a site with activable aspect.
     *
     * unsecured method
     *
     *
     * @param n
     * @return
     */
    public Boolean isKoyaCompany(NodeRef n) {
        return unsecuredNodeService.getType(n).equals(KoyaModel.QNAME_KOYA_COMPANY)
                && unsecuredNodeService.hasAspect(n, KoyaModel.QNAME_KOYA_ACTIVABLE);
    }

    /**
     *
     * ==== Global secured item methods =====.
     *
     */
    /**
     *
     * @param n
     * @param newName
     * @return
     */
    public SecuredItem rename(NodeRef n, String newName) {
        //todo check new name validity
        nodeService.setProperty(n, ContentModel.PROP_NAME, newName);
        return null;

    }

    /**
     *
     * @param n
     * @throws KoyaServiceException
     */
    public void delete(NodeRef n) throws KoyaServiceException {
        nodeService.deleteNode(n);

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
    public SecuredItem getParent(NodeRef currentNode) throws KoyaServiceException {
        NodeRef parentNr = unsecuredNodeService.getPrimaryParent(currentNode).getParentRef();
        if (isKoyaCompany(parentNr)
                || (unsecuredNodeService.getProperty(parentNr, ContentModel.PROP_NAME).equals(DOCLIB_NAME)
                && isKoyaCompany(unsecuredNodeService.getPrimaryParent(parentNr).getParentRef()))) {
            //If parent is a company or doclib node (which primary parent is a company)
            return nodeCompanyBuilder(parentNr);
        } else if (unsecuredNodeService.getType(parentNr).equals(KoyaModel.QNAME_KOYA_SPACE)) {
            return nodeSpaceBuilder(parentNr);
        } else if (unsecuredNodeService.getType(parentNr).equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
            return nodeDossierBuilder(parentNr);
        } else if (nodeIsChildOfDossier(parentNr)) {
            return nodeContentBuilder(parentNr);
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODE_HIERACHY);
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
    public List<SecuredItem> getParentsList(NodeRef n, Integer nbAncestor) throws KoyaServiceException {
        List<SecuredItem> parents = new ArrayList<>();
        SecuredItem parent = getParent(n);

        if (parent == null) {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODE_HIERACHY);
        } else if (parent.getClass().isAssignableFrom(Company.class)) {
            parents.add(parent);
            return parents;
        } else {
            parents.add(parent);

            if (Objects.equals(nbAncestor, NB_ANCESTOR_INFINTE)) {
                parents.addAll(getParentsList(parent.getNodeRefasObject(), NB_ANCESTOR_INFINTE));
            } else if (nbAncestor > 1) {
                nbAncestor--;
                parents.addAll(getParentsList(parent.getNodeRefasObject(), nbAncestor));
            }
        }

        return parents;
    }

    /**
     * get the company this nodeRef belongs to
     * 
     * @param nodeRef
     * @return 
     */
    public SecuredItem getCompany(NodeRef nodeRef) throws KoyaServiceException {
        Map params = new HashMap(1);
        params.put(AncestorNodeLocator.TYPE_KEY, KoyaModel.QNAME_KOYA_COMPANY);
        
        NodeRef companyNR = ancestorNodeLocator.getNode(nodeRef,params);
        
        if(companyNR != null) {
            return nodeCompanyBuilder(companyNR);
        } else {
            throw new KoyaServiceException(KoyaErrorCodes.INVALID_NODEREF, "nodeRef not whithin a Company");
        }
        
        
    }
    /**
     * return true if node given in argument has a Dossier in his ancestors.
     *
     * unsecured method
     * 
     * @param nodeRef
     * @return
     */
    private Boolean nodeIsChildOfDossier(NodeRef nodeRef) {

        try {
            NodeRef parent = unsecuredNodeService.getPrimaryParent(nodeRef).getParentRef();

            if (unsecuredNodeService.getType(parent).equals(KoyaModel.QNAME_KOYA_DOSSIER)) {
                return true;
            } else {
                return nodeIsChildOfDossier(parent);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     *
     */
    /**
     * Returns company whose node belongs to. Null is node is not a comapny
     * child
     * 
     * unsecured method
     *
     *
     * TODO modify with nodeLocator implementation
     *
     * @param n
     * @return
     */
    public Company getNodeCompany(NodeRef n) {
        if (n == null) {
            return null;
        } else if (unsecuredNodeService.getType(n).equals(KoyaModel.QNAME_KOYA_COMPANY)) {
            return nodeCompanyBuilder(n);
        } else {
            return getNodeCompany(unsecuredNodeService.getPrimaryParent(n).getParentRef());
        }
    }

}
