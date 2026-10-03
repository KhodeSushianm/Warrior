import { randomUUID } from "crypto";
import { NextRequest, NextResponse } from "next/server";
import {
  buildGitHubAuthorizeUrl,
  getAppBaseUrl,
  getGitHubOAuthConfig,
} from "@/lib/github-oauth";

export const dynamic = "force-dynamic";

export async function GET(request: NextRequest) {
  try {
    const { clientId } = getGitHubOAuthConfig();
    const state = randomUUID();
    const baseUrl = getAppBaseUrl(request);
    const redirectUri = `${baseUrl}/api/github/callback`;

    const authorizeUrl = buildGitHubAuthorizeUrl({
      clientId,
      redirectUri,
      state,
    });

    const response = NextResponse.redirect(authorizeUrl);
    response.cookies.set("github_oauth_state", state, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 10,
      path: "/",
    });

    return response;
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unable to start GitHub connection.";
    const url = new URL("/", request.url);
    url.searchParams.set("github_error", message);

    return NextResponse.redirect(url);
  }
}
