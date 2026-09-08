package dev.devwrapped.backend.analyzer;

import java.util.List;

/**
 * Classifies a batch of raw commit messages into {@link CommitType} using an LLM instead of the
 * regex/keyword rules in {@link CommitClassifier}. One call classifies many commits at once
 * (never one call per commit — see {@link CommitTypeResolver}'s chunking) so a full analysis costs
 * a handful of LLM calls, not hundreds. Implementations must return a list the same size and order
 * as the input, or throw — {@link CommitTypeResolver} treats a size mismatch the same as a thrown
 * exception and falls back to {@link CommitClassifier} for that chunk.
 */
public interface AiCommitClassifier {

    String name();

    List<CommitType> classifyBatch(List<String> messages);
}
