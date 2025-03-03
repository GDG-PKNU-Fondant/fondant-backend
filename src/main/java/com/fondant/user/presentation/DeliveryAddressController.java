package com.fondant.user.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.presentation.dto.request.DeliveryAddressAddRequest;
import com.fondant.user.presentation.dto.request.DeliveryAddressUpdateRequest;
import com.fondant.user.presentation.dto.response.DeliveryAddressResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/address")
public class DeliveryAddressController {

    private final UserService userService;

    public DeliveryAddressController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<ResponseDto<List<DeliveryAddressResponse>>> getDeliveryAddress(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.getDeliveryAddress(user.getUserId())));
    }

    @PostMapping("/")
    public ResponseEntity<ResponseDto<Void>> addDeliveryAddress(@CurrentUser CustomUserDetails user, @RequestBody DeliveryAddressAddRequest request) {
        userService.addDeliveryAddress(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PatchMapping("/")
    public ResponseEntity<ResponseDto<Void>> updateDeliveryAddress(@CurrentUser CustomUserDetails user, @RequestBody DeliveryAddressUpdateRequest request) {
        userService.updateDeliveryAddress(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @DeleteMapping("/{deliveryAddressId}")
    public ResponseEntity<ResponseDto<Void>> deleteDeliveryAddress(@CurrentUser CustomUserDetails user,@PathVariable Long deliveryAddressId) {
        userService.deleteDeliveryAddress(user.getUserId(), deliveryAddressId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}
