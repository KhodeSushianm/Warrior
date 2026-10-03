const GITHUB_API = "https://api.github.com";

export type RepoSummary = {
  owner: string;
  name: string;
  fullName: string;
  htmlUrl: string;
  description: string | null;
  defaultBranch: string;
  visibility: "public" | "private";
  language: string | null;
  stars: number;
  forks: number;
  openIssues: number;
  sizeKb: number;
  pushedAt: string | null;
  createdAt: string | null;
};

export type BranchSummary = {
  name: string;
  protected: boolean;
  sha: string | null;
};

export type CommitSummary = {
  sha: string;
  shortSha: string;
  message: string;
  author: string;
  authorAvatarUrl: string | null;
  date: string | null;
  htmlUrl: string;
};

export type GitHubConnection = {
  connected: boolean;
  mode: "token" | "none";
  repo: string | null;
  reason?: string;
};

type RawRepo = {
  name: string;
  full_name: string;
  html_url: string;
  description: string | null;
  default_branch: string;
  private: boolean;
  language: string | null;
  stargazers_count: number;
  forks_count: number;
  open_issues_count: number;
  size: number;
  pushed_at: string | null;
  created_at: string | null;
  owner?: { login: string };
};

type RawBranch = {
  name: string;
  protected?: boolean;
  commit?: { sha?: string } | null;
};

type RawCommit = {
  sha: string;
  html_url: string;
  commit: {
    message: string;
    author?: { name?: string | null; date?: string | null } | null;
  };
  author?: { login?: string | null; avatar_url?: string | null } | null;
};

function getAccessToken(): string | null {
  const token = process.env.GITHUB_ACCESS_TOKEN?.trim();
  return token ? token : null;
}

function getRepoSlug(): string | null {
  const repo = process.env.GITHUB_REPO?.trim();
  if (!repo || !repo.includes("/")) {
    return null;
  }
  return repo.replace(/^\/+|\/+$/g, "");
}

export function getGitHubConnection(): GitHubConnection {
  const token = getAccessToken();
  const repo = getRepoSlug();

  if (!token) {
    return { connected: false, mode: "none", repo: null, reason: "GITHUB_ACCESS_TOKEN is not set." };
  }
  if (!repo) {
    return { connected: false, mode: "none", repo: null, reason: "GITHUB_REPO must be in owner/name format." };
  }

  return { connected: true, mode: "token", repo };
}

async function githubFetch(path: string, token: string): Promise<Response> {
  return fetch(`${GITHUB_API}${path}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28",
      "User-Agent": "warrior-nextjs-dashboard",
    },
    cache: "no-store",
  });
}

export class GitHubApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "GitHubApiError";
    this.status = status;
  }
}

export async function fetchRepoSummary(): Promise<RepoSummary> {
  const connection = getGitHubConnection();
  if (!connection.connected || !connection.repo) {
    throw new GitHubApiError(connection.reason ?? "GitHub is not configured.", 400);
  }

  const token = getAccessToken() as string;
  const response = await githubFetch(`/repos/${connection.repo}`, token);

  if (!response.ok) {
    throw new GitHubApiError(`Unable to load repository (${response.status}).`, response.status);
  }

  const raw = (await response.json()) as RawRepo;
  const owner = raw.owner?.login ?? connection.repo.split("/")[0] ?? "";

  return {
    owner,
    name: raw.name,
    fullName: raw.full_name,
    htmlUrl: raw.html_url,
    description: raw.description,
    defaultBranch: raw.default_branch,
    visibility: raw.private ? "private" : "public",
    language: raw.language,
    stars: raw.stargazers_count,
    forks: raw.forks_count,
    openIssues: raw.open_issues_count,
    sizeKb: raw.size,
    pushedAt: raw.pushed_at,
    createdAt: raw.created_at,
  };
}

export async function fetchBranches(limit = 8): Promise<BranchSummary[]> {
  const connection = getGitHubConnection();
  if (!connection.connected || !connection.repo) {
    return [];
  }

  const token = getAccessToken() as string;
  const response = await githubFetch(
    `/repos/${connection.repo}/branches?per_page=${limit}`,
    token,
  );

  if (!response.ok) {
    return [];
  }

  const raw = (await response.json()) as RawBranch[];
  return raw.map((branch) => ({
    name: branch.name,
    protected: Boolean(branch.protected),
    sha: branch.commit?.sha ?? null,
  }));
}

export async function fetchRecentCommits(limit = 6): Promise<CommitSummary[]> {
  const connection = getGitHubConnection();
  if (!connection.connected || !connection.repo) {
    return [];
  }

  const token = getAccessToken() as string;
  const response = await githubFetch(
    `/repos/${connection.repo}/commits?per_page=${limit}`,
    token,
  );

  if (!response.ok) {
    return [];
  }

  const raw = (await response.json()) as RawCommit[];
  return raw.map((commit) => ({
    sha: commit.sha,
    shortSha: commit.sha.slice(0, 7),
    message: commit.commit.message.split("\n")[0] ?? "",
    author: commit.author?.login ?? commit.commit.author?.name ?? "unknown",
    authorAvatarUrl: commit.author?.avatar_url ?? null,
    date: commit.commit.author?.date ?? null,
    htmlUrl: commit.html_url,
  }));
}

export async function fetchAuthedLogin(): Promise<string | null> {
  const token = getAccessToken();
  if (!token) {
    return null;
  }

  const response = await githubFetch("/user", token);
  if (!response.ok) {
    return null;
  }

  const raw = (await response.json()) as { login?: string };
  return raw.login ?? null;
}
