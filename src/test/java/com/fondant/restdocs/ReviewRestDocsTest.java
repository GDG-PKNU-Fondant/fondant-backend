package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.s3.application.S3Service;
import com.fondant.review.domain.entity.ReviewEntity;
import com.fondant.review.domain.repository.ReviewRepository;
import com.fondant.review.presentation.dto.request.ReviewCreateRequest;
import com.fondant.review.presentation.dto.request.ReviewUpdateRequest;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.Gender;
import com.fondant.test.repository.UserTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static com.fondant.global.response.CommonResponseFields.commonResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@ActiveProfiles("test")
public class ReviewRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserTestRepository userRepository;

    @MockitoSpyBean
    private S3Service s3Service;

    private String token;
    private UserEntity user;
    private Long reviewId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserEntity.builder()
                .snsType(SNSType.KAKAO)
                .name("리뷰유저")
                .nickname("fondant")
                .profileUrl("https://example.com/profile.jpg")
                .phoneNumber("010-1234-5678")
                .email("fondant@example.com")
                .gender(Gender.FEMALE)
                .birth(Date.valueOf(LocalDate.of(2000, 1, 1)))
                .createAt(LocalDate.now())
                .role(UserRole.USER)
                .build());

        token = jwtUtil.generateToken("access", user.getId(), "USER", 1000L * 60 * 10);

        ReviewEntity saved = reviewRepository.save(ReviewEntity.builder()
                .userId(user.getId())
                .productId(1L)
                .content("기존 리뷰")
                .score(4.5)
                .build());


        reviewId = saved.getId();
    }

    @Test
    @DisplayName("리뷰 생성 API")
    @WithMockCustomUser
    void createReview() throws Exception {
        //given
        doReturn("https://mocked-url").when(s3Service).uploadReviewImage(any());

        ReviewCreateRequest request = new ReviewCreateRequest(List.of(1L, 2L),"맛있어요", 5.0);

        MockMultipartFile jsonPart = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));
        MockMultipartFile imagePart = new MockMultipartFile("files", "photo.jpg", "image/jpeg", "test-image".getBytes());

        //when & then
        mockMvc.perform(multipart("/api/reviews/{productId}", 1L)
                        .file(jsonPart)
                        .file(imagePart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("reviews/create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("productId").description("리뷰를 작성할 상품 ID")
                        ),
                        requestParts(
                                partWithName("data").description("리뷰 작성 정보 JSON"),
                                partWithName("files").description("첨부 이미지 파일들").optional()
                        ),
                        responseFields(commonResponseFields())
                ));
    }

    @Test
    @DisplayName("리뷰 수정 API")
    @WithMockCustomUser
    void updateReview() throws Exception {
        ReviewUpdateRequest request = new ReviewUpdateRequest(List.of(2L),"수정된 리뷰 내용", 3.5);

        MockMultipartFile jsonPart = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));
        MockMultipartFile imagePart = new MockMultipartFile("files", "update.jpg", "image/jpeg", "test-update".getBytes());

        mockMvc.perform(multipart("/api/reviews/{reviewId}", reviewId)
                        .file(jsonPart)
                        .file(imagePart)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        })
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("reviews/update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("reviewId").description("수정할 리뷰 ID")
                        ),
                        requestParts(
                                partWithName("data").description("리뷰 수정 정보 JSON"),
                                partWithName("files").description("첨부 이미지 파일들").optional()
                        ),
                        responseFields(commonResponseFields())
                ));
    }

    @Test
    @DisplayName("리뷰 삭제 API")
    @WithMockCustomUser
    void deleteReview() throws Exception {
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("reviews/delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("reviewId").description("삭제할 리뷰 ID")
                        ),
                        responseFields(commonResponseFields())
                ));
    }
}