package at.ac.webster.eddi;

import at.ac.webster.eddi.model.CreateConversation;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/channels/differ")
@RegisterRestClient
public interface IRestDifferEndpoint {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/createConversation")
    Response triggerConversationCreated(CreateConversation createConversation);

    @POST
    @Path("/endBotConversation")
    Response endBotConversation(@QueryParam("intent") String intent,
                                @QueryParam("botUserId") String botUserId,
                                @QueryParam("differConversationId") String differConversationId);
}


