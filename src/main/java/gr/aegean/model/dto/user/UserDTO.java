package gr.aegean.model.dto.user;


import gr.aegean.model.user.UserRole;

public record UserDTO(Integer id,
                      String firstname,
                      String lastname,
                      String username,
                      String email,
                      UserRole role) {
}
