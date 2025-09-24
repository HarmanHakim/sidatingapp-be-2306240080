package io.harman.sidating_app_be;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.service.UserProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.ZoneId;
import java.util.Locale;

@SpringBootApplication
@EnableJpaAuditing
public class SidatingAppBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SidatingAppBeApplication.class, args);
    }

    @Bean
    public CommandLineRunner createDummyData(UserProfileService userProfileService) {
        return args -> {
            System.out.println("Generating dummy user profiles...");
            Faker faker = new Faker(new Locale("id_ID"));

            for (int i = 0; i < 10; i++) {
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
                        .birthdate(faker.date().birthday(20, 25)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate())
                        .build();

                userProfileService.createUserProfile(createUserDto);
            }

            System.out.println("Dummy data generation complete.");
        };
    }
}
