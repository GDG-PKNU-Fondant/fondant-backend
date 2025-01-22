package com.fondant.test;

import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/success")
    public ResponseEntity<ResponseDto<TestDto>> successTest(){
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                TestDto.of("강지원",25)));
    }
}
