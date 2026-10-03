import { db } from "@/db";
import { sql } from "drizzle-orm";
import { cookies } from "next/headers";

export const dynamic = "force-dynamic";

type HomePageProps = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

function getParamValue(
  value: string | string[] | undefined,
): string | null {
  if (Array.isArray(value)) {
    return value[0] ?? null;
  }

  return value ?? null;
}

export default async function HomePage({ searchParams }: HomePageProps) {
  await db.execute(sql`select 1`);

  const params = await searchParams;
  const connectedFromCallback = getParamValue(params.github_connected) === "1";
  const disconnected = getParamValue(params.github_disconnected) === "1";
  const githubError = getParamValue(params.github_error);

  const cookieStore = await cookies();
  const isConnected = cookieStore.get("github_connected")?.value === "1";
  const githubLogin = cookieStore.get("github_login")?.value ?? "";
  const githubName = cookieStore.get("github_name")?.value ?? "";
  const githubAvatarUrl = cookieStore.get("github_avatar_url")?.value ?? "";
  const githubProfileUrl = cookieStore.get("github_profile_url")?.value ?? "";
  const githubEmail = cookieStore.get("github_email")?.value ?? "";

  const githubConfigured = Boolean(process.env.GITHUB_CLIENT_ID && process.env.GITHUB_CLIENT_SECRET);

  return (
    <main className="grid min-h-screen place-items-center bg-slate-100 px-6 py-12">
      <section className="w-full max-w-2xl rounded-3xl bg-white p-10 shadow-[0_24px_60px_rgba(16,24,40,0.12)]">
        <p className="m-0 text-sm uppercase tracking-[0.08em] text-slate-600">Integration</p>
        <h1 className="mt-3 text-[clamp(2rem,5vw,3rem)] font-semibold leading-[1.05] text-slate-950">
          Connect to GitHub
        </h1>
        <p className="mt-4 text-base text-slate-700">
          Link your GitHub account to verify OAuth connectivity from this Next.js app.
        </p>

        {!githubConfigured ? (
          <div className="mt-6 rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
            GitHub OAuth is not configured yet. Add <code>GITHUB_CLIENT_ID</code> and <code>GITHUB_CLIENT_SECRET</code> in your environment.
          </div>
        ) : null}

        {connectedFromCallback ? (
          <div className="mt-6 rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-900">
            Successfully connected your GitHub account.
          </div>
        ) : null}

        {disconnected ? (
          <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700">
            GitHub account disconnected.
          </div>
        ) : null}

        {githubError ? (
          <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
            {githubError}
          </div>
        ) : null}

        <div className="mt-8 rounded-2xl border border-slate-200 p-5">
          {isConnected ? (
            <div className="flex flex-col gap-4">
              <div className="flex items-center gap-4">
                {githubAvatarUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={githubAvatarUrl}
                    alt="GitHub avatar"
                    className="h-14 w-14 rounded-full border border-slate-200"
                  />
                ) : (
                  <div className="grid h-14 w-14 place-items-center rounded-full border border-slate-200 bg-slate-100 text-slate-600">
                    GH
                  </div>
                )}

                <div>
                  <p className="text-lg font-semibold text-slate-900">
                    {githubName || githubLogin || "Connected GitHub account"}
                  </p>
                  {githubLogin ? <p className="text-sm text-slate-600">@{githubLogin}</p> : null}
                  {githubEmail ? <p className="text-sm text-slate-600">{githubEmail}</p> : null}
                </div>
              </div>

              <div className="flex flex-wrap gap-3">
                {githubProfileUrl ? (
                  <a
                    href={githubProfileUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
                  >
                    View GitHub Profile
                  </a>
                ) : null}

                <form action="/api/github/disconnect" method="post">
                  <button
                    type="submit"
                    className="inline-flex items-center rounded-xl border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    Disconnect
                  </button>
                </form>
              </div>
            </div>
          ) : (
            <a
              href="/api/github/connect"
              className="inline-flex items-center rounded-xl bg-slate-900 px-5 py-3 text-sm font-medium text-white hover:bg-slate-800"
            >
              Connect GitHub Account
            </a>
          )}
        </div>
      </section>
    </main>
  );
}
