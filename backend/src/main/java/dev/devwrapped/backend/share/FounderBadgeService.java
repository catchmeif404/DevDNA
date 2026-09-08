package dev.devwrapped.backend.share;

import dev.devwrapped.backend.user.User;
import dev.devwrapped.backend.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The first 3 accounts ever created (by users.id, i.e. signup order - a User row is created on
 * first login, which immediately triggers that account's own analysis, so this doubles as "first
 * 3 people who used DevDNA") get a founder badge on top of their regular developer-type badge.
 */
@Service
@RequiredArgsConstructor
public class FounderBadgeService {

    private final UserRepository userRepository;

    /** 1, 2, or 3 if githubLogin is one of the first 3 users; null otherwise. */
    public Integer founderRank(String githubLogin) {
        List<User> firstThree = userRepository.findTop3ByOrderByIdAsc();
        for (int i = 0; i < firstThree.size(); i++) {
            if (firstThree.get(i).getGithubLogin().equals(githubLogin)) {
                return i + 1;
            }
        }
        return null;
    }
}
