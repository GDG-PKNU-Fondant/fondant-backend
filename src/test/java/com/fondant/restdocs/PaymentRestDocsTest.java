package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.portone.util.PortOneApiClient;
import com.fondant.order.domain.entity.OrderEntity;
import com.fondant.order.domain.repository.OrderRepository;
import com.fondant.order.presentation.dto.CheckoutItem;
import com.fondant.order.presentation.dto.request.OrderDetails;
import com.fondant.order.presentation.dto.request.PaymentMethod;
import com.fondant.payment.application.dto.PaymentDetails;
import com.fondant.payment.domain.entity.PaymentEntity;
import com.fondant.payment.domain.entity.PaymentStatus;
import com.fondant.payment.domain.repository.PaymentRepository;
import com.fondant.payment.presentation.dto.PaymentRequest;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.user.application.UserService;
import com.fondant.user.domain.entity.DeliveryAddressEntity;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class PaymentRestDocsTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JWTUtil jwtUtil;
    @Autowired private ProductRepository productRepository;
    @Autowired private OptionRepository optionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserService userService;
    @MockBean
    private PortOneApiClient portOneApiClient;

    private UserEntity user;
    private OrderEntity order;
    private ProductEntity product;
    private OptionEntity option;
    private DeliveryAddressEntity deliveryAddress;

    @BeforeEach
    void setUp() {
        /* ---------- 사용자 ---------- */
        user = userRepository.save(UserEntity.builder()
                .snsType(SNSType.LOCAL)
                .name("테스트유저")
                .phoneNumber("010-1234-5678")
                .verifiedPhone(true)
                .email("test@test.com")
                .createAt(LocalDateTime.now().toLocalDate())
                .role(UserRole.USER)
                .point(10000)
                .build());

        /* ---------- 상품 ---------- */
        product = productRepository.save(ProductEntity.builder()
                .name("테스트상품")
                .description("테스트 상품 설명")
                .thumbnail("product.jpg")
                .price(10000.0)
                .market(null)
                .startDate(LocalDate.now())
                .maxCount(10)
                .build());

        /* ---------- 옵션 ---------- */
        option = optionRepository.save(OptionEntity.builder()
                .productId(product.getId())
                .name("테스트옵션")
                .price(0.0)
                .build());

        /* ---------- 배송지 ---------- */
        DeliveryAddressEntity addr = new DeliveryAddressEntity(
                "서울시 강남구", true, "12345", "집", "홍길동", "010-1234-5678");
        user.getDeliveryAddresses().add(addr);
        userRepository.save(user);   // 배송지 함께 저장
        deliveryAddress = userRepository.findById(user.getId())
                .orElseThrow()
                .getDeliveryAddresses()
                .get(0);

        /* ---------- 주문 ---------- */
        order = orderRepository.save(OrderEntity.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .deliveryAddress(deliveryAddress.getDeliveryAddress())
                .totalPrice(10000.0)
                .status(null)               // 필요 시 RESERVED 등으로 변경
                .build());
        Long orderId = order.getId();

        /* ---------- 결제(READY) 선등록 ---------- */
        paymentRepository.save(PaymentEntity.builder()
                .amount(10000.0)
                .method("CARD")
                .paymentId("test-payment-id")
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .order(order)
                .failReason(null)
                .build());

        /* ---------- 포트원 Mock ---------- */
        given(portOneApiClient.getPaymentDetails("test-payment-id"))
                .willReturn(new PaymentDetails(
                        "test-payment-id",
                        orderId, // 실제 저장된 주문 ID
                        "테스트주문",
                        10000,
                        "CARD",
                        new PaymentDetails.PayCustomer(user.getId().toString(), user.getEmail()),
                        new OrderDetails(
                                List.of(new CheckoutItem(option.getId(), product.getId(), 1)), // 실제 저장된 id 사용
                                List.of(),
                                0,
                                PaymentMethod.TOSS,
                                deliveryAddress.getId(), // 실제 저장된 배송지 id 사용
                                10000,
                                1L
                        )
                ));
    }

    /* ---------- RestDocs 공통 필드 ---------- */
    private static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("content").description("응답 데이터")
        };
    }

    @Test
    @DisplayName("결제 완료")
    void completePayment() throws Exception {
        /* ---------- 토큰 발급: 실제 DB 사용자 ID 사용 ---------- */
        String accessToken = jwtUtil.generateToken(
                "access",
                user.getId(),                // 실제 사용자 ID
                UserRole.USER.toString(),
                100_000L);

        PaymentRequest request = new PaymentRequest("test-payment-id", order.getId());

        mockMvc.perform(post("/api/payment/complete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("payment-complete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}"),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")),
                        requestFields(
                                fieldWithPath("paymentId").description("결제 ID"),
                                fieldWithPath("orderId").description("주문 ID")),
                        responseFields(commonResponseFields())
                                .andWithPrefix("content.", new FieldDescriptor[]{
                                        fieldWithPath("amount").description("결제 금액"),
                                        fieldWithPath("status").description("결제 상태"),
                                        fieldWithPath("paymentMethod").description("결제 수단")
                                })));
    }

    @Test
    @DisplayName("결제 취소")
    void cancelPayment() throws Exception {
        String accessToken = jwtUtil.generateToken(
                "access",
                user.getId(),
                UserRole.USER.toString(),
                100_000L);

        mockMvc.perform(org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete("/api/payment/{paymentId}", "test-payment-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("payment-cancel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}"),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                        ),
                        responseFields(commonResponseFields())
                ));
    }
}
