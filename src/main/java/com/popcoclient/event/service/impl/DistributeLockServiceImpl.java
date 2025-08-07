package com.popcoclient.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Slf4j
public class DistributeLockServiceImpl {
    private final StringRedisTemplate lockRedisTemplate;
    private final RedisScript<Boolean> lockScript;
    private final RedisScript<Boolean> unlockScript;

    public DistributeLockServiceImpl(StringRedisTemplate lockRedisTemplate) {
        this.lockRedisTemplate = lockRedisTemplate;
        this.lockScript = createLockScript();
        this.unlockScript = createUnlockScript();
    }

    /**
     * 분산 락 획득 (논블로킹)
     * @param lockKey 락 키
     * @param lockValue 락 값
     * @param expireTimeSeconds 만료 시간
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String lockKey, String lockValue, long expireTimeSeconds) {
        try {
            Boolean success = lockRedisTemplate.execute(
                    lockScript,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(expireTimeSeconds)
            );

            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Failed to acquire lock: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 분산 락 획득 (블로킹 - 타임아웃 있음)
     * @param lockKey 락 키
     * @param lockValue 락 값
     * @param expireTimeSeconds 락 만료 시간
     * @param timeoutSeconds 대기 타임아웃
     * @return 락 획득 성공 여부
     */
    public boolean tryLockWithTimeout(String lockKey, String lockValue,
                                      long expireTimeSeconds, long timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (tryLock(lockKey, lockValue, expireTimeSeconds)) {
                return true;
            }

            try {
                Thread.sleep(50); // 50ms 대기 후 재시도
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Lock acquisition interrupted: {}", lockKey);
                return false;
            }
        }

        log.warn("Lock acquisition timeout: {}", lockKey);
        return false;
    }

    /**
     * 분산 락 해제
     * @param lockKey 락 키
     * @param lockValue 락 값
     * @return 락 해제 성공 여부
     */
    public boolean unlock(String lockKey, String lockValue) {
        try {
            Boolean success = lockRedisTemplate.execute(
                    unlockScript,
                    Collections.singletonList(lockKey),
                    lockValue
            );

            log.debug("Unlock - key: {}, value: {}, success: {}",
                    lockKey, lockValue, success);

            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Failed to release lock: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 락을 사용하여 작업 실행 (try-with-resources 패턴)
     * @param lockKey 락 키
     * @param expireTimeSeconds 락 만료 시간
     * @param timeoutSeconds 대기 타임아웃
     * @param task 실행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과
     * @throws LockAcquisitionException 락 획득 실패 시
     */
    public <T> T executeWithLock(String lockKey, long expireTimeSeconds,
                                 long timeoutSeconds, Supplier<T> task) {
        String lockValue = UUID.randomUUID().toString();

        if (!tryLockWithTimeout(lockKey, lockValue, expireTimeSeconds, timeoutSeconds)) {
            throw new LockAcquisitionException("Failed to acquire lock: " + lockKey);
        }

        try {
            return task.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 락을 사용하여 void 작업 실행
     */
    public void executeWithLock(String lockKey, long expireTimeSeconds,
                                long timeoutSeconds, Runnable task) {
        executeWithLock(lockKey, expireTimeSeconds, timeoutSeconds, () -> {
            task.run();
            return null;
        });
    }

    /**
     * 락 생성 스크립트 (원자적 연산 보장)
     */
    private RedisScript<Boolean> createLockScript() {
        String script = """
            if redis.call('EXISTS', KEYS[1]) == 0 then
                redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
                return true
            else
                return false
            end
            """;

        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    /**
     * 락 해제 스크립트 (원자적 연산 보장)
     */
    private RedisScript<Boolean> createUnlockScript() {
        String script = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return true
            else
                return false
            end
            """;

        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    /**
     * 퀴즈 이벤트용 락 키 생성 유틸
     */
    public static class LockKeys {
        private static final String QUIZ_PREFIX = "quiz:lock:";

        public static String participantAnswer(Long quizId, Long userId) {
            return QUIZ_PREFIX + "answer:" + quizId + ":" + userId;
        }

        public static String quizProgress(Long quizId) {
            return QUIZ_PREFIX + "progress:" + quizId;
        }

        public static String participantCount(Long quizId) {
            return QUIZ_PREFIX + "count:" + quizId;
        }

        public static String eventNotification(Long eventId) {
            return QUIZ_PREFIX + "notification:" + eventId;
        }
    }

    /**
     * 락 획득 실패 예외
     */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}
