package fr.itldev.koya.webscript.workflow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * 
 * inspired by org.alfresco.repo.web.scripts.workflow.TaskInstanceGet
 */
public class TaskIsAssignee extends AbstractWorkflowWebscript {

	@Override
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder,
			WebScriptRequest req, Status status, Cache cache) {
		Map<String, String> params = req.getServiceMatch().getTemplateVars();

		// getting task id from request parameters
		String taskId = params.get("task_instance_id");

		Map<String, Object> model = new HashMap<String, Object>();

		Boolean isAssignee = Boolean.FALSE;

		/**
		 * Tring to get workflow task .If fails, user can't access task.
		 * 
		 * TODO improve response time. Cache ?
		 */
		try {
			// searching for task in repository
			WorkflowTask workflowTask = workflowService.getTaskById(taskId);

			// task was not found -> return 404
			if (workflowTask == null) {
				throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
						"Unable to find workflow task with id: " + taskId);
			}
			isAssignee = Boolean.TRUE;
		} catch (Exception e) {

		}

		model.put("username", authenticationService.getCurrentUserName());
		model.put("isassignee", isAssignee);
		return model;
	}

}