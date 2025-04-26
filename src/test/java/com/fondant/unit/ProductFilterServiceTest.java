package com.fondant.unit;

import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.product.application.ProductService;
import com.fondant.product.application.dto.FilterInfo;
import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.product.category.domain.repository.CategoryRepository;
import com.fondant.product.domain.entity.ProductCategoryEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.ProductCategoryRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@DisplayName("상품 필터링 카운트 테스트")
public class ProductFilterServiceTest{
    @Autowired
    private ProductService productFilterService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    private MarketEntity market;
    private CategoryEntity category1;
    private CategoryEntity category2;
    private ProductEntity product1;
    private ProductEntity product2;

    @BeforeEach
    void setUp() {
        // 마켓 생성
        market = createMarket();

        // 대분류 및 소분류 카테고리 생성
        category1 = createCategoryWithSubCategories("초콜릿", "domain/icon/chocolate", List.of("다크초콜릿", "화이트초콜릿"));
        category2 = createCategoryWithSubCategories("쿠키", "domain/icon/cookie", List.of("르뱅쿠키", "비건쿠키"));

        // 상품 생성
        product1 = createProduct("다크초콜릿 바", 12000, "box", "free_shipping", market);
        product2 = createProduct("르뱅 쿠키", 8000, "bag", "discount", market);

        // 상품과 소분류 연결
        linkProductToSubCategory(product1, category1.getChildren().get(0)); // 다크초콜릿
        linkProductToSubCategory(product2, category2.getChildren().get(0)); // 르뱅쿠키
    }

    private MarketEntity createMarket() {
        return marketRepository.save(MarketEntity.builder()
                .name("테스트 마켓")
                .description("테스트용 마켓입니다.")
                .thumbnail("test-market-thumbnail.png")
                .background("test-market-background.png")
                .totalSales(0L)
                .totalReviews(0L)
                .build());
    }

    private CategoryEntity createCategoryWithSubCategories(String mainCategoryName, String iconUrl, List<String> subCategoryNames) {
        CategoryEntity mainCategory = CategoryEntity.builder()
                .name(mainCategoryName)
                .iconUrl(iconUrl)
                .build();

        for (String subName : subCategoryNames) {
            mainCategory.addChild(CategoryEntity.builder()
                    .name(subName)
                    .build());
        }

        return categoryRepository.save(mainCategory);
    }

    private ProductEntity createProduct(String name, int price, String packagingType, String benefit, MarketEntity market) {
        return productRepository.save(ProductEntity.builder()
                .name(name)
                .description(name + " 설명입니다.")
                .thumbnail("product-thumbnail.png")
                .price(price)
                .market(market)
                .startDate(LocalDate.of(2025, 1, 1))
                .maxCount(100)
                //.packagingType(packagingType)
                //.benefit(benefit)
                .build());
    }

    private void linkProductToSubCategory(ProductEntity product, CategoryEntity subCategory) {
        productCategoryRepository.save(ProductCategoryEntity.builder()
                .product(product)
                .category(subCategory)
                .build());
    }

    @Test
    @DisplayName("가격 범위로 상품 수 필터링")
    void countProductsByPriceFilter_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.of(10000.0),
                Optional.of(13000.0),
                List.of(),
                List.of(),
                List.of()
        );

        // when
        Long count = productFilterService.getFilteredProductCounts(filterInfo).count();

        // then
        assertThat(count).isEqualTo(1L);
    }

    // 사용예정 : 포장타입 및 혜택 추가 예정
    /*
    @Test
    @DisplayName("포장 타입으로 상품 수 필터링")
    void countProductsByPackagingTypeFilter_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of("bag"),
                List.of()
        );

        // when
        Long count = productFilterService.getFilteredProductCounts(filterInfo).count();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("혜택으로 상품 수 필터링")
    void countProductsByBenefitFilter_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(),
                List.of("free_shipping")
        );

        // when
        Long count = productFilterService.getFilteredProductCounts(filterInfo).count();

        // then
        assertThat(count).isEqualTo(1L);
    }
*/
    @Test
    @DisplayName("대분류 카테고리로 상품 수 필터링")
    void countProductsByMainCategoryFilter_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.empty(),
                Optional.empty(),
                List.of(category1.getId()),
                List.of(),
                List.of()
        );

        // when
        Long count = productFilterService.getFilteredProductCounts(filterInfo).count();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("모든 조건 없이 전체 상품 수 필터링")
    void countAllProducts_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(),
                List.of()
        );

        // when
        Long count = productFilterService.getFilteredProductCounts(filterInfo).count();

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("가격 + 포장타입 + 혜택 + 카테고리 조건 조합 필터링")
    void countProductsByMultipleFilters_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.of(10000.0),
                Optional.of(13000.0),
                List.of(category1.getId()),
                List.of("box"),
                List.of("free_shipping")
        );

        // when
        Long count = productFilterService.getFilteredProductCounts(filterInfo).count();

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("가격 + 포장타입 + 혜택 + 카테고리 조건 조합으로 상품 조회 테스트")
    void getFilteredProducts_withMultipleConditions_success() {
        // given
        FilterInfo filterInfo = new FilterInfo(
                Optional.of(10000.0),
                Optional.of(13000.0),
                List.of(category1.getId()),
                List.of("box"),
                List.of("free_shipping")
        );

        Pageable pageable = PageRequest.of(0, 10);

        // when
        ProductsResponse response = productFilterService.getFilteredProducts(filterInfo, pageable);

        // then
        assertThat(response.pageInfo()).isNotNull();
        assertThat(response.pageInfo().currentPage()).isEqualTo(0);
        assertThat(response.pageInfo().totalPage()).isEqualTo(1);

        assertThat(response.products().size()).isEqualTo(1);
        assertThat(response.products().get(0).name()).isEqualTo("다크초콜릿 바");


    }

}
