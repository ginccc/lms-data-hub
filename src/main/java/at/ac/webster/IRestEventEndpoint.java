package at.ac.webster;

import at.ac.webster.model.Event;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/event")
public interface IRestEventEndpoint {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createEvent(Event event);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Event> readEvents();

    @DELETE
    @Path("{eventId}")
    Response deleteEvent(@PathParam("eventId") String eventId);
}
