package dev.devwrapped.backend.analyzer;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Conventional Commits prefix + keyword fallback (section 10). */
@Component
public class CommitClassifier {

    private static final Pattern PREFIX_FIX = Pattern.compile("^fix(\\(.+\\))?:", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREFIX_FEATURE = Pattern.compile("^feat(\\(.+\\))?:", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREFIX_REFACTOR = Pattern.compile("^refactor(\\(.+\\))?:", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREFIX_DOCS = Pattern.compile("^docs(\\(.+\\))?:", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREFIX_TEST = Pattern.compile("^test(\\(.+\\))?:", Pattern.CASE_INSENSITIVE);

    private static final Pattern KEYWORD_FIX = Pattern.compile("\\b(fix|bug|hotfix|patch)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEYWORD_FEATURE = Pattern.compile("\\b(add|implement|create)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEYWORD_REFACTOR = Pattern.compile("\\b(refactor|cleanup|simplify)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEYWORD_DOCS = Pattern.compile("\\b(docs|readme|documentation)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEYWORD_TEST = Pattern.compile("\\b(test|spec)\\b", Pattern.CASE_INSENSITIVE);

    public CommitType classify(String message) {
        if (message == null || message.isBlank()) {
            return CommitType.OTHER;
        }
        String firstLine = message.strip().lines().findFirst().orElse(message);

        // Conventional Commits prefix always wins over keyword fallback (a "test:" commit that
        // happens to say "add" is still a TEST commit, not a FEATURE).
        if (PREFIX_FIX.matcher(firstLine).find()) return CommitType.FIX;
        if (PREFIX_FEATURE.matcher(firstLine).find()) return CommitType.FEATURE;
        if (PREFIX_REFACTOR.matcher(firstLine).find()) return CommitType.REFACTOR;
        if (PREFIX_DOCS.matcher(firstLine).find()) return CommitType.DOCUMENTATION;
        if (PREFIX_TEST.matcher(firstLine).find()) return CommitType.TEST;

        if (KEYWORD_FIX.matcher(firstLine).find()) return CommitType.FIX;
        if (KEYWORD_FEATURE.matcher(firstLine).find()) return CommitType.FEATURE;
        if (KEYWORD_REFACTOR.matcher(firstLine).find()) return CommitType.REFACTOR;
        if (KEYWORD_DOCS.matcher(firstLine).find()) return CommitType.DOCUMENTATION;
        if (KEYWORD_TEST.matcher(firstLine).find()) return CommitType.TEST;

        return CommitType.OTHER;
    }
}
