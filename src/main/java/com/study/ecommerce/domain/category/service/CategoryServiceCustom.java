package com.study.ecommerce.domain.category.service;

import com.study.ecommerce.domain.category.dto.req.CategoryRequest;
import com.study.ecommerce.domain.category.dto.resp.CategoryResponse;
import com.study.ecommerce.domain.category.entity.Category;
import com.study.ecommerce.domain.category.repository.CategoryRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceCustom implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        // 모든 카테고리 조회
        List<Category> allCategories = categoryRepository.findAll();

        // 부모 Id 별로 하위 카테고리를 그룹화
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(cat -> cat.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        // 루트 카테고리만 필터링
        List<Category> rootCategories = allCategories.stream()
                .filter(cat -> cat.getParentId() == null)
                .toList();


        return rootCategories.stream()
                .map(root -> buildCategoryHierarchy(root, childrenMap))
                .toList();
    }

    private CategoryResponse buildCategoryHierarchy(Category category, Map<Long, List<Category>> childrenMap) {
        List<CategoryResponse> children = new ArrayList<>();

        if(childrenMap.containsKey(category.getId())) {
            children = childrenMap.get(category.getId()).stream()
                    .map(child -> buildCategoryHierarchy(child, childrenMap))
                    .toList();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDepth(),
                category.getParentId(),
                children
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 id는 존재하지 않습니다."));

        return null;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Long parentId = null;
        int depth = 1;

        if(request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("상위 카테고리를 찾을 수 없습니다."));
            parentId = parent.getId();
            depth = parent.getDepth() + 1;
        }

        Category category = Category.builder()
                .name(request.name())
                .depth(depth)
                .parentId(parentId)
                .build();

        categoryRepository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDepth(),
                category.getParentId(),
                List.of()       // 어떤 애가 올지 모르기 때문에 비어있는 리스트
        );
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category beforeCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 id 값은 존재하지 않습니다."));

        return new CategoryResponse(
                beforeCategory.getId(),
                request.name(),
                beforeCategory.getDepth(),
                request.parentId(),
                List.of()
        );
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 id를 찾을 수 없습니다"));
        int depth = category.getDepth();

        if(depth != 1) {
            categoryRepository.delete(category);
        }

        categoryRepository.deleteById(id);

    }
}
