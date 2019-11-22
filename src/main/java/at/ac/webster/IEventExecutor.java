package at.ac.webster;

import at.ac.webster.model.Event;
import at.ac.webster.scheduledjobs.IScheduledJobs;

public interface IEventExecutor {
    void scheduleEvent(Event event) throws EventExecutorException;

    void stopEvent(String eventId) throws IScheduledJobs.ScheduledJobsException;

    class EventExecutorException extends Exception {
        EventExecutorException(String message, Exception e) {
            super(message, e);
        }
    }
}
