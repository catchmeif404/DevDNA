package dev.devwrapped.backend.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * MVP 테스트 모드 요청 DTO. 실서비스에서는 인증된 세션의 GitHub 로그인을 사용하지만,
 * 현재는 GitHub OAuth App 연동 전이라 공개 GitHub username을 직접 받아 파이프라인을 검증한다.
 */
public record CreateAnalysisRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9-]{1,39}$") String githubUsername) {
}
