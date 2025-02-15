package com.fondant.infra.oauth2.application;

import com.fondant.infra.oauth2.dto.*;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service

public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Oauth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        Optional<UserEntity> extraData = userRepository.findByEmail(oAuth2Response.getEmail());

        System.out.println(oAuth2Response.getEmail());


        if (extraData.isPresent()) {
            if (registrationId.equals("naver")) {
                UserEntity updatedUser = extraData.get().toBuilder()
                        .id(extraData.get().getId())
                        .name(oAuth2Response.getName())
                        .phoneNumber(oAuth2Response.getPhoneNumber())
                        .build();

                userRepository.save(updatedUser);
            } else {
                UserEntity updatedUser = extraData.get().toBuilder()
                        .id(extraData.get().getId())
                        .name(oAuth2Response.getName())
                        .build();

                userRepository.save(updatedUser);
            }

            OAuth2UserDTO userDTO = OAuth2UserDTO.builder()
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .provider(oAuth2Response.getProvider())
                    .role(String.valueOf(UserRole.USER))
                    .phoneNumber(oAuth2Response.getPhoneNumber())
                    .build();
            return new CustomOAuth2User(userDTO);
        } else {
            UserEntity userEntity = UserEntity.builder()
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .snsType(SNSType.valueOf(oAuth2Response.getProvider()))
                    .role((UserRole.USER))
                    .createAt(LocalDate.now())
                    .build();

            userRepository.save(userEntity);
        }

        OAuth2UserDTO userDTO = OAuth2UserDTO.builder()
                .name(oAuth2Response.getName())
                .email(oAuth2Response.getEmail())
                .provider(oAuth2Response.getProvider())
                .role(String.valueOf(UserRole.USER))
                .phoneNumber(oAuth2Response.getPhoneNumber())
                .build();
        return new CustomOAuth2User(userDTO);
    }
}
