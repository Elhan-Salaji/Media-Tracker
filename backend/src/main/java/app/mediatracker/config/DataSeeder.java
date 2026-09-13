package app.mediatracker.config;

import app.mediatracker.feature.library.model.UserLibraryEntry;
import app.mediatracker.feature.library.repo.UserLibraryEntryRepository;
import app.mediatracker.feature.user.model.User;
import app.mediatracker.feature.user.repo.UserRepository;
import app.mediatracker.seed.DemoMediaData;
import app.mediatracker.seed.DemoUserData;

import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds demo users and library entries into MongoDB on application startup.
 *
 * Behavior: Runs only when the users collection is empty. For each generated demo user,
 * a set of demo media library entries is created and associated with the user's ID.
 */
@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedDatabase(UserRepository userRepository, UserLibraryEntryRepository userLibraryEntryRepository, DemoUserData userTestDataLoader, DemoMediaData mediaTestDataLoader) {
        return args -> {
            //only if User Database is empty!
            if (userRepository.count() == 0) {

                List<User> userTestData = userTestDataLoader.createDemoUsers();
                
                for(User user : userTestData) {
                    userRepository.save(user); //save users
                    ObjectId userId = user.getId();
                    List<UserLibraryEntry> mediaTestData = mediaTestDataLoader.createDemoMediaData();
                    for(UserLibraryEntry mediaEntry : mediaTestData) {
                        mediaEntry.setUserId(userId);
                        userLibraryEntryRepository.save(mediaEntry);
                    }
                }
            }
        };
    }
}
