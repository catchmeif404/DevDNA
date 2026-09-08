import assert from "node:assert/strict";
import { readFile, mkdir } from "node:fs/promises";
import { chromium } from "playwright";

const origin = process.env.DEVDNA_TEST_URL ?? "http://localhost:3010";
const output = new URL("../artifacts/case-file/", import.meta.url);
await mkdir(output, { recursive: true });
const badge = await readFile(new URL("../public/sample-badge.svg", import.meta.url), "utf8");
const card = await readFile(new URL("../public/sample-card.svg", import.meta.url), "utf8");
const result = {
  githubUsername: "example-dev",
  totalCommits: 1842, totalRepositories: 24, totalPullRequests: 89,
  peakHour: 2, peakWeekday: 6, topLanguage: "TypeScript",
  developerType: "BUG_SLAYER",
  typeScores: JSON.stringify({ BUG_SLAYER: 89, BUILDER: 72, COLLABORATOR: 65, NIGHT_OWL: 44,
    POLYGLOT: 40, WEEKEND_WARRIOR: 38, REFACTOR_MASTER: 32, DOCUMENTARIAN: 28, TESTER: 24, EXPLORER: 18 }),
  languageRatios: JSON.stringify({ TypeScript: .68, Java: .24, CSS: .08 }),
  aiSummary: null, dnaVector: null,
};
const browser = await chromium.launch();
const errors = [];
let checks = 0;
try {
  const context = await browser.newContext({ locale: "en-US" });
  const page = await context.newPage();
  page.on("pageerror", (error) => errors.push(error.message));
  let signedIn = false;
  let mode = "complete";
  let fixture = result;
  await page.route("**/api/**", async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;
    const respond = (json, status = 200) => route.fulfill({
      status, contentType: "application/json", body: JSON.stringify(json),
    });
    if (path.endsWith("/me")) return respond(signedIn ? { githubLogin: "example-dev", avatarUrl: null } : null, signedIn ? 200 : 401);
    if (path.endsWith("/logout")) { signedIn = false; return respond({}); }
    if (path === "/api/analyses") return respond({ jobId: 17, status: "PENDING" });
    if (path.startsWith("/api/badge/")) return route.fulfill({ contentType: "image/svg+xml", body: badge });
    if (path.endsWith("/card")) return route.fulfill({ contentType: "image/svg+xml", body: card });
    if (path.endsWith("/result")) return mode === "error" ? respond({}, 404) : respond(fixture);
    if (path.startsWith("/api/analyses/")) return respond({
      jobId: 17, status: mode === "pending" ? "COLLECTING" : mode === "failed" ? "FAILED" : "COMPLETED",
      progress: mode === "pending" ? 35 : 100, errorMessage: "server detail",
    });
    return respond({}, 404);
  });

  async function noOverflow() {
    const overflow = await page.evaluate(() => {
      const viewport = document.documentElement.clientWidth;
      return [...document.querySelectorAll("main, h1, h2, button, select, img, .subject-name, .score-label")]
        .filter((element) => {
          const rect = element.getBoundingClientRect();
          return rect.width && (rect.right > viewport + 1 || rect.left < -1 || element.scrollWidth > element.clientWidth + 1);
        }).map((element) => element.tagName + ":" + element.className);
    });
    assert.deepEqual(overflow, []);
    checks++;
  }
  async function snapshot(name) {
    await page.evaluate(() => document.fonts.ready);
    await page.screenshot({ path: new URL(name + ".png", output).pathname, fullPage: true });
  }
  async function assertImages() {
    const imgs = page.locator("main img");
    for (const img of await imgs.all()) {
      await img.evaluate((element) => element.decode());
      assert(await img.evaluate((element) => element.naturalWidth > 0));
    }
    checks++;
  }
  for (const locale of ["en", "ko"]) {
    for (const width of [1440, 390, 320]) {
      await page.setViewportSize({ width, height: 900 });
      await page.goto(origin + (locale === "en" ? "/en" : "/ko"));
      await page.getByRole("link", { name: locale === "en" ? "Open my file with GitHub" : "GitHub로 내 기록 열기" }).waitFor();
      assert.equal(await page.locator("html").getAttribute("lang"), locale);
      await noOverflow();
      await assertImages();
      await snapshot("home-" + locale + "-" + width);
      await page.goto(origin + (locale === "en" ? "/en" : "/ko") + "/dev/example-dev");
      await page.getByRole("heading", { name: "BUG HUNTER", exact: true }).waitFor();
      await assertImages();
      await noOverflow();
      await snapshot("result-" + locale + "-" + width);
      await page.getByRole("tab", { name: locale === "en" ? "Share report" : "공유 보고서" }).click();
      await assertImages();
      await noOverflow();
      await snapshot("report-" + locale + "-" + width);
    }
  }

  await page.setViewportSize({ width: 390, height: 844 });
  signedIn = true;
  await page.goto(origin + "/en");
  await page.getByRole("button", { name: "Reopen my investigation" }).waitFor();
  mode = "pending";
  await page.getByRole("button", { name: "Reopen my investigation" }).click();
  await page.getByRole("heading", { name: "Collecting evidence" }).waitFor();
  assert.equal(new URL(page.url()).searchParams.get("job"), "17");
  await page.getByRole("combobox", { name: "Language" }).selectOption("ko");
  await page.getByRole("heading", { name: "증거 수집 중" }).waitFor();
  assert.equal(new URL(page.url()).searchParams.get("job"), "17");
  await snapshot("processing-ko-mobile");
  mode = "complete";
  await page.getByRole("heading", { name: "BUG HUNTER", exact: true }).waitFor({ timeout: 15000 });
  checks++;

  await page.getByRole("combobox", { name: "언어" }).selectOption("en");
  await page.getByRole("button", { name: "Copy badge markdown" }).waitFor();
  await context.grantPermissions(["clipboard-read", "clipboard-write"]);
  await page.getByRole("button", { name: "Copy badge markdown" }).click();
  await page.getByRole("status").filter({ hasText: "Copied to clipboard." }).waitFor();
  const markdown = await page.evaluate(() => navigator.clipboard.readText());
  assert(markdown.includes("/en/dev/example-dev"));
  assert(!markdown.includes("?job="));
  const shareUrl = new URL(await page.getByRole("link", { name: "Share on X" }).getAttribute("href"));
  assert.equal(shareUrl.searchParams.get("url"), origin + "/en/dev/example-dev");
  await page.evaluate(() => Object.defineProperty(navigator.clipboard, "writeText", {
    configurable: true, value: async () => { throw new Error("denied"); },
  }));
  await page.getByRole("button", { name: "Copy badge markdown" }).click();
  await page.getByRole("status").filter({ hasText: "Clipboard unavailable" }).waitFor();
  checks++;

  await page.getByRole("tab", { name: "README badge" }).focus();
  await page.keyboard.press("ArrowRight");
  assert.equal(await page.getByRole("tab", { name: "Share report" }).getAttribute("aria-selected"), "true");
  await page.keyboard.press("Home");
  assert.equal(await page.getByRole("tab", { name: "README badge" }).getAttribute("aria-selected"), "true");
  checks++;

  mode = "error";
  await page.goto(origin + "/en/dev/missing");
  await page.getByRole("heading", { name: "The investigation hit a dead end." }).waitFor();
  await noOverflow();
  await snapshot("error-en-mobile");
  mode = "complete";
  await page.getByRole("button", { name: "Check again" }).click();
  await page.getByRole("heading", { name: "BUG HUNTER", exact: true }).waitFor();
  await page.goto(origin + "/en/dev/example-dev?job=invalid");
  await page.getByText("This job reference is invalid.", { exact: false }).waitFor();
  mode = "failed";
  await page.goto(origin + "/en/dev/example-dev?job=18");
  await page.getByText("The analysis could not be completed.", { exact: false }).waitFor();
  checks++;

  mode = "complete";
  fixture = { ...result, githubUsername: "a".repeat(39), developerType: "POLYGLOT", totalCommits: 2147483647 };
  await page.goto(origin + "/en/dev/long-subject");
  await page.getByRole("heading", { name: "MULTILINGUAL OPERATOR", exact: true }).waitFor();
  await noOverflow();
  fixture = { ...result, languageRatios: "invalid", typeScores: "[]", developerType: "unknown" };
  await page.goto(origin + "/en/dev/unclassified");
  await page.getByRole("heading", { name: "UNCLASSIFIED", exact: true }).waitFor();
  await noOverflow();
  await page.goto(origin + "/en");
  await page.getByRole("button", { name: "Log out", exact: true }).click();
  await page.getByRole("link", { name: "Open my file with GitHub" }).waitFor();
  assert.deepEqual(errors, []);
  console.log("Case-file UI checks passed:", checks, "/ screenshots:", output.pathname);
} finally {
  await browser.close();
}
