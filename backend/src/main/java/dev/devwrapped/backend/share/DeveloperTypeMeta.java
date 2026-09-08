package dev.devwrapped.backend.share;

import java.util.Map;

/** Artifact copy for the type IDs owned by scoring.DeveloperTypeScorer. */
final class DeveloperTypeMeta {
    private record Type(String label, String tagline) {}

    private static final Type UNKNOWN = new Type("UNCLASSIFIED", "Evidence pending classification.");
    private static final Map<String, Type> TYPES = Map.ofEntries(
            Map.entry("NIGHT_OWL", new Type("AFTER-HOURS OPERATOR", "Activity recorded after hours.")),
            Map.entry("BUG_SLAYER", new Type("BUG HUNTER", "Defects found. Tracks removed.")),
            Map.entry("BUILDER", new Type("SERIAL BUILDER", "A trail of new features.")),
            Map.entry("POLYGLOT", new Type("MULTILINGUAL OPERATOR", "Several languages. One subject.")),
            Map.entry("WEEKEND_WARRIOR", new Type("WEEKEND OPERATIVE", "Weekend activity on the record.")),
            Map.entry("REFACTOR_MASTER", new Type("CODE RESTORER", "The code has been put in order.")),
            Map.entry("DOCUMENTARIAN", new Type("ARCHIVIST", "Everything leaves a paper trail.")),
            Map.entry("TESTER", new Type("QUALITY INSPECTOR", "Claims checked against the evidence.")),
            Map.entry("EXPLORER", new Type("REPO EXPLORER", "Evidence across repositories.")),
            Map.entry("COLLABORATOR", new Type("COLLABORATOR", "Multiple contributors on file.")));

    private DeveloperTypeMeta() {}

    private static Type type(String id) {
        return id == null ? UNKNOWN : TYPES.getOrDefault(id, UNKNOWN);
    }

    static String label(String id) {
        return type(id).label();
    }

    static String tagline(String id) {
        return type(id).tagline();
    }
}
