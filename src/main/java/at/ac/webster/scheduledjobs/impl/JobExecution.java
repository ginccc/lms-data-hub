package at.ac.webster.scheduledjobs.impl;

import at.ac.webster.scheduledjobs.IJob;
import at.ac.webster.scheduledjobs.IJobExecutionFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JobExecution implements Job, Serializable {
    private static final long serialVersionUID = 1560910141752149997L;
    private final IJobExecutionFactory jobExecutionFactory;

    public JobExecution(IJobExecutionFactory jobExecutionFactory) {
        this.jobExecutionFactory = jobExecutionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            Object clazzName = dataMap.get(IJob.Execution.class.getName());
            Class<IJob.Execution> executionClass = (Class<IJob.Execution>) Class.forName(clazzName.toString());
            IJob.Execution execution = jobExecutionFactory.createJob(executionClass);

            Map<String, String> jobInformation = convert(dataMap.getWrappedMap());
            jobInformation.remove(IJob.Execution.class.getName());

            execution.execute(jobInformation);
        } catch (ClassNotFoundException | IJobExecutionFactory.JobExecutionFactory e) {
            throw new JobExecutionException("Cannot instantiate Job of Type: " + IJob.Execution.class.getName(), e);
        }
    }

    private static Map<String, String> convert(final Map<String, Object> wrappedMap) {
        Map<String, String> ret = new HashMap<>();

        for (String key : wrappedMap.keySet()) {
            Object value = wrappedMap.get(key);
            if (value instanceof String) {
                ret.put(key, (String) value);
            }
        }

        return ret;
    }
}
