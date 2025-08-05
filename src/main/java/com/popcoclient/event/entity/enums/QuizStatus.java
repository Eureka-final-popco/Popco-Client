package com.popcoclient.event.entity.enums;

public enum QuizStatus {

    WAITING("대기 중"),
    ACTIVE("진행 중"),
    FINISHED("종료");

    private final String description;

    QuizStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static QuizStatus fromTimerStatus(Long startTime) {
        if (startTime == null) {
            return WAITING;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        int remainingTime = Math.max(0, 30 - (int) (elapsed / 1000));

        return remainingTime > 0 ? ACTIVE : FINISHED;
    }
}