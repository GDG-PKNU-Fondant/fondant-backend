package com.fondant.restdocs;

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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Transactional
public class CategoryRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryTestRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserTestRepository userRepository;

    @MockitoSpyBean
    private JWTUtil jwtUtil;

    private CategoryEntity category1;
    private CategoryEntity category2;
    private UserEntity testUser;
    private String mockToken;

    private static final String BASE_URL = "/api/category";

    @BeforeEach
    void setUp() {
        category1 = categoryRepository.save(CategoryEntity.builder()
                .name("초콜릿")
                .build());

        category1.addChild(CategoryEntity.builder()
                .name("화이트초콜릿")
                .build());

        category1.addChild(CategoryEntity.builder()
                .name("다크초콜릿")
                .build());

        category2 = categoryRepository.save(CategoryEntity.builder()
                .name("쿠키")
                .build());

        category2.addChild(CategoryEntity.builder()
                .name("르벵쿠키")
                .build());

        category2.addChild(CategoryEntity.builder()
                .name("비건쿠키")
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

    }

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    @Test
    void getAllCategories() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andDo(document("categories/get-all-category",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[] {
                                fieldWithPath("categories[]").description("대분류 카테고리 목록"),
                                fieldWithPath("categories[].id").description("대분류 카테고리 아이디"),
                                fieldWithPath("categories[].name").description("대분류 카테고리 이름"),
                                fieldWithPath("categories[].subCategories[]").description("소분류 카테고리 목록"),
                                fieldWithPath("categories[].subCategories[].id").description("소분류 카테고리 아이디"),
                                fieldWithPath("categories[].subCategories[].name").description("소분류 카테고리 이름"),
                        })));
    }
}
