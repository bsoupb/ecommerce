package com.study.ecommerce.domain.product.entity;

import com.study.ecommerce.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Builder
    public Product(String name, String description,
                   Long price, Integer stockQuantity,
                   ProductStatus status, Long sellerId,
                   Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
    }

    public enum ProductStatus {
        ACTIVE, SOLD_OUT, DELETED
    }

    // 비지니스 메서드

    // decreasesStock(int quantity): 재고를 줄이는 메서드
    // 에러를 터뜨려야하는 부분 -> error
    // 상태 변경 -> SOLD_OUT -> if
    public void decreasesStock(int quantity) {
        int restStock = this.stockQuantity -= quantity;

        if(restStock < 0) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        this.stockQuantity = restStock;
        if(this.stockQuantity == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    // increasesStock(int quantity): 재고를 증가시키는 메서드
    public void increasesStock(int quantity) {
        this.stockQuantity += quantity;

        if(status == ProductStatus.SOLD_OUT && this.stockQuantity > 0) {
            this.status = ProductStatus.ACTIVE;
        }

    }

    // update -> 전체 셀러아이디는 바뀔 수가 없다고 가정
    public void update(String name, String description,
                       Long price, Integer stockQuantity,
                       ProductStatus status, Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.categoryId = categoryId;
    }

    // delete -> soft delete 상태를 변경
    public void delete() {
        this.status = ProductStatus.SOLD_OUT;
    }
}
