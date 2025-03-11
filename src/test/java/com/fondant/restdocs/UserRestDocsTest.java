package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.config.SecurityConfig;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.test.repository.UserTestRepository;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.user.application.UserService;
import com.fondant.user.domain.entity.Gender;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.presentation.dto.request.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Import(SecurityConfig.class)
@Transactional
public class UserRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserTestRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    private static final String BASE_URL = "/api/user";

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @BeforeEach
    public void setUp() {
    }

    @Test
    @WithMockCustomUser
    void getUserInfo() throws Exception {

        mockMvc.perform(get(BASE_URL + "/"))
                .andExpect(status().isOk())
                .andDo(document("user/get-user-info",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}")
                                        .optional(),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                                        .optional()
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("phoneNumber").description("전화번호").optional(),
                                fieldWithPath("verifiedPhone").description("전화번호 인증 여부").optional(),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("birth").description("생일").optional(),
                                fieldWithPath("nickname").description("닉네임").optional(),
                                fieldWithPath("profileUrl").description("프로필 사진 URL").optional(),
                                fieldWithPath("gender").description("성별").optional()
                        })
                ));
    }

    @Test
    @WithMockCustomUser
    void updateUserInfo() throws Exception {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("updated name")
                .phoneNumber("010-9999-9999")
                .verifiedPhone(true)
                .email("<EMAIL>")
                .nickname("updated nickname")
                .profileUrl("https://updated-profile.com/image.jpg")
                .gender(Gender.FEMALE)
                .birth(Date.valueOf("2000-01-01"))
                .build();

        mockMvc.perform(patch(BASE_URL + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("user/update-user-info",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("변경할 사용자 이름").optional(),
                                fieldWithPath("phoneNumber").description("변경할 전화번호").optional(),
                                fieldWithPath("verifiedPhone").description("전화번호 인증 여부").optional(),
                                fieldWithPath("email").description("변경할 이메일").optional(),
                                fieldWithPath("nickname").description("변경할 닉네임").optional(),
                                fieldWithPath("profileUrl").description("변경할 프로필 사진 URL").optional(),
                                fieldWithPath("gender").description("변경할 성별").optional(),
                                fieldWithPath("birth").description("변경할 생일").optional()
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("response").description("응답 데이터")
                        )
                ));

        UserEntity updatedUser = userRepository.findByEmail(request.email());

        assertThat(updatedUser.getName()).isEqualTo(request.name());
        assertThat(updatedUser.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(updatedUser.isVerifiedPhone()).isEqualTo(request.verifiedPhone());
        assertThat(updatedUser.getEmail()).isEqualTo(request.email());
        assertThat(updatedUser.getNickname()).isEqualTo(request.nickname());
        assertThat(updatedUser.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(updatedUser.getProfileUrl()).isEqualTo(request.profileUrl());
        assertThat(updatedUser.getGender()).isEqualTo(request.gender());
    }
}