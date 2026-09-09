"use client";

import { useEffect, useState } from "react";
import { ArrowLeft, ArrowUpRight, Check, Copy, FileSearch, FileText, Fingerprint, RefreshCw, Tag } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useParams, useSearchParams } from "next/navigation";
import { Link, getPathname } from "@/i18n/navigation";
import {
  type AnalysisResult, type JobStatus, badgeUrl, getJobResult, getJobStatus,
  getLatestResultByUsername, shareCardUrl,
} from "@/lib/api";
import { developerTypeMeta, type DeveloperTypeLocale } from "@/lib/developerType";
import CaseFile from "@/components/CaseFile";

type ErrorKey = "errorNoResult" | "errorStatusFetchFailed" | "errorAnalysisFailed" | "errorInvalidJob";

export default function ResultPage() {
  const { username } = useParams<{ username: string }>();
  const jobId = useSearchParams().get("job");
  return <ResultLoader key={`${username}:${jobId}`} username={username} jobId={jobId} />;
}

function ResultLoader({ username, jobId }: { username: string; jobId: string | null }) {
  const t = useTranslations("result");
  const [job, setJob] = useState<JobStatus | null>(null);
  const [result, setResult] = useState<AnalysisResult | null>(null);
  const [error, setError] = useState<ErrorKey | null>(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let cancelled = false;
    let timer: ReturnType<typeof setTimeout>;
    async function poll() {
      try {
        if (jobId === null) {
          const data = await getLatestResultByUsername(username);
          if (!cancelled) setResult(data);
          return;
        }
        const id = Number(jobId);
        if (!/^\d+$/.test(jobId) || !Number.isSafeInteger(id) || id < 1) {
          if (!cancelled) setError("errorInvalidJob");
          return;
        }
        const status = await getJobStatus(id);
        if (cancelled) return;
        setJob(status);
        if (status.status === "COMPLETED") {
          const data = await getJobResult(id);
          if (!cancelled) setResult(data);
        } else if (status.status === "FAILED") {
          setError("errorAnalysisFailed");
        } else {
          timer = setTimeout(poll, 2000);
        }
      } catch {
        if (!cancelled) setError(jobId === null ? "errorNoResult" : "errorStatusFetchFailed");
      }
    }
    void poll();
    return () => { cancelled = true; clearTimeout(timer); };
  }, [jobId, username, attempt]);

  if (result) return <Report result={result} />;
  const statusKeys = {
    PENDING: "statusPending", COLLECTING: "statusCollecting", ANALYZING: "statusAnalyzing",
    COMPLETED: "statusCompleted", FAILED: "statusFailed",
  } as const;
  const progress = Math.min(100, Math.max(0, job?.progress ?? 0));
  return (
    <CaseFile result>
      <section className="status-body" aria-live="polite">
        <FileSearch size={42} strokeWidth={1.2} className="status-icon" aria-hidden="true" />
        <div className="section-kicker">DEVDNA / {t("classification")}</div>
        <h1>{error ? t("errorTitle") : t(statusKeys[job?.status ?? "PENDING"])}</h1>
        <p>{error ? t(error) : t("analyzingMessage", { username })}</p>
        {!error && <>
          <div className="progress-label"><label htmlFor="analysis-progress">{t("progress")}</label><span>{progress}%</span></div>
          <progress id="analysis-progress" max={100} value={progress} />
        </>}
        <div className="action-row">
          {error && <button className="button" onClick={() => { setError(null); setJob(null); setAttempt((v) => v + 1); }}><RefreshCw size={16} aria-hidden="true" />{t("retry")}</button>}
          <Link href="/" className="inline-link"><ArrowLeft size={16} aria-hidden="true" />{t("backToMainButton")}</Link>
        </div>
      </section>
    </CaseFile>
  );
}

function numericEntries(value: string | null): [string, number][] {
  if (!value) return [];
  try {
    const parsed: unknown = JSON.parse(value);
    if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) return [];
    return Object.entries(parsed).filter((entry): entry is [string, number] =>
      typeof entry[1] === "number" && Number.isFinite(entry[1]) && entry[1] >= 0,
    ).sort((a, b) => b[1] - a[1]);
  } catch { return []; }
}

