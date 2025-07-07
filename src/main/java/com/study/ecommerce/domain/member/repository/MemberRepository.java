package com.study.ecommerce.domain.member.repository;

import com.study.ecommerce.domain.member.entity.Member;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {         // <Entity, key>
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsById(@NonNull Long id);

    boolean existsByIdAndIsDeletedFalse(Long id);
}
