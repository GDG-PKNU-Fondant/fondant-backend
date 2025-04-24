package com.fondant.product.category.application;

import com.fondant.product.category.application.dto.CategoryDetailsInfo;
import com.fondant.product.category.application.dto.MainCategoryInfo;
import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.product.category.domain.repository.CategoryRepository;
import com.fondant.product.category.application.dto.CategoryInfo;
import com.fondant.product.category.presentation.dto.response.CategoriesResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoriesResponse<CategoryDetailsInfo> getAllCategories(){
        List<CategoryEntity> categories = categoryRepository.findAllByChildrenIsNotNull();

        return CategoriesResponse.of(categories.stream()
                .map(this::toCategoryDetailsInfo)
                .toList());
    }

    public CategoryDetailsInfo toCategoryDetailsInfo(CategoryEntity category) {
        return CategoryDetailsInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .iconUrl(category.getIconUrl())
                .subCategories(
                        category.getChildren().stream()
                                .map(this::toCategoryInfo)
                                .toList())
                .build();
    }

    public CategoryInfo toCategoryInfo(CategoryEntity category) {
        return new CategoryInfo(
                category.getId(),
                category.getName()
        );
    }

    @Transactional
    public CategoriesResponse<MainCategoryInfo> getAllMainCategories(){
        List<CategoryEntity> categories = categoryRepository.findAllByChildrenIsNotNull();

        return CategoriesResponse.of(categories.stream()
                .map(this::toMainCategoryInfo)
                .toList());
    }

    public MainCategoryInfo toMainCategoryInfo(CategoryEntity category) {
        return new MainCategoryInfo(
                category.getId(),
                category.getName(),
                category.getIconUrl()
        );
    }

    private List<Long> getAllChildren(Long parentId) {
        List<CategoryEntity> children = categoryRepository.getByParentId(parentId);
        return children.stream().map(CategoryEntity::getId).toList();
    }
}
