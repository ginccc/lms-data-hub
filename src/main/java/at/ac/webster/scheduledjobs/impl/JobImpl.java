package at.ac.webster.scheduledjobs.impl;

import at.ac.webster.scheduledjobs.IJob;
import at.ac.webster.scheduledjobs.IScheduledJobs;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JobImpl implements IJob {
    private final SimpleScheduleBuilder scheduleBuilder;
    private final JobBuilder jobBuilder;
    private final TriggerBuilder<Trigger> triggerBuilder;
    private String name;
    private String group;
    private Class<? extends Execution> execution;
    private Map<String, String> jobInformation = new HashMap<>();

    private boolean usedSchedule = false;
    private boolean failOver = true;

    JobImpl(JobBuilder jobBuilder,
            TriggerBuilder<Trigger> triggerBuilder,
            SimpleScheduleBuilder scheduleBuilder,
            String name,
            String group) {
        this.jobBuilder = jobBuilder;
        this.triggerBuilder = triggerBuilder;
        this.scheduleBuilder = scheduleBuilder;
        this.name = name;
        this.group = group;
    }

    @Override
    public IJob jobExecution(Class<? extends Execution> execution) {
        this.execution = execution;

        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public void addJobInformation(String key, String str) {
        jobInformation.put(key, str);
    }

    @Override
    public IJob interval(int interval, TimeUnit timeUnit, int repeatTimes) throws IScheduledJobs.ScheduledJobsException {
        switch (timeUnit) {
            case NANOSECONDS:
                throw new IScheduledJobs.ScheduledJobsException("Nanoseconds are not supported by the underlying framework!");

            case MICROSECONDS:
                throw new IScheduledJobs.ScheduledJobsException("Microseconds are not supported by the underlying framework!");

            default:
            case MILLISECONDS:
                this.scheduleBuilder.withIntervalInMilliseconds(interval);
                break;

            case SECONDS:
                this.scheduleBuilder.withIntervalInSeconds(interval);
                break;

            case MINUTES:
                this.scheduleBuilder.withIntervalInMinutes(interval);
                break;

            case HOURS:
                this.scheduleBuilder.withIntervalInHours(interval);
                break;

            case DAYS:
                this.scheduleBuilder.withIntervalInHours((int) TimeUnit.DAYS.toHours(interval));
                break;
        }

        if (repeatTimes > -1) {
            this.scheduleBuilder.withRepeatCount(repeatTimes);
        } else {
            this.scheduleBuilder.repeatForever();
        }

        usedSchedule = true;

        return this;
    }

    @Override
    public IJob cronSchedule(String scheduleString) {
        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(scheduleString));

        return this;
    }

    @Override
    public IJob startAt(Date startTime) {
        this.triggerBuilder.startAt(startTime);

        return this;
    }

    @Override
    public IJob endAt(Date endTime) {
        triggerBuilder.endAt(endTime);

        return this;
    }

    @Override
    public IJob setRecoveryOnFailure(boolean failOver) {
        this.failOver = failOver;

        return this;
    }

    JobDetail buildJob() throws IScheduledJobs.ScheduledJobsException {
        this.jobBuilder.withIdentity(this.name, this.group);

        if (execution != null) {
            this.jobBuilder.ofType(JobExecution.class);
            this.jobBuilder.usingJobData(Execution.class.getName(), execution.getName());
            for (String key : jobInformation.keySet()) {
                if (Execution.class.getName().equals(key)) {
                    throw new IScheduledJobs.ScheduledJobsException(Execution.class.getName() +
                            " is a reserved name and is not allowed as a key in jobInformation!");
                }
                String value = jobInformation.get(key);
                JobDataMap dataMap = new JobDataMap();
                dataMap.put(key, value);
                this.jobBuilder.usingJobData(dataMap);
            }
        }

        this.jobBuilder.requestRecovery(failOver);
        return this.jobBuilder.build();
    }

    Trigger buildTrigger() {
        this.triggerBuilder.withIdentity(name, group);

        if (usedSchedule) {
            this.triggerBuilder.withSchedule(scheduleBuilder);
        }

        return this.triggerBuilder.build();
    }
}
