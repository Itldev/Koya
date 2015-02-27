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
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneUtils;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

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

        Dossier created = koyaNodeService.getSecuredItem(car.getChildRef(), Dossier.class);
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
                    return koyaNodeService.getSecuredItem(((ChildAssociationRef) input).getChildRef(), Dossier.class);
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
                    return koyaNodeService.getSecuredItem(rs.iterator().next().getNodeRef(), Dossier.class);
                default:
                    throw new KoyaServiceException(KoyaErrorCodes.MANY_DOSSIERS_REFERENCE, reference);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public List<Dossier> getInactiveDossier(final Space space, final Date inactiveFrom, final boolean notNotifiedOnly) throws KoyaServiceException {
        String luceneRequest = "+PATH:\"" + nodeService.getPath(space.getNodeRefasObject()).toPrefixString(prefixResolver) + "/*\" +TYPE:\"koya:dossier\" +@koya\\:lastModificationDate:[MIN TO \"" + LuceneUtils.getLuceneDateString(inactiveFrom) + "\"]";
        if (notNotifiedOnly) {
            luceneRequest += " +@koya\\:notified:false";
        }
        logger.debug(luceneRequest);

        ResultSet rs = null;
        try {
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, luceneRequest);

            List<Dossier> inactiveDossier = new ArrayList<>(rs.length());
            for (NodeRef n : rs.getNodeRefs()) {
                inactiveDossier.add(koyaNodeService.getSecuredItem(n, Dossier.class));
            }

            return inactiveDossier;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
}
