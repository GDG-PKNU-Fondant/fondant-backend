package com.fondant.restdocs;


import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.domain.entity.CategoryEntity;
import com.fondant.product.domain.entity.ProductCategoryEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.test.repository.CategoryTestRepository;
import com.fondant.test.repository.MarketTestRepository;
import com.fondant.test.repository.ProductCategoryTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

    private MarketEntity market;
    private CategoryEntity category1;
    private CategoryEntity category2;
    private ProductCategoryEntity categoryProduct1;
    private ProductCategoryEntity categoryProduct2;
    private ProductEntity product1;
    private ProductEntity product2;

    private static final String BASE_URL = "/api/product";

    @BeforeEach
    void setUp() {
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
                .price(15000)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(50)
                .build());

        product2 = productRepository.save(ProductEntity.builder()
                .name("헤이즐넛 쿠키")
                .description("헤이즐넛 쿠키 입니다.")
                .thumbnail("product-thumbnail.png")
                .price(15000)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(50)
                .build());

        categoryProduct1 = productCategoryRepository.save(ProductCategoryEntity.builder()
                .product(product1)
                .category(category1)
                .build());
    }

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @Test
    void getProductsByMarketAndCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{marketId}/{categoryId}", market.getId(), category1.getId())
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("products/get-products-by-market-and-category",
                        preprocessRequest(prettyPrint()),  // 요청 JSON 정렬
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
                        ).andWithPrefix("response.", new FieldDescriptor[] {
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

