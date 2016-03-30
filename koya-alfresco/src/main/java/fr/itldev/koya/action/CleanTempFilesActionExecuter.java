package fr.itldev.koya.action;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;

public class CleanTempFilesActionExecuter extends ActionExecuterAbstractBase {

	Logger logger = Logger.getLogger(CleanTempFilesActionExecuter.class);

	public static final String NAME = "cleanTempFiles";

	private SearchService searchService;
	private NodeService nodeService;
	private String delDelay;

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public String getDelDelay() {
		return delDelay;
	}

	public void setDelDelay(String delDelay) {
		this.delDelay = delDelay;
	}

	@Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

		Date delBefore = Duration.subtract(new Date(), new Duration(delDelay));

		ResultSet rs = null;
		try {
			rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					SearchService.LANGUAGE_FTS_ALFRESCO, "ASPECT:\"koya:temp\"");

			Iterator<ResultSetRow> i = rs.iterator();
			while (i.hasNext()) {
				NodeRef n = i.next().getNodeRef();
				Date created = (Date) nodeService.getProperty(n, ContentModel.PROP_CREATED);

				if (created.before(delBefore)) {
					if (logger.isTraceEnabled()) {
						logger.error(
								"Delete temp file " + n.toString() + " older than " + delDelay);
					}
					nodeService.deleteNode(n);
				}
			}

		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	}

}
