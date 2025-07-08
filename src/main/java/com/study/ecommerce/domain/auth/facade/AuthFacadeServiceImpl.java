package com.study.ecommerce.domain.auth.facade;

import com.study.ecommerce.domain.auth.dto.req.LoginRequest;
import com.study.ecommerce.domain.auth.dto.req.SignUpRequest;
import com.study.ecommerce.domain.auth.dto.resp.TokenResponse;
import com.study.ecommerce.domain.auth.service.AuthCommandService;
import com.study.ecommerce.domain.member.entity.Member;
import com.study.ecommerce.domain.member.service.command.MemberCommandService;
import com.study.ecommerce.domain.member.service.query.MemberQueryService;
import com.study.ecommerce.global.error.ErrorCode;
import com.study.ecommerce.global.error.exception.BusinessException;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacadeServiceImpl implements AuthFacadeService {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final AuthCommandService authCommandService;

    @Override
    @Transactional
    public void signup(SignUpRequest request) {

        if(memberQueryService.existByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }

        String encodePassword = authCommandService.encodePassword(request.password());

        memberCommandService.createMember(
                request.email(),
                request.name(),
                encodePassword,
                request.address(),
                Member.Role.CUSTOMER
        );

        // 장바구니
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 회원 조회
        Member member = memberQueryService.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if(!authCommandService.validatePassword(request.password(), member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }


        // 토큰 생성
        TokenResponse token = authCommandService.generateToken(
                member.getEmail(),
                member.getRole().name()
        );

        // 회원정보로 토큰 응답 완성
        return new TokenResponse(
                token.accessToken(),
                token.tokenType(),
                member.getEmail(),
                member.getName(),
                member.getRole().name()
        );
    }
}
