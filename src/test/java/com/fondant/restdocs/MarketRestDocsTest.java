package com.fondant.restdocs;

import com.fondant.market.domain.entity.MarketCategoryEntity;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.domain.entity.CategoryEntity;
import com.fondant.test.repository.CategoryTestRepository;
import com.fondant.test.repository.MarketCategoryTestRepository;
import com.fondant.test.repository.MarketTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static com.fondant.restdocs.ProductRestDocsTest.commonResponseFields;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class MarketRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarketTestRepository marketRepository;

    @Autowired
    private CategoryTestRepository categoryRepository;

    @Autowired
    private MarketCategoryTestRepository marketCategoryRepository;

    private MarketEntity market1;
    private MarketEntity market2;
    private CategoryEntity category;

    private static final String BASE_URL = "/api/markets";

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(CategoryEntity.builder()
                .name("쿠키")
                .build());

        market1 = marketRepository.save(MarketEntity.builder()
                .name("Market A")
                .description("쿠키를 판매하는 마켓 A")
                .thumbnail("market-a-thumbnail.jpg")
                .background("market-a-bg.jpg")
                .build());

        market2 = marketRepository.save(MarketEntity.builder()
                .name("Market B")
                .description("쿠키를 판매하는 마켓 B")
                .thumbnail("market-b-thumbnail.jpg")
                .background("market-b-bg.jpg")
                .build());

        marketCategoryRepository.save(MarketCategoryEntity.builder()
                .market(market1)
                .category(category)
                .build());

        marketCategoryRepository.save(MarketCategoryEntity.builder()
                .market(market2)
                .category(category)
                .build());
    }

    @Test
    void getMarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/categories/{categoryId}", category.getId())
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("markets/get-markets-by-category",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("categoryId").description("조회할 카테고리 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)")
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("pageInfo").description("페이지 정보"),
                                fieldWithPath("pageInfo.currentPage").description("현재 페이지"),
                                fieldWithPath("pageInfo.totalPage").description("전체 페이지"),
                                fieldWithPath("markets[].id").description("마켓 ID"),
                                fieldWithPath("markets[].name").description("마켓 이름"),
                                fieldWithPath("markets[].description").description("마켓 한줄 소개"),
                                fieldWithPath("markets[].thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("markets[].totalSales").description("마켓의 총 판매량"),
                                fieldWithPath("markets[].totalReviews").description("마켓의 총 리뷰 수")
                        })));
    }
}