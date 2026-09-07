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
  typeScores: string | null;
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

export function githubLoginUrl(): string {
  return `${API_URL}/api/auth/github`;
}
