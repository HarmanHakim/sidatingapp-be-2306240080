package io.harman.sidating_app_be;

import java.time.ZoneId;
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

import io.harman.sidating_app_be.dto.post.CreatePostDto;
import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.service.PostService;
import io.harman.sidating_app_be.service.UserProfileService;

@SpringBootApplication
@EnableJpaAuditing
public class SidatingAppBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SidatingAppBeApplication.class, args);
    }

    @Bean
    public CommandLineRunner createDummyData(UserProfileService userProfileService, PostService postService) {
        return args -> {
            System.out.println("Generating dummy user profiles and posts...");
            Faker faker = new Faker(new Locale("id_ID"));

            // Create 100 user profiles and keep references
            List<UserProfile> createdUsers = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Name name = faker.name();
                CreateUserDto createUserDto = CreateUserDto.builder()
                        .name(name.fullName())
                        .nickname(name.firstName())
                        .email(name.username() + "@apap.id")
                        .phoneNumber(faker.phoneNumber().phoneNumber())
                        .location(faker.address().fullAddress())
                        .bio(faker.witcher().quote())
                        .interests(faker.witcher().school())
                        .gender(i % 2 == 0 ? "MALE" : "FEMALE")
                        .birthdate(faker.date().birthday(20, 40)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate())
                        .build();

                UserProfile created = userProfileService.createUserProfile(createUserDto);
                if (created != null) createdUsers.add(created);
            }

            // Create 100 posts, associate randomly with created users
            Random rnd = new Random();
            for (int i = 0; i < 100; i++) {
                if (createdUsers.isEmpty()) break;
                UserProfile u = createdUsers.get(rnd.nextInt(createdUsers.size()));

                CreatePostDto createPostDto = CreatePostDto.builder()
                        .userProfileId(u.getId())
                        .imageUrl("https://picsum.photos/seed/" + UUID.randomUUID() + "/800/600")
                        .caption(faker.lorem().sentence())
                        .build();

                postService.createPost(createPostDto);
            }

            System.out.println("Dummy data generation complete.");
        };
    }
}
