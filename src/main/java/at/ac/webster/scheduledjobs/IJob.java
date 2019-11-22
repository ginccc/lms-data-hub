package at.ac.webster.scheduledjobs;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface IJob {

    /**
     * Sets the class which will be executed each time the defined job gets triggered.
     * NOTE: The class will be newly instantiated on each job execution,
     * so all stored data in member variables will get lost respectively and therefore cannot be used on the next job execution.
     *
     * @param execution A class implementing the interface IJob.Execution. Must be Serializable.
     * @return the current instance of this Job
     */
    IJob jobExecution(Class<? extends Execution> execution);

    String getName();

    String getGroup();

    void addJobInformation(String key, String str);

    /**
     * Define that this job should execute more than once based reoccurring interval.
     *
     * @param interval    define the interval based on the TimeUnit of the second param
     * @param timeUnit    the timeUnit to be used for the first parameter
     * @param repeatTimes how many times this job should be repeated after the first execution (-1 == repeat forever)
     * @return the current instance of this Job
     * @throws IScheduledJobs.ScheduledJobsException
     */
    IJob interval(int interval, TimeUnit timeUnit, int repeatTimes) throws IScheduledJobs.ScheduledJobsException;


    /**
     * Define an interval in which this job should be triggered.
     * NOTE: If interval(int, TimeUnit, int) is set, cronSchedule will be ignored.
     *
     * @param scheduleString unix-like cron expressions
     * @return the current instance of this Job
     */
    IJob cronSchedule(String scheduleString);

    /**
     * Define the point in time at which the job should start executing.
     *
     * @param startTime point in time
     * @return the current instance of this Job
     */
    IJob startAt(Date startTime);

    /**
     * Define the point in time at which the job should end executing.
     * The parameter overrules the interval information!
     *
     * @param endTime point in time
     * @return the current instance of this Job
     */
    IJob endAt(Date endTime);

    /**
     * In case of a cluster scenario this parameter defines whether this Job should be recovered by another Node
     * in case of a failure.
     *
     * @param failOver if this job should be recovered in case of a failure (default: true)
     * @return the current instance of this Job
     */
    IJob setRecoveryOnFailure(boolean failOver);

    /**
     * Interface to be implemented by all classes which will be used by the underlying framework to execute the job.
     * The execute method will be called each time the job gets triggered.
     */
    public interface Execution extends Serializable {
        public void execute(final Map<String, String> jobInformation);
    }
}
