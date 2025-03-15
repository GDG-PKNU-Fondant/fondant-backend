package com.fondant.user.application;

import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.DeliveryAddressEntity;
import com.fondant.user.exception.UserError;
import com.fondant.user.presentation.dto.request.DeliveryAddressAddRequest;
import com.fondant.user.presentation.dto.request.DeliveryAddressUpdateRequest;
import com.fondant.user.presentation.dto.request.JoinRequest;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.presentation.dto.request.UserUpdateRequest;
import com.fondant.user.presentation.dto.response.DeliveryAddressResponse;
import com.fondant.user.presentation.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UserResponse getUserInfo(Long userId) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);

        UserEntity userEntity = userEntityOptional.orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        return UserResponse.builder()
                .name(userEntity.getName())
                .phoneNumber(userEntity.getPhoneNumber())
                .verifiedPhone(userEntity.isVerifiedPhone())
                .email(userEntity.getEmail())
                .birth(userEntity.getBirth())
                .nickname(userEntity.getNickname())
                .profileUrl(userEntity.getProfileUrl())
                .gender(userEntity.getGender())
                .build();
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);

        UserEntity userEntity = userEntityOptional.orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        userEntity = userEntity.toBuilder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .verifiedPhone(request.verifiedPhone())
                .email(request.email())
                .birth(request.birth())
                .nickname(request.nickname())
                .profileUrl(request.profileUrl())
                .gender(request.gender())
                .build();

        userRepository.save(userEntity);
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
  
    @Transactional
    public List<DeliveryAddressResponse> getDeliveryAddress(Long userId){
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        return user.getDeliveryAddresses().stream()
                .map(entity -> DeliveryAddressResponse.builder()
                        .id(entity.getId())
                        .deliveryAddress(entity.getDeliveryAddress())
                        .isPrimary(entity.getIsPrimary())
                        .alias(entity.getAlias())
                        .postCode(entity.getPostCode())
                        .receiverName(entity.getReceiverName())
                        .receiverPhoneNumber(entity.getReceiverPhoneNumber())
                        .build())
                .toList();
    }

    @Transactional
    public void updateDeliveryAddress(Long userId, DeliveryAddressUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        DeliveryAddressEntity address = user.findDeliveryAddressById(request.id());

        if (request.isPrimary()) {
            user.getDeliveryAddresses().forEach(existingAddress -> {
                if (!existingAddress.getId().equals(address.getId())) {
                    existingAddress.updateIsPrimary(false);
                }
            });
        }

        address.update(request);

        userRepository.save(user);
    }

    @Transactional
    public void addDeliveryAddress(Long userId, DeliveryAddressAddRequest request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        DeliveryAddressEntity address = DeliveryAddressEntity.builder()
                .deliveryAddress(request.deliveryAddress())
                .alias(request.alias())
                .postCode(request.postCode())
                .receiverName(request.receiverName())
                .receiverPhoneNumber(request.receiverPhoneNumber())
                .isPrimary(request.isPrimary())
                .build();

        if (address.getIsPrimary()) {
            user.getDeliveryAddresses()
                    .forEach(existingAddresses ->
                            existingAddresses.updateIsPrimary(false)
                    );
        }

        user.getDeliveryAddresses().add(address);

        userRepository.save(user);
    }

    @Transactional
    public void deleteDeliveryAddress(Long userId, Long deliveryAddressId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        DeliveryAddressEntity deleteAddress = user.getDeliveryAddresses().stream()
                .filter(address -> address.getId().equals(deliveryAddressId))
                .findFirst()
                .orElseThrow(() -> new ApiException(UserError.ADDRESS_NOT_FOUND));

        user.getDeliveryAddresses().remove(deleteAddress);

        userRepository.save(user);
    }
}
