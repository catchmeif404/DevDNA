package dev.devwrapped.backend.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Pushes job ids onto the Redis-backed analysis queue (SQS 대체, section 5/9). */
@Component
public class AnalysisQueuePublisher {

    private final StringRedisTemplate redisTemplate;
    private final String queueKey;

    public AnalysisQueuePublisher(StringRedisTemplate redisTemplate,
            @Value("${app.analysis-queue-key}") String queueKey) {
        this.redisTemplate = redisTemplate;
        this.queueKey = queueKey;
    }

    public void enqueue(Long jobId) {
        redisTemplate.opsForList().rightPush(queueKey, String.valueOf(jobId));
    }
}
