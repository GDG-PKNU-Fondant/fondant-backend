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
import org.springframework.restdocs.payload.FieldDescriptor;
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

    @Test
    @DisplayName("리뷰 조회 API - 정렬 기준별")
    void getReviews_withSortType() throws Exception {
        // given
        Long productId = 1L;

        reviewRepository.save(ReviewEntity.builder().productId(productId).userId(user.getId()).score(3.0).content("보통").build());
        Thread.sleep(10);
        reviewRepository.save(ReviewEntity.builder().productId(productId).userId(user.getId()).score(5.0).content("최고").build());
        Thread.sleep(10);
        reviewRepository.save(ReviewEntity.builder().productId(productId).userId(user.getId()).score(1.0).content("별로").build());

        // when & then
        mockMvc.perform(get("/api/reviews")
                        .param("productId", String.valueOf(productId))
                        .param("sortType", "HIGH_SCORE")
                        .param("page", "0")
                        .param("size", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(document("reviews/get-sorted",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("productId").description("리뷰를 조회할 상품 ID"),
                                parameterWithName("sortType").description("정렬 기준 (LATEST: 최신순, HIGH_SCORE: 별점 높은순, LOW_SCORE: 별점 낮은순)").optional(),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지당 항목 수")
                        ),
                        responseFields(commonResponseFields())
                                .andWithPrefix("content.", new FieldDescriptor[]{
                                        fieldWithPath("pageInfo.currentPage").description("현재 페이지 번호"),
                                        fieldWithPath("pageInfo.totalPage").description("전체 페이지 수"),
                                        fieldWithPath("productId").description("리뷰를 조회한 상품 ID"),
                                        fieldWithPath("reviews[].userId").description("리뷰 작성자의 ID"),
                                        fieldWithPath("reviews[].imageUrls").description("리뷰에 첨부된 이미지 URL 목록"),
                                        fieldWithPath("reviews[].content").description("리뷰 본문 내용"),
                                        fieldWithPath("reviews[].score").description("리뷰 별점"),
                                        fieldWithPath("reviews[].tags").description("리뷰에 포함된 태그 정보 목록")
                                })
                ));
    }

}