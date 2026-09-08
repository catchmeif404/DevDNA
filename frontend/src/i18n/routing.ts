import { defineRouting } from "next-intl/routing";

// Korean is the default and stays at clean URLs (/, /dev/[username], ...); English lives under
// /en (/en, /en/dev/[username], ...) -- same "as-needed" convention as aws-cost-calculator's
// frontend, so every already-shared Korean URL (badge markdown, README links) keeps working.
export const routing = defineRouting({
  locales: ["ko", "en"],
  defaultLocale: "ko",
  localePrefix: "as-needed",
});

export type Locale = (typeof routing.locales)[number];
