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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;

/* *
 * inspired by 
 * /root/projects/repository/source/java/org/alfresco/repo/workflow/WorkflowInterpreter.java
 * 
 * deployement section
 * 
 * 
 */
public class Deploy extends AbstractWebScript {
	private Logger logger = Logger.getLogger(this.getClass());

	private WorkflowService workflowService;

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String response = "";
		Content workflowDef = null;

		FormData formData = (FormData) req.parseContent();
		FormData.FormField[] fields = formData.getFields();
		for (FormData.FormField field : fields) {
			if (field.getName().equals("file") && field.getIsFile()) {
				workflowDef = field.getContent();
			}
		}

		WorkflowDeployment deployment = workflowService.deployDefinition("activiti", workflowDef.getInputStream(),
				MimetypeMap.MIMETYPE_XML);

		WorkflowDefinition def = deployment.getDefinition();

		logger.debug("Deploy new workflow version :  " + def.getName());

		if (deployment.getProblems().length > 0) {
			String msg = "";
			for (String problem : deployment.getProblems()) {
				msg += problem + ";";
			}
			throw new WebScriptException(msg);
		}

		// TODO success message

		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
