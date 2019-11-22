package at.ac.webster.scheduledjobs;

import at.ac.webster.scheduledjobs.impl.ScheduledJobs;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class QuartzBootstrap {
    private Logger log = Logger.getLogger(QuartzBootstrap.class);

    @Produces
    @ApplicationScoped
    public IScheduledJobs provideScheduledJobs() {
        try {
            ScheduledJobs scheduledJobs = new ScheduledJobs();
            registerSchedularShutdownHook(scheduledJobs);
            return scheduledJobs;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private void registerSchedularShutdownHook(final ScheduledJobs scheduler) {
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook_QuartzScheduler") {
            @Override
            public void run() {
                try {
                    scheduler.shutdownScheduler(true);
                } catch (Throwable e) {
                    String message = "QuartzScheduler did not stop as expected.";
                    log.error(message);
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        });
    }
}
