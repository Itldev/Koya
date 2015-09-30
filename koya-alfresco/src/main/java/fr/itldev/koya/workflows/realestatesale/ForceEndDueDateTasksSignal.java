package fr.itldev.koya.workflows.realestatesale;

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.Execution;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

public class ForceEndDueDateTasksSignal extends BaseJavaDelegate {

	private RuntimeService runtimeService;

	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		// TODO variable name as param
		final String executionId = execution.getVariable(
				"koyares_globalDueDatesExecId").toString();

		List<Execution> executions = runtimeService.createExecutionQuery()
				.signalEventSubscriptionName("forceEndDueDateTasks").list();

		CollectionUtils.filter(executions, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				Execution e = (Execution) o;
				return e.getId().equals(executionId);
			}
		});

		if (executions.size() == 1) {
			runtimeService.signalEventReceived("forceEndDueDateTasks",
					executionId);
		} else {
			logger.error("error send signal : exec size = " + executions.size());
		}

	}

}
