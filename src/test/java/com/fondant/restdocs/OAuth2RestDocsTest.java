package com.fondant.restdocs;

import com.fondant.global.config.SecurityConfig;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.domain.repository.RefreshRepository;
import com.fondant.infra.oauth2.dto.NaverResponse;
import com.fondant.user.domain.entity.UserRole;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Import(SecurityConfig.class)
@Transactional
public class OAuth2RestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private RefreshRepository refreshRepository;

    private static final String BASE_URL = "/api/user";

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @Test
    @DisplayName("API - Access Token 재발급")
    void reissueAccessToken() throws Exception {

        //given
        String refreshToken = jwtUtil.generateToken("refresh", String.valueOf(1), UserRole.USER.toString(), 24 * 24 * 1000L);
        Cookie cookie = new Cookie("refresh", refreshToken);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        //when
        when(refreshRepository.existsByRefresh(refreshToken)).thenReturn(true);
        doNothing().when(refreshRepository).deleteByExpiresBefore(any());
        doNothing().when(refreshRepository).deleteByRefresh(any());

        //then
        mockMvc.perform(post(BASE_URL + "/reissue")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andDo(document("/user/reissue-access-and-refresh-token",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("refresh").description("refresh 토큰을 담은 쿠키")
                        ),
                        responseCookies(
                                cookieWithName("refresh").description("새로 발급된 refresh 토큰을 담은 쿠키")
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("accessToken").description("재발급된 Access 토큰")
                        })
                ));
    }

    @Test
    @DisplayName("API - 로그아웃")
    void logout() throws Exception {
        //given
        String refreshToken = jwtUtil.generateToken("refresh", String.valueOf(1), UserRole.USER.toString(), 24 * 24 * 1000L);

        Cookie cookie = new Cookie("refresh", refreshToken);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        //when
        when(refreshRepository.existsByRefresh(refreshToken)).thenReturn(true);
        doNothing().when(refreshRepository).deleteByExpiresBefore(any());
        doNothing().when(refreshRepository).deleteByRefresh(any());

        //then
        mockMvc.perform(post(BASE_URL + "/logout")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andDo(document("user/logout",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("refresh").description("refresh 토큰을 담은 쿠키")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                ));
    }
}