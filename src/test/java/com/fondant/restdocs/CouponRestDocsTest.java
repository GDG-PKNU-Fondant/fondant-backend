package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.coupon.application.CouponService;
import com.fondant.coupon.domain.entity.CouponEntity;
import com.fondant.coupon.domain.entity.CouponMarketEntity;
import com.fondant.coupon.domain.entity.DiscountType;
import com.fondant.coupon.domain.repository.CouponRepository;
import com.fondant.coupon.domain.repository.UserCouponRepository;
import com.fondant.coupon.presentation.dto.request.CouponIssueRequest;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.fondant.global.response.CommonResponseFields.commonResponseFields;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class CouponRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private MarketEntity market;
    private CouponEntity coupon;
    private String mockToken;

    @BeforeEach
    void setUp() {
        market = marketRepository.save(MarketEntity.builder()
                .name("퐁당 마켓")
                .description("신메뉴 업데이트 매달 1일 ! 전국 택배가능 초콜릿 쿠키 전문 퐁당 마켓")
                .thumbnail("test-thumbnail.png")
                .background("test-background.png")
                .totalSales(100L)
                .totalReviews(10L)
                .build()
        );

        coupon = CouponEntity.builder()
                .name("테스트 쿠폰")
                .discountType(DiscountType.AMOUNT)
                .discountAmount(1000.0)
                .minOrderAmount(10000.0)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .build();
        couponRepository.save(coupon);

        CouponMarketEntity couponMarket = CouponMarketEntity.builder()
                .coupon(coupon)
                .market(market)
                .build();
        coupon.getCouponMarkets().add(couponMarket);
    }

    @Test
    @DisplayName("발급 가능한 쿠폰 목록 조회")
    @WithMockCustomUser
    void getAvailableCouponsForUser() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        mockToken = jwtUtil.generateToken("access", userDetails.getUserId(), "USER", 60 * 10 * 1000L);

        mockMvc.perform(get("/api/coupon/issuable")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + mockToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(document("coupon-get-issuable",
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
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                                .andWithPrefix("content.", new FieldDescriptor[]{
                                        subsectionWithPath("sliceInfo").description("페이지 정보"),
                                        fieldWithPath("sliceInfo.hasNext").description("다음 페이지 존재 여부"),
                                        subsectionWithPath("coupons").description("발급 가능한 쿠폰 목록")
                                })
                                .andWithPrefix("content.coupons[].", new FieldDescriptor[]{
                                        fieldWithPath("id").description("쿠폰 ID"),
                                        fieldWithPath("name").description("쿠폰 이름"),
                                        fieldWithPath("discountType").description("할인 유형 (AMOUNT/PERCENTAGE)"),
                                        fieldWithPath("discountAmount").description("할인 금액/비율"),
                                        fieldWithPath("minOrderAmount").description("최소 주문 금액"),
                                        fieldWithPath("startDate").description("쿠폰 시작일"),
                                        fieldWithPath("endDate").description("쿠폰 종료일"),
                                        fieldWithPath("isGlobal").description("전역 쿠폰 여부"),
                                        fieldWithPath("markets[].id").description("마켓 ID"),
                                        fieldWithPath("markets[].name").description("마켓 이름")
                                })
                ));
    }

    @Test
    @DisplayName("발급받은 쿠폰 목록 조회")
    @WithMockCustomUser
    void getIssuedCoupons() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        mockToken = jwtUtil.generateToken("access", userDetails.getUserId(), "USER", 60 * 10 * 1000L);

        couponService.issueCoupon(userDetails.getUserId(), coupon.getId());

        mockMvc.perform(get("/api/coupon/issued")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + mockToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(document("coupon-get-issued",
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
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                                .andWithPrefix("content.", new FieldDescriptor[]{
                                        subsectionWithPath("sliceInfo").description("페이지 정보"),
                                        fieldWithPath("sliceInfo.hasNext").description("다음 페이지 존재 여부"),
                                        subsectionWithPath("coupons").description("발급받은 쿠폰 목록")
                                })
                                .andWithPrefix("content.coupons[].", new FieldDescriptor[]{
                                        fieldWithPath("id").description("쿠폰 ID"),
                                        fieldWithPath("name").description("쿠폰 이름"),
                                        fieldWithPath("discountType").description("할인 유형 (AMOUNT/PERCENTAGE)"),
                                        fieldWithPath("discountAmount").description("할인 금액/비율"),
                                        fieldWithPath("minOrderAmount").description("최소 주문 금액"),
                                        fieldWithPath("startDate").description("쿠폰 시작일"),
                                        fieldWithPath("endDate").description("쿠폰 종료일"),
                                        fieldWithPath("isGlobal").description("전역 쿠폰 여부"),
                                        fieldWithPath("markets[].id").description("마켓 ID"),
                                        fieldWithPath("markets[].name").description("마켓 이름")
                                })
                ));
    }

    @Test
    @DisplayName("쿠폰 발급")
    @WithMockCustomUser
    void issueCoupon() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        mockToken = jwtUtil.generateToken("access", userDetails.getUserId(), "USER", 60 * 10 * 1000L);

        CouponIssueRequest request = new CouponIssueRequest(coupon.getId());

        mockMvc.perform(post("/api/coupon/issue")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + mockToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("coupon-issue",
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
                        requestFields(
                                fieldWithPath("couponId").description("발급할 쿠폰 ID")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                ));
    }
}


