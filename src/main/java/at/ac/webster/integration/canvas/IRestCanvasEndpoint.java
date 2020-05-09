package at.ac.webster.integration.canvas;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/canvas")
public interface IRestCanvasEndpoint {

    @POST
    @Path("/sync")
    void fetchDataFromCanvas(@QueryParam("token") String token);

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    Response getUser(@QueryParam("token") String token);

    @POST
    @Path("/user/{userId}")
    Response removeUser(@PathParam("userId") String userId);

    @GET
    @Path("/grades/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getGrades(@PathParam("userId") String userId);

    @GET
    @Path("/assignments/{assignmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getAssignment(@PathParam("assignmentId") String assignmentId);
}
