package com.fondant.user.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.user.application.PointService;
import com.fondant.user.application.ReissueService;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.presentation.dto.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/point")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    @GetMapping
    public ResponseEntity<ResponseDto<PointResponse>> getPoint(@CurrentUser CustomUserDetails user){
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                pointService.getPoint(user.getUserId())));
    }
}
