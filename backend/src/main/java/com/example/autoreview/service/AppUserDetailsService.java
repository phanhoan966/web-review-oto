package com.example.autoreview.publicsite.service;

import com.example.autoreview.domain.User;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.security.Roles;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Set<String> expanded = expandRoles(user.getRoles());
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        expanded.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities);
    }

    private Set<String> expandRoles(Set<String> roles) {
        Set<String> all = new HashSet<>(roles);
        if (roles.contains(Roles.SYSTEM_ADMIN)) {
            all.add(Roles.MANAGER);
            all.add(Roles.ADMIN);
            all.add(Roles.USER);
        }
        if (roles.contains(Roles.MANAGER)) {
            all.add(Roles.ADMIN);
            all.add(Roles.USER);
        }
        if (roles.contains(Roles.ADMIN)) {
            all.add(Roles.USER);
        }
        return all;
    }
}
