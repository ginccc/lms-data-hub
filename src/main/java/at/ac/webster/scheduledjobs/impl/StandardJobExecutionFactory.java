package at.ac.webster.scheduledjobs.impl;

import at.ac.webster.scheduledjobs.IJob;
import at.ac.webster.scheduledjobs.IJobExecutionFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StandardJobExecutionFactory implements IJobExecutionFactory {

    public IJob.Execution createJob(Class<IJob.Execution> jobClass) throws JobExecutionFactory {
        try {
            return jobClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JobExecutionFactory(e.getLocalizedMessage(), e);
        }
    }
}
