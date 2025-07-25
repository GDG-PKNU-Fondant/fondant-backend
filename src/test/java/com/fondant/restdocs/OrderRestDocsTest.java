package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.order.presentation.dto.CheckoutItem;
import com.fondant.order.presentation.dto.request.OrderPrepareRequest;
import com.fondant.order.presentation.dto.request.OrderRequest;
import com.fondant.order.presentation.dto.request.PaymentMethod;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.util.ProductUtil;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.DeliveryAddressEntity;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.market.domain.repository.MarketRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
public class OrderRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductUtil productUtil;


    private DeliveryAddressEntity deliveryAddress;
    private ProductEntity product;
    private OptionEntity option;

    @BeforeEach
    void setUp() {
        MarketEntity market = marketRepository.save(
                MarketEntity.builder()
                        .name("테스트마켓")
                        .thumbnail("market.jpg")
                        .background("bg.jpg")
                        .freeDeliveryLimit(30_000.0)
                        .deliveryFee(2_500.0)
                        .build()
        );

        product = productRepository.save(
                ProductEntity.builder()
                        .name("테스트상품")
                        .description("테스트 상품 설명")
                        .thumbnail("product.jpg")
                        .price(12_000.0)
                        .market(market)
                        .startDate(LocalDate.now())
                        .maxCount(100)
                        .build()
        );

        option = optionRepository.save(
                OptionEntity.builder()
                        .productId(product.getId())
                        .name("테스트옵션")
                        .price(2_000.0)
                        .build()
        );
    }

    private static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("content").description("응답 데이터")
        };
    }

    @Test
    @WithMockCustomUser
    @Transactional
    @DisplayName("주문 생성")
    void createOrder() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), 100_000L);

        UserEntity user = userService.getUserEntityById(userDetails.getUserId());
        deliveryAddress = new DeliveryAddressEntity(
                "서울시 강남구", true, "12345", "집", "홍길동", "010-1234-5678");
        user.getDeliveryAddresses().add(deliveryAddress);
        userRepository.save(user);
        deliveryAddress = userRepository.findById(user.getId()).orElseThrow()
                .getDeliveryAddresses().get(0);

        int expectedAmount = 12_000 * 2 + 2_500;

        OrderRequest request = new OrderRequest(
                List.of(new CheckoutItem(product.getId(), option.getId(), 2)),
                List.of(),
                0,
                PaymentMethod.TOSS,
                deliveryAddress.getId(),
                expectedAmount,
                1L
        );

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("order-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {access-token}"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application/json")),
                        requestFields(
                                fieldWithPath("checkoutItems").description("주문 상품 목록"),
                                fieldWithPath("checkoutItems[].productId").description("상품 ID"),
                                fieldWithPath("checkoutItems[].optionId").description("옵션 ID"),
                                fieldWithPath("checkoutItems[].quantity").description("수량"),
                                fieldWithPath("couponAllies").description("적용 쿠폰 목록"),
                                fieldWithPath("discountPoints").description("사용 포인트"),
                                fieldWithPath("payment").description("결제 수단"),
                                fieldWithPath("deliveryAddressId").description("배송지 ID"),
                                fieldWithPath("expectedAmount").description("예상 결제 금액"),
                                fieldWithPath("cartId").description("장바구니 ID")),
                        responseFields(commonResponseFields())
                                .andWithPrefix("content.", new FieldDescriptor[]{
                                        fieldWithPath("orderId").description("생성된 주문 ID")
                                })));
    }

    @Test
    @WithMockCustomUser
    @Transactional
    @DisplayName("주문 준비")
    void prepareOrder() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), 100_000L);

        UserEntity user = userService.getUserEntityById(userDetails.getUserId());
        deliveryAddress = new DeliveryAddressEntity(
                "서울시 강남구", true, "12345", "집", "홍길동", "010-1234-5678");
        user.getDeliveryAddresses().add(deliveryAddress);
        userRepository.save(user);
        deliveryAddress = userRepository.findById(user.getId()).orElseThrow()
                .getDeliveryAddresses().get(0);

        OrderPrepareRequest request = new OrderPrepareRequest(
                List.of(new CheckoutItem(option.getId(), product.getId(), 2))
        );

        mockMvc.perform(post("/api/orders/page-info")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("order-prepare",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {access-token}"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application/json")),
                        requestFields(
                                fieldWithPath("items").description("주문 상품 목록"),
                                fieldWithPath("items[].optionId").description("옵션 ID"),
                                fieldWithPath("items[].productId").description("상품 ID"),
                                fieldWithPath("items[].quantity").description("수량")
                        ),
                        responseFields(commonResponseFields())
                                .andWithPrefix("content.", new FieldDescriptor[] {
                                        fieldWithPath("orderItems").description("주문 상품 정보 목록"),
                                        fieldWithPath("orderItems[].productId").description("상품 ID"),
                                        fieldWithPath("orderItems[].productName").description("상품명"),
                                        fieldWithPath("orderItems[].thumbnailUrl").description("상품 썸네일 URL"),
                                        fieldWithPath("orderItems[].optionId").description("옵션 ID"),
                                        fieldWithPath("orderItems[].optionName").description("옵션명"),
                                        fieldWithPath("orderItems[].quantity").description("수량"),
                                        fieldWithPath("orderItems[].price").description("상품 가격"),
                                        fieldWithPath("orderItems[].optionPrice").description("옵션 가격"),
                                        fieldWithPath("orderItems[].discountRate").description("할인율"),
                                        fieldWithPath("orderItems[].discountedPrice").description("할인 적용가"),
                                        fieldWithPath("totalOrderPrice").description("총 주문 금액"),
                                        fieldWithPath("deliveryAddresses").description("배송지 목록"),
                                        fieldWithPath("deliveryAddresses[].id").description("배송지 ID"),
                                        fieldWithPath("deliveryAddresses[].deliveryAddress").description("배송지 주소"),
                                        fieldWithPath("deliveryAddresses[].isPrimary").description("기본 배송지 여부"),
                                        fieldWithPath("deliveryAddresses[].postCode").description("우편번호"),
                                        fieldWithPath("deliveryAddresses[].alias").description("배송지 별칭"),
                                        fieldWithPath("deliveryAddresses[].receiverName").description("수령인 이름"),
                                        fieldWithPath("deliveryAddresses[].receiverPhoneNumber").description("수령인 연락처"),
                                        fieldWithPath("point").description("사용자 보유 포인트")
                                })
                ));
    }

    @Test
    @DisplayName("상품 구매시 재고 Locking 검사")
    void lockOrder() throws Exception {
        // given
        final int threadCount = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productUtil.reserveStock(product.getId(), 1);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        final ProductEntity product1 = productRepository.findById(product.getId()).orElseThrow();

        // then
        assertThat(product1.getMaxCount()).isEqualTo(0);
    }
}
