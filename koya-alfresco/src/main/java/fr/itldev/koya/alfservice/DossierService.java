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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;

/**
 * Dossiers Handling Service
 *
 *
 */
public class DossierService {

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    private KoyaNodeService koyaNodeService;
    protected SearchService searchService;
    private NamespacePrefixResolver prefixResolver;
    private TransactionService transactionService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }
    
    public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	// </editor-fold>
    /**
     *
     * @param title
     * @param parent
     * @param prop
     * @return
     * @throws KoyaServiceException
     */
    public Dossier create(String title, NodeRef parent, Map<QName, String> prop) throws KoyaServiceException {

        //Dossier must have a title
        if (title == null || title.isEmpty()) {
            throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_EMPTY_NAME);
        }

        //parent must be a Space
        if (!nodeService.getType(parent).equals(KoyaModel.TYPE_SPACE)) {
            throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_NOT_IN_SPACE);
        }

        String name = koyaNodeService.getUniqueValidFileNameFromTitle(title);

        if (nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name) != null) {
            throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_NAME_EXISTS);
        }

        //build node properties
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, title);
        if (prop != null) {
            properties.putAll(prop);
        }

        ChildAssociationRef car = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                KoyaModel.TYPE_DOSSIER,
                properties);

        Dossier created = koyaNodeService.getKoyaNode(car.getChildRef(), Dossier.class);
        logger.info("[Koya] Dossier creation : " + created.getTitle() + " created (" + created.getNodeRef() + ")");
        return created;
    }

    /**
     *
     * @param parent
     * @return
     * @throws KoyaServiceException
     */
    public List<Dossier> list(NodeRef parent) throws KoyaServiceException {

        if (!nodeService.getType(parent).equals(KoyaModel.TYPE_SPACE)) {
            throw new KoyaServiceException(KoyaErrorCodes.DOSSIER_INVALID_PARENT_NODE);
        }

        List nodes = nodeService.getChildAssocs(parent, new HashSet<QName>() {
            {
                add(KoyaModel.TYPE_DOSSIER);
            }
        });

        /**
         * transform List<ChildAssociationRef> to List<Dossier>
         */
        CollectionUtils.transform(nodes, new Transformer() {
            @Override
            public Object transform(Object input) {
                try {
                    return koyaNodeService.getKoyaNode(((ChildAssociationRef) input).getChildRef(), Dossier.class);
                } catch (KoyaServiceException ex) {
                    return null;
                }
            }
        });

        return nodes;
    }

    /**
     * Get Dossier by reference
     *
     * @param company
     * @param reference
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public Dossier getDossier(final Company company, final String reference) throws KoyaServiceException {
        String luceneRequest = "TYPE:\"koya:dossier\" AND @koya\\:reference:\"" + reference + "\" AND PATH:\" /app:company_home/st:sites/cm:" + company.getName() + "/cm:documentLibrary/*/*\"";

        ResultSet rs = null;
        try {
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, luceneRequest);
            switch (rs.length()) {
                case 0:
                    throw new KoyaServiceException(KoyaErrorCodes.NO_SUCH_DOSSIER_REFERENCE, reference);
                case 1:
                    return koyaNodeService.getKoyaNode(rs.iterator().next().getNodeRef(), Dossier.class);
                default:
                    throw new KoyaServiceException(KoyaErrorCodes.MANY_DOSSIERS_REFERENCE, reference);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public List<Dossier> getInactiveDossier(final Space space,
            final Date inactiveFrom, final boolean notNotifiedOnly) throws KoyaServiceException {
        String luceneRequest = "+PATH:\"" + nodeService.getPath(space.getNodeRef()).toPrefixString(prefixResolver) + "/*\" +TYPE:\"koya:dossier\" +@koya\\:lastModificationDate:[MIN TO \"" + LuceneUtils.getLuceneDateString(inactiveFrom) + "\"]";
        if (notNotifiedOnly) {
            luceneRequest += " +@koya\\:notified:false";
        }
        logger.debug(luceneRequest);

        ResultSet rs = null;
        try {
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, luceneRequest);

            List<Dossier> inactiveDossier = new ArrayList<>(rs.length());
            for (NodeRef n : rs.getNodeRefs()) {
                inactiveDossier.add(koyaNodeService.getKoyaNode(n, Dossier.class));
            }

            return inactiveDossier;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public void addOrUpdateLastModifiedDate(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        if (!type.equals(ContentModel.TYPE_CONTENT)
                && !type.equals(KoyaModel.TYPE_DOSSIER)) {
            // We want to update the lastModified Aspect only for content, not
            // for thumbnail or whatever
            return;
        }

        logger.debug("node "
                + nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)
                + "/"
                + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                + " of type " + nodeService.getType(nodeRef).getLocalName()
                + " Modified");
        for (Map.Entry<QName, Serializable> e : nodeService.getProperties(
                nodeRef).entrySet()) {
            logger.trace(e.getKey().getLocalName() + " : "
                    + e.getValue().toString());
        }
        // get dossier
        try {
            NodeRef n = null;
            final Dossier d = koyaNodeService
                    .getFirstParentOfType(nodeRef, Dossier.class);
            if (d != null) {

                logger.trace("Updating lastModificationDate of dossier : "
                        + d.getTitle());
                n = d.getNodeRef();
            } else if (type.equals(KoyaModel.TYPE_DOSSIER)) {
                n = nodeRef;
            }
            if (n != null) {
                final NodeRef dossierNodeRef = n;
                AuthenticationUtil
                        .runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
                            @Override
                            public Object doWork() throws Exception {
                                //Quick and Dirty hack to avoid multiple files uploads failure
                                UserTransaction transaction = transactionService.getNonPropagatingUserTransaction();
                                try {
                                    transaction.begin();

                                    // Add lastModified Aspect if not already
                                    // present
                                    if (!nodeService.hasAspect(dossierNodeRef,
                                            KoyaModel.ASPECT_LASTMODIFIED)) {
                                        Map<QName, Serializable> props = new HashMap<>();
                                        nodeService.addAspect(dossierNodeRef,
                                                KoyaModel.ASPECT_LASTMODIFIED,
                                                props);
                                    }

                                    nodeService.setProperty(dossierNodeRef,
                                            KoyaModel.PROP_LASTMODIFICATIONDATE,
                                            new Date());
                                    nodeService.setProperty(dossierNodeRef,
                                            KoyaModel.PROP_NOTIFIED, Boolean.FALSE);
                                    transaction.commit();
                                } catch (Exception cfe) {
                                    logger.debug("ConcurrencyFailureException on dossier " + d.getTitle());
                                    transaction.rollback();
                                }
                                return null;
                            }
                        });
            }
        } catch (KoyaServiceException ex) {
            logger.error("error while determinating nodeRef Koya Typed parents", ex);
        }

    }

}
