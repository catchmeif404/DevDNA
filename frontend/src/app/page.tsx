"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getMe, githubLoginUrl, logout, startMyAnalysis, type Me } from "@/lib/api";

export default function Home() {
  const router = useRouter();
  const [me, setMe] = useState<Me | null | undefined>(undefined);
  const [starting, setStarting] = useState(false);

  useEffect(() => {
    getMe()
      .then(setMe)
      .catch(() => setMe(null));
  }, []);

  async function handleLogout() {
    await logout();
    setMe(null);
  }

  async function handleReanalyze() {
    if (!me) return;
    setStarting(true);
    try {
      const { jobId } = await startMyAnalysis();
      router.push(`/dev/${me.githubLogin}?job=${jobId}`);
    } catch {
      setStarting(false);
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-b from-slate-950 to-indigo-950 px-6 text-white">
      <p className="text-sm tracking-[0.3em] text-indigo-300">DEV WRAPPED</p>
      <h1 className="mt-4 max-w-xl text-center text-3xl font-bold sm:text-4xl">
        당신의 GitHub, 재미있게 분석해드립니다
      </h1>
      <p className="mt-3 max-w-md text-center text-slate-400">
        GitHub 공개 활동을 분석해 당신이 어떤 개발자인지 알려드려요.
      </p>

      {me === undefined ? null : me ? (
        <div className="mt-10 flex w-full max-w-sm flex-col items-center gap-4">
          <div className="flex items-center gap-3">
            {me.avatarUrl && (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={me.avatarUrl} alt={me.githubLogin} className="h-10 w-10 rounded-full" />
            )}
            <span className="font-semibold">{me.githubLogin}님 환영합니다</span>
          </div>
          <button
            onClick={handleReanalyze}
            disabled={starting}
            className="w-full rounded-lg bg-indigo-500 px-4 py-3 font-semibold transition hover:bg-indigo-400 disabled:opacity-50"
          >
            {starting ? "분석 요청 중..." : "내 GitHub 다시 분석하기"}
          </button>
          <a
            href={`/dev/${me.githubLogin}`}
            className="text-sm text-slate-400 underline underline-offset-4 hover:text-slate-200"
          >
            최근 결과 보기
          </a>
          <button onClick={handleLogout} className="text-sm text-slate-500 hover:text-slate-300">
            로그아웃
          </button>
        </div>
      ) : (
        <a
          href={githubLoginUrl()}
          className="mt-10 flex w-full max-w-sm items-center justify-center gap-2 rounded-lg bg-white px-4 py-3 font-semibold text-slate-900 transition hover:bg-slate-200"
        >
          <svg viewBox="0 0 16 16" width="20" height="20" fill="currentColor" aria-hidden="true">
            <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z" />
          </svg>
          GitHub로 로그인
        </a>
      )}

      <p className="mt-6 max-w-sm text-center text-xs text-slate-500">
        GitHub로 로그인하면 자동으로 회원님의 공개 활동을 분석해 결과를 보여드려요.
      </p>
    </div>
  );
}
