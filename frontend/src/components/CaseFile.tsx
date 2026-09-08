"use client";

import { ArrowLeft, ArrowUpRight } from "lucide-react";
import { useTranslations } from "next-intl";
import { Link } from "@/i18n/navigation";
import LanguageSwitcher from "./LanguageSwitcher";

export default function CaseFile({ children, result = false }: {
  children: React.ReactNode;
  result?: boolean;
}) {
  const t = useTranslations("common");
  return (
    <div className="case-workspace">
      <nav className="workspace-nav" aria-label={t("navigation")}>
        <a href="https://catchmeif404.com">catchmeif404 <ArrowUpRight size={14} aria-hidden="true" /></a>
        <span>{t("archive")}</span>
      </nav>
      <main className="case-sheet" id="main">
        <div className="file-tab">{t("fileTab")}</div>
        <header className="file-topline">
          {result ? <Link href="/" className="inline-link"><ArrowLeft size={15} aria-hidden="true" /> DevDNA</Link>
            : <span>REF. 0000-404 / B</span>}
          <LanguageSwitcher />
        </header>
        {children}
        <footer className="file-footer">
          <p>{t("footer")}</p>
          <div><span>DevDNA / catchmeif404</span><a href="https://github.com/catchmeif404/DevDNA">{t("source")} <ArrowUpRight size={14} aria-hidden="true" /></a></div>
        </footer>
      </main>
    </div>
  );
}
