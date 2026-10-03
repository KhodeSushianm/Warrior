import { NextRequest, NextResponse } from "next/server";

export const dynamic = "force-dynamic";

const GITHUB_COOKIES = [
  "github_oauth_state",
  "github_connected",
  "github_login",
  "github_name",
  "github_avatar_url",
  "github_profile_url",
  "github_email",
];

export async function POST(request: NextRequest) {
  const url = new URL("/", request.url);
  url.searchParams.set("github_disconnected", "1");

  const response = NextResponse.redirect(url);
  for (const cookieName of GITHUB_COOKIES) {
    response.cookies.delete(cookieName);
  }

  return response;
}
