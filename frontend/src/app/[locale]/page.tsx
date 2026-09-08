"use client";

import { useEffect, useState } from "react";
import { ArrowRight, FileSearch, FolderOpen, LogOut, RefreshCw } from "lucide-react";
import { useTranslations } from "next-intl";
import { Link, useRouter } from "@/i18n/navigation";
import { getMe, githubLoginUrl, logout, startMyAnalysis, type Me } from "@/lib/api";
import CaseFile from "@/components/CaseFile";

export default function Home() {
  const router = useRouter();
  const t = useTranslations("home");
  const [me, setMe] = useState<Me | null | undefined>(undefined);
  const [busy, setBusy] = useState<"analysis" | "logout" | null>(null);
  const [error, setError] = useState<"analysisError" | "logoutError" | null>(null);

  useEffect(() => {
    let active = true;
    getMe().then((user) => { if (active) setMe(user); }).catch(() => { if (active) setMe(null); });
    return () => { active = false; };
  }, []);

  async function handleAction(action: "analysis" | "logout") {
    if (!me || busy) return;
    setBusy(action);
    setError(null);
    try {
      if (action === "logout") { await logout(); setMe(null); }
      else {
        const { jobId } = await startMyAnalysis();
        router.push(`/dev/${me.githubLogin}?job=${jobId}`);
      }
    } catch { setError(action === "analysis" ? "analysisError" : "logoutError"); }
    finally { setBusy(null); }
  }

  return (
    <CaseFile>
      <section className="file-cover" aria-labelledby="product-title">
        <div className="section-kicker"><span>{t("exhibit")}</span><span className="case-state">{t("open")}</span></div>
        <h1 id="product-title" className="product-title">DevDNA<span className="title-period">.</span></h1>
        <p className="cover-subtitle">{t("title")}</p>
        <p className="cover-copy">{t("subtitle")}</p>
        <div className="subject-record">
          <span className="field-label">{t("subject")}</span>
          {me ? <strong className="subject-name">@{me.githubLogin}</strong> :
            <span className="redacted-subject" aria-label={t("unidentified")}><span /><span /></span>}
          <span className="stamp">{t(me ? "identified" : "unidentified")}</span>
        </div>
        <div className="intake-actions">
          {me === undefined ? <p className="session-pending" role="status"><FileSearch size={18} aria-hidden="true" /> {t("checkingSession")}</p>
            : me ? <>
              <button className="button button-primary" onClick={() => handleAction("analysis")} disabled={busy !== null}>
                <RefreshCw size={18} className={busy === "analysis" ? "spin" : ""} aria-hidden="true" />
                {t(busy === "analysis" ? "reanalyzing" : "reanalyzeButton")}
              </button>
              <Link href={`/dev/${me.githubLogin}`} className="inline-link">{t("viewLatestResult")} <ArrowRight size={17} aria-hidden="true" /></Link>
              <button className="icon-button" title={t("logout")} aria-label={t("logout")} disabled={busy !== null} onClick={() => handleAction("logout")}><LogOut size={18} /></button>
            </> : <a href={githubLoginUrl()} className="button button-primary"><FolderOpen size={19} aria-hidden="true" />{t("loginButton")}<ArrowRight size={18} aria-hidden="true" /></a>}
        </div>
        {error && <p className="error-note" role="alert">{t(error)}</p>}
        <p className="intake-note">{t("loginHint")}</p>
      </section>
      <section className="file-section" aria-labelledby="evidence-title">
        <div className="section-heading"><h2 id="evidence-title">{t("evidenceTitle")}</h2><span>01 / 02</span></div>
        <dl className="evidence-index">
          <div><dt>01 / {t("commits")}</dt><dd>{t("commitsNote")}</dd></div>
          <div><dt>02 / {t("repos")}</dt><dd>{t("reposNote")}</dd></div>
          <div><dt>03 / {t("prs")}</dt><dd>{t("prsNote")}</dd></div>
        </dl>
      </section>
      <section className="file-section" aria-labelledby="sample-title">
        <div className="section-heading"><h2 id="sample-title">{t("sampleTitle")}</h2><span>02 / 02</span></div>
        <figure className="sample-evidence">
          {/* This SVG comes from the same backend renderer as public README badges. */}
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src="/sample-badge.svg" alt={t("sampleAlt")} width={600} height={200} />
          <figcaption>{t("sampleCaption")}</figcaption>
        </figure>
      </section>
    </CaseFile>
  );
}
