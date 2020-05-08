package at.ac.webster.integration.canvas.job;

import edu.ksu.canvas.model.Grade;
import io.quarkus.scheduler.Scheduled;

import java.io.IOException;

public interface ICanvasDataManager {
    @Scheduled(every = "1m")
    void checkCanvasForEachStudent();

    void fetchAndUpdateDataFromCanvas(String token) throws IOException;

    Grade fetchGrades(String userId);
}
