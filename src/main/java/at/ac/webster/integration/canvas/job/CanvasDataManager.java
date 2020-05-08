package at.ac.webster.integration.canvas.job;

import at.ac.webster.EventExecutor;
import at.ac.webster.integration.canvas.model.AdvancedAssignment;
import at.ac.webster.integration.canvas.model.AdvancedUser;
import at.ac.webster.serialization.IJsonSerialization;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.interfaces.AssignmentReader;
import edu.ksu.canvas.interfaces.CourseReader;
import edu.ksu.canvas.interfaces.EnrollmentReader;
import edu.ksu.canvas.interfaces.UserReader;
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.Enrollment;
import edu.ksu.canvas.model.Grade;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.model.assignment.Assignment;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.GetEnrollmentOptions;
import edu.ksu.canvas.requestOptions.ListCourseAssignmentsOptions;
import edu.ksu.canvas.requestOptions.ListUserCoursesOptions;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import org.bson.Document;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static at.ac.webster.utilities.RuntimeUtilities.convertToLocalDate;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@ApplicationScoped
public class CanvasDataManager implements ICanvasDataManager {
    private final Logger log = Logger.getLogger(EventExecutor.EventJob.class);

    private static final String canvasBaseUrl = "https://canvas.instructure.com";
    private static final String DATABASE_NAME = "lms-data-hub";
    private static final String COLLECTION_CANVAS_USERS = "canvas_users";
    private static final String COLLECTION_CANVAS_COURSES = "canvas_courses";
    private static final String COLLECTION_CANVAS_ASSIGNMENTS = "canvas_assignments";
    private static final String COLLECTION_CANVAS_ENROLLMENTS = "canvas_enrollments";

    private final MongoCollection<AdvancedUser> canvasUsers;
    private final MongoCollection<Course> canvasCourses;
    private final MongoCollection<AdvancedAssignment> canvasAssignments;
    private final MongoCollection<Enrollment> canvasEnrollments;
    private final Mailer mailer;
    private final IJsonSerialization jsonSerialization;
    private final CanvasApiFactory apiFactory = new CanvasApiFactory(canvasBaseUrl);

