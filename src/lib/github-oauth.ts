import type { NextRequest } from "next/server";

type GitHubOAuthConfig = {
  clientId: string;
  clientSecret: string;
};

type GitHubTokenResponse = {
  access_token: string;
  token_type: string;
  scope: string;
  error?: string;
  error_description?: string;
};

type GitHubUser = {
  id: number;
  login: string;
  name: string | null;
  avatar_url: string;
  html_url: string;
};

type GitHubEmail = {
  email: string;
  primary: boolean;
  verified: boolean;
  visibility: "public" | "private" | null;
};

export function getGitHubOAuthConfig(): GitHubOAuthConfig {
  const clientId = process.env.GITHUB_CLIENT_ID;
  const clientSecret = process.env.GITHUB_CLIENT_SECRET;

  if (!clientId || !clientSecret) {
    throw new Error("Missing GITHUB_CLIENT_ID or GITHUB_CLIENT_SECRET environment variables.");
  }

  return { clientId, clientSecret };
}

export function getAppBaseUrl(request: NextRequest): string {
  const explicitBaseUrl = process.env.NEXT_PUBLIC_APP_URL?.trim();
  if (explicitBaseUrl) {
    return explicitBaseUrl.replace(/\/$/, "");
  }

  const host = request.headers.get("x-forwarded-host") ?? request.headers.get("host");
  const protocol = request.headers.get("x-forwarded-proto") ?? "http";

  if (!host) {
    throw new Error("Unable to determine app base URL from request headers.");
  }

  return `${protocol}://${host}`;
}

export function buildGitHubAuthorizeUrl(params: {
  clientId: string;
  redirectUri: string;
  state: string;
}) {
  const searchParams = new URLSearchParams({
    client_id: params.clientId,
    redirect_uri: params.redirectUri,
    scope: "read:user user:email",
    state: params.state,
  });

  return `https://github.com/login/oauth/authorize?${searchParams.toString()}`;
}

export async function exchangeCodeForToken(params: {
  clientId: string;
  clientSecret: string;
  code: string;
  state: string;
}): Promise<string> {
  const response = await fetch("https://github.com/login/oauth/access_token", {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      client_id: params.clientId,
      client_secret: params.clientSecret,
      code: params.code,
      state: params.state,
    }),
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`GitHub token exchange failed with status ${response.status}`);
  }

  const tokenData = (await response.json()) as GitHubTokenResponse;

  if (!tokenData.access_token) {
    throw new Error(tokenData.error_description || tokenData.error || "GitHub did not return an access token.");
  }

  return tokenData.access_token;
}

export async function fetchGitHubProfile(accessToken: string): Promise<{
  user: GitHubUser;
  primaryEmail: string | null;
}> {
  const [userResponse, emailsResponse] = await Promise.all([
    fetch("https://api.github.com/user", {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        Accept: "application/vnd.github+json",
        "User-Agent": "nextjs-github-connect-app",
      },
      cache: "no-store",
    }),
    fetch("https://api.github.com/user/emails", {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        Accept: "application/vnd.github+json",
        "User-Agent": "nextjs-github-connect-app",
      },
      cache: "no-store",
    }),
  ]);

  if (!userResponse.ok) {
    throw new Error(`Failed to fetch GitHub user profile (${userResponse.status}).`);
  }

  const user = (await userResponse.json()) as GitHubUser;

  let primaryEmail: string | null = null;
  if (emailsResponse.ok) {
    const emails = (await emailsResponse.json()) as GitHubEmail[];
    const primary = emails.find((entry) => entry.primary && entry.verified) ?? emails.find((entry) => entry.verified);
    primaryEmail = primary?.email ?? null;
  }

  return { user, primaryEmail };
}
