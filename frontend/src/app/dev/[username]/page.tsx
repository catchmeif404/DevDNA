"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useSearchParams } from "next/navigation";
import {
  type AnalysisResult,
  type JobStatus,
  badgeUrl,
  getJobResult,
  getJobStatus,
  getLatestResultByUsername,
  shareCardUrl,
} from "@/lib/api";
import { developerTypeMeta, weekdayLabel } from "@/lib/developerType";

const STATUS_LABEL: Record<string, string> = {
  PENDING: "대기 중",
  COLLECTING: "GitHub 데이터 수집 중",
  ANALYZING: "분석 중",
  COMPLETED: "완료",
  FAILED: "실패",
};

export default function ResultPage() {
  const params = useParams<{ username: string }>();
  const searchParams = useSearchParams();
  const jobId = searchParams.get("job");

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
        if (!cancelled) setError("아직 분석 결과가 없습니다. 홈에서 먼저 분석을 요청해주세요.");
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
          setError(status.errorMessage || "분석에 실패했습니다.");
        } else {
          timer = setTimeout(poll, 2000);
        }
      } catch {
        if (!cancelled) setError("분석 상태를 불러오지 못했습니다.");
      }
    }

    poll();
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [jobId, params.username]);

  if (error) {
    return <StatusScreen title="문제가 발생했어요" message={error} />;
  }

  if (!result) {
    const status = job?.status ?? "PENDING";
    return (
      <StatusScreen
        title={STATUS_LABEL[status] ?? "분석 중"}
        message={`${params.username}님의 GitHub 활동을 살펴보고 있어요...`}
        progress={job?.progress ?? 0}
      />
    );
  }

  const meta = developerTypeMeta(result.developerType);
  const languageRatios: Record<string, number> = result.languageRatios
    ? JSON.parse(result.languageRatios)
    : {};
  const typeScores: Record<string, number> = result.typeScores ? JSON.parse(result.typeScores) : {};
  const sortedTypeScores = Object.entries(typeScores).sort((a, b) => b[1] - a[1]);

  const pageUrl = `${window.location.origin}/dev/${result.githubUsername}`;
  const shareText = `나는 ${meta.emoji} ${meta.label}! "${meta.tagline}" - DevDNA로 내 GitHub 분석해보기`;
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
        <Link
          href="/"
          className="inline-flex items-center gap-1 text-sm text-slate-400 transition hover:text-white"
        >
          ← 메인으로
        </Link>
        <p className="mt-6 text-center text-sm tracking-[0.3em] text-indigo-300">DEV DNA</p>
        <p className="mt-2 text-center text-slate-400">{result.githubUsername}님의 개발자 유형</p>
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
          가장 활발한 요일: {weekdayLabel(result.peakWeekday)}요일
        </div>

        <div className="mt-10">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-400">
            Personality
          </h2>
          <div className="space-y-4">
            {sortedTypeScores.map(([type, score]) => {
              const typeMeta = developerTypeMeta(type);
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

        {result.aiSummary && (
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
            공유 카드 보기
          </a>
          <div className="flex gap-3">
            <a
              href={xShareUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg bg-slate-900/60 px-5 py-2 text-sm font-semibold hover:bg-slate-800"
            >
              X에 공유하기
            </a>
            <a
              href={threadsShareUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg bg-slate-900/60 px-5 py-2 text-sm font-semibold hover:bg-slate-800"
            >
              Threads에 공유하기
            </a>
          </div>

          <div className="mt-4 flex flex-col items-center gap-2">
            <img src={badgeUrl(result.githubUsername)} alt="DevDNA badge" className="h-auto max-w-full" />
            <button
              type="button"
              onClick={copyBadgeMarkdown}
              className="rounded-lg bg-slate-900/60 px-5 py-2 text-sm font-semibold hover:bg-slate-800"
            >
              {badgeCopied ? "복사됨!" : "README 뱃지 마크다운 복사"}
            </button>
          </div>

          <Link
            href="/"
            className="mt-6 rounded-lg border border-slate-700 px-5 py-2 text-sm font-semibold text-slate-300 transition hover:border-slate-500 hover:text-white"
          >
            메인으로 돌아가기
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
