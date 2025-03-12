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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private MarketEntity market3;
    private CategoryEntity category1;
    private CategoryEntity category2;

    private static final String BASE_URL = "/api/markets";

    @BeforeEach
    void setUp() {
        marketCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        marketRepository.deleteAll();
        
        category1 = categoryRepository.save(CategoryEntity.builder()
                .name("쿠키")
                .build());

        category2 = categoryRepository.save(CategoryEntity.builder()
                .name("빵")
                .build());

        market1 = marketRepository.save(MarketEntity.builder()
                .name("상윤이네 쿠키")
                .description("쿠키를 판매하는 마켓")
                .thumbnail("market-a-thumbnail.jpg")
                .background("market-a-bg.jpg")
                .totalSales(500L)
                .totalReviews(100L)
                .build());

        market2 = marketRepository.save(MarketEntity.builder()
                .name("명인 강지원")
                .description("쿠키를 판매하는 마켓")
                .thumbnail("market-b-thumbnail.jpg")
                .background("market-b-bg.jpg")
                .totalSales(500L)
                .totalReviews(200L)
                .build());

        market3 = marketRepository.save(MarketEntity.builder()
                .name("도현베이커리")
                .description("빵을 판매하는 마켓")
                .thumbnail("market-b-thumbnail.jpg")
                .background("market-b-bg.jpg")
                .totalSales(100L)
                .totalReviews(70L)
                .build());

        marketCategoryRepository.save(MarketCategoryEntity.builder()
                .market(market1)
                .category(category1)
                .build());

        marketCategoryRepository.save(MarketCategoryEntity.builder()
                .market(market2)
                .category(category1)
                .build());

        marketCategoryRepository.save(MarketCategoryEntity.builder()
                .market(market3)
                .category(category2)
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
    void getMarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/categories/{categoryId}", category2.getId())
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

    @Test
    void getMarketById() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{marketId}", market1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("markets/get-market-by-id",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("marketId").description("조회할 마켓 ID")
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("id").description("마켓 ID"),
                                fieldWithPath("name").description("마켓 이름"),
                                fieldWithPath("description").description("마켓 한줄 소개"),
                                fieldWithPath("thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("background").description("마켓 배경 이미지 URL")
                        })));
    }

    @Test
    void getTop10MarketsByPopularity() throws Exception {
        mockMvc.perform(get(BASE_URL + "/top10")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("markets/get-top10-markets-by-popularity",
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
                                fieldWithPath("markets").description("마켓 목록"),
                                fieldWithPath("markets[].id").description("마켓 ID"),
                                fieldWithPath("markets[].name").description("마켓 이름"),
                                fieldWithPath("markets[].description").description("마켓 한줄 소개"),
                                fieldWithPath("markets[].thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("markets[].totalSales").description("총 판매 수량"),
                                fieldWithPath("markets[].totalReviews").description("총 리뷰 개수")
                        })));
    }

    @Test
    void getTop30MarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{categoryId}/top30", category1.getId())
                .param("page", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("markets/get-top30-markets-by-category",
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
                                fieldWithPath("markets").description("마켓 목록"),
                                fieldWithPath("markets[].id").description("마켓 ID"),
                                fieldWithPath("markets[].name").description("마켓 이름"),
                                fieldWithPath("markets[].description").description("마켓 한줄 소개"),
                                fieldWithPath("markets[].thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("markets[].totalSales").description("총 판매 수량"),
                                fieldWithPath("markets[].totalReviews").description("총 리뷰 개수")
                        })));
    }

    @Test
    void getRandomTop5MarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{categoryId}/top5", category2.getId())
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("markets/get-random-top5-markets-by-category",
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
                                fieldWithPath("markets").description("마켓 목록"),
                                fieldWithPath("markets[].id").description("마켓 ID"),
                                fieldWithPath("markets[].name").description("마켓 이름"),
                                fieldWithPath("markets[].description").description("마켓 한줄 소개"),
                                fieldWithPath("markets[].thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("markets[].totalSales").description("총 판매 수량"),
                                fieldWithPath("markets[].totalReviews").description("총 리뷰 개수")
                        })));
    }
}