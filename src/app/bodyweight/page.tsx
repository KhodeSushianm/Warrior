import Link from "next/link";
import { and, desc, eq, inArray, sql } from "drizzle-orm";
import { db } from "@/db";
import {
  bodyweightSets,
  bodyweightWorkouts,
  type BodyweightSetRow,
  type BodyweightWorkoutRow,
} from "@/db/schema";
import {
  BODYWEIGHT_EXERCISES,
  LOAD_TYPES,
  LOAD_TYPE_LABELS,
  MEASURE_TYPES,
  formatKg,
  formatTonnage,
  setTonnageKg,
  summarizeSets,
  type LoadType,
} from "@/lib/bodyweight";

export const dynamic = "force-dynamic";

type BodyweightPageProps = {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

function getParamValue(value: string | string[] | undefined): string | null {
  if (Array.isArray(value)) {
    return value[0] ?? null;
  }
  return value ?? null;
}

function formatDate(value: Date | string | null): string {
  if (!value) return "—";
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return date.toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function localDateTimeInputValue(date: Date): string {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 16);
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3">
      <p className="text-[11px] uppercase tracking-[0.12em] text-slate-400">{label}</p>
      <p className="mt-1 text-lg font-semibold text-white">{value}</p>
    </div>
  );
}

const inputClassName =
  "mt-1 w-full rounded-xl border border-white/10 bg-white/[0.04] px-3 py-2.5 text-sm text-slate-100 placeholder:text-slate-500 focus:border-emerald-400/40 focus:outline-none";

const labelClassName = "text-[11px] uppercase tracking-[0.12em] text-slate-400";

