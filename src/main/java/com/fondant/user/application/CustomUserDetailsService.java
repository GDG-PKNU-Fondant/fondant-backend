package com.fondant.user.application;

import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.jwt.dto.JWTUserDTO;
import com.fondant.user.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException{

        JWTUserDTO userData = userRepository.findByEmail(userEmail)
                .map(user -> JWTUserDTO.builder()
                        .userId(String.valueOf(user.getId()))
                        .userEmail(user.getEmail())
                        .password(user.getPassword())
                        .role(user.getRole().name())
                        .build()
                ).orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다."));

        return new CustomUserDetails(userData);
    }
}