function Report({ result }: { result: AnalysisResult }) {
  const locale = useLocale() as DeveloperTypeLocale;
  const t = useTranslations("result");
  const weekday = useTranslations("weekday");
  const meta = developerTypeMeta(result.developerType, locale);
  const scores = numericEntries(result.typeScores);
  const languages = numericEntries(result.languageRatios);
  const [artifact, setArtifact] = useState<"badge" | "card">("badge");
  const [feedback, setFeedback] = useState<"copyBadgeCopied" | "copyFailed" | null>(null);
  const [failedImage, setFailedImage] = useState<string | null>(null);
  const imageUrl = artifact === "badge" ? badgeUrl(result.githubUsername) : shareCardUrl(result.githubUsername);
  const resultPath = getPathname({ locale, href: `/dev/${result.githubUsername}` });
  // Results load after hydration; keeping the fallback also makes rendering safe on the server.
  const pageUrl = `${typeof window === "undefined" ? "" : window.location.origin}${resultPath}`;
  const shareText = t("shareText", { type: meta.label, tagline: meta.tagline });
  const shareQuery = new URLSearchParams({ text: shareText, url: pageUrl });
  const threadsQuery = new URLSearchParams({ text: `${shareText} ${pageUrl}` });
  const peakDay = result.peakWeekday >= 1 && result.peakWeekday <= 7
    ? weekday(String(result.peakWeekday)) : t("unrecorded");
  const number = (n: number) => n.toLocaleString(locale);
  const classificationLabel = result.classificationStatus === "UNCERTAIN"
    ? t("classificationUncertain")
    : result.classificationStatus === "INSUFFICIENT_EVIDENCE" ? t("classificationInsufficient") : t("reviewed");
  const observationWindow = result.observationFrom && result.observationTo
    ? t("observationWindow", { from: result.observationFrom.slice(0, 10), to: result.observationTo.slice(0, 10) })
    : t("observationWindowUnknown");

  useEffect(() => {
    if (!feedback) return;
    const timer = setTimeout(() => setFeedback(null), 2500);
    return () => clearTimeout(timer);
  }, [feedback]);

  async function copyArtifact() {
    try {
      await navigator.clipboard.writeText(artifact === "badge"
        ? `[![DevDNA](${badgeUrl(result.githubUsername)})](${pageUrl})`
        : pageUrl);
      setFeedback("copyBadgeCopied");
    } catch { setFeedback("copyFailed"); }
  }

  return (
    <CaseFile result>
      <section className="result-cover" aria-labelledby="result-title">
        <div className="section-kicker"><span>{t("report")}</span><span>{meta.code}</span></div>
        <div className="result-identity">
          <div><span className="field-label">{t("subject")}</span><strong className="subject-name">@{result.githubUsername}</strong></div>
          <span className={`stamp ${result.classificationStatus !== "CLASSIFIED" ? "stamp-warning" : ""}`}>{classificationLabel}</span>
        </div>
        <Fingerprint size={48} strokeWidth={1.2} className="status-icon" aria-hidden="true" />
        <p className="type-code">{t("classification")} / {meta.code}</p>
        <h1 id="result-title" className="result-title">{meta.label}</h1>
        <p className="result-tagline">{meta.tagline}</p>
        <p className="result-description">{meta.description}</p>
      </section>
      <section className="file-section" aria-labelledby="stats-title">
        <div className="section-heading"><h2 id="stats-title">{t("evidence")}</h2><span>01 / 03</span></div>
        <dl className="stats-ledger">
          <div><dt>{t("commits")}</dt><dd>{number(result.totalCommits)}</dd></div>
          <div><dt>{t("repositories")}</dt><dd>{number(result.totalRepositories)}</dd></div>
          <div><dt>{t("pullRequests")}</dt><dd>{number(result.totalPullRequests)}</dd></div>
        </dl>
        <dl className="detail-ledger">
          <div><dt>{t("peakTime")}</dt><dd>{result.peakHour >= 0 && result.peakHour < 24 ? `${String(result.peakHour).padStart(2, "0")}:00` : t("unrecorded")}</dd></div>
          <div><dt>{t("mainLanguage")}</dt><dd>{result.topLanguage ?? t("unrecorded")}</dd></div>
          <div><dt>{t("mostActiveDay")}</dt><dd>{peakDay}</dd></div>
        </dl>
        <p className="intake-note">{observationWindow}</p>
        {result.commitSampleCapped && <p className="intake-note">{t("sampleLimit")}</p>}
        {result.classificationStatus === "UNCERTAIN" && <p className="intake-note">{t("uncertainNote")}</p>}
        {result.classificationStatus === "INSUFFICIENT_EVIDENCE" && <p className="intake-note">{t("insufficientNote")}</p>}
      </section>
      <section className="file-section" aria-labelledby="patterns-title">
        <div className="section-heading"><h2 id="patterns-title">{t("patterns")}</h2><span>02 / 03</span></div>
        <p className="intake-note">{t("scoresNote")}</p>
        {scores.length ? scores.map(([type, rawScore]) => {
          const item = developerTypeMeta(type, locale);
          const score = Math.min(100, rawScore);
          return <div className="score-row" key={type}>
            <div className="score-label"><strong>{item.label}</strong><span>{number(score)} / 100</span></div>
            <div className="score-track" aria-hidden="true"><div style={{ width: `${score}%` }} /></div>
            <p>{item.description}</p>
          </div>;
        }) : <p className="intake-note">{t("noScores")}</p>}
        {languages.length > 0 && <div className="language-ledger" aria-label={t("languages")}>
          {languages.map(([language, ratio]) => <span key={language}>{language}<strong>{Math.round(Math.min(1, ratio) * 100)}%</strong></span>)}
        </div>}
        {locale === "ko" && result.aiSummary && <aside className="investigator-note">
          <h3>{t("summary")}</h3><p>{result.aiSummary}</p>
        </aside>}
      </section>
      <section className="file-section" aria-labelledby="attachments-title">
        <div className="section-heading"><h2 id="attachments-title">{t("attachments")}</h2><span>03 / 03</span></div>
        <div className="artifact-tabs" role="tablist" aria-label={t("attachments")}>
          {(["badge", "card"] as const).map((value) => <button key={value} id={`tab-${value}`}
            role="tab" aria-selected={artifact === value} aria-controls="artifact-panel"
            tabIndex={artifact === value ? 0 : -1}
            onClick={() => { setArtifact(value); setFeedback(null); }}
            onKeyDown={(event) => {
              if (["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) {
                event.preventDefault();
                const next = event.key === "Home" ? "badge" : event.key === "End" ? "card" : artifact === "badge" ? "card" : "badge";
                setArtifact(next); setFeedback(null);
                document.getElementById(`tab-${next}`)?.focus();
              }
            }}>
            {value === "badge" ? <Tag size={16} aria-hidden="true" /> : <FileText size={16} aria-hidden="true" />}{t(value)}
          </button>)}
        </div>
        <div id="artifact-panel" role="tabpanel" aria-labelledby={`tab-${artifact}`}>
          {failedImage === imageUrl ? <p className="artifact-missing" role="status">{t("artifactError")}</p> :
            <figure className={`artifact-preview ${artifact === "card" ? "card-preview" : ""}`}>
              {/* API-generated SVGs are served at their native size, without image optimization. */}
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img key={imageUrl} src={imageUrl} alt={t(artifact === "badge" ? "badgeAlt" : "cardAlt", { username: result.githubUsername })}
                onError={() => setFailedImage(imageUrl)} />
            </figure>}
          <div className="action-row">
            <button className="button button-primary" onClick={copyArtifact}>
              {feedback === "copyBadgeCopied" ? <Check size={16} aria-hidden="true" /> : <Copy size={16} aria-hidden="true" />}
              {t(artifact === "badge" ? "copyBadge" : "copyLink")}
            </button>
            <a className="inline-link" href={imageUrl} target="_blank" rel="noopener noreferrer">
              {t(artifact === "badge" ? "openBadge" : "viewShareCard")}<ArrowUpRight size={16} aria-hidden="true" />
            </a>
          </div>
          <p className="copy-feedback" role="status">{feedback ? t(feedback) : ""}</p>
        </div>
        <div className="share-links">
          <a className="inline-link" href={`https://twitter.com/intent/tweet?${shareQuery}`} target="_blank" rel="noopener noreferrer">{t("shareOnX")}<ArrowUpRight size={14} aria-hidden="true" /></a>
          <a className="inline-link" href={`https://www.threads.net/intent/post?${threadsQuery}`} target="_blank" rel="noopener noreferrer">{t("shareOnThreads")}<ArrowUpRight size={14} aria-hidden="true" /></a>
        </div>
      </section>
    </CaseFile>
  );
}
