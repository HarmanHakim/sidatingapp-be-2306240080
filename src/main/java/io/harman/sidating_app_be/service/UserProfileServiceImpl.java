package io.harman.sidating_app_be.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Optional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.dto.user.CreateUserDto;
import io.harman.sidating_app_be.dto.user.UpdateUserDto;
import io.harman.sidating_app_be.model.UserProfile;
import io.harman.sidating_app_be.repository.UserProfileRepository;
import io.harman.sidating_app_be.dto.user.ReadUserProfileDto;
import java.time.Period;


@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Override
    public int getRandomMatchScore(UUID userId1, UUID userId2) {
        // Cari objek UserProfile berdasarkan ID
        Optional<UserProfile> user1Opt = userProfileRepository.findById(userId1);
        Optional<UserProfile> user2Opt = userProfileRepository.findById(userId2);
        // kalau ada yang null, return 0
        if (userId1 == null || userId2 == null) {
            return 0;
        }

        // kalau sama (match dengan diri sendiri), return 0
        if (userId1.equals(userId2)) {
            return 0;
        }
        // Periksa apakah kedua pengguna ditemukan
        if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
            return 0; // Mengembalikan 0 jika salah satu pengguna tidak ditemukan
        }

        // kalau salah satu user tidak ditemukan, return 0
        if (userId1 == null || userId2 == null) {
            return 0;
        }


        UserProfile user1 = user1Opt.get();
        UserProfile user2 = user2Opt.get();

        Random random = new Random();
        if (user1.getGender().equals(user2.getGender())) {
            // Jika gender sama, skor random antara 0-49
            return random.nextInt(50);
        } else {
            // Jika gender berbeda, skor random antara 50-100
            return 50 + random.nextInt(51);
        }
    }

    @Override
    public String getMatchMessage(int score) {
        if (score <= 50) {
            return "Sebatas Teman";
        } else if (score <= 70) {
            return "Teman Sejati";
        } else if (score <= 90) {
            return "Cocok";
        } else {
            return "Cinta Abadi";
        }
    }

    @Override
    public String getMatchImage(int score) {
        if (score <= 50) {
            return "https://image.idntimes.com/post/20250707/1000267415_0946f347-af1f-4e17-99fd-ac4c6d8f0b33.jpg";
        } else if (score <= 70) {
            return "https://pbs.twimg.com/media/FddMGmGVIAA1TZK.jpg";
        } else if (score <= 90) {
            return "https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg";
        } else {
            return "https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg";
        }
    }


    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Override
    public UserProfile createUserProfile(CreateUserDto dto) {
        UserProfile userProfile = UserProfile.builder()
            .id(UUID.randomUUID())
            .name(dto.getName())
            .nickname(dto.getNickname())
            .birthdate(dto.getBirthdate())
            .hobbies(dto.getHobbies())
            .gender(dto.getGender())
            .location(dto.getLocation())
            .bio(dto.getBio())
            .email(dto.getEmail())
            .phoneNumber(dto.getPhoneNumber())
            .interests(dto.getInterests())
            .build();
        return userProfileRepository.save(userProfile);
    }

    @Override
    public List<UserProfile> getAllUserProfile() {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        System.out.println("Fetched " + userProfiles.size() + " user profiles from the database.");
        return userProfiles;
    }

    public ReadUserProfileDto mapToReadUserProfileDto(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        // Menghitung umur dari tanggal lahir
        LocalDate today = LocalDate.now();
        int age = Period.between(userProfile.getBirthdate(), today).getYears();
        
        // Menentukan kategori umur
        String ageGroup;
        if (age >= 18 && age <= 25) {
            ageGroup = "18-25";
        } else if (age >= 26 && age <= 35) {
            ageGroup = "26-35";
        } else if (age >= 36 && age <= 45) {
            ageGroup = "36-45";
        } else {
            ageGroup = "45+";
        }

        return ReadUserProfileDto.builder()
            .id(userProfile.getId())
            .name(userProfile.getName())
            .nickname(userProfile.getNickname())
            .birthdate(userProfile.getBirthdate())
            .age(age)
            .ageGroup(ageGroup)
            .hobbies(userProfile.getHobbies())
            .gender(userProfile.getGender())
            .location(userProfile.getLocation())
            .bio(userProfile.getBio())
            .email(userProfile.getEmail())
            .phoneNumber(userProfile.getPhoneNumber())
            .interests(userProfile.getInterests())
            .createdAt(userProfile.getCreatedAt())
            .updatedAt(userProfile.getUpdatedAt())
            .isActive(userProfile.isActive())
            .build();
    }

    @Override
    public UserProfile getUserProfile(UUID id) {
        return userProfileRepository.findById(id).orElse(null);
    }

    @Override
    public UserProfile updateUserProfile(UpdateUserDto updateUserDto) {
        UserProfile userProfile = userProfileRepository.findById(updateUserDto.getId()).orElse(null);
        
        if (userProfile == null) return null;
        
        userProfile = userProfile.toBuilder()
            .id(updateUserDto.getId())
            .name(updateUserDto.getName())
            .nickname(updateUserDto.getNickname())
            .birthdate(updateUserDto.getBirthdate())
            .hobbies(updateUserDto.getHobbies())
            .gender(updateUserDto.getGender())
            .location(updateUserDto.getLocation())
            .bio(updateUserDto.getBio())
            .email(updateUserDto.getEmail())
            .phoneNumber(updateUserDto.getPhoneNumber())
            .interests(updateUserDto.getInterests())
            .isActive(updateUserDto.isActive())
            .build();

            return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile deleteProfile(UUID id) {
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findById(id);
        if (optionalUserProfile.isEmpty()) {
            return null;
        }
        UserProfile userProfile = optionalUserProfile.get();
        // Hibernate akan menjalankan SQLDelete, tidak menghapus permanen
        userProfileRepository.delete(userProfile);
        return userProfile;
    }

    @Override
    public List<UserProfile> searchUserProfilesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return userProfileRepository.findAll(); // Tampilkan semua jika input kosong
        }
        return userProfileRepository.findByNameContainingIgnoreCase(name);
    }



}

