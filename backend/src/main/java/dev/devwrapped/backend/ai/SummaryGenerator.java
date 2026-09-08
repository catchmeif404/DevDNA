package dev.devwrapped.backend.ai;

import dev.devwrapped.backend.analyzer.Features;
import dev.devwrapped.backend.scoring.ScoringResult;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Rule-based natural-language summary. Section 3.2 places real LLM-based analysis in "MVP 이후",
 * so this template stands in for it — same interface an LLM-backed generator would fill later,
 * fed only computed features per section 14's "정확한 통계 -> 자연어 해석" split.
 */
@Component
public class SummaryGenerator {

    private static final Map<String, String> OPENING = Map.ofEntries(
            Map.entry("NIGHT_OWL", "새벽 시간대에 유독 커밋이 몰리는"),
            Map.entry("BUG_SLAYER", "버그를 찾아 고치는 데 유독 강한"),
            Map.entry("BUILDER", "꾸준히 기능을 쌓아 올리는"),
            Map.entry("POLYGLOT", "여러 언어를 넘나드는"),
            Map.entry("WEEKEND_WARRIOR", "주말에 유독 커밋이 몰리는"),
            Map.entry("REFACTOR_MASTER", "코드 정리와 리팩터링에 진심인"),
            Map.entry("DOCUMENTARIAN", "문서와 기록을 꼼꼼히 남기는"),
            Map.entry("TESTER", "테스트 코드로 안정성을 챙기는"),
            Map.entry("EXPLORER", "여러 저장소를 넘나들며 활동하는"),
            Map.entry("COLLABORATOR", "PR과 협업으로 존재감을 드러내는"));

    public String generate(Features features, ScoringResult scoring) {
        String opening = OPENING.getOrDefault(scoring.developerType(), "고유한 스타일을 가진");
        String language = features.topLanguage() == null ? "다양한 언어" : features.topLanguage();
        return "%s 개발자입니다. 총 %,d개의 커밋 중 %.0f%%가 새벽(0~6시)에 작성됐고, 주로 %s(으)로 작업합니다."
                .formatted(opening, features.commitCount(), features.nightCommitRatio() * 100, language);
    }
}
