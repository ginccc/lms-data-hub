package at.ac.webster.scheduledjobs.impl;

import at.ac.webster.scheduledjobs.IJob;
import at.ac.webster.scheduledjobs.IJobExecutionFactory;
import at.ac.webster.scheduledjobs.IScheduledJobs;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ScheduledJobs implements IScheduledJobs {
    private final Scheduler scheduler;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ScheduledJobs() {
        this(null);
    }

    public ScheduledJobs(InputStream config) {
        this(new StdSchedulerFactory(), new StandardJobExecutionFactory(), config);
    }

    public ScheduledJobs(IJobExecutionFactory jobFactory, InputStream config) {
        this(new StdSchedulerFactory(), jobFactory, config);
    }

    public ScheduledJobs(StdSchedulerFactory schedulerFactory, IJobExecutionFactory executionFactory, InputStream config) {
        try {
            if (config != null) {
                schedulerFactory.initialize(config);
            }
            this.scheduler = schedulerFactory.getScheduler();
            scheduler.setJobFactory(new ExtendedJobFactory(executionFactory));
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new RuntimeException("Error while initializing ScheduledJobsFactory!", e);
        }
    }

    @Override
    public IJob newJob(String name) {
        return newJob(name, Scheduler.DEFAULT_GROUP);
    }

    @Override
    public IJob newJob(String name, String group) {
        return new JobImpl(JobBuilder.newJob(),
                TriggerBuilder.newTrigger(),
                SimpleScheduleBuilder.simpleSchedule(), name, group);
    }

    @Override
    public void submitJob(IJob job) throws ScheduledJobsException {
        try {
            if (!(job instanceof JobImpl)) {
                throw new ScheduledJobsException("Param 'job' needs to be an instance of JobImpl");
            }

            final JobImpl jobImpl = (JobImpl) job;
            scheduler.scheduleJob(jobImpl.buildJob(), jobImpl.buildTrigger());
        } catch (SchedulerException e) {
            throw new ScheduledJobsException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean containsJob(String name) throws ScheduledJobsException {
        return containsJob(name, Scheduler.DEFAULT_GROUP);
    }

    @Override
    public boolean containsJob(String name, String group) throws ScheduledJobsException {
        try {
            return scheduler.checkExists(new JobKey(name, group));
        } catch (SchedulerException e) {
            throw new ScheduledJobsException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean deleteJob(String name) throws ScheduledJobsException {
        return deleteJob(name, Scheduler.DEFAULT_GROUP);
    }

    @Override
    public boolean deleteJob(String name, String group) throws ScheduledJobsException {
        try {
            return scheduler.deleteJob(new JobKey(name, group));
        } catch (SchedulerException e) {
            throw new ScheduledJobsException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void shutdownScheduler(boolean gracefulShutdown) throws ScheduledJobsException {
        try {
            scheduler.shutdown(gracefulShutdown);
        } catch (SchedulerException e) {
            throw new ScheduledJobsException(e.getLocalizedMessage(), e);
        }
    }
}
