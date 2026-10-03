import { db } from "@/db";
import { sql } from "drizzle-orm";
import { cookies } from "next/headers";
import Link from "next/link";
import {
  fetchAuthedLogin,
  fetchBranches,
  fetchRecentCommits,
  fetchRepoSummary,
  getGitHubConnection,
  type BranchSummary,
  type CommitSummary,
  type RepoSummary,
} from "@/lib/github-api";

export const dynamic = "force-dynamic";

type HomePageProps = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

function getParamValue(value: string | string[] | undefined): string | null {
  if (Array.isArray(value)) {
    return value[0] ?? null;
  }
  return value ?? null;
}

function formatDate(value: string | null): string {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return date.toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3">
      <p className="text-[11px] uppercase tracking-[0.12em] text-slate-400">{label}</p>
      <p className="mt-1 text-lg font-semibold text-white">{value}</p>
    </div>
  );
}

export default async function HomePage({ searchParams }: HomePageProps) {
  await db.execute(sql`select 1`);

  const params = await searchParams;
  const oauthConnected = getParamValue(params.github_connected) === "1";
  const disconnected = getParamValue(params.github_disconnected) === "1";
  const githubError = getParamValue(params.github_error);

  const cookieStore = await cookies();
  const oauthLogin = cookieStore.get("github_login")?.value ?? "";
  const oauthAvatar = cookieStore.get("github_avatar_url")?.value ?? "";
  const oauthProfileUrl = cookieStore.get("github_profile_url")?.value ?? "";
  const oauthEmail = cookieStore.get("github_email")?.value ?? "";
  const oauthName = cookieStore.get("github_name")?.value ?? "";
  const isOAuthConnected = cookieStore.get("github_connected")?.value === "1";

  const connection = getGitHubConnection();
  const oauthConfigured = Boolean(process.env.GITHUB_CLIENT_ID && process.env.GITHUB_CLIENT_SECRET);

  let repo: RepoSummary | null = null;
  let branches: BranchSummary[] = [];
  let commits: CommitSummary[] = [];
  let authenticatedAs: string | null = null;
  let repoError: string | null = null;

  if (connection.connected) {
    try {
      const [repoData, branchData, commitData, loginData] = await Promise.all([
        fetchRepoSummary(),
        fetchBranches(8),
        fetchRecentCommits(6),
        fetchAuthedLogin(),
      ]);
      repo = repoData;
      branches = branchData;
      commits = commitData;
      authenticatedAs = loginData;
    } catch (error) {
      repoError = error instanceof Error ? error.message : "Unable to load repository data.";
    }
  }

  return (
    <main className="min-h-screen bg-[#0b1020] px-6 py-12 text-slate-100">
      <div className="mx-auto w-full max-w-4xl space-y-8">
        <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">GitHub Integration</p>
            <h1 className="mt-2 text-[clamp(1.9rem,4.5vw,2.75rem)] font-semibold leading-tight text-white">
              Repository Connection
            </h1>
          </div>
          <span
            className={`inline-flex w-fit items-center gap-2 rounded-full border px-4 py-2 text-sm font-medium ${
              connection.connected && repo
                ? "border-emerald-400/30 bg-emerald-400/10 text-emerald-300"
                : "border-amber-400/30 bg-amber-400/10 text-amber-300"
            }`}
          >
            <span
              className={`h-2 w-2 rounded-full ${
                connection.connected && repo ? "bg-emerald-400" : "bg-amber-400"
              }`}
            />
            {connection.connected && repo ? "Connected" : "Not connected"}
          </span>
        </header>

        {oauthConnected ? (
          <div className="rounded-2xl border border-emerald-400/25 bg-emerald-400/10 px-5 py-4 text-sm text-emerald-200">
            GitHub OAuth account connected successfully.
          </div>
        ) : null}

        {disconnected ? (
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] px-5 py-4 text-sm text-slate-300">
            OAuth account disconnected.
          </div>
        ) : null}

        {githubError ? (
          <div className="rounded-2xl border border-rose-400/25 bg-rose-400/10 px-5 py-4 text-sm text-rose-200">
            {githubError}
          </div>
        ) : null}

        {repoError ? (
          <div className="rounded-2xl border border-rose-400/25 bg-rose-400/10 px-5 py-4 text-sm text-rose-200">
            {repoError}
          </div>
        ) : null}

        {!connection.connected ? (
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] px-5 py-4 text-sm text-slate-300">
            {connection.reason ?? "Set GITHUB_ACCESS_TOKEN and GITHUB_REPO in your environment."}
          </div>
        ) : null}

        {repo ? (
          <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <a
                  href={repo.htmlUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-xl font-semibold text-white hover:text-emerald-300"
                >
                  {repo.fullName}
                </a>
                <p className="mt-1 text-sm text-slate-400">
                  {repo.description || "No description provided."}
                </p>
                <p className="mt-3 text-xs text-slate-500">
                  Authenticated as <span className="text-slate-300">{authenticatedAs ?? "unknown"}</span> ·
                  Last push {formatDate(repo.pushedAt)}
                </p>
              </div>
              <span className="inline-flex w-fit items-center rounded-full border border-white/10 bg-white/[0.05] px-3 py-1 text-xs uppercase tracking-wide text-slate-300">
                {repo.visibility}
              </span>
            </div>

            <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
              <Stat label="Branch" value={repo.defaultBranch} />
              <Stat label="Language" value={repo.language ?? "—"} />
              <Stat label="Stars" value={repo.stars} />
              <Stat label="Forks" value={repo.forks} />
            </div>

            {branches.length > 0 ? (
              <div className="mt-6">
                <p className="text-[11px] uppercase tracking-[0.12em] text-slate-400">Branches</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {branches.map((branch) => (
                    <span
                      key={branch.name}
                      className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.04] px-3 py-1.5 font-mono text-xs text-slate-200"
                    >
                      {branch.name}
                      {branch.protected ? (
                        <span className="text-[10px] uppercase text-amber-300">protected</span>
                      ) : null}
                    </span>
                  ))}
                </div>
              </div>
            ) : null}

            {commits.length > 0 ? (
              <div className="mt-7">
                <p className="text-[11px] uppercase tracking-[0.12em] text-slate-400">Recent commits</p>
                <ul className="mt-3 divide-y divide-white/5 border border-white/10 rounded-2xl">
                  {commits.map((commit) => (
                    <li key={commit.sha} className="flex items-start gap-3 px-4 py-3">
                      {commit.authorAvatarUrl ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={commit.authorAvatarUrl}
                          alt=""
                          className="mt-0.5 h-7 w-7 rounded-full border border-white/10"
                        />
                      ) : (
                        <div className="mt-0.5 grid h-7 w-7 place-items-center rounded-full border border-white/10 bg-white/[0.05] text-[10px] text-slate-300">
                          GH
                        </div>
                      )}
                      <div className="min-w-0 flex-1">
                        <a
                          href={commit.htmlUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="block truncate text-sm text-slate-100 hover:text-emerald-300"
                        >
                          {commit.message}
                        </a>
                        <p className="mt-0.5 font-mono text-xs text-slate-500">
                          {commit.shortSha} · {commit.author} · {formatDate(commit.date)}
                        </p>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
          </section>
        ) : null}

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <h2 className="text-lg font-semibold text-white">OAuth account link</h2>
          <p className="mt-2 text-sm text-slate-400">
            Optional browser-based GitHub login. Requires <code>GITHUB_CLIENT_ID</code> and{" "}
            <code>GITHUB_CLIENT_SECRET</code>.
          </p>

          {!oauthConfigured ? (
            <div className="mt-4 rounded-2xl border border-amber-400/25 bg-amber-400/10 px-4 py-3 text-sm text-amber-200">
              OAuth app credentials are not configured yet.
            </div>
          ) : null}

          <div className="mt-5">
            {isOAuthConnected ? (
              <div className="flex flex-col gap-4">
                <div className="flex items-center gap-4">
                  {oauthAvatar ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={oauthAvatar}
                      alt="GitHub avatar"
                      className="h-12 w-12 rounded-full border border-white/10"
                    />
                  ) : (
                    <div className="grid h-12 w-12 place-items-center rounded-full border border-white/10 bg-white/[0.05] text-xs text-slate-300">
                      GH
                    </div>
                  )}
                  <div>
                    <p className="font-semibold text-white">{oauthName || oauthLogin}</p>
                    {oauthLogin ? <p className="text-sm text-slate-400">@{oauthLogin}</p> : null}
                    {oauthEmail ? <p className="text-sm text-slate-400">{oauthEmail}</p> : null}
                  </div>
                </div>
                <div className="flex flex-wrap gap-3">
                  {oauthProfileUrl ? (
                    <a
                      href={oauthProfileUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="rounded-xl bg-white px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-200"
                    >
                      View profile
                    </a>
                  ) : null}
                  <form action="/api/github/disconnect" method="post">
                    <button
                      type="submit"
                      className="rounded-xl border border-white/15 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-white/[0.06]"
                    >
                      Disconnect
                    </button>
                  </form>
                </div>
              </div>
            ) : (
              <a
                href="/api/github/connect"
                className="inline-flex items-center rounded-xl bg-white px-5 py-3 text-sm font-medium text-slate-900 hover:bg-slate-200"
              >
                Connect GitHub account
              </a>
            )}
          </div>
        </section>

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-white">Bodyweight training log</h2>
              <p className="mt-2 text-sm text-slate-400">
                ثبت تمرینات بدنسازی با وزن بدن — log pull-ups, push-ups, squats and holds from the browser
                with the same domain rules as the Android app (load types, rep/duration ranges, added
                tonnage).
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <Link
                href="/bodyweight"
                className="inline-flex w-fit shrink-0 items-center rounded-xl bg-white px-5 py-3 text-sm font-medium text-slate-900 hover:bg-slate-200"
              >
                Open bodyweight log
              </Link>
              <Link
                href="/bodyweight?lang=fa"
                className="text-sm font-medium text-emerald-300/90 hover:text-emerald-300"
              >
                نسخه فارسی
              </Link>
            </div>
          </div>
        </section>

        <footer className="pb-4 text-center text-xs text-slate-500">
          Live data fetched server-side from the GitHub REST API.
        </footer>
      </div>
    </main>
  );
}
