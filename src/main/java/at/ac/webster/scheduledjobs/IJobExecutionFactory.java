package at.ac.webster.scheduledjobs;

public interface IJobExecutionFactory {
    IJob.Execution createJob(Class<IJob.Execution> jobClass) throws JobExecutionFactory;

    public class JobExecutionFactory extends Exception {
        private static final long serialVersionUID = -5167145521928002353L;

        public JobExecutionFactory(String message, Exception e) {
            super(message, e);
        }
    }
}
