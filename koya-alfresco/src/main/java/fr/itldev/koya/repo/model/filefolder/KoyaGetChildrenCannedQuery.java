package fr.itldev.koya.repo.model.filefolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.node.ReferenceablePropertiesEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.model.filefolder.GetChildrenCannedQuery;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterSortNodeEntity;
import static org.alfresco.repo.node.getchildren.GetChildrenCannedQuery.MAX_FILTER_SORT_PROPS;
import static org.alfresco.repo.node.getchildren.GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE;
import static org.alfresco.repo.node.getchildren.GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE;
import static org.alfresco.repo.node.getchildren.GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER;
import static org.alfresco.repo.node.getchildren.GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryParams;
import org.alfresco.repo.security.permissions.PermissionCheckedValue;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.log4j.Logger;
import org.springframework.shell.support.util.NaturalOrderComparator;

public class KoyaGetChildrenCannedQuery extends GetChildrenCannedQuery {

    private final Logger logger = Logger.getLogger(this.getClass());
//    private NodeService nodeService;
    private final NodeDAO nodeDAO;
    private final QNameDAO qnameDAO;
    private final CannedQueryDAO cannedQueryDAO;
    private final NodePropertyHelper nodePropertyHelper;
    private final TenantService tenantService;

    private static final String QUERY_NAMESPACE = "alfresco.node";
    private static final String QUERY_SELECT_GET_CHILDREN_WITH_PROPS = "select_GetChildrenCannedQueryWithProps";
    private static final String QUERY_SELECT_GET_CHILDREN_WITHOUT_PROPS = "select_GetChildrenCannedQueryWithoutProps";

    public KoyaGetChildrenCannedQuery(NodeDAO nodeDAO, QNameDAO qnameDAO,
            CannedQueryDAO cannedQueryDAO,
            NodePropertyHelper nodePropertyHelper, TenantService tenantService,
            NodeService nodeService,
            MethodSecurityBean<NodeRef> methodSecurity,
            CannedQueryParameters params, HiddenAspect hiddenAspect,
            DictionaryService dictionaryService, Set<QName> ignoreAspectQNames) {
        super(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper,
                tenantService, nodeService, methodSecurity, params,
                hiddenAspect, dictionaryService, ignoreAspectQNames);
        
        this.nodeDAO = nodeDAO;
        this.qnameDAO=qnameDAO;
        this.cannedQueryDAO=cannedQueryDAO;
        this.nodePropertyHelper=nodePropertyHelper;
        this.tenantService=tenantService;
        

    }

