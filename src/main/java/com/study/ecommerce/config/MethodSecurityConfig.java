package com.study.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// @EnableMethodSecurity: 메서드 단위의 권한 제어를 활성화
// -> Controller, Service 레벨에서 권한 체크가 필요한 경우
// -> @PreAuthorize, @PostAuthorize 같은 메서드 보안 애노테이션을 활성화
// RestController 에서 프리핸들러 사용할 때 등록해야만 사용할 수 있음
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
}
