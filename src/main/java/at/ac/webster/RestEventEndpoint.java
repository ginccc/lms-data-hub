package at.ac.webster;

import at.ac.webster.model.Event;
import at.ac.webster.scheduledjobs.IScheduledJobs;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.NoLogWebApplicationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.CONFLICT;

@ApplicationScoped
public class RestEventEndpoint implements IRestEventEndpoint {
    private Logger log = Logger.getLogger(RestEventEndpoint.class);
    private IEventExecutor eventExecutor;
    private final MongoCollection<Event> eventCollection;

    @Inject
    public RestEventEndpoint(MongoClient mongoClient, IEventExecutor eventExecutor) {
        this.eventExecutor = eventExecutor;

        eventCollection = mongoClient.getDatabase("lms-data-hub").getCollection("events", Event.class);
    }

    @Override
    public Response createEvent(Event event) {
        try {
            if (eventCollection.find(new Document("_id", event.getId())).first() != null) {
                Response errorResponse = Response.status(CONFLICT)
                        .entity("event with id " + event.getId() + " already exists")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
                throw new NoLogWebApplicationException(errorResponse);
            }

            eventCollection.insertOne(event);
            eventExecutor.scheduleEvent(event);
            return Response.ok().build();

        } catch (IEventExecutor.EventExecutorException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new InternalServerErrorException();
        }
    }

    @Override
    public List<Event> readEvents() {
        List<Event> ret = new LinkedList<>();
        FindIterable<Event> events = eventCollection.find();
        for (Event event : events) {
            ret.add(event);
        }
        return ret;
    }

    @Override
    public Response deleteEvent(String eventId) {
        try {
            if (eventCollection.find(new Document("_id", eventId)).first() == null) {
                throw new NotFoundException("event with id" + eventId + " does not exist");
            }
            eventCollection.deleteOne(new Document("_id", eventId));
            eventExecutor.stopEvent(eventId);
            return Response.ok().build();
        } catch (IScheduledJobs.ScheduledJobsException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new InternalServerErrorException();
        }
    }
}
