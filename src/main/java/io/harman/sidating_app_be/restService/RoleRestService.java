package io.harman.sidating_app_be.restservice;

import java.util.List;

import io.harman.sidating_app_be.model.Role;

public interface RoleRestService {
    List<Role> getAllRoles();
    Role getRoleByRoleName(String name);
}
