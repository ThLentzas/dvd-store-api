package gr.aegean.service;

import gr.aegean.model.user.User;
import gr.aegean.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Service class for user-related functionality.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * Registers a new user.
     * @param user the user to be registered.
     * @return the ID of the newly registered user for the URI
     */
    public Integer registerUser(User user) {
        return userRepository.registerUser(user);
    }
}
