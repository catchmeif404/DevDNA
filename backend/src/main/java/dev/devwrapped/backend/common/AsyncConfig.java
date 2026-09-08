package dev.devwrapped.backend.common;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Spring Boot's default async executor allows several jobs to run at once (core pool size 8),
 * which is fine for CPU work but not for GithubApiClient calls: two analyses running concurrently
 * multiply the request burst against GitHub's shared-per-token Search API secondary rate limit
 * (see AnalysisRunner/GithubApiClient) - exactly the kind of collision the old single-threaded
 * queue consumer serialized away for free. A pool size of 1 restores that same serialization.
 */
@Configuration
public class AsyncConfig {

    @Bean("analysisExecutor")
    public Executor analysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("analysis-");
        executor.initialize();
        return executor;
    }
}
