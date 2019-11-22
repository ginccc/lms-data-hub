package at.ac.webster;

import at.ac.webster.model.Event;
import at.ac.webster.model.Interval;
import at.ac.webster.scheduledjobs.IJob;
import at.ac.webster.scheduledjobs.IScheduledJobs;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.Map;

import static at.ac.webster.utilities.RuntimeUtilities.isNullOrEmpty;

@ApplicationScoped
public class EventExecutor implements IEventExecutor {
    private static final String EVENT_GROUP = "eventGroup";
    @Inject
    IScheduledJobs scheduler;

    @Override
    public void scheduleEvent(Event event) throws EventExecutorException {
        try {
            IJob job = scheduler.newJob("event-" + event.getId(), EVENT_GROUP);
            Date startAt = event.getStartAt();
            if (startAt != null) {
                job.startAt(startAt);
            }
            Date endAt = event.getEndAt();
            if (endAt != null) {
                job.endAt(endAt);
            }
            String cronSchedule = event.getCronSchedule();
            if (!isNullOrEmpty(cronSchedule)) {
                job.cronSchedule(cronSchedule);
            }
            Interval interval = event.getInterval();
            if (interval != null) {
                job.interval(interval.getInterval(), interval.getTimeUnit(), interval.getRepeatTimes());
            }
            job.addJobInformation("eventId", event.getId());
            job.jobExecution(EventJob.class);
            scheduler.submitJob(job);
        } catch (IScheduledJobs.ScheduledJobsException e) {
            throw new EventExecutorException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void stopEvent(String eventId) throws IScheduledJobs.ScheduledJobsException {
        scheduler.deleteJob("event-" + eventId, EVENT_GROUP);
    }

    public static class EventJob implements IJob.Execution {
        private Logger log = Logger.getLogger(EventJob.class);

        @Override
        public void execute(Map<String, String> jobInformation) {
            String eventId = jobInformation.get("eventId");
            log.info("event with id " + eventId + " is triggering a bot conversations with a student...");
        }
    }
}
