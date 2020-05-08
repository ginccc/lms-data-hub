package at.ac.webster.integration.canvas.model;

import edu.ksu.canvas.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AdvancedUser extends User {
    private String token;
}
