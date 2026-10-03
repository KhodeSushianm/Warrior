import { NextResponse } from "next/server";
import {
  fetchAuthedLogin,
  fetchBranches,
  fetchRecentCommits,
  fetchRepoSummary,
  getGitHubConnection,
  GitHubApiError,
} from "@/lib/github-api";

export const dynamic = "force-dynamic";

export async function GET() {
  const connection = getGitHubConnection();

  if (!connection.connected) {
    return NextResponse.json(
      {
        connected: false,
        reason: connection.reason ?? "GitHub is not configured.",
      },
      { status: 200 },
    );
  }

  try {
    const [repo, branches, commits, login] = await Promise.all([
      fetchRepoSummary(),
      fetchBranches(8),
      fetchRecentCommits(6),
      fetchAuthedLogin(),
    ]);

    return NextResponse.json({
      connected: true,
      mode: connection.mode,
      authenticatedAs: login,
      repo,
      branches,
      commits,
      fetchedAt: new Date().toISOString(),
    });
  } catch (error) {
    const status = error instanceof GitHubApiError ? error.status : 500;
    const message = error instanceof Error ? error.message : "Unable to load GitHub repository data.";

    return NextResponse.json({ connected: false, reason: message }, { status });
  }
}
