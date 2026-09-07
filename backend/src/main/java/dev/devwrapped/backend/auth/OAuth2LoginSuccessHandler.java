package dev.devwrapped.backend.auth;

import dev.devwrapped.backend.analysis.AnalysisJob;
import dev.devwrapped.backend.analysis.AnalysisJobRepository;
import dev.devwrapped.backend.analysis.AnalysisQueuePublisher;
import dev.devwrapped.backend.user.User;
import dev.devwrapped.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * GitHub 로그인 성공 시 User를 upsert하고, 로그인한 계정 자신에 대한 AnalysisJob을
 * 바로 생성/큐잉한 뒤 그 job을 폴링하는 결과 페이지로 리다이렉트한다.
 * Access Token은 세션 밖으로 노출하지 않으며 DB에 저장하지 않는다 (섹션 6/20 원칙).
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AnalysisJobRepository jobRepository;
    private final AnalysisQueuePublisher queuePublisher;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, AnalysisJobRepository jobRepository,
            AnalysisQueuePublisher queuePublisher, @Value("${app.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.queuePublisher = queuePublisher;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Number idAttribute = oAuth2User.getAttribute("id");
        Long githubId = idAttribute.longValue();
        String login = oAuth2User.getAttribute("login");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        User user = userRepository.findByGithubId(githubId).orElseGet(() -> new User(githubId, login, avatarUrl));
        user.setGithubLogin(login);
        user.setAvatarUrl(avatarUrl);
        user = userRepository.save(user);

        AnalysisJob job = new AnalysisJob(login);
        job.setUserId(user.getId());
        job = jobRepository.save(job);
        queuePublisher.enqueue(job.getId());

        response.sendRedirect(frontendUrl + "/dev/" + login + "?job=" + job.getId());
    }
}
