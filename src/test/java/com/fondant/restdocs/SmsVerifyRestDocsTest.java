package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.test.repository.SmsVerificationTestRepository;
import com.fondant.user.application.SmsVerificationService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.SmsVerificationEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.presentation.dto.request.SmsSendRequest;
import com.fondant.user.presentation.dto.request.SmsVerifyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class SmsVerifyRestDocsTest {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SmsVerificationService smsVerificationService;

    @Autowired
    private SmsVerificationTestRepository smsVerificationRepository;

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/user";

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @BeforeEach
    void setUp(){
        smsVerificationRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        smsVerificationRepository.save(
                SmsVerificationEntity.builder()
                        .phoneNumber("010-1234-5678")
                        .verificationCode("123456")
                        .expiresAt(now.plusMinutes(1))
                        .createdAt(now)
                        .build());
    }

    @Test
    @WithMockCustomUser
    void sendSms() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), userDetails.getUserId());

        willDoNothing().given(smsVerificationService).sendMessage(anyString());

        mockMvc.perform(post(BASE_URL + "/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SmsSendRequest("010-1234-5678")))
                        .header(HttpHeaders.AUTHORIZATION," Bearer " + access))
                .andExpect(status().isOk())
                        .andDo(document("user/send-SMS",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {access-token}"),
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application/json")
                                ),
                                requestFields(
                                        fieldWithPath("phoneNumber").description("문자를 보낼 사용자의 전화번호")
                                ),
                                responseFields(
                                        commonResponseFields()
                                )));
    }

    @Test
    @WithMockCustomUser
    void verifySms() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), userDetails.getUserId());

        mockMvc.perform(post(BASE_URL + "/sms/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SmsVerifyRequest("010-1234-5678", "123456")))
                        .header(HttpHeaders.AUTHORIZATION," Bearer " + access))
                .andExpect(status().isOk())
                .andDo(document("user/verify-SMS",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {access-token}"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application/json")
                        ),
                        requestFields(
                                fieldWithPath("phoneNumber").description("문자를 보낼 사용자의 전화번호"),
                                fieldWithPath("code").description("인증 코드")
                        ),
                        responseFields(
                                commonResponseFields()
                        )));
    }
}
