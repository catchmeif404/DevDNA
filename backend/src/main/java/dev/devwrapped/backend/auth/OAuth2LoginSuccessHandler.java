package dev.devwrapped.backend.auth;

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
 * GitHub 로그인 성공 시 User를 upsert하고 결과 페이지로 리다이렉트한다.
 * Access Token은 세션 밖으로 노출하지 않으며 DB에 저장하지 않는다 (섹션 6/20 원칙).
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long githubId = Long.valueOf(String.valueOf(oAuth2User.getAttribute("id")));
        String login = oAuth2User.getAttribute("login");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        User user = userRepository.findByGithubId(githubId).orElseGet(() -> new User(githubId, login, avatarUrl));
        user.setGithubLogin(login);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        response.sendRedirect(frontendUrl + "/dev/" + login);
    }
}
