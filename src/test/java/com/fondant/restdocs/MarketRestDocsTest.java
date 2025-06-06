package com.fondant.restdocs;

import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.market.domain.entity.MarketCategoryEntity;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.entity.MarketHashtagEntity;
import com.fondant.market.domain.repository.MarketHashtagRepository;
import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.test.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.fondant.global.response.CommonResponseFields.commonResponseFields;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
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

    @Autowired
    private MarketHashtagRepository marketHashtagRepository;

    @Autowired
    private UserTestRepository userRepository;

    @Autowired
    private MarketLikeTestRepository marketLikeRepository;


    private CategoryEntity categoryCookie;
    private CategoryEntity categoryBread;
    private CategoryEntity categoryBakedGoods;

    private static final String BASE_URL = "/api/markets";

    @BeforeEach
    void setUp() {
        marketCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        marketRepository.deleteAll();

        CategoryEntity categoryCookie = CategoryEntity.builder()
                .name("쿠키")
                .iconUrl("domain/icon-url/cookie")
                .build();

        CategoryEntity categoryBread = CategoryEntity.builder()
                .name("빵")
                .iconUrl("domain/icon-url/bread")
                .build();

        CategoryEntity categoryBakedGoods = CategoryEntity.builder()
                .name("구움과자")
                .iconUrl("domain/icon-url/baked")
                .build();

        categoryCookie.addChild(CategoryEntity.builder().name("초코쿠키").build());
        categoryCookie.addChild(CategoryEntity.builder().name("버터쿠키").build());

        categoryBread.addChild(CategoryEntity.builder().name("식빵").build());
        categoryBread.addChild(CategoryEntity.builder().name("모닝빵").build());

        categoryBakedGoods.addChild(CategoryEntity.builder().name("마들렌").build());
        categoryBakedGoods.addChild(CategoryEntity.builder().name("휘낭시에").build());

        categoryRepository.save(categoryCookie);
        categoryRepository.save(categoryBread);
        categoryRepository.save(categoryBakedGoods);

        int marketCount = 1;
        for (CategoryEntity category : List.of(categoryCookie, categoryBread, categoryBakedGoods)) {
            String categoryName = category.getName();

            for (CategoryEntity subCategory : category.getChildren()) {
                for (int i = 1; i <= 40; i++) {
                    MarketEntity market = marketRepository.save(MarketEntity.builder()
                            .name("마켓 " + marketCount)
                            .description(categoryName + "을 판매하는 마켓 " + marketCount)
                            .thumbnail("market-" + marketCount + "-thumbnail.jpg")
                            .background("market-" + marketCount + "-bg.jpg")
                            .totalSales(100L * marketCount)
                            .totalReviews(10L * marketCount)
                            .businessNumber("123-45-67890")
                            .instagramProfile("https://instagram.com/market" + marketCount)
                            .latitude(12.345 + marketCount * 0.001)
                            .longitude(123.4567 + marketCount * 0.001)
                            .address("부산광역시 용소로 " + marketCount + "번길")
                            .build());

                    marketCategoryRepository.save(MarketCategoryEntity.builder()
                            .market(market)
                            .category(subCategory)
                            .build());

                    marketCount++;
                }
            }
        }
    }

    @Test
    @WithMockCustomUser
    void getMarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/categories/{categoryId}", categoryBread.getId())
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
                        ).andWithPrefix("content.", new FieldDescriptor[]{
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
    @WithMockCustomUser
    void getMarketById() throws Exception {
        MarketEntity market = marketRepository.findAll().get(0);

        marketHashtagRepository.saveAll(List.of(
                MarketHashtagEntity.builder().market(market).name("기념일맞춤").build(),
                MarketHashtagEntity.builder().market(market).name("수제간식").build()
        ));

        mockMvc.perform(get(BASE_URL + "/{marketId}", market.getId())
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
                        ).andWithPrefix("content.", new FieldDescriptor[]{
                                fieldWithPath("id").description("마켓 ID"),
                                fieldWithPath("name").description("마켓 이름"),
                                fieldWithPath("description").description("마켓 한줄 소개"),
                                fieldWithPath("thumbnail").description("마켓 썸네일 이미지 URL"),
                                fieldWithPath("background").description("마켓 배경 이미지 URL"),
                                fieldWithPath("liked").description("유저의 좋아요 여부"),
                                fieldWithPath("likeCount").description("마켓 좋아요 총 개수"),
                                fieldWithPath("isTop10").description("카테고리별 인기 마켓 TOP10 여부"),
                                fieldWithPath("hashtags").description("해시태그 목록 (최대 5개)"),
                                fieldWithPath("subCategoryIds").description("마켓이 취급하는 서브 카테고리 ID 목록"),
                                fieldWithPath("profile").description("마켓 상세 정보 목록")
                        }).andWithPrefix("content.profile.", new FieldDescriptor[]{
                                fieldWithPath("businessNumber").description("사업자 등록번호"),
                                fieldWithPath("instagramProfile").description("인스타그램 프로필 링크"),
                                fieldWithPath("latitude").description("마켓 위치 위도"),
                                fieldWithPath("longitude").description("마켓 위치 경도"),
                                fieldWithPath("address").description("마켓 주소"),
                        })));
    }

    @Test
    @WithMockCustomUser
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
                        ).andWithPrefix("content.", new FieldDescriptor[]{
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
    @WithMockCustomUser
    void getTop30MarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{categoryId}/top30", categoryCookie.getId())
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
                        ).andWithPrefix("content.", new FieldDescriptor[]{
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
    @WithMockCustomUser
    void getRandomTop5MarketsByCategory() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{categoryId}/top5", categoryCookie.getId())
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
                        ).andWithPrefix("content.", new FieldDescriptor[]{
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