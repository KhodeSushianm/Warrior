import { NextRequest, NextResponse } from "next/server";
import {
  exchangeCodeForToken,
  fetchGitHubProfile,
  getGitHubOAuthConfig,
} from "@/lib/github-oauth";

export const dynamic = "force-dynamic";

export async function GET(request: NextRequest) {
  const callbackUrl = new URL(request.url);
  const code = callbackUrl.searchParams.get("code");
  const state = callbackUrl.searchParams.get("state");
  const stateCookie = request.cookies.get("github_oauth_state")?.value;

  if (!code || !state) {
    const url = new URL("/", request.url);
    url.searchParams.set("github_error", "Missing code or state from GitHub callback.");
    return NextResponse.redirect(url);
  }

  if (!stateCookie || stateCookie !== state) {
    const url = new URL("/", request.url);
    url.searchParams.set("github_error", "Invalid OAuth state. Please try connecting again.");

    const response = NextResponse.redirect(url);
    response.cookies.delete("github_oauth_state");
    return response;
  }

  try {
    const { clientId, clientSecret } = getGitHubOAuthConfig();
    const accessToken = await exchangeCodeForToken({
      clientId,
      clientSecret,
      code,
      state,
    });

    const { user, primaryEmail } = await fetchGitHubProfile(accessToken);

    const url = new URL("/", request.url);
    url.searchParams.set("github_connected", "1");

    const response = NextResponse.redirect(url);
    response.cookies.delete("github_oauth_state");
    response.cookies.set("github_connected", "1", {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 30,
      path: "/",
    });
    response.cookies.set("github_login", user.login, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 30,
      path: "/",
    });
    response.cookies.set("github_name", user.name ?? "", {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 30,
      path: "/",
    });
    response.cookies.set("github_avatar_url", user.avatar_url, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 30,
      path: "/",
    });
    response.cookies.set("github_profile_url", user.html_url, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 30,
      path: "/",
    });
    response.cookies.set("github_email", primaryEmail ?? "", {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 30,
      path: "/",
    });

    return response;
  } catch (error) {
    const message = error instanceof Error ? error.message : "GitHub connection failed.";
    const url = new URL("/", request.url);
    url.searchParams.set("github_error", message);

    const response = NextResponse.redirect(url);
    response.cookies.delete("github_oauth_state");
    return response;
  }
}
