package com.fondant.restdocs;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

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
    @Autowired private CartMarketTestRepository cartMarketEntityTestRepository;
    @Autowired private CartItemTestRepository cartItemEntityTestRepository;
    @Autowired private CartItemOptionTestRepository cartItemOptionTestRepository;

    @Autowired private UserTestRepository userTestRepository;
    @Autowired private MarketTestRepository marketTestRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OptionRepository optionRepository;

    private static final String BASE_URL = "/api/cart";
    private String mockToken;

    @BeforeEach
    void setUp() {
        UserEntity user = userTestRepository.save(
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
                        .build());

        MarketEntity market = marketTestRepository.save(MarketEntity.builder()
                .name("마켓1")
                .description("마켓 설명")
                .thumbnail("thumb.jpg")
                .background("bg.jpg")
                .build());

        ProductEntity product1 = productRepository.save(ProductEntity.builder()
                .name("옵션 없는 상품")
                .description("설명")
                .thumbnail("no-option.jpg")
                .price(10000)
                .market(market)
                .startDate(LocalDate.now())
                .maxCount(100)
                .build());

        ProductEntity product2 = productRepository.save(ProductEntity.builder()
                .name("옵션 있는 상품")
                .description("설명")
                .thumbnail("with-option.jpg")
                .price(12000)
                .market(market)
                .startDate(LocalDate.now())
                .maxCount(100)
                .build());

        OptionEntity option = optionRepository.save(OptionEntity.builder()
                .name("기본 옵션")
                .productId(product2.getId())
                .price(3000)
                .build());

        CartEntity cart = cartTestRepository.save(CartEntity.builder().user(user).build());
        CartMarketEntity cartMarket = cartMarketEntityTestRepository.save(CartMarketEntity.builder()
                .cart(cart)
                .market(market)
                .build());

        CartItemEntity item1 = cartItemEntityTestRepository.save(CartItemEntity.builder()
                .cartMarket(cartMarket)
                .product(product1)
                .quantity(1)
                .build());

        CartItemEntity item2 = cartItemEntityTestRepository.save(CartItemEntity.builder()
                .cartMarket(cartMarket)
                .product(product2)
                .quantity(2)
                .build());

        cartItemOptionTestRepository.save(CartItemOptionEntity.builder()
                .cartItem(item2)
                .option(option)
                .quantity(2)
                .build());

        mockToken = jwtUtil.generateToken("access", user.getId(), user.getRole().name(), 600000L);
    }

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @Test
    void getCartItems() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("cart/get-cart-items",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("pageInfo").description("페이지 정보"),
                                fieldWithPath("pageInfo.currentPage").description("현재 페이지 번호"),
                                fieldWithPath("pageInfo.totalPage").description("총 페이지 수"),
                                fieldWithPath("cartItems[].cartItemId").description("장바구니 아이템 ID"),
                                fieldWithPath("cartItems[].marketId").description("마켓 ID"),
                                fieldWithPath("cartItems[].marketName").description("마켓 이름"),
                                fieldWithPath("cartItems[].productId").description("상품 ID"),
                                fieldWithPath("cartItems[].productName").description("상품 이름"),
                                fieldWithPath("cartItems[].quantity").description("수량"),
                                fieldWithPath("cartItems[].arrivalDate").description("도착 예정일"),
                                fieldWithPath("cartItems[].options").description("선택 옵션 목록 (옵션이 없으면 null)"),
                                fieldWithPath("cartItems[].options[].optionId")
                                        .optional().type("Number").description("옵션 ID"),
                                fieldWithPath("cartItems[].options[].optionName")
                                        .optional().type("String").description("옵션 이름"),
                                fieldWithPath("cartItems[].options[].optionPrice")
                                        .optional().type("Number").description("옵션 가격"),
                                fieldWithPath("cartItems[].options[].quantity")
                                        .optional().type("Number").description("옵션 수량")
                        })));
    }
}