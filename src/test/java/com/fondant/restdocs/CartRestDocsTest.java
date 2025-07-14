package com.fondant.restdocs;

import com.fondant.cart.domain.entity.*;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.test.repository.*;
import com.fondant.user.domain.entity.*;
import com.fondant.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class CartRestDocsTest {

    @Autowired private MockMvc mockMvc;
    @MockitoSpyBean private JWTUtil jwtUtil;

    @Autowired private CartTestRepository cartTestRepository;
    @Autowired private CartMarketTestRepository cartMarketTestRepository;
    @Autowired private CartItemTestRepository cartItemTestRepository;
    @Autowired private CartItemOptionTestRepository cartItemOptionTestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MarketTestRepository marketTestRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OptionRepository optionRepository;

    private UserEntity testUser;
    private String mockToken;
    private static final String BASE_URL = "/api/cart";

    @BeforeEach
    void setUp() {
        mockToken = "jwtToken";

        testUser = userRepository.save(
                UserEntity.builder()
                        .snsType(SNSType.NAVER)
                        .name("test-user")
                        .phoneNumber("010-0000-0000")
                        .email("test-user@example.com")
                        .birth(Date.valueOf(LocalDate.of(2001, 1, 1)))
                        .nickname("test-user-nickname")
                        .profileUrl("https://example.com")
                        .createAt(Date.valueOf(LocalDate.of(2025, 1, 1)).toLocalDate())
                        .gender(Gender.FEMALE)
                        .role(UserRole.USER)
                        .build()
        );

        mockToken = jwtUtil.generateToken("access", testUser.getId(), "USER", 60 * 10 * 1000L);

        MarketEntity market = marketTestRepository.save(
                MarketEntity.builder()
                        .name("달미롱")
                        .thumbnail("thumbnail.jpg")
                        .background("bg.jpg")
                        .freeDeliveryLimit(3500.0)
                        .deliveryFee(3000.0)
                        .build()
        );

        ProductEntity product1 = productRepository.save(
                ProductEntity.builder()
                        .name("쫀득쿠키")
                        .description("옵션이 없는 상품입니다.")
                        .thumbnail("thumbnail.jpg")
                        .price(5000.0)
                        .market(market)
                        .startDate(LocalDate.now())
                        .build()
        );

        ProductEntity product2 = productRepository.save(
                ProductEntity.builder()
                        .name("딸기모찌")
                        .description("옵션이 있는 상품입니다.")
                        .thumbnail("thumbnail.jpg")
                        .price(8000.0)
                        .market(market)
                        .startDate(LocalDate.now())
                        .build()
        );

        OptionEntity option1 = optionRepository.save(
                OptionEntity.builder()
                        .name("3개 세트")
                        .price(2000.0)
                        .productId(product2.getId())
                        .build()
        );

        OptionEntity option2 = optionRepository.save(
                OptionEntity.builder()
                        .name("5개 세트")
                        .price(3000.0)
                        .productId(product2.getId())
                        .build()
        );

        CartEntity cart = cartTestRepository.save(
                CartEntity.builder()
                        .user(testUser)
                        .build()
        );

        CartMarketEntity cartMarket = cartMarketTestRepository.save(
                CartMarketEntity.builder()
                        .cart(cart)
                        .market(market)
                        .build()
        );

        CartItemEntity item1 = cartItemTestRepository.save(
                CartItemEntity.builder()
                        .cartMarket(cartMarket)
                        .product(product1)
                        .quantity(1)
                        .arrivalDate(LocalDate.now().plusDays(3))
                        .build()
        );

        CartItemEntity item2 = cartItemTestRepository.save(
                CartItemEntity.builder()
                        .cartMarket(cartMarket)
                        .product(product2)
                        .quantity(1)
                        .arrivalDate(LocalDate.now().plusDays(3))
                        .build()
        );


        CartItemOptionEntity itemOption1 = CartItemOptionEntity.builder()
                .option(option1)
                .quantity(1)
                .build();
        item2.addCartItemOption(itemOption1);

        CartItemOptionEntity itemOption2 = CartItemOptionEntity.builder()
                .option(option2)
                .quantity(3)
                .build();
        item2.addCartItemOption(itemOption2);

        cartItemOptionTestRepository.save(itemOption1);
        cartItemOptionTestRepository.save(itemOption2);
    }

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("content").description("응답 데이터")
        };
    }

    @Test
    @WithMockCustomUser
    void getCartItems() throws Exception {
        mockMvc.perform(get(BASE_URL + "/list")
                        .param("page", "0")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("cart/get-cart-items",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("content.", new FieldDescriptor[] {
                                fieldWithPath("pageInfo").description("페이지 정보"),
                                fieldWithPath("pageInfo.currentPage").description("현재 페이지 번호"),
                                fieldWithPath("pageInfo.totalPage").description("총 페이지 수"),
                                fieldWithPath("markets[].marketId").description("마켓 ID"),
                                fieldWithPath("markets[].marketName").description("마켓 이름"),
                                fieldWithPath("markets[].freeDeliveryLimit").description("해당 마켓의 무료 배송 기준 금액"),
                                fieldWithPath("markets[].products").description("상품 목록"),
                                fieldWithPath("markets[].products[].productId").description("상품 ID"),
                                fieldWithPath("markets[].products[].productName").description("상품 이름"),
                                fieldWithPath("markets[].products[].basePrice").description("상품 기본 가격"),
                                fieldWithPath("markets[].products[].thumbnail").description("상품 썸네일 URL"),
                                fieldWithPath("markets[].products[].quantity").description("상품 수량"),
                                fieldWithPath("markets[].products[].arrivalDate").description("도착 예정일"),
                                fieldWithPath("markets[].products[].options").description("옵션 목록 (없으면 빈 배열)").type(JsonFieldType.ARRAY).optional(),
                                fieldWithPath("markets[].products[].options[].optionId").type(JsonFieldType.NUMBER).description("옵션 ID"),
                                fieldWithPath("markets[].products[].options[].optionName").type(JsonFieldType.STRING).description("옵션 이름"),
                                fieldWithPath("markets[].products[].options[].additionalPrice").type(JsonFieldType.NUMBER).description("옵션 추가 금액"),
                                fieldWithPath("markets[].products[].options[].quantity").type(JsonFieldType.NUMBER).description("옵션 수량")
                        })));
    }

    @Test
    @WithMockCustomUser
    void addCartItem() throws Exception {
        ProductEntity productWithOption = productRepository.findAll()
                .stream().filter(p -> p.getName().equals("딸기모찌")).findFirst().orElseThrow();

        OptionEntity option1 = optionRepository.findAll()
                .stream().filter(o -> o.getName().equals("3개 세트")).findFirst().orElseThrow();
        OptionEntity option2 = optionRepository.findAll()
                .stream().filter(o -> o.getName().equals("5개 세트")).findFirst().orElseThrow();

        mockMvc.perform(RestDocumentationRequestBuilders.post(BASE_URL + "/add")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "productId": %d,
                      "quantity": 2,
                      "options": [
                        {
                          "optionId": %d,
                          "quantity": 1
                        },
                        {
                          "optionId": %d,
                          "quantity": 2
                        }
                      ]
                    }
                    """.formatted(productWithOption.getId(), option1.getId(), option2.getId())))
                .andExpect(status().isOk())
                .andDo(document("cart/add-cart-item",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("productId").description("상품 ID"),
                                fieldWithPath("quantity").description("상품 수량"),
                                fieldWithPath("options").description("선택된 옵션 리스트").optional(),
                                fieldWithPath("options[].optionId").description("옵션 ID"),
                                fieldWithPath("options[].quantity").description("옵션 수량")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("content.productId").description("추가된 상품 ID"),
                                fieldWithPath("content.productName").description("추가된 상품명"),
                                fieldWithPath("content.quantity").description("추가된 수량"),
                                fieldWithPath("content.options[].optionId").description("옵션 ID"),
                                fieldWithPath("content.options[].optionName").description("옵션 이름"),
                                fieldWithPath("content.options[].quantity").description("옵션 수량")
                        )
                ));
    }

    @Test
    @WithMockCustomUser
    void updateCartItemQuantity() throws Exception {
        CartItemEntity cartItem = cartItemTestRepository.findAll().get(0);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/api/cart/items/{cartItemId}", cartItem.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "quantity": 5
                        }
                    """)
                )
                .andExpect(status().isOk())
                .andDo(document("cart/update-cart-item",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("quantity").description("수정할 수량")
                        ),
                        pathParameters(
                                parameterWithName("cartItemId").description("장바구니 항목 ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("content.productId").type(JsonFieldType.NUMBER).description("수정된 상품 ID"),
                                fieldWithPath("content.productName").type(JsonFieldType.STRING).description("상품 이름"),
                                fieldWithPath("content.quantity").type(JsonFieldType.NUMBER).description("수정된 수량"),
                                fieldWithPath("content.options").type(JsonFieldType.ARRAY).description("옵션 목록 (없으면 빈 배열)").optional(),
                                fieldWithPath("content.options[].optionId").type(JsonFieldType.NUMBER).description("옵션 ID").optional(),
                                fieldWithPath("content.options[].optionName").type(JsonFieldType.STRING).description("옵션 이름").optional(),
                                fieldWithPath("content.options[].quantity").type(JsonFieldType.NUMBER).description("옵션 수량").optional()
                        )
                ));
    }

    @Test
    @WithMockCustomUser
    void deleteCartItem() throws Exception {
        CartItemEntity cartItem = cartItemTestRepository.findAll().get(0);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete(BASE_URL + "/delete/{cartItemId}", cartItem.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("cart/delete-cart-item",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("cartItemId").description("장바구니 항목 ID")
                        ),
                        responseFields(commonResponseFields())
                ));
    }
}