/**
 * AI infrastructure and its one narrow use in the pipeline: {@link
 * dev.devwrapped.backend.ai.SummaryGenerator} produces natural-language summaries from already-
 * computed features and never sees raw commit text. {@link dev.devwrapped.backend.ai.ClaudeApiClient}
 * is shared infra (also used by {@link dev.devwrapped.backend.analyzer.ClaudeAiCommitClassifier} in
 * the analyzer package, which is the one place raw commit messages do reach an LLM — to classify
 * each one into a fixed {@link dev.devwrapped.backend.analyzer.CommitType}, never to compute a
 * statistic itself).
 */
package dev.devwrapped.backend.ai;
