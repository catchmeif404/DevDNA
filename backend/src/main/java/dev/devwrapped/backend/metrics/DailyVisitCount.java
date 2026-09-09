package dev.devwrapped.backend.metrics;

import java.time.LocalDate;

// 일별 방문 집계 native query의 별칭과 메서드 이름을 맞춘 projection이다.
public interface DailyVisitCount {
    LocalDate getDay();

    long getCount();
}
