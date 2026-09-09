import { defineRouting } from "next-intl/routing";

// English is the default and stays at clean URLs (/, /dev/[username], ...); Korean is available
// under /ko (/ko, /ko/dev/[username], ...).
export const routing = defineRouting({
  locales: ["ko", "en"],
  defaultLocale: "en",
  localePrefix: "as-needed",
});

export type Locale = (typeof routing.locales)[number];
