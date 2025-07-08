package com.study.ecommerce.domain.member.entity;

import com.study.ecommerce.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {    // createdAt, updatedAt 자동 생성

    @Id     // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // AutoIncrement
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String address;

    @Enumerated(EnumType.STRING)    // enum 타입 가져오기
    @Column(nullable = false)
    private Role role;

    private boolean isDeleted;

    @Builder
    public Member(Long id, String email, String password, String name, String address, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.role = role;
    }

    public enum Role {
        CUSTOMER, SELLER, ADMIN
    }

    public void updateName(String name) {
        this.name = name;
    }
}
