package io.harman.sidating_app_be.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
