/**
 * Feature extraction from raw GitHub data (commit counts, language ratios, peak hours). Commit-type
 * classification (feat/fix/refactor/docs/test/other) may be delegated to an LLM
 * ({@link dev.devwrapped.backend.analyzer.CommitTypeResolver}), but every ratio/histogram built on
 * top of the resulting labels is still plain deterministic arithmetic — the AI only ever emits one
 * of a fixed set of labels, never a statistic.
 */
package dev.devwrapped.backend.analyzer;