export default async function BodyweightPage({ searchParams }: BodyweightPageProps) {
  const params = await searchParams;
  const logged = getParamValue(params.logged) === "1";
  const deleted = getParamValue(params.deleted) === "1";
  const logError = getParamValue(params.log_error);

  let recentWorkouts: BodyweightWorkoutRow[] = [];
  let setsByWorkout = new Map<string, BodyweightSetRow[]>();
  let totalSessions = 0;
  let totalSets = 0;
  let totalReps = 0;
  let tonnageKg = 0;
  let dataError: string | null = null;

  try {
    recentWorkouts = await db
      .select()
      .from(bodyweightWorkouts)
      .orderBy(desc(bodyweightWorkouts.performedAt))
      .limit(30);

    const ids = recentWorkouts.map((workout) => workout.id);
    const recentSets = ids.length
      ? await db.select().from(bodyweightSets).where(inArray(bodyweightSets.workoutId, ids))
      : [];

    setsByWorkout = new Map<string, BodyweightSetRow[]>();
    for (const set of recentSets) {
      const list = setsByWorkout.get(set.workoutId) ?? [];
      list.push(set);
      setsByWorkout.set(set.workoutId, list);
    }

    const [sessionsRow] = await db
      .select({ count: sql<number>`count(*)::int` })
      .from(bodyweightWorkouts);
    totalSessions = sessionsRow?.count ?? 0;

    // Only completed-style, non-warm-up sets count — exactly like the Android metrics (sec.7).
    const [setStats] = await db
      .select({
        sets: sql<number>`count(*)::int`,
        reps: sql<number>`coalesce(sum(${bodyweightSets.reps}), 0)::int`,
        tonnage: sql<number>`coalesce(sum(case when ${bodyweightWorkouts.loadType} = 'BODYWEIGHT_PLUS' then ${bodyweightSets.externalLoadKg} * coalesce(${bodyweightSets.reps}, 0) else 0 end), 0)::float8`,
      })
      .from(bodyweightSets)
      .innerJoin(
        bodyweightWorkouts,
        eq(bodyweightSets.workoutId, bodyweightWorkouts.id),
      )
      .where(and(eq(bodyweightSets.isWarmup, false)));

    totalSets = setStats?.sets ?? 0;
    totalReps = setStats?.reps ?? 0;
    tonnageKg = setStats?.tonnage ?? 0;
  } catch (error) {
    dataError = error instanceof Error ? error.message : "Unable to load the training log.";
  }

  const defaultPerformedAt = localDateTimeInputValue(new Date());

  return (
    <main className="min-h-screen bg-[#0b1020] px-6 py-12 text-slate-100">
      <div className="mx-auto w-full max-w-4xl space-y-8">
        <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">Warrior · Strength</p>
            <h1 className="mt-2 text-[clamp(1.9rem,4.5vw,2.75rem)] font-semibold leading-tight text-white">
              Bodyweight Training Log
            </h1>
          </div>
          <Link
            href="/"
            className="inline-flex w-fit items-center gap-2 rounded-full border border-white/10 bg-white/[0.05] px-4 py-2 text-sm font-medium text-slate-300 hover:text-emerald-300"
          >
            ← Repository dashboard
          </Link>
        </header>

        {logged ? (
          <div className="rounded-2xl border border-emerald-400/25 bg-emerald-400/10 px-5 py-4 text-sm text-emerald-200">
            Session saved — sets are already counted in the stats below.
          </div>
        ) : null}

        {deleted ? (
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] px-5 py-4 text-sm text-slate-300">
            Session deleted.
          </div>
        ) : null}

        {logError ? (
          <div className="rounded-2xl border border-rose-400/25 bg-rose-400/10 px-5 py-4 text-sm text-rose-200">
            {logError}
          </div>
        ) : null}

        {dataError ? (
          <div className="rounded-2xl border border-rose-400/25 bg-rose-400/10 px-5 py-4 text-sm text-rose-200">
            {dataError}
          </div>
        ) : null}

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Stat label="Sessions" value={totalSessions} />
            <Stat label="Working sets" value={totalSets} />
            <Stat label="Total reps" value={totalReps.toLocaleString("en-US")} />
            <Stat label="Added tonnage" value={formatTonnage(tonnageKg)} />
          </div>
          <p className="mt-4 text-xs text-slate-500">
            Added tonnage = SUM(external load × reps) for weighted (added-load), non-warm-up sets only.
            Pure bodyweight and assisted work contributes 0 — the same rule as the Android app.
          </p>
        </section>

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <h2 className="text-lg font-semibold text-white">Log a session</h2>
          <p className="mt-2 text-sm text-slate-400">
            ثبت تمرین بدنسازی با وزن بدن — one exercise per session, expanded into individual sets.
          </p>

          <form action="/api/bodyweight/log" method="post" className="mt-6 space-y-5">
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label htmlFor="exerciseKey" className={labelClassName}>
                  Exercise
                </label>
                <select id="exerciseKey" name="exerciseKey" required className={inputClassName}>
                  {BODYWEIGHT_EXERCISES.map((exercise) => (
                    <option key={exercise.key} value={exercise.key}>
                      {exercise.nameEn} · {exercise.nameFa}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="performedAt" className={labelClassName}>
                  Performed at
                </label>
                <input
                  id="performedAt"
                  name="performedAt"
                  type="datetime-local"
                  defaultValue={defaultPerformedAt}
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="loadType" className={labelClassName}>
                  Load type
                </label>
                <select id="loadType" name="loadType" defaultValue="BODYWEIGHT" className={inputClassName}>
                  {LOAD_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {LOAD_TYPE_LABELS[type]} · {type}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="measureType" className={labelClassName}>
                  Measure
                </label>
                <select id="measureType" name="measureType" defaultValue="REPS" className={inputClassName}>
                  {MEASURE_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type === "REPS" ? "Repetitions" : "Hold duration"} · {type}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="setCount" className={labelClassName}>
                  Sets
                </label>
                <input
                  id="setCount"
                  name="setCount"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={99}
                  defaultValue={3}
                  required
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="reps" className={labelClassName}>
                  Reps per set
                </label>
                <input
                  id="reps"
                  name="reps"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={999}
                  placeholder="12"
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="durationSec" className={labelClassName}>
                  Hold per set (sec)
                </label>
                <input
                  id="durationSec"
                  name="durationSec"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={3600}
                  placeholder="45"
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="externalLoadKg" className={labelClassName}>
                  Added load (kg)
                </label>
                <input
                  id="externalLoadKg"
                  name="externalLoadKg"
                  type="text"
                  inputMode="decimal"
                  defaultValue={0}
                  className={inputClassName}
                />
              </div>
            </div>

            <div>
              <label htmlFor="notes" className={labelClassName}>
                Notes
              </label>
              <input
                id="notes"
                name="notes"
                type="text"
                maxLength={500}
                placeholder="Optional — grip, band colour, how it felt…"
                className={inputClassName}
              />
            </div>

            <div className="flex flex-wrap items-center justify-between gap-4">
              <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-slate-300">
                <input type="checkbox" name="isWarmup" className="h-4 w-4 accent-emerald-400" />
                Warm-up sets (excluded from stats)
              </label>
              <button
                type="submit"
                className="inline-flex items-center rounded-xl bg-white px-5 py-3 text-sm font-medium text-slate-900 hover:bg-slate-200"
              >
                Save session
              </button>
            </div>

            <p className="text-xs text-slate-500">
              Fill <span className="text-slate-300">Reps per set</span> for repetition exercises and{" "}
              <span className="text-slate-300">Hold per set</span> for timed ones. Persian digits are
              accepted in every numeric field.
            </p>
          </form>
        </section>

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <h2 className="text-lg font-semibold text-white">Recent sessions</h2>

          {recentWorkouts.length === 0 ? (
            <div className="mt-4 rounded-2xl border border-dashed border-white/15 px-5 py-8 text-center text-sm text-slate-400">
              No sessions logged yet — your first set is one form away.
            </div>
          ) : (
            <ul className="mt-3 divide-y divide-white/5 rounded-2xl border border-white/10">
              {recentWorkouts.map((workout) => {
                const sets = setsByWorkout.get(workout.id) ?? [];
                const loadType = workout.loadType as LoadType;
                const tonnage = sets.reduce((sum, set) => sum + setTonnageKg(loadType, set), 0);
                const label = LOAD_TYPE_LABELS[loadType] ?? workout.loadType;

                return (
                  <li key={workout.id} className="px-4 py-3">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-slate-100">
                          {workout.exerciseNameEn}{" "}
                          <span className="font-normal text-slate-400">· {workout.exerciseNameFa}</span>
                        </p>
                        <p className="mt-0.5 text-xs text-slate-500">
                          {formatDate(workout.performedAt)} · {label}
                          {workout.notes ? ` · ${workout.notes}` : ""}
                        </p>
                      </div>
                      <form method="post" action="/api/bodyweight/delete" className="shrink-0">
                        <input type="hidden" name="id" value={workout.id} />
                        <button
                          type="submit"
                          className="text-xs font-medium text-slate-500 hover:text-rose-300"
                        >
                          Delete
                        </button>
                      </form>
                    </div>

                    <div className="mt-2 flex flex-wrap items-center gap-2">
                      {summarizeSets(sets).map((chip, index) => (
                        <span
                          key={`${workout.id}-${index}`}
                          className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.04] px-3 py-1.5 font-mono text-xs text-slate-200"
                        >
                          {chip.label}
                        </span>
                      ))}
                      {sets.some((set) => set.isWarmup) ? (
                        <span className="text-[10px] uppercase text-amber-300">warm-up</span>
                      ) : null}
                      {tonnage > 0 ? (
                        <span className="inline-flex items-center rounded-lg border border-emerald-400/30 bg-emerald-400/10 px-3 py-1.5 font-mono text-xs text-emerald-300">
                          {formatTonnage(tonnage)}
                        </span>
                      ) : null}
                      {loadType !== "BODYWEIGHT_PLUS" && sets.some((set) => set.externalLoadKg > 0) ? (
                        <span className="inline-flex items-center rounded-lg border border-white/10 bg-white/[0.04] px-3 py-1.5 font-mono text-xs text-slate-300">
                          {formatKg(sets[0]?.externalLoadKg ?? 0)} kg {loadType === "ASSISTED" ? "assistance" : "load"}
                        </span>
                      ) : null}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <footer className="pb-4 text-center text-xs text-slate-500">
          Stored in PostgreSQL via Drizzle · same domain rules as the Warrior Android app.
        </footer>
      </div>
    </main>
  );
}
