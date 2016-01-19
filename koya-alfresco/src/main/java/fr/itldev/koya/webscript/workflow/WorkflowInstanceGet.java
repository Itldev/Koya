package fr.itldev.koya.webscript.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.itldev.koya.model.exceptions.KoyaErrorCodes;

public class WorkflowInstanceGet extends AbstractWorkflowWebscript {
	public static final String PARAM_INCLUDE_TASKS = "includeTasks";

	private Logger logger = Logger.getLogger(this.getClass());

	private PermissionService permissionService;

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	protected Map<String, Object> buildModel(final WorkflowModelBuilder modelBuilder, WebScriptRequest req,
			Status status, Cache cache) {
		Map<String, String> params = req.getServiceMatch().getTemplateVars();
		String currentUser = authenticationService.getCurrentUserName();

		// getting workflow instance id from request parameters
		String workflowInstanceId = params.get("workflow_instance_id");

		final boolean includeTasks = getIncludeTasks(req);

		final WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);

		// task was not found -> return 404
		if (workflowInstance == null) {
			throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
					"Unable to find workflow instance with id: " + workflowInstanceId);
		}

		/**
		 * check current user permissions on workflow related item if current
		 * user has read permissions on reference node then override permissions
		 * by extracting workflow details as system user
		 */
		List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(workflowInstance.getWorkflowPackage());

		if (childRefs.size() != 1) {
			throw new WebScriptException("KoyaError : " + KoyaErrorCodes.WF_NO_OR_TOO_MANY_REFERENCE_ITEM);
		}
		try {
			NodeRef referenceNode = childRefs.get(0).getChildRef();
			if (!permissionService.hasPermission(referenceNode, PermissionService.READ).equals(AccessStatus.ALLOWED)) {

				throw new WebScriptException("KoyaError : " + KoyaErrorCodes.WF_NO_READ_PERMISSION_ON_REFERENCE_ITEM);
			}
		} catch (Exception ex) {
			throw new WebScriptException("KoyaError : " + KoyaErrorCodes.WF_NO_READ_PERMISSION_ON_REFERENCE_ITEM);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> wfModel = AuthenticationUtil
				.runAsSystem(new AuthenticationUtil.RunAsWork<Map<String, Object>>() {
					@Override
					public Map<String, Object> doWork() throws Exception {
						return modelBuilder.buildDetailed(workflowInstance, includeTasks);
					}
				});

		// build the model for ftl
		model.put("workflowInstance", wfModel);

		return model;
	}

	private boolean getIncludeTasks(WebScriptRequest req) {
		String includeTasks = req.getParameter(PARAM_INCLUDE_TASKS);
		if (includeTasks != null) {
			try {
				return Boolean.valueOf(includeTasks);
			} catch (Exception e) {
				// do nothing, false will be returned
			}
		}

		// Defaults to false.
		return false;
	}

}
