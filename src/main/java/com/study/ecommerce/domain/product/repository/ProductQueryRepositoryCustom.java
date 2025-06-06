package com.study.ecommerce.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.ecommerce.domain.category.entity.QCategory;
import com.study.ecommerce.domain.member.entity.QMember;
import com.study.ecommerce.domain.product.dto.req.ProductSearchCondition;
import com.study.ecommerce.domain.product.dto.resp.ProductSummaryDto;
import com.study.ecommerce.domain.product.entity.Product.ProductStatus;
import com.study.ecommerce.domain.product.entity.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class ProductQueryRepositoryCustom implements ProductQueryRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryDto> searchProducts(
            ProductSearchCondition condition,
            Pageable pageable
    ) {

        QProduct product = QProduct.product;
        QCategory category = QCategory.category;
        QMember member = QMember.member;

        // tuple
        List<Tuple> tuples = queryFactory
                .select(member.name, member.email)
                .from(member)
                .fetch();

        for(Tuple tuple : tuples) {
            String name = tuple.get(member.name);
            String email = tuple.get(member.email);
        }

        List<ProductSummaryDto> content = queryFactory
                .select(Projections.constructor(ProductSummaryDto.class,
                        product.id,
                        product.name,
                        product.price,
                        product.stockQuantity,
                        category.name.coalesce("분류 없음").as("categoryName"),
                        product.status))
                .from(product)
                .leftJoin(category).on(product.categoryId.eq(category.id))
                .where(
                        keywordContains(condition.keyword()),
                        categoryIdEq(condition.categoryId()),
                        priceGoe(BigDecimal.valueOf(condition.minPrice())),
                        priceLoe(BigDecimal.valueOf(condition.maxPrice())),
                        sellerIdEq(condition.sellerId()),
                        statusActive()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, product))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        keywordContains(condition.keyword()),
                        categoryIdEq(condition.categoryId()),
                        priceGoe(BigDecimal.valueOf(condition.minPrice())),
                        priceLoe(BigDecimal.valueOf(condition.maxPrice())),
                        sellerIdEq(condition.sellerId()),
                        statusActive()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    /**
     * 키워드 검색 조건 (상품명 또는 설명에 포함)
     * @param keyword
     * @return BooleanExpression
     */
    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        QProduct product = QProduct.product;
        return product.name.containsIgnoreCase(keyword)
                .or(product.description.containsIgnoreCase(keyword));
    }

    /**
     *  카테고리 ID 조건
     * @param categoryId
     * @return BooleanExpression
     */
    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? QProduct.product.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression priceGoe(BigDecimal minPrice) {
        return minPrice != null ? QProduct.product.price.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(BigDecimal maxPrice) {
        return maxPrice != null ? QProduct.product.price.loe(maxPrice) : null;
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        return sellerId != null ? QProduct.product.sellerId.eq(sellerId) : null;
    }

    private BooleanExpression statusActive() {
        return QProduct.product.status.eq(ProductStatus.ACTIVE);
    }

    private BooleanExpression priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return priceGoe(minPrice).and(priceLoe(maxPrice));
    }

    private BooleanExpression stockAvailable() {
        return QProduct.product.stockQuantity.gt(0);
    }

    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable, QProduct product) {
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order order : pageable.getSort()) {
                switch (order.getProperty()) {
                    case "price" :
                        return order.isAscending() ? product.price.asc() : product.price.desc();

                    case "createdAt":
                        return order.isAscending() ? product.createdAt.asc() : product.createdAt.desc();

                    default:
                        return product.id.desc();
                }
            }
        }

        return product.id.desc();
    }

    //    @Override
//    public Page<ProductSummaryDto> searchProducts(
//            ProductSearchCondition condition,
//            Pageable pageable
//    ) {
//        QProduct product = QProduct.product;
//        QCategory category = QCategory.category;
//
//        // 동적 쿼리를 생성하기 위한 조건
//        BooleanBuilder builder = new BooleanBuilder();
//
//        if(StringUtils.hasText(condition.keyword())) {
//            builder.and(product.name.containsIgnoreCase(condition.keyword())
//                    .or(product.description.containsIgnoreCase(condition.keyword())));
//        }
//
//        if(condition.categoryId() != null) {
//            builder.and(product.categoryId.eq(condition.categoryId()));
//        }
//
//        if(condition.minPrice() != null) {
//            builder.and(product.price.goe(condition.minPrice()));
//        }
//
//        if(condition.maxPrice() != null) {
//            builder.and(product.price.loe(condition.maxPrice()));
//        }
//
//        if(condition.sellerId() != null) {
//            builder.and(product.sellerId.eq(condition.sellerId()));
//        }
//
//        builder.and(product.status.eq(ProductStatus.ACTIVE));
//
//        // 전체 카운트 쿼리
//        JPAQuery<Long> countQuery = queryFactory
//                .select(product.count())
//                .from(product)
//                .where(builder);
//
//        // 조인 없이 카테고리 이름을 가져오기 위한 서브 쿼리 방식
//        // 실제 조회 쿼리
//        List<ProductSummaryDto> summaryDtos = queryFactory
//                .select(Projections.constructor(ProductSummaryDto.class,
//                        product.id,
//                        product.name,
//                        product.price,
//                        product.stockQuantity,
//                        // 카테고리 이름 대신 상수값을 반환
////                        Expressions.asString("Category").as("categoryName"),
//
//                        // 과제: 상수값이 아닌 실제값 가져오는 것 (join 사용 작성)
////                        category.name,
//
//                        // 과제: 상수값이 아닌 실제값 가져오는 것 (서브쿼리로 작성)
//                        JPAExpressions.select(category.name)
//                                .from(category)
//                                .where(category.id.eq(product.categoryId)),
//                        product.status
//                ))
//                .from(product)
//                .where(builder)
//                // 조인
//                // 과제: 상수값이 아닌 실제값 가져오는 것 (join 사용 작성)
////                .join(category)
////                .on(product.id.eq(category.id))
//                .offset(pageable.getOffset())       // 페이지 갯수 -> 한페이지에 몇개씩 나타내겠는가
//                .limit(pageable.getPageSize())
//                .orderBy(getOrderSpecifier(pageable, product))
//                .fetch();   // select 가져올 때 무조건 써야 함 (fetch: List, fetchOne: 단일)
//
//        return PageableExecutionUtils.getPage(summaryDtos, pageable, countQuery::fetchOne);
//    }
}