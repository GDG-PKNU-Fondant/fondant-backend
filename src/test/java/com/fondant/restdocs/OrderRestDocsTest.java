package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.global.config.SecurityConfig;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.order.application.OrderService;
import com.fondant.order.domain.repository.OrderRepository;
import com.fondant.order.presentation.dto.request.OrderCreateRequest;
import com.fondant.order.presentation.dto.request.OrderItem;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.presentation.dto.request.DeliveryAddressAddRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Import(SecurityConfig.class)
@Transactional
public class OrderRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private OptionRepository optionRepository;

    private static final String BASE_URL = "/api/order";

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    private DeliveryAddressAddRequest address1;
    private ProductEntity product1;
    private ProductEntity product2;
    private MarketEntity market1;
    private OptionEntity option1;
    private OptionEntity option2;
    private String access;
    private OrderCreateRequest orderCreateRequest;
    private OrderItem orderItem;

    @BeforeEach
    void before() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getUserId();
        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), userDetails.getUserId());

        userService.addDeliveryAddress(
                userDetails.getUserId(),
                address1 = DeliveryAddressAddRequest.builder()
                        .alias("집")
                        .postCode("12345")
                        .deliveryAddress("서울시 강남구 테헤란로 123")
                        .isPrimary(true)
                        .receiverName("박세찬")
                        .receiverPhoneNumber("010-1234-5678")
                .build());

        marketRepository.save(
                market1 = MarketEntity.builder()
                        .background("url")
                        .createAt(LocalDate.now().minusDays(1))
                        .deliveryFee(3000.0)
                        .description("깨끗한")
                        .freeDeliveryLimit(10000L)
                        .name("지훈백반")
                        .thumbnail("url")
                        .totalReviews(1200L)
                        .updateAt(LocalDate.now())
                        .build()
        );

        productRepository.save(
                product1 = ProductEntity.builder()
                        .name("쿠키")
                        .description("맜있는")
                        .price(14000)
                        .startDate(LocalDate.now().plusDays(3))
                        .thumbnail("url")
                        .market(market1)
                        .maxCount(50)
                        .build()
                );

        productRepository.save(
                product2 = ProductEntity.builder()
                        .name("젤리")
                        .description("맜있는")
                        .price(20000)
                        .startDate(LocalDate.now().plusDays(3))
                        .thumbnail("url")
                        .market(market1)
                        .maxCount(50)
                        .build()
        );

        optionRepository.save(
                option1 = OptionEntity.builder()
                        .productId(product1.getId())
                        .name("복숭아맛")
                        .price(1000.0)
                        .build()
        );

        optionRepository.save(
                option2 = OptionEntity.builder()
                        .productId(product1.getId())
                        .name("냉면맛")
                        .price(2000.0)
                        .build()
        );

        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .marketId(market1.getId())
                        .optionId(option1.getId())
                        .productId(product1.getId())
                        .quantity(2)
                        .price(14000.0)
                        .optionPrice(1000.0)
                        .deliveryFee(3000L)
                        .build(),
                OrderItem.builder()
                        .marketId(market1.getId())
                        .optionId(option2.getId())
                        .productId(product2.getId())
                        .quantity(1)
                        .price(20000.0)
                        .optionPrice(2000.0)
                        .deliveryFee(3000L)
                        .build()
        );

        orderCreateRequest = OrderCreateRequest.builder()
                .items(items)
                .addressId(userService.getDeliveryAddress(userDetails.getUserId()).get(0).id())
                .totalPrice(55000.0)
                .build();
    }

    @Test
    @WithMockCustomUser
    void addOrder() throws Exception {
        mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderCreateRequest))
                    .header(HttpHeaders.AUTHORIZATION," Bearer " + access))
                .andExpect(status().isOk())
                .andDo(document("order/add_order",
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
                                fieldWithPath("items[].marketId").description("마켓 ID"),
                                fieldWithPath("items[].optionId").description("옵션 ID"),
                                fieldWithPath("items[].productId").description("상품 ID"),
                                fieldWithPath("items[].quantity").description("수량"),
                                fieldWithPath("items[].discountRate").description("할인율"),
                                fieldWithPath("items[].price").description("상품 가격"),
                                fieldWithPath("items[].optionPrice").description("옵션 가격"),
                                fieldWithPath("items[].deliveryFee").description("배송비"),
                                fieldWithPath("addressId").description("배송지 ID"),
                                fieldWithPath("totalPrice").description("총 결제 금액")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                ));
    }
}
