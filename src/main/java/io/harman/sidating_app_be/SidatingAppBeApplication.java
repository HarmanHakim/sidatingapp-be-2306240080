package io.harman.sidating_app_be;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.model.Post;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.PostRepository;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.service.UserProfileService;

@SpringBootApplication
@EnableJpaAuditing
public class SidatingAppBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SidatingAppBeApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDatabase(
            UserProfileService userProfileService,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository) {

        return args -> {
            System.out.println("🚀 Starting data seeding...");

            // skip seeding jika data sudah ada
            if (userProfileRepository.count() > 0 || postRepository.count() > 0) {
                System.out.println("✅ Data already exists, skipping seeding.");
                return;
            }

            Faker faker = new Faker(Locale.of("id_ID"));
            Random rnd = new Random();

            // ======== Generate 100 dummy users ==========
            List<UserProfile> users = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Name name = faker.name();
                LocalDate birthdate = Instant.ofEpochMilli(faker.date().birthday(18, 40).getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                UserProfile user = UserProfile.builder()
                        .name(name.fullName())
                        .nickname(name.firstName())
                        .email(name.username() + i + "@apap.id")
                        .phoneNumber(faker.phoneNumber().cellPhone())
                        .location(faker.address().city())
                        .bio(faker.lorem().sentence(10))
                        .interests(faker.witcher().school())
                        .gender(i % 2 == 0 ? "MALE" : "FEMALE")
                        .birthdate(birthdate)
                        .isActive(true)
                        .build();

                users.add(user);
            }

            List<UserProfile> savedUsers = userProfileRepository.saveAllAndFlush(users);

            // ======== Generate 100 dummy posts ==========
            List<Post> posts = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                UserProfile owner = savedUsers.get(rnd.nextInt(savedUsers.size()));

                Post post = Post.builder()
                        .userProfile(owner)
                        .imageUrl(faker.internet().avatar())
                        .caption(faker.lorem().sentence(6))
                        .isActive(true)
                        .build();

                posts.add(post);
            }

            postRepository.saveAllAndFlush(posts);

            System.out.println("✅ Seeded " + savedUsers.size() + " users and " + posts.size() + " posts.");
        };
    }

}
