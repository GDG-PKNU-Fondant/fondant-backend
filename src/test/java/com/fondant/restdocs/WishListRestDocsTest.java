package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.product.domain.entity.*;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductImageRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.test.repository.CategoryTestRepository;
import com.fondant.test.repository.MarketTestRepository;
import com.fondant.test.repository.ProductCategoryTestRepository;
import com.fondant.test.repository.UserTestRepository;
import com.fondant.user.domain.entity.Gender;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import com.fondant.wishlist.application.dto.request.WishListRegistRequest;
import com.fondant.wishlist.domain.entity.WishListEntity;
import com.fondant.wishlist.domain.repository.WishListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
@ExtendWith(MockitoExtension.class)
public class WishListRestDocsTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MarketTestRepository marketRepository;

    @Autowired
    private CategoryTestRepository categoryRepository;

    @Autowired
    private ProductCategoryTestRepository productCategoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private UserTestRepository userRepository;

    @MockitoSpyBean
    private JWTUtil jwtUtil;


    private MarketEntity market;
    private CategoryEntity category1;
    private CategoryEntity category2;
    private ProductCategoryEntity categoryProduct1;
    private ProductCategoryEntity categoryProduct2;
    private ProductEntity product1;
    private ProductEntity product2;
    private ProductImageEntity productImage1;
    private ProductImageEntity productImage2;
    private OptionEntity option1;
    private UserEntity testUser;
    private WishListEntity wishList1;
    private String mockToken;

    private static final String BASE_URL = "/api/wishlist";

    @BeforeEach
    void setUp() {
        mockToken = "jwtToken";

        market = marketRepository.save(MarketEntity.builder()
                .name("퐁당 마켓")
                .description("신메뉴 업데이트 매달 1일 ! 전국 택배가능 초콜릿 쿠키 전문 퐁당 마켓")
                .thumbnail("test-thumbnail.png")
                .background("test-background.png")
                .build()
        );

        category1 = categoryRepository.save(CategoryEntity.builder()
                .name("초콜릿")
                .build());

        category2 = categoryRepository.save(CategoryEntity.builder()
                .name("쿠키")
                .build());

        product1 = productRepository.save(ProductEntity.builder()
                .name("두바이 초콜릿")
                .description("카다이프 듬뿍 두바이 초콜릿입니다.")
                .thumbnail("product-thumbnail.png")
                .price(15000.0)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(50)
                .build());

        product2 = productRepository.save(ProductEntity.builder()
                .name("헤이즐넛 쿠키")
                .description("헤이즐넛 쿠키 입니다.")
                .thumbnail("product-thumbnail.png")
                .price(15000.0)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(50)
                .build());

        categoryProduct1 = productCategoryRepository.save(ProductCategoryEntity.builder()
                .product(product1)
                .category(category1)
                .build());

        productImage1 = productImageRepository.save(ProductImageEntity.builder()
                .productId(product1.getId())
                .imageUrl("test-image.png")
                .imageType(ImageType.PRODUCT_PHOTO)
                .build());

        productImage2 = productImageRepository.save(ProductImageEntity.builder()
                .productId(product1.getId())
                .imageUrl("test-detail-page.png")
                .imageType(ImageType.DETAIL_PAGE)
                .build());

        option1 = optionRepository.save(
                OptionEntity.builder()
                        .name("3개 세트")
                        .productId(product1.getId())
                        .price(20000.0)
                        .build());

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

        wishList1 = wishListRepository.save(
                WishListEntity.builder()
                        .product(product1)
                        .userId(testUser.getId())
                        .build()
        );
    }


    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @Test
    void registerWishListTest() throws Exception {
        // Given
        WishListRegistRequest request = new WishListRegistRequest(
                testUser.getId(),
                product1.getId()
        );

        //Then
        mockMvc.perform(post(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("wishlist/post-wishlist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("userId").description("유저 ID"),
                                fieldWithPath("productId").description("관심상품으로 등록할 상품 ID")
                        ),
                        responseFields(
                                commonResponseFields()
                        )));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void getWishList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .with(user("testUser").roles("USER"))
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("wishlist/get-wishlist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)")
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("pageInfo").description("페이지 정보"),
                                fieldWithPath("pageInfo.currentPage").description("현재 페이지"),
                                fieldWithPath("pageInfo.totalPage").description("전체 페이지"),
                                fieldWithPath("products[].id").description("상품 ID"),
                                fieldWithPath("products[].name").description("상품 이름"),
                                fieldWithPath("products[].price").description("상품 가격"),
                                fieldWithPath("products[].score").description("리뷰 평점"),
                                fieldWithPath("products[].thumbnailUrl").description("상품 썸네일 URL"),
                                fieldWithPath("products[].discountRate").description("상품 할인율"),
                                fieldWithPath("products[].discountPrice").description("상품 할인 후 가격")
                        })));
    }
}
