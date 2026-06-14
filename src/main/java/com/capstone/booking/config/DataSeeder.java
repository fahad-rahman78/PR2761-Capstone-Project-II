package com.capstone.booking.config;

import com.capstone.booking.model.Resource;
import com.capstone.booking.model.User;
import com.capstone.booking.repository.ResourceRepository;
import com.capstone.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Inserts a little sample data the first time the app starts, so the UI has
 * something to show and you can book straight away without setting up data
 * by hand. It only seeds when the tables are empty, so restarting the app
 * will not create duplicates.
 *
 * Disabled in the "test" profile - tests create their own data.
 */
@Configuration
@Profile("!test")
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               ResourceRepository resourceRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("Alice Khan", "alice@example.com"));
                userRepository.save(new User("Bob Ahmed", "bob@example.com"));
                userRepository.save(new User("Chitra Roy", "chitra@example.com"));
            }
            if (resourceRepository.count() == 0) {
                // capacity 1 -> exclusive booking (the double-booking demo)
                resourceRepository.save(new Resource("Meeting Room A", "meeting_room", 1));
                resourceRepository.save(new Resource("Dr. Hasan (Clinic)", "doctor", 1));
                // capacity 3 -> shows the capacity feature working
                resourceRepository.save(new Resource("Group Study Pod", "study_pod", 3));
            }
        };
    }
}
