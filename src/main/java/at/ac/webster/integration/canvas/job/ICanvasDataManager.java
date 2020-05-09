package at.ac.webster.integration.canvas.job;

import edu.ksu.canvas.model.Grade;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.model.assignment.Assignment;
import io.quarkus.scheduler.Scheduled;

import java.io.IOException;

public interface ICanvasDataManager {
    @Scheduled(every = "1m")
    void checkCanvasForEachStudent();

    void fetchAndUpdateDataFromCanvas(String token) throws IOException;

    Grade fetchGrades(String userId);

    User getCurrentUser(String token);

    void removeUser(String userId);

    Assignment fetchAssignment(String assignmentId);
}
