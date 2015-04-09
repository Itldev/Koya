package fr.itldev.koya.repo.model.filefolder;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.model.filefolder.GetChildrenCannedQuery;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.springframework.shell.support.util.NaturalOrderComparator;

public class KoyaGetChildrenCannedQuery extends GetChildrenCannedQuery {

	private NodeService nodeService;

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

		this.nodeService = nodeService;
	}

	@Override
	protected List<NodeRef> queryAndFilter(CannedQueryParameters params) {
		List<NodeRef> results = super
				.queryAndFilter(new CannedQueryParameters(params
						.getParameterBean(), params.getPageDetails(),
						KoyaCannedQuerySortDetailsFactory
								.getCannedQuerySortDetails(params
										.getSortDetails().getSortPairs()),
						params.getTotalResultCountMax(), params
								.getQueryExecutionId()));

		@SuppressWarnings("rawtypes")
		Filter f = new Filter<Pair<QName, SortOrder>>() {

			@Override
			public Boolean apply(Pair<QName, SortOrder> value) {
				return !value.getFirst().equals(
						GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER)
						&& !value.getFirst().equals(
								GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE);
			}
		};

		@SuppressWarnings("unchecked")
		List<Pair<QName, SortOrder>> sortPairs = CollectionUtils.filter(params
				.getSortDetails().getSortPairs(), f);

		if (sortPairs.size() > 0) {
			Collections.sort(results, new PropComparatorAsc(sortPairs,
					nodeService));
		}

		return results;
	}

	private class PropComparatorAsc implements Comparator<NodeRef> {

		private List<Pair<QName, SortOrder>> sortProps;
		private NaturalOrderComparator<String> naturalOrderComparator;
		private NodeService nodeService;

		public PropComparatorAsc(List<Pair<QName, SortOrder>> sortProps,
				NodeService nodeService) {
			this.sortProps = sortProps;
			this.nodeService = nodeService;
			this.naturalOrderComparator = new NaturalOrderComparator<String>();
		}

		public int compare(NodeRef n1, NodeRef n2) {
			return compareImpl(n1, n2, sortProps);
		}

		private int compareImpl(NodeRef node1In, NodeRef node2In,
				List<Pair<QName, SortOrder>> sortProps) {
			Object pv1 = null;
			Object pv2 = null;

			QName sortPropQName = (QName) sortProps.get(0).getFirst();
			boolean sortAscending = (sortProps.get(0).getSecond() == SortOrder.ASCENDING);

			NodeRef node1 = node1In;
			NodeRef node2 = node2In;

			if (sortAscending == false) {
				node1 = node2In;
				node2 = node1In;
			}

			int result = 0;

			pv1 = nodeService.getProperty(node1, sortPropQName);
			pv2 = nodeService.getProperty(node2, sortPropQName);

			if (pv1 == null) {
				return (pv2 == null ? 0 : -1);
			} else if (pv2 == null) {
				return 1;
			}

			if (pv1 instanceof String) {
				result = naturalOrderComparator.compare((String) pv1,
						(String) pv2);
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
				throw new RuntimeException("Unsupported sort type: "
						+ pv1.getClass().getName());
			}

			if ((result == 0) && (sortProps.size() > 1)) {
				return compareImpl(node1In, node2In,
						sortProps.subList(1, sortProps.size()));
			}

			return result;
		}
	}
}

class KoyaCannedQuerySortDetailsFactory {

	public static CannedQuerySortDetails getCannedQuerySortDetails(
			List<Pair<? extends Object, SortOrder>> sortPairs) {

		@SuppressWarnings("rawtypes")
		Filter f = new Filter<Pair<QName, SortOrder>>() {

			@Override
			public Boolean apply(Pair<QName, SortOrder> value) {
				return value.getFirst().equals(
						GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER)
						|| value.getFirst().equals(
								GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE);
			}
		};

		@SuppressWarnings("unchecked")
		List<Pair<? extends Object, SortOrder>> l = CollectionUtils.filter(
				(List<Pair<? extends Object, SortOrder>>) sortPairs, f);

		return new CannedQuerySortDetails(l);

	}

}
