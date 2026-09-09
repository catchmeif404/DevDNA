"use client";

import { Languages } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useTransition } from "react";
import { usePathname, useRouter } from "@/i18n/navigation";
import type { Locale } from "@/i18n/routing";

export default function LanguageSwitcher() {
  const locale = useLocale();
  const t = useTranslations("common");
  const pathname = usePathname();
  const router = useRouter();
  const [pending, startTransition] = useTransition();

  return (
    <div className="language-switch" role="tablist" aria-label={t("language")}>
      <Languages size={17} aria-hidden="true" />
      {(["ko", "en"] as Locale[]).map((nextLocale) => (
        <button
          key={nextLocale}
          type="button"
          role="tab"
          aria-selected={locale === nextLocale}
          disabled={pending || locale === nextLocale}
          onClick={() => startTransition(() => router.replace(
            `${pathname}${window.location.search}${window.location.hash}`,
            { locale: nextLocale, scroll: false },
          ))}
        >
          {nextLocale === "ko" ? "한국어" : "English"}
        </button>
      ))}
    </div>
  );
}
