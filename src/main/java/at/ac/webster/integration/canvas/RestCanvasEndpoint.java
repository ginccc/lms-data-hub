package at.ac.webster.integration.canvas;

import at.ac.webster.integration.canvas.job.ICanvasDataManager;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import java.io.IOException;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
public class RestCanvasEndpoint implements IRestCanvasEndpoint {

    @Inject
    ICanvasDataManager canvasDataManager;

    @Override
    public void fetchDataFromCanvas(String token) {
        try {
            canvasDataManager.fetchAndUpdateDataFromCanvas(token);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Response getGrades(String userId) {
        return Response.ok(canvasDataManager.fetchGrades(userId)).build();
    }
}