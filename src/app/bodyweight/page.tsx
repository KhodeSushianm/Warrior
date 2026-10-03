import type { Metadata } from "next";
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
  MEASURE_TYPES,
  formatKg,
  groupSets,
  setTonnageKg,
  type LoadType,
} from "@/lib/bodyweight";
import {
  formatNumber,
  formatSessionDate,
  formatSetGroup,
  formatTonnageIntl,
  resolveLang,
  t,
  type Lang,
} from "@/lib/bodyweight-i18n";

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

export async function generateMetadata({ searchParams }: BodyweightPageProps): Promise<Metadata> {
  const params = await searchParams;
  const lang = resolveLang(getParamValue(params.lang));
  return { title: t(lang).pageTitle };
}

export default async function BodyweightPage({ searchParams }: BodyweightPageProps) {
  const params = await searchParams;
  const lang = resolveLang(getParamValue(params.lang));
  const m = t(lang);
  const isFa = lang === "fa";
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
    <main
      dir={isFa ? "rtl" : "ltr"}
      lang={lang}
      className="min-h-screen bg-[#0b1020] px-6 py-12 text-slate-100"
    >
      <div className="mx-auto w-full max-w-4xl space-y-8">
        <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">{m.kicker}</p>
            <h1 className="mt-2 text-[clamp(1.9rem,4.5vw,2.75rem)] font-semibold leading-tight text-white">
              {m.title}
            </h1>
          </div>
          <div className="flex w-fit items-center gap-2">
            <Link
              href="/"
              className="inline-flex w-fit items-center gap-2 rounded-full border border-white/10 bg-white/[0.05] px-4 py-2 text-sm font-medium text-slate-300 hover:text-emerald-300"
            >
              {isFa ? "→" : "←"} {m.backToDashboard}
            </Link>
            <Link
              href={isFa ? "/bodyweight" : "/bodyweight?lang=fa"}
              className="inline-flex w-fit items-center gap-2 rounded-full border border-emerald-400/30 bg-emerald-400/10 px-4 py-2 text-sm font-medium text-emerald-300 hover:bg-emerald-400/20"
            >
              {m.languageToggle}
            </Link>
          </div>
        </header>

        {logged ? (
          <div className="rounded-2xl border border-emerald-400/25 bg-emerald-400/10 px-5 py-4 text-sm text-emerald-200">
            {m.savedBanner}
          </div>
        ) : null}

        {deleted ? (
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] px-5 py-4 text-sm text-slate-300">
            {m.deletedBanner}
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
            <Stat label={m.statSessions} value={formatNumber(lang, totalSessions, 0)} />
            <Stat label={m.statWorkingSets} value={formatNumber(lang, totalSets, 0)} />
            <Stat label={m.statTotalReps} value={formatNumber(lang, totalReps, 0)} />
            <Stat label={m.statTonnage} value={formatTonnageIntl(lang, tonnageKg)} />
          </div>
          <p className="mt-4 text-xs text-slate-500">{m.tonnageNote}</p>
        </section>

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <h2 className="text-lg font-semibold text-white">{m.logHeading}</h2>
          <p className="mt-2 text-sm text-slate-400">{m.logIntro}</p>

          <form action="/api/bodyweight/log" method="post" className="mt-6 space-y-5">
            <input type="hidden" name="lang" value={lang} />
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label htmlFor="exerciseKey" className={labelClassName}>
                  {m.fieldExercise}
                </label>
                <select id="exerciseKey" name="exerciseKey" required className={inputClassName}>
                  {BODYWEIGHT_EXERCISES.map((exercise) => (
                    <option key={exercise.key} value={exercise.key}>
                      {isFa
                        ? `${exercise.nameFa} · ${exercise.nameEn}`
                        : `${exercise.nameEn} · ${exercise.nameFa}`}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="performedAt" className={labelClassName}>
                  {m.fieldPerformedAt}
                </label>
                <input
                  id="performedAt"
                  name="performedAt"
                  type="datetime-local"
                  defaultValue={defaultPerformedAt}
                  dir="ltr"
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="loadType" className={labelClassName}>
                  {m.fieldLoadType}
                </label>
                <select id="loadType" name="loadType" defaultValue="BODYWEIGHT" className={inputClassName}>
                  {LOAD_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {m.loadTypeLabels[type]} · {type}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="measureType" className={labelClassName}>
                  {m.fieldMeasure}
                </label>
                <select id="measureType" name="measureType" defaultValue="REPS" className={inputClassName}>
                  {MEASURE_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type === "REPS" ? m.measureRepetitions : m.measureDuration} · {type}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor="setCount" className={labelClassName}>
                  {m.fieldSets}
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
                  dir="ltr"
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="reps" className={labelClassName}>
                  {m.fieldReps}
                </label>
                <input
                  id="reps"
                  name="reps"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={999}
                  placeholder="12"
                  dir="ltr"
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="durationSec" className={labelClassName}>
                  {m.fieldDuration}
                </label>
                <input
                  id="durationSec"
                  name="durationSec"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  max={3600}
                  placeholder="45"
                  dir="ltr"
                  className={inputClassName}
                />
              </div>

              <div>
                <label htmlFor="externalLoadKg" className={labelClassName}>
                  {m.fieldExternalLoad}
                </label>
                <input
                  id="externalLoadKg"
                  name="externalLoadKg"
                  type="text"
                  inputMode="decimal"
                  defaultValue={0}
                  dir="ltr"
                  className={inputClassName}
                />
              </div>
            </div>

            <div>
              <label htmlFor="notes" className={labelClassName}>
                {m.fieldNotes}
              </label>
              <input
                id="notes"
                name="notes"
                type="text"
                maxLength={500}
                placeholder={m.notesPlaceholder}
                className={inputClassName}
              />
            </div>

            <div className="flex flex-wrap items-center justify-between gap-4">
              <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-slate-300">
                <input type="checkbox" name="isWarmup" className="h-4 w-4 accent-emerald-400" />
                {m.warmupLabel}
              </label>
              <button
                type="submit"
                className="inline-flex items-center rounded-xl bg-white px-5 py-3 text-sm font-medium text-slate-900 hover:bg-slate-200"
              >
                {m.saveButton}
              </button>
            </div>

            <p className="text-xs text-slate-500">
              {m.fillHintLead}{" "}
              <span className="text-slate-300">{m.fillHintReps}</span> {m.fillHintMiddle}{" "}
              <span className="text-slate-300">{m.fillHintDuration}</span> {m.fillHintTail}
            </p>
          </form>
        </section>

        <section className="rounded-3xl border border-white/10 bg-white/[0.03] p-6">
          <h2 className="text-lg font-semibold text-white">{m.recentHeading}</h2>

          {recentWorkouts.length === 0 ? (
            <div className="mt-4 rounded-2xl border border-dashed border-white/15 px-5 py-8 text-center text-sm text-slate-400">
              {m.emptyState}
            </div>
          ) : (
            <ul className="mt-3 divide-y divide-white/5 rounded-2xl border border-white/10">
              {recentWorkouts.map((workout) => {
                const sets = setsByWorkout.get(workout.id) ?? [];
                const loadType = workout.loadType as LoadType;
                const tonnage = sets.reduce((sum, set) => sum + setTonnageKg(loadType, set), 0);
                const label = m.loadTypeLabels[loadType] ?? workout.loadType;
                const primaryName = isFa ? workout.exerciseNameFa : workout.exerciseNameEn;
                const secondaryName = isFa ? workout.exerciseNameEn : workout.exerciseNameFa;
                const overloadedSet = sets.find((set) => set.externalLoadKg > 0);

                return (
                  <li key={workout.id} className="px-4 py-3">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-slate-100">
                          {primaryName}{" "}
                          <span className="font-normal text-slate-400">· {secondaryName}</span>
                        </p>
                        <p className="mt-0.5 text-xs text-slate-500">
                          {formatSessionDate(lang, workout.performedAt)} · {label}
                          {workout.notes ? ` · ${workout.notes}` : ""}
                        </p>
                      </div>
                      <form method="post" action="/api/bodyweight/delete" className="shrink-0">
                        <input type="hidden" name="id" value={workout.id} />
                        <input type="hidden" name="lang" value={lang} />
                        <button
                          type="submit"
                          className="text-xs font-medium text-slate-500 hover:text-rose-300"
                        >
                          {m.deleteButton}
                        </button>
                      </form>
                    </div>

                    <div className="mt-2 flex flex-wrap items-center gap-2">
                      {groupSets(sets).map((group, index) => (
                        <span
                          key={`${workout.id}-${index}`}
                          className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.04] px-3 py-1.5 font-mono text-xs text-slate-200"
                        >
                          {formatSetGroup(lang, group)}
                        </span>
                      ))}
                      {sets.some((set) => set.isWarmup) ? (
                        <span className="text-[10px] uppercase text-amber-300">{m.warmupChip}</span>
                      ) : null}
                      {tonnage > 0 ? (
                        <span className="inline-flex items-center rounded-lg border border-emerald-400/30 bg-emerald-400/10 px-3 py-1.5 font-mono text-xs text-emerald-300">
                          {formatTonnageIntl(lang, tonnage)}
                        </span>
                      ) : null}
                      {loadType !== "BODYWEIGHT_PLUS" && overloadedSet ? (
                        <span className="inline-flex items-center rounded-lg border border-white/10 bg-white/[0.04] px-3 py-1.5 font-mono text-xs text-slate-300">
                          {formatKg(overloadedSet.externalLoadKg)} kg{" "}
                          {loadType === "ASSISTED" ? m.assistanceSuffix : m.loadSuffix}
                        </span>
                      ) : null}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <footer className="pb-4 text-center text-xs text-slate-500">{m.footer}</footer>
      </div>
    </main>
  );
}
