package io.harman.sidating_app_be.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.harman.sidating_app_be.model.UserProfile;

@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final List<UserProfile> userProfileDB = new ArrayList<>();

    @Override
    public UserProfile createUserProfile(UserProfile userProfile) {
        userProfile.setId(UUID.randomUUID());
        userProfileDB.add(userProfile);
        return userProfile;
    }

    @Override
    public List<UserProfile> getAllUserProfile() {
        return userProfileDB;
    }

    @Override
    public UserProfile getUserProfile(UUID id) {
        for (UserProfile userProfile : userProfileDB) {
            if (id != null && id.equals(userProfile.getId())) {
                return userProfile;
            }
        }
        return null;
    }

    @Override
    public UserProfile updateUserProfile(UserProfile updatedUserProfile) {
        for (int i = 0; i < userProfileDB.size(); i++) {
            if (userProfileDB.get(i).getId().equals(updatedUserProfile.getId())) {
                userProfileDB.set(i, updatedUserProfile);
                return updatedUserProfile;
            }
        }
        return null;
    }
    
    @Override
    public UserProfile deleteProfile(UUID id) {
        var iterator = userProfileDB.iterator();
        while (iterator.hasNext()) {
            UserProfile userProfile = iterator.next();
            if (id != null && id.equals(userProfile.getId())) {
                iterator.remove();
                return userProfile;
            }
        }
        return null;
    }
    
    @Override
    public Map<String, Object> calculateMatchScore(UUID userId1, UUID userId2) {
        UserProfile user1 = getUserProfile(userId1);
        UserProfile user2 = getUserProfile(userId2);

        if (user1 == null || user2 == null) {
            return null;
        }

        int score;
        // Pengecekan gender
        if (Objects.equals(user1.getGender(), user2.getGender())) {
            // DIUBAH: Menghasilkan skor acak 0-49 menggunakan Math.random()
            score = (int) (Math.random() * 50); 
        } else {
            // DIUBAH: Menghasilkan skor acak 50-100 menggunakan Math.random()
            score = (int) (Math.random() * 51) + 50; 
        }

        String message;
        String imageUrl;

        if (score <= 50) {
            message = "Sebatas Teman";
            imageUrl = "https://image.idntimes.com/post/20250707/1000267415_0946f347-af1f-4e17-99fd-ac4c6d8f0b33.jpg";
        } else if (score <= 70) {
            message = "Teman Sejati";
            imageUrl = "https://pbs.twimg.com/media/FddMGMGVIAA1TZK.jpg";
        } else if (score <= 90) {
            message = "Cocok";
            imageUrl = "https://i.pinimg.com/736x/ce/a8/9f/cea89fdbabc6429cc0cf192245ad75a5.jpg";
        } else {
            message = "Cinta Abadi";
            imageUrl = "https://png.pngtree.com/background/20220714/original/pngtree-romantic-love-design-with-pink-picture-image_1606181.jpg";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("message", message);
        result.put("imageUrl", imageUrl);

        return result;
    }
}