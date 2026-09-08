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
    <label className="language-switch" title={t("language")}>
      <Languages size={17} aria-hidden="true" />
      <select aria-label={t("language")} value={locale} disabled={pending}
        onChange={(event) => {
          const nextLocale = event.target.value as Locale;
          startTransition(() => router.replace(
            `${pathname}${window.location.search}${window.location.hash}`,
            { locale: nextLocale, scroll: false },
          ));
        }}>
        <option value="ko">한국어</option>
        <option value="en">English</option>
      </select>
    </label>
  );
}
