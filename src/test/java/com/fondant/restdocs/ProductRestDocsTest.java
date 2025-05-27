package com.fondant.restdocs;


import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketCategoryEntity;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.product.domain.entity.*;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductImageRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.test.repository.*;
import com.fondant.user.domain.entity.Gender;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

import static com.fondant.global.response.CommonResponseFields.commonResponseFields;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class ProductRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

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
    private UserTestRepository userRepository;

    @Autowired
    private MarketTestRepository marketTestRepository;

    @Autowired
    private MarketCategoryTestRepository marketCategoryRepository;

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
    private ProductImageEntity productDetail1;
    private OptionEntity option1;
    private UserEntity testUser;
    private String mockToken;

    private static final String BASE_URL = "/api/product";

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

        category1 = categoryRepository.save(CategoryEntity.builder()
                .name("초콜릿")
                .iconUrl("domain/icon-url/chocolate")
                .build());

        category1.addChild(CategoryEntity.builder()
                .name("화이트초콜릿")
                .build());

        category1.addChild(CategoryEntity.builder()
                .name("다크초콜릿")
                .build());

        categoryRepository.save(category1);

        category2 = categoryRepository.save(CategoryEntity.builder()
                .name("쿠키")
                .iconUrl("domain/icon-url/cookie")
                .build());

        category2.addChild(CategoryEntity.builder()
                .name("르벵쿠키")
                .build());

        category2.addChild(CategoryEntity.builder()
                .name("비건쿠키")
                .build());

        categoryRepository.save(category2);

        product1 = productRepository.save(ProductEntity.builder()
                .name("두바이 초콜릿")
                .description("카다이프 듬뿍 두바이 초콜릿입니다.")
                .thumbnail("product-thumbnail.png")
                .price(15000.0)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(50)
                .discountRate(0.0)
                .build());

        product2 = productRepository.save(ProductEntity.builder()
                .name("헤이즐넛 쿠키")
                .description("헤이즐넛 쿠키 입니다.")
                .thumbnail("product-thumbnail.png")
                .price(15000.0)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(50)
                .discountRate(0.0)
                .build());

        categoryProduct1 = productCategoryRepository.save(ProductCategoryEntity.builder()
                .product(product1)
                .category(category1.getChildren().get(1))
                .build());

        productImage1 = productImageRepository.save(ProductImageEntity.builder()
                .productId(product1.getId())
                .imageUrl("test-image.png")
                .imageType(ImageType.PRODUCT_PHOTO)
                .imgOrder(1)
                .build());

        productImage2 = productImageRepository.save(ProductImageEntity.builder()
                .productId(product1.getId())
                .imageUrl("test-image.png")
                .imageType(ImageType.PRODUCT_PHOTO)
                .imgOrder(2)
                .build());

        productDetail1 = productImageRepository.save(ProductImageEntity.builder()
                .productId(product1.getId())
                .imageUrl("test-detail-page.png")
                .imageType(ImageType.DETAIL_PAGE)
                .imgOrder(1)
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

        marketCategoryRepository.save(MarketCategoryEntity.builder()
                .market(market)
                .category(category1)
                .build());

    }

    @Test
    void getProductsByMarketAndCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{marketId}/{categoryId}", market.getId(), category1.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("products/get-products-by-market-and-category",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("marketId").description("조회할 마켓의 ID"),
                                parameterWithName("categoryId").description("조회할 카테고리의 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)")
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("content.", new FieldDescriptor[] {
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

    @Test
    void getProductDetails() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{productId}", product1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andDo(document("products/get-product-details",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("productId").description("조회할 상품의 ID")
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("content.", new FieldDescriptor[] {
                                fieldWithPath("photos").description("상품 사진 정보 목록"),
                                fieldWithPath("photos[].imgUrl").description("상품 사진 개별 URL"),
                                fieldWithPath("photos[].imgOrder").description("상품 사진 순서"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("options").description("상품 옵션 목록").optional(),
                                fieldWithPath("options[].id").description("옵션 ID").optional(),
                                fieldWithPath("options[].name").description("옵션 이름").optional(),
                                fieldWithPath("options[].price").description("옵션 가격").optional(),
                                fieldWithPath("description").description("상품 설명"),
                                fieldWithPath("detailPages").description("상품 상세 페이지 이미지 정보 목록"),
                                fieldWithPath("detailPages[].imgUrl").description("상품 상세 페이지 이미지 URL"),
                                fieldWithPath("detailPages[].imgOrder").description("상품 상세 페이지 이미지 순서"),
                                fieldWithPath("marketInfo").description("마켓 목록"),
                                fieldWithPath("marketInfo.id").description("마켓 ID"),
                                fieldWithPath("marketInfo.name").description("마켓 이름"),
                                fieldWithPath("marketInfo.description").description("마켓 한줄 소개"),
                                fieldWithPath("marketInfo.thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("marketInfo.totalSales").description("총 판매 수량"),
                                fieldWithPath("marketInfo.totalReviews").description("총 리뷰 개수"),
                                fieldWithPath("marketInfo.freeDeliveryLimit").description("무료배송 기준"),
                                fieldWithPath("basePrice").description("상품 기본 가격 (옵션 가격 추가 전)"),
                        })));
    }
}

