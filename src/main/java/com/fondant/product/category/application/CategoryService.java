package com.fondant.product.category.application;

import com.fondant.product.category.domain.CategoryEntity;
import com.fondant.product.category.domain.repository.CategoryRepository;
import com.fondant.product.category.application.dto.CategoryInfo;
import com.fondant.product.category.application.dto.MainCategoryInfo;
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
    public CategoriesResponse<MainCategoryInfo> getAllCategories(){
        List<CategoryEntity> categories = categoryRepository.findAllByChildrenIsNotNull();

        return CategoriesResponse.of(categories.stream()
                .map(this::toMainCategoryInfo)
                .toList());
    }

    public MainCategoryInfo toMainCategoryInfo(CategoryEntity category) {
        return MainCategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
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
    public CategoriesResponse<CategoryInfo> getAllMainCategories(){
        List<CategoryEntity> categories = categoryRepository.findAllByChildrenIsNotNull();

        return CategoriesResponse.of(categories.stream()
                .map(this::toCategoryInfo)
                .toList());
    }

    private List<Long> getAllChildren(Long parentId) {
        List<CategoryEntity> children = categoryRepository.getByParentId(parentId);
        return children.stream().map(CategoryEntity::getId).toList();
    }
}