    @Inject
    public CanvasDataManager(MongoClient mongoClient, Mailer mailer, IJsonSerialization jsonSerialization) {
        canvasUsers = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_CANVAS_USERS, AdvancedUser.class);
        canvasCourses = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_CANVAS_COURSES, Course.class);
        canvasAssignments = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_CANVAS_ASSIGNMENTS, AdvancedAssignment.class);
        canvasEnrollments = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_CANVAS_ENROLLMENTS, Enrollment.class);
        this.mailer = mailer;
        this.jsonSerialization = jsonSerialization;
    }


    @Override
    @Scheduled(every = "1m")
    public void checkCanvasForEachStudent() {
        FindIterable<AdvancedUser> users = canvasUsers.find();
        for (AdvancedUser user : users) {
            String token = user.getToken();
            if (token != null && !"".equals(token)) {
                try {
                    fetchAndUpdateDataFromCanvas(token);
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            } else {
                log.warn("No token defined for user with ID " + user.getId());
            }
        }
    }

    @Override
    public void fetchAndUpdateDataFromCanvas(String token) throws IOException {
        OauthToken oauthToken = new NonRefreshableOauthToken(token);
        var assignmentReader = apiFactory.getReader(AssignmentReader.class, oauthToken);
        var enrollmentReader = apiFactory.getReader(EnrollmentReader.class, oauthToken);
        var courseReader = apiFactory.getReader(CourseReader.class, oauthToken);

        var thisUser = fetchUser(apiFactory, oauthToken);

        if (thisUser.isPresent()) {
            User user = thisUser.get();
            String userId = storeUser(token, user);

            var userEnrollments = fetchEnrollments(enrollmentReader, userId);
            storeEnrollments(userEnrollments);

            var courses = fetchCourses(courseReader, userId);
            storeCourses(assignmentReader, courses);

            log.info("Fetch from Canvas completed for User " + user.getName() + " " + user.getId());
        }
    }

    @Override
    public Grade fetchGrades(String userId) {
        Enrollment enrollment = canvasEnrollments.find(new Document("userId", userId)).first();
        return enrollment != null ? enrollment.getGrades() : null;
    }

    private Optional<User> fetchUser(CanvasApiFactory apiFactory, OauthToken oauthToken) throws IOException {
        UserReader userReader = apiFactory.getReader(UserReader.class, oauthToken);
        return userReader.showUserDetails("self");
    }

    private String storeUser(String token, User user) {
        String userId = String.valueOf(user.getId());
        canvasUsers.updateOne(new Document("id", userId).append("token", token), new Document("$setOnInsert", user), new UpdateOptions().upsert(true));
        return userId;
    }

    private List<Course> fetchCourses(CourseReader courseReader, String userId) throws IOException {
        ListUserCoursesOptions listUserCoursesOptions = new ListUserCoursesOptions(userId);
        return courseReader.listUserCourses(listUserCoursesOptions);
    }

    private void storeCourses(AssignmentReader assignmentReader, List<Course> courses) throws IOException {

        for (Course course : courses) {
            Document query = new Document("id", course.getId());
            canvasCourses.updateOne(query, new Document("$setOnInsert", course), new UpdateOptions().upsert(true));

            List<Assignment> assignments = assignmentReader.
                    listCourseAssignments(new ListCourseAssignmentsOptions(course.getId().toString()));
            for (Assignment assignment : assignments) {
                query = new Document("_id", assignment.getId());
                var currentAssignmentData = canvasAssignments.find(query).first();
                var newAssignment = jsonSerialization.deserialize(jsonSerialization.serialize(assignment), AdvancedAssignment.class);
                if (currentAssignmentData != null) {
                    newAssignment.setUserBeenInformed(currentAssignmentData.isUserBeenInformed());
                }
                setAssignment(query, newAssignment, currentAssignmentData == null);

                if (assignment.getDueAt() != null && !newAssignment.isUserBeenInformed()) {
                    var now = LocalDate.now();
                    LocalDate twoWeeksFromNow = now.plusWeeks(2);
                    LocalDate assignmentDueDate = convertToLocalDate(assignment.getDueAt());
                    if (now.isBefore(assignmentDueDate) && twoWeeksFromNow.isAfter(assignmentDueDate)) {
                        informStudentAboutEvent(course);
                        newAssignment.setUserBeenInformed(true);
                        setAssignment(query, newAssignment, false);
                    }
                }
            }
        }
    }

    private void setAssignment(Document query, AdvancedAssignment assignmentUpdate, boolean isFirstTime) {
        if (isFirstTime) {
            canvasAssignments.insertOne(assignmentUpdate);
        } else {
            canvasAssignments.updateOne(query, new Document("$set", assignmentUpdate), new UpdateOptions().upsert(true));
        }
    }

    private void informStudentAboutEvent(Course course) {
        var user = canvasEnrollments.find(new Document("courseId", course.getId())).first().getUser();

        if (user != null) {
            var name = user.getName();
            var firstName = name.substring(0, name.indexOf(" "));
            var subject = "There is an upcoming assignment.. ";
            var body = String.format("Hi %s! Would you like to find someone for help? Let's talk here: <link>   Yours Truly, Webster Bot", firstName);
            String sendTo = user.getLoginId();
            mailer.send(Mail.withText(sendTo, subject, body));
            log.info("Email about Assignment was send to " + sendTo);
        }
    }

    private List<Enrollment> fetchEnrollments(EnrollmentReader enrollmentReader, String userId) throws IOException {
        GetEnrollmentOptions enrollmentOptions = new GetEnrollmentOptions(userId);
        return enrollmentReader.getUserEnrollments(enrollmentOptions);
    }

    private void storeEnrollments(List<Enrollment> userEnrollments) {
        for (Enrollment enrollment : userEnrollments) {
            Document query = new Document("_id", enrollment.getId());
            Enrollment currentEnrollmentData = canvasEnrollments.find(query).first();
            if (currentEnrollmentData == null) {
                canvasEnrollments.insertOne(enrollment);
            } else {
                canvasEnrollments.updateOne(query, new Document("$set", enrollment), new UpdateOptions().upsert(true));
            }

            if (enrollment.getGrades() != null &&
                    currentEnrollmentData != null &&
                    currentEnrollmentData.getGrades() != null &&
                    enrollment.getGrades().getFinalScore() != null &&
                    !enrollment.getGrades().getFinalScore().equals(currentEnrollmentData.getGrades().getFinalScore())) {
                informStudentAboutGrades(enrollment);
            }
        }
    }

    private void informStudentAboutGrades(Enrollment enrollment) {
        var user = enrollment.getUser();
        var sendTo = user.getLoginId();
        var name = user.getName();
        var firstName = name.substring(0, name.indexOf(" "));
        var subject = "Your Grades are here.. ";
        var body = String.format("Hi %s! Let's talk about it. Click here: <link>  Yours Truly, Webster Bot", firstName);
        mailer.send(Mail.withText(sendTo, subject, body));
        log.info("Email about Grades was send to " + sendTo);
    }
}
