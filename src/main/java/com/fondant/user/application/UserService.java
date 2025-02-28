package com.fondant.user.application;

import com.fondant.user.presentation.dto.request.JoinRequest;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public UserEntity joinUser(JoinRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        UserEntity userEntity = UserEntity.builder()
                .snsType(SNSType.LOCAL)
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .birth(request.birth())
                .nickname("")
                .profileUrl("")
                .createAt(LocalDate.now())
                .gender(request.gender())
                .role(UserRole.USER)
                .build();

        return userRepository.save(userEntity);
    }
}
