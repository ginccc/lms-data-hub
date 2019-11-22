package at.ac.webster.scheduledjobs.impl;

import at.ac.webster.scheduledjobs.IJobExecutionFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

public class ExtendedJobFactory implements JobFactory {
    private final IJobExecutionFactory jobExecutionFactory;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ExtendedJobFactory(IJobExecutionFactory jobExecutionFactory) {
        this.jobExecutionFactory = jobExecutionFactory;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler Scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            Constructor<? extends Job> constructor = jobClass.getDeclaredConstructor(IJobExecutionFactory.class);
            return constructor.newInstance(jobExecutionFactory);
        } catch (Exception e) {
            String message = String.format("Problem instantiating class '%s'", jobClass.getName());
            logger.error(message, e);
            throw new SchedulerException(message, e);
        }
    }
}
