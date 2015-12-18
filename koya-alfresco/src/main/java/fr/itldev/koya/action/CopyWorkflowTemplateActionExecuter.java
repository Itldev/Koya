/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.itldev.koya.action;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.log4j.Logger;

public class CopyWorkflowTemplateActionExecuter extends
		ActionExecuterAbstractBase {

	Logger logger = Logger.getLogger(CopyWorkflowTemplateActionExecuter.class);

	public static final String NAME = "copyWorkflowTemplate";
	public static final String PARAM_WORKFLOWID = "workflowId";

	private NodeService nodeService;
	private SearchService searchService;
	private CopyService copyService;
	private NamespaceService namespaceService;

	private String xpathTemplatesRoot;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setXpathTemplatesRoot(String xpathTemplatesRoot) {
		this.xpathTemplatesRoot = xpathTemplatesRoot;
	}

	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef,
	 *      org.alfresco.repo.ref.NodeRef)
	 */
	@Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

		String workflowId = ruleAction.getParameterValue(PARAM_WORKFLOWID)
				.toString();

		List<NodeRef> nodeRefs = searchService.selectNodes(nodeService
				.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
				xpathTemplatesRoot + "/cm:" + workflowId, null,
				namespaceService, false);

		/**
		 * If template rootNode exists, then copy in target dossier
		 */
		if (nodeRefs.size() == 1) {
			logger.info("Apply template on " + workflowId
					+ " workflow creation dossier " + actionedUponNodeRef);
			for (ChildAssociationRef associationRef : nodeService
					.getChildAssocs(nodeRefs.get(0))) {
				copyService.copyAndRename(associationRef.getChildRef(),
						actionedUponNodeRef, associationRef.getTypeQName(),
						associationRef.getQName(), true);
			}
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	}
}
