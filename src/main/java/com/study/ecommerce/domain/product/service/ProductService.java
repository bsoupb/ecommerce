package com.study.ecommerce.domain.product.service;

import com.study.ecommerce.domain.category.entity.Category;
import com.study.ecommerce.domain.category.repository.CategoryRepository;
import com.study.ecommerce.domain.member.entity.Member;
import com.study.ecommerce.domain.member.repository.MemberRepository;
import com.study.ecommerce.domain.product.dto.req.ProductCreateRequest;
import com.study.ecommerce.domain.product.dto.req.ProductSearchCondition;
import com.study.ecommerce.domain.product.dto.req.ProductUpdateRequest;
import com.study.ecommerce.domain.product.dto.resp.ProductResponse;
import com.study.ecommerce.domain.product.dto.resp.ProductSummaryDto;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(ProductSearchCondition condition, Pageable pageable) {
        Page<ProductSummaryDto> productSummaryDtos = productRepository.searchProducts(condition, pageable);

        return productSummaryDtos.map(dto -> new ProductResponse(
                dto.id(),
                dto.name(),
                null,
                dto.price(),
                dto.stockQuantity(),
                dto.status(),
                dto.categoryName()
        ));

//        List<Long> categoryIds = productSummaryDtos.getContent().stream()
//                .map(dto -> {
//                    Product product = productRepository.findById(dto.id())
//                            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
//                    return product.getCategoryId();
//                })
//                .filter(Objects::nonNull)
//                .distinct()
//                .toList();
//
//        Map<Long, String> categoryMap = new HashMap<>();
//
//        if(!categoryIds.isEmpty()) {
//            List<Category> categories = categoryRepository.findAllById(categoryIds);
//            categories.forEach(category ->
//                    categoryMap.put(category.getId(), category.getName()));
//        }
//
//        return productSummaryDtos.map(dto -> {
//            Product product = productRepository.findById(dto.id())
//                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
//
//            String categoryName = "분류 없음";
//            if(product.getCategoryId() != null) {
//                categoryName = categoryMap.getOrDefault(product.getCategoryId(), "분류 없음");
//            }
//
//            return new ProductResponse(
//                    dto.id(),
//                    dto.name(),
//                    null,
//                    dto.price(),
//                    dto.stockQuantity(),
//                    dto.status(),
//                    categoryName
//            );
//        });
    }

    // create
    // email -> 판매자 찾기
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String email) {
        Member seller = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다."));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .status(Product.ProductStatus.ACTIVE)
                .sellerId(seller.getId())
                .categoryId(category.getId())
                .build();

        productRepository.save(product);

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                category.getName()
        );
    }

    // delete
    @Transactional
    public void deleteProduct(Long id, String email) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 현재 사용자가 판매자인지 확인
        Member seller = memberRepository.findById(product.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다."));

        if(!seller.getEmail().equals(email)) {
            throw new IllegalArgumentException("상품 삭제 권한이 없습니다.");
        }

        product.delete();
    }
    
    // id 기준으로 가져오기(get)
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 카테고리 정보 조회
        Category category = null;
        String categoryName = "분류 없음";
        
        if(product.getCategoryId() != null) {
            category = categoryRepository.findById(product.getCategoryId())
                    .orElse(null);
            
            if(category != null) {
                categoryName = category.getName();
            }
        }
        
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                categoryName
        );
    }
    
    // update
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, String email) {
        // 프로덕트 찾기
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        // 현재 사용자가 판매자인지 확인
        Member seller = memberRepository.findById(product.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다."));

        if(!seller.getEmail().equals(email)) {
            throw new IllegalArgumentException("상품을 수정할 권한이 없습니다.");
        }
        // 카테고리 찾기
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
        // 프로덕트 업데이트하고 -> jpa 더티체킹 더티캐싱
        product.update(
            request.name(),
            request.description(),
            request.price(),
            request.stockQuantity(),
            request.status(),
            category.getId()
        );
        // 반환
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                category.getName()
        );
    }

}
