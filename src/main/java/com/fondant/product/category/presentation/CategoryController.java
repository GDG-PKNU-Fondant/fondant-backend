package com.fondant.product.category.presentation;

import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.category.application.CategoryService;
import com.fondant.product.category.presentation.dto.response.CategoriesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto<CategoriesResponse>> getAllCategories() {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                categoryService.getAllCategories()
        ));
    }

    @GetMapping("/main")
    public ResponseEntity<ResponseDto<CategoriesResponse>> getAllMainCategories() {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                categoryService.getAllMainCategories()
        ));
    }
}