    @Override
    protected List<NodeRef> queryAndFilter(CannedQueryParameters parameters) {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        // Get parameters
        GetChildrenCannedQueryParams paramBean = (GetChildrenCannedQueryParams) parameters.getParameterBean();

        // Get parent node
        NodeRef parentRef = paramBean.getParentRef();
        ParameterCheck.mandatory("nodeRef", parentRef);
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(parentRef);
        if (nodePair == null) {
            throw new InvalidNodeRefException("Parent node does not exist: " + parentRef, parentRef);
        }
        Long parentNodeId = nodePair.getFirst();

        // Set query params - note: currently using SortableChildEntity to hold (supplemental-) query params
        FilterSortNodeEntity params = new FilterSortNodeEntity();

        // Set parent node id
        params.setParentNodeId(parentNodeId);

        // Get filter details
        Set<QName> childNodeTypeQNames = paramBean.getChildTypeQNames();
        Set<QName> assocTypeQNames = paramBean.getAssocTypeQNames();
        final List<FilterProp> filterProps = paramBean.getFilterProps();
        String pattern = paramBean.getPattern();

        // Get sort details
        CannedQuerySortDetails sortDetails = parameters.getSortDetails();
        @SuppressWarnings({"unchecked", "rawtypes"})
        final List<Pair<QName, SortOrder>> sortPairs = (List) sortDetails.getSortPairs();

        // Set sort / filter params
        // Note - need to keep the sort properties in their requested order
        List<QName> sortFilterProps = new ArrayList<>(filterProps.size() + sortPairs.size());
        for (Pair<QName, SortOrder> sort : sortPairs) {
            QName sortQName = sort.getFirst();
            if (!sortFilterProps.contains(sortQName)) {
                sortFilterProps.add(sortQName);
            }
        }
        for (FilterProp filter : filterProps) {
            QName filterQName = filter.getPropName();
            if (!sortFilterProps.contains(filterQName)) {
                sortFilterProps.add(filterQName);
            }
        }

        int filterSortPropCnt = sortFilterProps.size();

        if (filterSortPropCnt > MAX_FILTER_SORT_PROPS) {
            throw new AlfrescoRuntimeException("GetChildren: exceeded maximum number filter/sort properties: (max=" + MAX_FILTER_SORT_PROPS + ", actual=" + filterSortPropCnt);
        }

        filterSortPropCnt = setFilterSortParams(sortFilterProps, params);

        // Set child node type qnames (additional filter - performed by DB query)
        if (childNodeTypeQNames != null) {
            Set<Long> childNodeTypeQNameIds = qnameDAO.convertQNamesToIds(childNodeTypeQNames, false);
            if (childNodeTypeQNameIds.size() > 0) {
                params.setChildNodeTypeQNameIds(new ArrayList<Long>(childNodeTypeQNameIds));
            }
        }

        if (assocTypeQNames != null) {
            Set<Long> assocTypeQNameIds = qnameDAO.convertQNamesToIds(assocTypeQNames, false);
            if (assocTypeQNameIds.size() > 0) {
                params.setAssocTypeQNameIds(assocTypeQNameIds);
            }
        }

        if (pattern != null) {
            // TODO, check that we should be tied to the content model in this way. Perhaps a configurable property
            // name against which compare the pattern?
            Pair<Long, QName> nameQName = qnameDAO.getQName(ContentModel.PROP_TITLE); //apply pattern on title field instead or name
            if (nameQName == null) {
                throw new AlfrescoRuntimeException("Unable to determine qname id of name property");
            }
            params.setNamePropertyQNameId(nameQName.getFirst());
            params.setPattern(pattern);
        }

        final List<NodeRef> result;

        if (filterSortPropCnt > 0) {
            // filtered and/or sorted - note: permissions will be applied post query
            final List<FilterSortNode> children = new ArrayList<FilterSortNode>(100);
            final FilterSortChildQueryCallback c = getFilterSortChildQuery(children, filterProps, paramBean);
            FilterSortResultHandler resultHandler = new FilterSortResultHandler(c);
            cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_WITH_PROPS, params, 0, Integer.MAX_VALUE, resultHandler);
            resultHandler.done();

            if (sortPairs.size() > 0) {
                // sort
                Collections.sort(children, new PropComparatorAsc(sortPairs));
            }

            result = new ArrayList<NodeRef>(children.size());
            for (FilterSortNode child : children) {
                result.add(tenantService.getBaseName(child.getNodeRef()));
            }
        } else {
            // unsorted (apart from any implicit order) - note: permissions are applied during result handling to allow early cutoff

            final int requestedCount = parameters.getResultsRequired();

            final List<NodeRef> rawResult = new ArrayList<NodeRef>(Math.min(1000, requestedCount));
            UnsortedChildQueryCallback callback = getUnsortedChildQueryCallback(rawResult, requestedCount, paramBean);
            UnsortedResultHandler resultHandler = new UnsortedResultHandler(callback);
            cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_WITHOUT_PROPS, params, 0, Integer.MAX_VALUE, resultHandler);
            resultHandler.done();

