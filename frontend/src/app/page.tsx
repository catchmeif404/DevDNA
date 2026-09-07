"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createAnalysis } from "@/lib/api";

export default function Home() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!username.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const { jobId } = await createAnalysis(username.trim());
      router.push(`/dev/${username.trim()}?job=${jobId}`);
    } catch {
      setError("분석 요청에 실패했습니다. GitHub username을 확인해주세요.");
      setLoading(false);
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

      <form onSubmit={handleSubmit} className="mt-10 flex w-full max-w-sm flex-col gap-3">
        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="GitHub username (예: octocat)"
          className="rounded-lg border border-slate-700 bg-slate-900/60 px-4 py-3 text-white placeholder:text-slate-500 focus:border-indigo-400 focus:outline-none"
        />
        <button
          type="submit"
          disabled={loading}
          className="rounded-lg bg-indigo-500 px-4 py-3 font-semibold transition hover:bg-indigo-400 disabled:opacity-50"
        >
          {loading ? "분석 요청 중..." : "지금 바로 분석하기"}
        </button>
        {error && <p className="text-sm text-red-400">{error}</p>}
      </form>

      <p className="mt-6 max-w-sm text-center text-xs text-slate-500">
        MVP 테스트 모드: 로그인 없이 공개 username으로 바로 분석합니다. 실제 서비스에서는 GitHub
        로그인 후 자동으로 분석됩니다.
      </p>
    </div>
  );
}
