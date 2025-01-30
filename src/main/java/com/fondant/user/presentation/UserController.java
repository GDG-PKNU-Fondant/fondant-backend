package com.fondant.user.presentation;

import com.fondant.user.application.ReissueService;
import com.fondant.user.application.UserService;
import com.fondant.user.presentation.dto.request.JoinRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;
    private final ReissueService reissueService;

    public UserController(UserService userService, ReissueService reissueService) {
        this.userService = userService;
        this.reissueService = reissueService;
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody JoinRequest joinRequest) {
        userService.joinUser(joinRequest);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return reissueService.reissueToken(request, response);
    }
}
