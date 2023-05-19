package gr.aegean.model.user;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userID;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private UserRole role;

    public User(String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String firstname, String lastname, String email, String password, UserRole role) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
