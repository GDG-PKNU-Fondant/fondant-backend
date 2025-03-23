package com.fondant.restdocs;

import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.market.domain.entity.MarketCategoryEntity;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.test.repository.CategoryTestRepository;
import com.fondant.test.repository.MarketCategoryTestRepository;
import com.fondant.test.repository.MarketTestRepository;
import com.fondant.test.repository.UserTestRepository;
import com.fondant.user.domain.entity.Gender;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;

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

    @Autowired
    private UserTestRepository userRepository;

    @MockitoSpyBean
    private JWTUtil jwtUtil;

    private CategoryEntity categoryCookie;
    private CategoryEntity categoryBread;
    private CategoryEntity categoryBakedGoods;
    private UserEntity testUser;
    private String mockToken;

    private static final String BASE_URL = "/api/markets";

    @BeforeEach
    void setUp() {
        marketCategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        marketRepository.deleteAll();

        categoryCookie = categoryRepository.save(
                CategoryEntity.builder().name("쿠키").build());
        categoryBread = categoryRepository.save(
                CategoryEntity.builder().name("빵").build());
        categoryBakedGoods = categoryRepository.save(
                CategoryEntity.builder().name("구움과자").build());

        int marketCount = 1;
        for (CategoryEntity category : Arrays.asList(categoryCookie, categoryBread, categoryBakedGoods)) {
            String categoryName;
            if (category == categoryCookie) {
                categoryName = "쿠키";
            } else if (category == categoryBread) {
                categoryName = "빵";
            } else {
                categoryName = "구움과자";
            }

            for (int i = 1; i <= 40; i++) {
                MarketEntity market = marketRepository.save(MarketEntity.builder()
                        .name("마켓 " + marketCount)
                        .description(categoryName + "을 판매하는 마켓 " + marketCount)
                        .thumbnail("market-" + marketCount + "-thumbnail.jpg")
                        .background("market-" + marketCount + "-bg.jpg")
                        .totalSales(100L * marketCount)
                        .totalReviews(10L * marketCount)
                        .build());

                marketCategoryRepository.save(MarketCategoryEntity.builder()
                        .market(market)
                        .category(category)
                        .build());
                marketCount++;
            }
        }

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
        mockMvc.perform(get(BASE_URL + "/categories/{categoryId}", categoryBread.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
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
        MarketEntity market = marketRepository.findAll().get(0);
        mockMvc.perform(get(BASE_URL + "/{marketId}", market.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
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
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
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
        mockMvc.perform(get(BASE_URL + "/{categoryId}/top30", categoryCookie.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
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
        mockMvc.perform(get(BASE_URL + "/{categoryId}/top5", categoryCookie.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
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