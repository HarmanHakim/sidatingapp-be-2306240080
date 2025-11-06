package io.harman.sidating_app_be.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(role);
        assertNull(role.getId());
        assertNull(role.getRoleName());
        assertNull(role.getUsers());
    }

    @Test
    void testAllArgsConstructor() {
        List<UserProfile> users = new ArrayList<>();
        Role roleWithArgs = new Role(1L, "Admin", users);

        assertEquals(1L, roleWithArgs.getId());
        assertEquals("Admin", roleWithArgs.getRoleName());
        assertEquals(users, roleWithArgs.getUsers());
    }

    @Test
    void testSettersAndGetters() {
        role.setId(2L);
        role.setRoleName("User");
        
        List<UserProfile> users = new ArrayList<>();
        role.setUsers(users);

        assertEquals(2L, role.getId());
        assertEquals("User", role.getRoleName());
        assertEquals(users, role.getUsers());
    }

    @Test
    void testRoleNameSetter() {
        role.setRoleName("Moderator");
        assertEquals("Moderator", role.getRoleName());
    }

    @Test
    void testUsersSetter() {
        List<UserProfile> users = new ArrayList<>();
        UserProfile user1 = new UserProfile();
        user1.setName("Test User");
        users.add(user1);

        role.setUsers(users);

        assertNotNull(role.getUsers());
        assertEquals(1, role.getUsers().size());
        assertEquals("Test User", role.getUsers().get(0).getName());
    }
}