            // permissions have been applied
            result = PermissionCheckedValue.PermissionCheckedValueMixin.create(rawResult);
        }

        if (start != null) {
            logger.debug("Base query " + (filterSortPropCnt > 0 ? "(sort=y, perms=n)" : "(sort=n, perms=y)") + ": " + result.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
        }

        return result;
    }

    private class PropComparatorAsc implements Comparator<FilterSortNode> {

        private final List<Pair<QName, SortOrder>> sortProps;
        private final NaturalOrderComparator<String> naturalOrderComparator;

        public PropComparatorAsc(List<Pair<QName, SortOrder>> sortProps) {
            this.sortProps = sortProps;
            this.naturalOrderComparator = new NaturalOrderComparator<>();
        }

        public int compare(FilterSortNode n1, FilterSortNode n2) {
            return compareImpl(n1, n2, sortProps);
        }

        private int compareImpl(FilterSortNode node1In, FilterSortNode node2In,
                List<Pair<QName, SortOrder>> sortProps) {
            Object pv1 = null;
            Object pv2 = null;

            QName sortPropQName = (QName) sortProps.get(0).getFirst();
            boolean sortAscending = (sortProps.get(0).getSecond() == SortOrder.ASCENDING);

            FilterSortNode node1 = node1In;
            FilterSortNode node2 = node2In;

            if (sortAscending == false) {
                node1 = node2In;
                node2 = node1In;
            }

            int result = 0;

            pv1 = node1.getVal(sortPropQName);
            pv2 = node2.getVal(sortPropQName);

            if (pv1 == null) {
                return (pv2 == null ? 0 : -1);
            } else if (pv2 == null) {
                return 1;
            }

            if (pv1 instanceof String) {
                result = naturalOrderComparator.compare(((String) pv1).toLowerCase(), ((String) pv2).toLowerCase());
            } else if (pv1 instanceof Date) {
                result = (((Date) pv1).compareTo((Date) pv2));
            } else if (pv1 instanceof Long) {
                result = (((Long) pv1).compareTo((Long) pv2));
            } else if (pv1 instanceof Integer) {
                result = (((Integer) pv1).compareTo((Integer) pv2));
            } else if (pv1 instanceof QName) {
                result = (((QName) pv1).compareTo((QName) pv2));
            } else if (pv1 instanceof Boolean) {
                result = (((Boolean) pv1).compareTo((Boolean) pv2));
            } else {
                // TODO other comparisons
                throw new RuntimeException("Unsupported sort type: " + pv1.getClass().getName());
            }

            if ((result == 0) && (sortProps.size() > 1)) {
                return compareImpl(node1In, node2In, sortProps.subList(1, sortProps.size()));
            }

            return result;
        }
    }

    // Set filter/sort props (between 0 and 3)
    private int setFilterSortParams(List<QName> filterSortProps,
            FilterSortNodeEntity params) {
        int cnt = 0;
        int propCnt = 0;

        for (QName filterSortProp : filterSortProps) {
            if (AuditablePropertiesEntity.getAuditablePropertyQNames().contains(filterSortProp)) {
                params.setAuditableProps(true);
            } else if (filterSortProp.equals(SORT_QNAME_NODE_TYPE) || filterSortProp.equals(SORT_QNAME_NODE_IS_FOLDER)) {
                params.setNodeType(true);
            } else {
                Long sortQNameId = getQNameId(filterSortProp);
                if (sortQNameId != null) {
                    if (propCnt == 0) {
                        params.setProp1qnameId(sortQNameId);
                    } else if (propCnt == 1) {
                        params.setProp2qnameId(sortQNameId);
                    } else if (propCnt == 2) {
                        params.setProp3qnameId(sortQNameId);
                    } else {
                        // belts and braces
                        throw new AlfrescoRuntimeException("GetChildren: unexpected - cannot set sort parameter: " + cnt);
                    }

                    propCnt++;
                } else {
                    logger.warn("Skipping filter/sort param - cannot find: " + filterSortProp);
                    break;
                }
            }

            cnt++;
        }

        return cnt;
    }

    private Long getQNameId(QName sortPropQName) {
        if (sortPropQName.equals(SORT_QNAME_CONTENT_SIZE) || sortPropQName.equals(SORT_QNAME_CONTENT_MIMETYPE)) {
            sortPropQName = ContentModel.PROP_CONTENT;
        }

        Pair<Long, QName> qnamePair = qnameDAO.getQName(sortPropQName);
        return (qnamePair == null ? null : qnamePair.getFirst());
    }

    private void preload(List<NodeRef> nodeRefs) {
        Long start = (logger.isTraceEnabled() ? System.currentTimeMillis() : null);

        nodeDAO.cacheNodes(nodeRefs);

        if (start != null) {
            logger.trace("Pre-load: " + nodeRefs.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
        }
    }

    protected class FilterSortResultHandler implements CannedQueryDAO.ResultHandler<FilterSortNodeEntity> {

        private final FilterSortChildQueryCallback resultsCallback;
        private boolean more = true;

        private FilterSortResultHandler(
                FilterSortChildQueryCallback resultsCallback) {
            this.resultsCallback = resultsCallback;
        }

        public boolean handleResult(FilterSortNodeEntity result) {
            // Do nothing if no further results are required
            if (!more) {
                return false;
            }

            Node node = result.getNode();
            NodeRef nodeRef = node.getNodeRef();

            Map<NodePropertyKey, NodePropertyValue> propertyValues = new HashMap<>(3);

            NodePropertyEntity prop1 = result.getProp1();
            if (prop1 != null) {
                propertyValues.put(prop1.getKey(), prop1.getValue());
            }

            NodePropertyEntity prop2 = result.getProp2();
            if (prop2 != null) {
                propertyValues.put(prop2.getKey(), prop2.getValue());
            }

            NodePropertyEntity prop3 = result.getProp3();
            if (prop3 != null) {
                propertyValues.put(prop3.getKey(), prop3.getValue());
            }

            Map<QName, Serializable> propVals = nodePropertyHelper.convertToPublicProperties(propertyValues);

            // Add referenceable / spoofed properties (including spoofed name if null)
            ReferenceablePropertiesEntity.addReferenceableProperties(node, propVals);

            // special cases
            // MLText (eg. cm:title, cm:description, ...)
            for (Map.Entry<QName, Serializable> entry : propVals.entrySet()) {
                if (entry.getValue() instanceof MLText) {
                    propVals.put(entry.getKey(), DefaultTypeConverter.INSTANCE.convert(String.class, (MLText) entry.getValue()));
                }
            }

            // ContentData (eg. cm:content.size, cm:content.mimetype)
            ContentData contentData = (ContentData) propVals.get(ContentModel.PROP_CONTENT);
            if (contentData != null) {
                propVals.put(SORT_QNAME_CONTENT_SIZE, contentData.getSize());
                propVals.put(SORT_QNAME_CONTENT_MIMETYPE, contentData.getMimetype());
            }

            // Auditable props (eg. cm:creator, cm:created, cm:modifier, cm:modified, ...)
            AuditablePropertiesEntity auditableProps = node.getAuditableProperties();
            if (auditableProps != null) {
                for (Map.Entry<QName, Serializable> entry : auditableProps.getAuditableProperties().entrySet()) {
                    propVals.put(entry.getKey(), entry.getValue());
                }
            }

            // Node type
            Long nodeTypeQNameId = node.getTypeQNameId();
            if (nodeTypeQNameId != null) {
                Pair<Long, QName> pair = qnameDAO.getQName(nodeTypeQNameId);
                if (pair != null) {
                    propVals.put(SORT_QNAME_NODE_TYPE, pair.getSecond());
                }
            }

            // Call back
            boolean more = resultsCallback.handle(new FilterSortNode(nodeRef, propVals));
            if (!more) {
                this.more = false;
            }

            return more;
        }

        public void done() {
        }
    }

    private class UnsortedResultHandler implements CannedQueryDAO.ResultHandler<NodeEntity> {

        private final UnsortedChildQueryCallback resultsCallback;

        private boolean more = true;

        private static final int BATCH_SIZE = 256 * 4;
        private final List<NodeRef> nodeRefs;

        private UnsortedResultHandler(UnsortedChildQueryCallback resultsCallback) {
            this.resultsCallback = resultsCallback;

            nodeRefs = new LinkedList<NodeRef>();
        }

        public boolean handleResult(NodeEntity result) {
            // Do nothing if no further results are required
            if (!more) {
                return false;
            }

            NodeRef nodeRef = result.getNodeRef();

            if (nodeRefs.size() >= BATCH_SIZE) {
                // batch
                preloadAndApplyPermissions();
            }

            nodeRefs.add(nodeRef);

            return more;
        }

        private void preloadAndApplyPermissions() {
            preload(nodeRefs);

            // TODO track total time for incremental permission checks ... and cutoff (eg. based on some config)
            List<NodeRef> results = applyPostQueryPermissions(nodeRefs, nodeRefs.size());

            for (NodeRef nodeRef : results) {
                // Call back
                boolean more = resultsCallback.handle(nodeRef);
                if (!more) {
                    this.more = false;
                    break;
                }
            }

            nodeRefs.clear();
        }

        public void done() {
            if (nodeRefs.size() >= 0) {
                // finish batch
                preloadAndApplyPermissions();
            }
        }
    }
}
