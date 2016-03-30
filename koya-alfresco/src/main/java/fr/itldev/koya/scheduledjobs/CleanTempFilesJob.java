package fr.itldev.koya.scheduledjobs;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import fr.itldev.koya.action.CleanTempFilesActionExecuter;

public class CleanTempFilesJob implements StatefulJob {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobData = context.getJobDetail().getJobDataMap();
		final ActionService actionService = (ActionService) jobData.get("actionService");
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				Action cleanTempFiles = actionService
						.createAction(CleanTempFilesActionExecuter.NAME, null);

				actionService.executeAction(cleanTempFiles, null);
				return null;
			}
		});

	}

}
