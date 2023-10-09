package gr.aegean.config;

import gr.aegean.deserializer.DvdGenreDeserializer;
import gr.aegean.deserializer.UserRoleDeserializer;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.model.user.UserRole;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Configuration class that creates an instance of the Jackson ObjectMapper with custom deserializers
 * for the DvdGenre and UserRole enums.
 */
@Configuration
public class DeserializerConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addDeserializer(DvdGenre.class, new DvdGenreDeserializer());
        module.addDeserializer(UserRole.class, new UserRoleDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
