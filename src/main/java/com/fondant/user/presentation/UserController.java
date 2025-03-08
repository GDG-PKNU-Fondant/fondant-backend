package com.fondant.user.presentation;

import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.user.application.ReissueService;
import com.fondant.user.application.SmsVerificationService;
import com.fondant.user.application.UserService;
import com.fondant.user.presentation.dto.request.SmsSendRequest;
import com.fondant.user.presentation.dto.request.SmsVerifyRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;
    private final ReissueService reissueService;
    private final SmsVerificationService smsVerificationService;


    public UserController(UserService userService, ReissueService reissueService, SmsVerificationService smsVerificationService) {
        this.userService = userService;
        this.reissueService = reissueService;
        this.smsVerificationService = smsVerificationService;
    }

    @PostMapping("/sms/send")
    public ResponseEntity<ResponseDto<Void>> sendOne(@RequestBody SmsSendRequest request) throws Exception {
        System.out.println(request);
        smsVerificationService.sendMessage(request.phoneNumber());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<ResponseDto<Void>> verifyOne(@RequestBody SmsVerifyRequest request){
        smsVerificationService.verifyCode(request.phoneNumber(), request.code());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return reissueService.reissueToken(request, response);
    }
}
