package gr.aegean.service;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.stereotype.Service;

import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.entity.User;
import gr.aegean.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void registerUser(User user) {
        if(userRepository.existsUserWithEmail(user.getEmail())) {
            throw new DuplicateResourceException("The provided email already exists");
        }

        userRepository.registerUser(user);
    }

    public void validateUser(User user) {
        validateName(user.getFirstname(), user.getLastname());
        validateEmail(user.getEmail());
        validatePassword(user.getPassword());
    }

    /**
     * Validates the user's firstname or the user's lastname, by verifying that they only contain letters.
     *
     * @throws IllegalArgumentException when either the user's name of the user's lastname
     * contains non-letter characters.
     */
    private void validateName(String firstname, String lastname) {
        if (firstname.length() > 30) {
            throw new IllegalArgumentException("Invalid firstname. Too many characters");
        }

        if(!firstname.matches("^[a-zA-Z]*$")) {
            throw new IllegalArgumentException("Invalid firstname. Name should contain only characters");
        }

        if (lastname.length() > 30) {
            throw new IllegalArgumentException("Invalid lastname. Too many characters");
        }

        if(!lastname.matches("^[a-zA-Z]*$")) {
            throw new IllegalArgumentException("Invalid lastname. Name should contain only characters");
        }
    }

    private void validateEmail(String email) {
        if(email.length() > 50) {
            throw new IllegalArgumentException("Invalid email. Too many characters");
        }

    }

    private void validatePassword(String password) {
        PasswordValidator validator = new PasswordValidator(
                new LengthRule(8, 128),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1)
        );

        RuleResult result = validator.validate(new PasswordData(password));
        if (!result.isValid()) {
            throw new IllegalArgumentException(validator.getMessages(result).get(0));
        }
    }
}
