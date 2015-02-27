/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.itldev.koya.action.scheduled;

import org.alfresco.repo.action.scheduled.CronScheduledQueryBasedTemplateActionDefinition;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author nico
 */
public class ActivableCronScheduledQueryBasedTemplateActionDefinition extends CronScheduledQueryBasedTemplateActionDefinition {

    private Logger logger = Logger.getLogger(this.getClass());
    private boolean enable = true;

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * Register with teh scheduler.
     *
     * @param scheduler
     * @throws SchedulerException
     */
    public void register(Scheduler scheduler) throws SchedulerException {
        if (enable) {
            super.register(scheduler);

        } else {
            logger.warn("Job " + getJobName() + " is not enabled");
        }
    }

    /**
     * Register with the scheduler.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (enable) {
            register(getScheduler());

        }

    }
}
