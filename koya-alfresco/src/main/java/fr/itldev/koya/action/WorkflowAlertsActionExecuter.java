package fr.itldev.koya.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import fr.itldev.koya.alfservice.KoyaMailService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.security.SpaceAclService;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.KoyaNode;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.permissions.KoyaPermissionCollaborator;

public class WorkflowAlertsActionExecuter extends ActionExecuterAbstractBase {
	
	public static final String NAME = "workflowAlerts";

	/**
	 * TODO generic mail alerts : only available for koyares workflow now.
	 */

	private static final String WF_RES_1_0 = "http://www.itldev.fr/model/workflow/realEstateSale/1.0";
	private static final QName TARGET_TASK = QName.createQName(WF_RES_1_0, "targetTask");

	private static Set<QName> REFERENCE_TYPES = new HashSet<QName>() {
		{
			add(KoyaModel.TYPE_DOSSIER);
		}
	};

	private WorkflowService workflowService;
	private List<String> wfTypes;
	private List<String> wfTasksDueDate;
	private List<Integer> alertDelays;
	private KoyaMailService koyaMailService;
	private KoyaNodeService koyaNodeService;
	private SpaceAclService spaceAclService;

	private NodeService nodeService;

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setWfTypes(List<String> wfTypes) {
		this.wfTypes = wfTypes;
	}

	public void setWfTasksDueDate(List<String> wfTasksDueDate) {
		this.wfTasksDueDate = wfTasksDueDate;
	}

	public void setAlertDelays(List<Integer> alertDelays) {
		this.alertDelays = alertDelays;
	}

	public void setKoyaMailService(KoyaMailService koyaMailService) {
		this.koyaMailService = koyaMailService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setSpaceAclService(SpaceAclService spaceAclService) {
		this.spaceAclService = spaceAclService;
	}

	@Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

		List<WorkflowInstance> candidates = new ArrayList<>();
		for (String wfType : wfTypes) {
			/**
			 * For each active workflow instance
			 */
			for (WorkflowDefinition wfDef : workflowService
					.getAllDefinitionsByName("activiti$" + wfType)) {
				WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery();
				workflowInstanceQuery.setActive(true);
				workflowInstanceQuery.setWorkflowDefinitionId(wfDef.getId());
				candidates.addAll(workflowService.getWorkflows(workflowInstanceQuery));
			}
		}

		for (WorkflowInstance wfi : candidates) {
			processWorkflowInstance(wfi);
		}
	}

	/**
	 * Process workflow instance.
	 * 
	 * Check dueDate tasks and send mail to Dossier related members and
	 * responsibles if due date match one of delays.
	 * 
	 * @param instance
	 */
	private void processWorkflowInstance(WorkflowInstance instance) {

		List<KoyaNode> koyaNodeReferences = new ArrayList<>();
		List<ChildAssociationRef> refs = nodeService.getChildAssocs(instance.getWorkflowPackage(),
				REFERENCE_TYPES);
		for (ChildAssociationRef car : refs) {
			koyaNodeReferences.add(koyaNodeService.getKoyaNode(car.getChildRef()));
		}

		/**
		 * Process all DueDate Tasks
		 */
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setTaskState(WorkflowTaskState.COMPLETED);
		taskQuery.setProcessId(instance.getId());
		Map<String, WorkflowTask> targetTaskMap = new HashMap<>();
		for (WorkflowTask wt : workflowService.queryTasks(taskQuery, false)) {
			if (wfTasksDueDate.contains(wt.getName())) {
				WorkflowTask t = targetTaskMap.get(wt.getProperties().get(TARGET_TASK).toString());
				if (t == null || ((Date) t.getProperties().get(WorkflowModel.PROP_COMPLETION_DATE))
						.before((Date) wt.getProperties()
								.get(WorkflowModel.PROP_COMPLETION_DATE))) {
					targetTaskMap.put(wt.getProperties().get(TARGET_TASK).toString(), wt);
				}
			}
		}

		/**
		 * filter DueDate Tasks with alertDelays
		 */

		List<Pair<WorkflowTask, Integer>> alertablesTasks = new ArrayList<>();

		for (Entry<String, WorkflowTask> e : targetTaskMap.entrySet()) {

			for (Integer delay : alertDelays) {
				// build dateRage corresponding to delay
				Calendar cStart = Calendar.getInstance();
				cStart.add(Calendar.DATE, delay - 1);
				Calendar cEnd = Calendar.getInstance();
				cEnd.add(Calendar.DATE, delay);

				Date taskDueDate = (Date) e.getValue().getProperties()
						.get(WorkflowModel.PROP_DUE_DATE);
				if (taskDueDate.after(cStart.getTime()) && taskDueDate.before(cEnd.getTime())) {
					alertablesTasks.add(new Pair<WorkflowTask, Integer>(e.getValue(), delay));
					break;
				}
			}
		}

		/**
		 * Gather alert mail users
		 */
		List<User> alertDest = new ArrayList<>();
		if (alertablesTasks.size() > 0) {
			alertDest.addAll(spaceAclService.listMembership((Space) koyaNodeReferences.get(0),
					KoyaPermissionCollaborator.MEMBER));
			alertDest.addAll(spaceAclService.listMembership((Space) koyaNodeReferences.get(0),
					KoyaPermissionCollaborator.RESPONSIBLE));
		}
		/**
		 * TODO User Mail Alert Filter
		 * 
		 */

		/**
		 * Send Mail alerts
		 */

		for (Pair<WorkflowTask, Integer> t : alertablesTasks) {
			koyaMailService.sendTaskExpireAlertMail(alertDest,
					t.getFirst().getProperties().get(TARGET_TASK).toString(),
					instance.getDefinition().getName(), (Space) koyaNodeReferences.get(0),
					t.getSecond(),
					(Date) t.getFirst().getProperties().get(WorkflowModel.PROP_DUE_DATE));

		}

	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	}

}
