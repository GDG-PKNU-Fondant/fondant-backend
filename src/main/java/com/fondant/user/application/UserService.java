package com.fondant.user.application;

import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.DeliveryAddressEntity;
import com.fondant.user.exception.UserError;
import com.fondant.user.presentation.dto.request.DeliveryAddressAddRequest;
import com.fondant.user.presentation.dto.request.DeliveryAddressUpdateRequest;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.presentation.dto.request.UserUpdateRequest;
import com.fondant.user.presentation.dto.response.DeliveryAddressResponse;
import com.fondant.user.presentation.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
    }

    protected UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntityById(Long userId) {
        return findUserById(userId);
    }

    public UserResponse getUserInfo(Long userId) {
        UserEntity userEntity = findUserById(userId);

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
        UserEntity userEntity = findUserById(userId);

        userRepository.save(userEntity.toBuilder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .verifiedPhone(request.verifiedPhone())
                .email(request.email())
                .birth(request.birth())
                .nickname(request.nickname())
                .profileUrl(request.profileUrl())
                .gender(request.gender())
                .build());
    }

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
        UserEntity user = findUserById(userId);

        DeliveryAddressEntity address = findDeliveryAddressById(userId, request.id());

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

        UserEntity user = findUserById(userId);

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
        UserEntity user = findUserById(userId);

        DeliveryAddressEntity deleteAddress = user.getDeliveryAddresses().stream()
                .filter(address -> address.getId().equals(deliveryAddressId))
                .findFirst()
                .orElseThrow(() -> new ApiException(UserError.ADDRESS_NOT_FOUND));

        user.getDeliveryAddresses().remove(deleteAddress);

        userRepository.save(user);
    }

    public DeliveryAddressEntity findDeliveryAddressById(Long userId, Long addressId) {
        UserEntity user = findUserById(userId);

        return user.getDeliveryAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ApiException(UserError.ADDRESS_NOT_FOUND));
    }
}
