package com.study.ecommerce.domain.category.facade;

import com.study.ecommerce.domain.category.dto.req.CategoryRequest;
import com.study.ecommerce.domain.category.dto.resp.CategoryResponse;

import java.util.List;

public interface CategoryFacadeService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategory(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);


}
