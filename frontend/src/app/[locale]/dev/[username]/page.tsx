"use client";

import { useEffect, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import { useParams, useSearchParams } from "next/navigation";
import { Link } from "@/i18n/navigation";
import {
  type AnalysisResult,
  type JobStatus,
  badgeUrl,
  getJobResult,
  getJobStatus,
  getLatestResultByUsername,
  shareCardUrl,
} from "@/lib/api";
import { developerTypeMeta, type DeveloperTypeLocale } from "@/lib/developerType";
import LanguageSwitcher from "@/components/LanguageSwitcher";

export default function ResultPage() {
  const params = useParams<{ username: string }>();
  const searchParams = useSearchParams();
  const jobId = searchParams.get("job");
  const locale = useLocale() as DeveloperTypeLocale;
  const t = useTranslations("result");
  const tWeekday = useTranslations("weekday");

  const STATUS_LABEL: Record<string, string> = {
    PENDING: t("statusPending"),
    COLLECTING: t("statusCollecting"),
    ANALYZING: t("statusAnalyzing"),
    COMPLETED: t("statusCompleted"),
    FAILED: t("statusFailed"),
  };

  const [job, setJob] = useState<JobStatus | null>(null);
  const [result, setResult] = useState<AnalysisResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [badgeCopied, setBadgeCopied] = useState(false);

  useEffect(() => {
    let cancelled = false;
    let timer: ReturnType<typeof setTimeout>;

    async function loadDirect() {
      try {
        const data = await getLatestResultByUsername(params.username);
        if (!cancelled) setResult(data);
      } catch {
        if (!cancelled) setError(t("errorNoResult"));
      }
    }

    async function poll() {
      if (!jobId) {
        await loadDirect();
        return;
      }
      try {
        const status = await getJobStatus(Number(jobId));
        if (cancelled) return;
        setJob(status);
        if (status.status === "COMPLETED") {
          const data = await getJobResult(Number(jobId));
          if (!cancelled) setResult(data);
        } else if (status.status === "FAILED") {
          setError(status.errorMessage || t("errorAnalysisFailed"));
        } else {
          timer = setTimeout(poll, 2000);
        }
      } catch {
        if (!cancelled) setError(t("errorStatusFetchFailed"));
      }
    }

    poll();
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [jobId, params.username, locale]);

  if (error) {
    return <StatusScreen title={t("errorTitle")} message={error} />;
  }

  if (!result) {
    const status = job?.status ?? "PENDING";
    return (
      <StatusScreen
        title={STATUS_LABEL[status] ?? t("statusAnalyzing")}
        message={t("analyzingMessage", { username: params.username })}
        progress={job?.progress ?? 0}
      />
    );
  }

  const meta = developerTypeMeta(result.developerType, locale);
  const languageRatios: Record<string, number> = result.languageRatios
    ? JSON.parse(result.languageRatios)
    : {};
  const typeScores: Record<string, number> = result.typeScores ? JSON.parse(result.typeScores) : {};
  const sortedTypeScores = Object.entries(typeScores).sort((a, b) => b[1] - a[1]);

  const pageUrl = `${window.location.origin}/dev/${result.githubUsername}`;
  const shareText = `${meta.emoji} ${meta.label}! "${meta.tagline}" - DevDNA`;
  const xShareUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(shareText)}&url=${encodeURIComponent(pageUrl)}`;
  const threadsShareUrl = `https://www.threads.net/intent/post?text=${encodeURIComponent(`${shareText} ${pageUrl}`)}`;
  const badgeMarkdown = `[![DevDNA](${badgeUrl(result.githubUsername)})](${pageUrl})`;

  async function copyBadgeMarkdown() {
    await navigator.clipboard.writeText(badgeMarkdown);
    setBadgeCopied(true);
    setTimeout(() => setBadgeCopied(false), 2000);
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-950 to-indigo-950 px-6 py-16 text-white">
      <div className="mx-auto max-w-2xl">
        <div className="flex items-center justify-between">
          <Link
            href="/"
            className="inline-flex items-center gap-1 text-sm text-slate-400 transition hover:text-white"
          >
            ← {t("backToMain")}
          </Link>
          <LanguageSwitcher />
        </div>
        <p className="mt-6 text-center text-sm tracking-[0.3em] text-indigo-300">DEV DNA</p>
        <p className="mt-2 text-center text-slate-400">{t("yourType", { username: result.githubUsername })}</p>
        <div className="mt-6 text-center text-7xl">{meta.emoji}</div>
        <h1 className="mt-2 text-center text-4xl font-bold">{meta.label}</h1>
        <p className="mt-2 text-center text-slate-300">&ldquo;{meta.tagline}&rdquo;</p>
        <p className="mx-auto mt-2 max-w-md text-center text-sm text-slate-400">{meta.description}</p>

        <div className="mt-10 grid grid-cols-3 gap-4 text-center">
          <Stat label="Commits" value={result.totalCommits.toLocaleString()} />
          <Stat label="Repositories" value={result.totalRepositories.toLocaleString()} />
          <Stat label="Pull Requests" value={result.totalPullRequests.toLocaleString()} />
        </div>

        <div className="mt-6 grid grid-cols-2 gap-4 text-center">
          <Stat label="Peak Coding Time" value={`${String(result.peakHour).padStart(2, "0")}:00`} />
          <Stat label="Main Language" value={result.topLanguage ?? "N/A"} />
        </div>

        <div className="mt-6 text-center text-sm text-slate-400">
          {t("mostActiveDay", { day: tWeekday(String(result.peakWeekday)) })}
        </div>

        <div className="mt-10 flex flex-col items-center gap-2">
          <img src={badgeUrl(result.githubUsername)} alt="DevDNA badge" className="h-auto max-w-full" />
          <button
            type="button"
            onClick={copyBadgeMarkdown}
            className="rounded-lg bg-slate-900/60 px-5 py-2 text-sm font-semibold hover:bg-slate-800"
          >
            {badgeCopied ? t("copyBadgeCopied") : t("copyBadge")}
          </button>
        </div>

        <div className="mt-10">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-400">
            Personality
          </h2>
          <div className="space-y-4">
            {sortedTypeScores.map(([type, score]) => {
              const typeMeta = developerTypeMeta(type, locale);
              return (
                <ScoreBar
                  key={type}
                  emoji={typeMeta.emoji}
                  label={typeMeta.label}
                  description={typeMeta.description}
                  score={score}
                />
              );
            })}
          </div>
        </div>

        {Object.keys(languageRatios).length > 0 && (
          <div className="mt-10">
            <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-400">
              Languages
            </h2>
            <div className="flex flex-wrap gap-2">
              {Object.entries(languageRatios)
                .sort((a, b) => b[1] - a[1])
                .map(([lang, ratio]) => (
                  <span key={lang} className="rounded-full bg-slate-800 px-3 py-1 text-sm">
                    {lang} {Math.round(ratio * 100)}%
                  </span>
                ))}
            </div>
          </div>
        )}

        {/* aiSummary is a Korean-only rule-based template (backend SummaryGenerator) -- showing
            it under an English UI would read as broken, so it's ko-only until the backend can
            produce a localized version. */}
        {locale === "ko" && result.aiSummary && (
          <p className="mt-10 rounded-lg bg-slate-900/60 p-4 text-center text-slate-200">
            {result.aiSummary}
          </p>
        )}

        <div className="mt-12 flex flex-col items-center gap-4">
          <img
            src={shareCardUrl(result.githubUsername)}
            alt="Share card"
            className="w-full max-w-xs rounded-2xl shadow-2xl"
          />
          <a
            href={shareCardUrl(result.githubUsername)}
            target="_blank"
            rel="noopener noreferrer"
            className="rounded-lg bg-indigo-500 px-5 py-2 font-semibold hover:bg-indigo-400"
          >
            {t("viewShareCard")}
          </a>
          <div className="flex gap-3">
            <a
              href={xShareUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg bg-slate-900/60 px-5 py-2 text-sm font-semibold hover:bg-slate-800"
            >
              {t("shareOnX")}
            </a>
            <a
              href={threadsShareUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg bg-slate-900/60 px-5 py-2 text-sm font-semibold hover:bg-slate-800"
            >
              {t("shareOnThreads")}
            </a>
          </div>

          <Link
            href="/"
            className="mt-6 rounded-lg border border-slate-700 px-5 py-2 text-sm font-semibold text-slate-300 transition hover:border-slate-500 hover:text-white"
          >
            {t("backToMainButton")}
          </Link>
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl bg-slate-900/60 py-4">
      <div className="text-2xl font-bold">{value}</div>
      <div className="mt-1 text-xs text-slate-400">{label}</div>
    </div>
  );
}

function ScoreBar({
  emoji,
  label,
  description,
  score,
}: {
  emoji: string;
  label: string;
  description: string;
  score: number;
}) {
  return (
    <div>
      <div className="flex items-center gap-3">
        <span className="w-40 shrink-0 text-sm text-slate-300">
          {emoji} {label}
        </span>
        <div className="h-2 flex-1 overflow-hidden rounded-full bg-slate-800">
          <div className="h-full rounded-full bg-indigo-400" style={{ width: `${score}%` }} />
        </div>
        <span className="w-8 shrink-0 text-right text-sm text-slate-400">{score}</span>
      </div>
      <p className="mt-1 pl-[52px] text-xs text-slate-500">{description}</p>
    </div>
  );
}

function StatusScreen({
  title,
  message,
  progress,
}: {
  title: string;
  message: string;
  progress?: number;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-b from-slate-950 to-indigo-950 px-6 text-center text-white">
      <h1 className="text-2xl font-bold">{title}</h1>
      <p className="mt-3 max-w-sm text-slate-400">{message}</p>
      {progress !== undefined && (
        <div className="mt-6 h-2 w-64 overflow-hidden rounded-full bg-slate-800">
          <div
            className="h-full rounded-full bg-indigo-400 transition-all"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}
    </div>
  );
}
