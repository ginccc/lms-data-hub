package at.ac.webster;

import at.ac.webster.eddi.IRestDifferEndpoint;
import at.ac.webster.eddi.model.CreateConversation;
import at.ac.webster.model.Event;
import at.ac.webster.model.Interval;
import at.ac.webster.scheduledjobs.IJob;
import at.ac.webster.scheduledjobs.IScheduledJobs;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static at.ac.webster.utilities.RuntimeUtilities.isNullOrEmpty;

@ApplicationScoped
public class EventExecutor implements IEventExecutor {
    private static final String EVENT_GROUP = "eventGroup";
    public static final String WEBSTER_BOT_USER_ID = "222020e2-7c00-41d0-ab1b-52f2531c95d5";
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

        @Inject
        @RestClient
        IRestDifferEndpoint differEndpoint;

        @Override
        public void execute(Map<String, String> jobInformation) {
            String eventId = jobInformation.get("eventId");
            log.info("event with id " + eventId + " is triggering a bot conversations with a student...");

            differEndpoint.triggerConversationCreated(new CreateConversation("Semester Party coming...",
                    WEBSTER_BOT_USER_ID, Arrays.asList("222020e2-7c00-41d0-ab1b-52f2531c95d5", "f2301bae-92fb-4148-af62-59e98f3d04b5")));
        }
    }
}
