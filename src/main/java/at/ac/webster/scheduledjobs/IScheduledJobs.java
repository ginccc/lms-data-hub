package at.ac.webster.scheduledjobs;

public interface IScheduledJobs {
    IJob newJob(String name);

    IJob newJob(String name, String group);

    void submitJob(IJob job) throws ScheduledJobsException;

    boolean containsJob(String name) throws ScheduledJobsException;

    boolean containsJob(String name, String group) throws ScheduledJobsException;

    boolean deleteJob(String name) throws ScheduledJobsException;

    boolean deleteJob(String name, String group) throws ScheduledJobsException;

    void shutdownScheduler(boolean gracefulShutdown) throws ScheduledJobsException;

    class ScheduledJobsException extends Exception {
        private static final long serialVersionUID = -406196065786216841L;

        public ScheduledJobsException(String message) {
            super(message);
        }

        public ScheduledJobsException(String message, Exception e) {
            super(message, e);
        }
    }
}
