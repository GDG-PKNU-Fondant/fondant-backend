package com.fondant.user.presentation;


import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.user.application.ReissueService;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.presentation.dto.request.UserUpdateRequest;
import com.fondant.user.presentation.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final ReissueService reissueService;

    public UserController(UserService userService, ReissueService reissueService) {
        this.userService = userService;
        this.reissueService = reissueService;
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<UserResponse>> getUserInfo(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.getUserInfo(user.getUserId())));
    }

    @PatchMapping("")
    public ResponseEntity<ResponseDto<Void>> updateUserInfo(@CurrentUser CustomUserDetails user, @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return reissueService.reissueToken(request, response);
    }
}
