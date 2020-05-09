package at.ac.webster.integration.canvas.model;

import edu.ksu.canvas.model.assignment.Assignment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AdvancedAssignment extends Assignment {
    private boolean isUserBeenInformed = false;
    private String assignmentId;
}
