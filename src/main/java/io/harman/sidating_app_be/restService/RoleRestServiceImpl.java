package io.harman.sidating_app_be.restService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.harman.sidating_app_be.model.Role;
import io.harman.sidating_app_be.repository.RoleRepository;


@Service
public class RoleRestServiceImpl implements RoleRestService {

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    public RoleRestServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleByRoleName(String name) {
        return roleRepository.findByRoleName(name).orElse(null);
    }
}