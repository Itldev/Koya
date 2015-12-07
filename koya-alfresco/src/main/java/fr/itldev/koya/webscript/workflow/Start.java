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
package fr.itldev.koya.webscript.workflow;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.action.CopyWorkflowTemplateActionExecuter;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Start named workflow on node
 * 
 * 
 * inspired by https://github.com/deas/alfresco/blob/5.0.c-fixes/root
 * /projects/remote -api/config/alfresco/templates/webscripts/org/alfresco
 * /repository/forms/form.post.json.js
 */

public class Start extends AbstractWebScript {
	private Logger logger = Logger.getLogger(this.getClass());

	private KoyaNodeService koyaNodeService;
	private FormService formService;
	private NodeService nodeService;
	private ActionService actionService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);
		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);

		String response = "";
		try {

			NodeRef n = koyaNodeService.getNodeRef((String) urlParamsMap
					.get(KoyaWebscript.WSCONST_NODEREF));
			Dossier d = koyaNodeService.getKoyaNode(n, Dossier.class);

			FormData fd = new FormData();
			for (String k : jsonPostMap.keySet()) {
				fd.addFieldData(k, jsonPostMap.get(k));
			}

			// Add related dossier as workflow parameter
			fd.addFieldData("prop_wf_relatednode", d.getNodeRef().toString());


			WorkflowInstance workflow = (WorkflowInstance) formService
					.saveForm(
							new Item("workflow", "activiti$"
									+ urlParamsMap.get("workflowId")), fd);
			// relationship between dossier node and activiti instance
			List<String> activitiIds = d.getActivitiIds();
			activitiIds.add(workflow.getId());
			nodeService.setProperty(d.getNodeRef(), KoyaModel.PROP_ACTIVITIIDS,
					new ArrayList<String>(activitiIds));
			d.setActivitiIds(activitiIds);

			// Add bpm:packageContains relationship from package node to dossier
			// node
			QName workflowPackageItemId = QName.createQName("wpi", d
					.getNodeRef().toString());
			nodeService
					.addChild(workflow.getWorkflowPackage(), d.getNodeRef(),
							WorkflowModel.ASSOC_PACKAGE_CONTAINS,
							workflowPackageItemId);


			/*
			 * Apply template to dossier if any exists : Do it in async action
			 * !!
			 */
			try {
				Map<String, Serializable> paramsCopyWorkflowTemplate = new HashMap<>();
				paramsCopyWorkflowTemplate.put(
						CopyWorkflowTemplateActionExecuter.PARAM_WORKFLOWID,
						urlParamsMap.get("workflowId"));

				Action copyWf = actionService.createAction(
						CopyWorkflowTemplateActionExecuter.NAME,
						paramsCopyWorkflowTemplate);

				// async exec
				copyWf.setExecuteAsynchronously(true);
				actionService.executeAction(copyWf, d.getNodeRef());
			} catch (Exception e) {
				logger.error("action  exception  " + e.toString());
			}
			response = KoyaWebscript.getObjectAsJson(d);

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
