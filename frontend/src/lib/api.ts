const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8090";

export type AnalysisStatus = "PENDING" | "COLLECTING" | "ANALYZING" | "COMPLETED" | "FAILED";

export interface JobStatus {
  jobId: number;
  status: AnalysisStatus;
  progress: number;
  errorMessage: string;
}

export interface AnalysisResult {
  githubUsername: string;
  totalCommits: number;
  totalRepositories: number;
  totalPullRequests: number;
  peakHour: number;
  peakWeekday: number;
  topLanguage: string | null;
  languageRatios: string | null;
  nightOwlScore: number;
  bugSlayerScore: number;
  builderScore: number;
  polyglotScore: number;
  developerType: string;
  dnaVector: string | null;
  aiSummary: string | null;
}

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`API error ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export function createAnalysis(githubUsername: string): Promise<{ jobId: number; status: AnalysisStatus }> {
  return fetch(`${API_URL}/api/analyses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ githubUsername }),
  }).then((res) => json<{ jobId: number; status: AnalysisStatus }>(res));
}

export function getJobStatus(jobId: number): Promise<JobStatus> {
  return fetch(`${API_URL}/api/analyses/${jobId}`).then((res) => json<JobStatus>(res));
}

export function getJobResult(jobId: number): Promise<AnalysisResult> {
  return fetch(`${API_URL}/api/analyses/${jobId}/result`).then((res) => json<AnalysisResult>(res));
}

export function getLatestResultByUsername(username: string): Promise<AnalysisResult> {
  return fetch(`${API_URL}/api/users/${username}/result`).then((res) => json<AnalysisResult>(res));
}

export function shareCardUrl(username: string): string {
  return `${API_URL}/api/share/${username}/card`;
}
