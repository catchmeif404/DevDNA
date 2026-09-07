package dev.devwrapped.backend.share;

import java.util.Map;

/** Shared emoji/label/tagline lookup for the 10 developer types (worker's DeveloperTypeScorer owns the list). */
final class DeveloperTypeMeta {

    private static final Map<String, String> EMOJI = Map.ofEntries(
            Map.entry("NIGHT_OWL", "🌙"),
            Map.entry("BUG_SLAYER", "🔥"),
            Map.entry("BUILDER", "🏗"),
            Map.entry("POLYGLOT", "🌐"),
            Map.entry("WEEKEND_WARRIOR", "🎉"),
            Map.entry("REFACTOR_MASTER", "🧹"),
            Map.entry("DOCUMENTARIAN", "📚"),
            Map.entry("TESTER", "🧪"),
            Map.entry("EXPLORER", "🧭"),
            Map.entry("COLLABORATOR", "🤝"));

    private static final Map<String, String> LABEL = Map.ofEntries(
            Map.entry("NIGHT_OWL", "NIGHT OWL"),
            Map.entry("BUG_SLAYER", "BUG SLAYER"),
            Map.entry("BUILDER", "BUILDER"),
            Map.entry("POLYGLOT", "POLYGLOT"),
            Map.entry("WEEKEND_WARRIOR", "WEEKEND WARRIOR"),
            Map.entry("REFACTOR_MASTER", "REFACTOR MASTER"),
            Map.entry("DOCUMENTARIAN", "DOCUMENTARIAN"),
            Map.entry("TESTER", "TESTER"),
            Map.entry("EXPLORER", "EXPLORER"),
            Map.entry("COLLABORATOR", "COLLABORATOR"));

    private static final Map<String, String> TAGLINE = Map.ofEntries(
            Map.entry("NIGHT_OWL", "새벽에 강한 개발자"),
            Map.entry("BUG_SLAYER", "버그를 사냥하는 개발자"),
            Map.entry("BUILDER", "꾸준히 만들어가는 개발자"),
            Map.entry("POLYGLOT", "여러 언어를 넘나드는 개발자"),
            Map.entry("WEEKEND_WARRIOR", "주말에 불타오르는 개발자"),
            Map.entry("REFACTOR_MASTER", "코드를 갈고 닦는 개발자"),
            Map.entry("DOCUMENTARIAN", "기록을 남기는 개발자"),
            Map.entry("TESTER", "테스트로 증명하는 개발자"),
            Map.entry("EXPLORER", "여러 저장소를 넘나드는 개발자"),
            Map.entry("COLLABORATOR", "협업으로 성장하는 개발자"));

    private DeveloperTypeMeta() {
    }

    static String emoji(String type) {
        return EMOJI.getOrDefault(type, "✨");
    }

    static String label(String type) {
        return LABEL.getOrDefault(type, type);
    }

    static String tagline(String type) {
        return TAGLINE.getOrDefault(type, "고유한 개발 스타일을 가진 개발자");
    }
}
