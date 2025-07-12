package com.fondant.restdocs;

import com.fondant.cart.application.CartService;
import com.fondant.cart.domain.entity.*;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.test.repository.*;
import com.fondant.user.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

import static java.sql.JDBCType.ARRAY;
import static javax.management.openmbean.SimpleType.STRING;
import static javax.swing.text.html.parser.DTDConstants.NUMBER;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class CartRestDocsTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JWTUtil jwtUtil;

    @Autowired private CartTestRepository cartTestRepository;
    @Autowired private CartMarketTestRepository cartMarketTestRepository;
    @Autowired private CartItemTestRepository cartItemTestRepository;
    @Autowired private CartItemOptionTestRepository cartItemOptionTestRepository;

    @Autowired private UserTestRepository userTestRepository;
    @Autowired private MarketTestRepository marketTestRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OptionRepository optionRepository;
    @Autowired private CartService cartService;
    private UserEntity user;

    private static final String BASE_URL = "/api/cart";
    private String mockToken;

    @BeforeEach
    void setUp() {
        user = userTestRepository.save(
                UserEntity.builder()
                        .snsType(SNSType.KAKAO)
                        .name("테스트 유저")
                        .email("test@user.com")
                        .nickname("testuser")
                        .birth(Date.valueOf("2000-01-01"))
                        .gender(Gender.FEMALE)
                        .role(UserRole.USER)
                        .phoneNumber("01012345678")
                        .createAt(LocalDate.now())
                        .profileUrl("http://profile.img")
                        .build()
        );

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
                        .user(user)
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

        mockToken = jwtUtil.generateToken("access", user.getId(), user.getRole().name(), 600000L);
    }

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("content").description("응답 데이터")
        };
    }

    @Test
    void getCartItems() throws Exception {
        mockMvc.perform(get(BASE_URL + "/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .param("page", "0")
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
}