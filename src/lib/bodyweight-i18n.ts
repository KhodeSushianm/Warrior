import { formatKg, type LoadType, type SetGroup } from "@/lib/bodyweight";

/**
 * Bilingual UI strings for the bodyweight log — the web echo of the Android app's
 * Persian-first philosophy (sec.13). English stays the default to match the dashboard;
 * `?lang=fa` renders a full RTL interface with Jalali dates and Persian digits, both
 * produced by the fa-IR Intl locale so they never need a lookup table.
 */

export const LANGS = ["en", "fa"] as const;
export type Lang = (typeof LANGS)[number];

export function resolveLang(value: string | null): Lang {
  return value === "fa" ? "fa" : "en";
}

export function localeOf(lang: Lang): string {
  return lang === "fa" ? "fa-IR" : "en-US";
}

const STRINGS = {
  en: {
    kicker: "Warrior · Strength",
    title: "Bodyweight Training Log",
    backToDashboard: "Repository dashboard",
    languageToggle: "فارسی",
    savedBanner: "Session saved — sets are already counted in the stats below.",
    deletedBanner: "Session deleted.",
    statSessions: "Sessions",
    statWorkingSets: "Working sets",
    statTotalReps: "Total reps",
    statTonnage: "Added tonnage",
    tonnageNote:
      "Added tonnage = SUM(external load × reps) for weighted (added-load), non-warm-up sets only. Pure bodyweight and assisted work contributes 0 — the same rule as the Android app.",
    logHeading: "Log a session",
    logIntro: "ثبت تمرین بدنسازی با وزن بدن — one exercise per session, expanded into individual sets.",
    fieldExercise: "Exercise",
    fieldPerformedAt: "Performed at",
    fieldLoadType: "Load type",
    fieldMeasure: "Measure",
    fieldSets: "Sets",
    fieldReps: "Reps per set",
    fieldDuration: "Hold per set (sec)",
    fieldExternalLoad: "Added load (kg)",
    fieldNotes: "Notes",
    notesPlaceholder: "Optional — grip, band colour, how it felt…",
    warmupLabel: "Warm-up sets (excluded from stats)",
    saveButton: "Save session",
    measureRepetitions: "Repetitions",
    measureDuration: "Hold duration",
    fillHintLead: "Fill",
    fillHintReps: "Reps per set",
    fillHintMiddle: "for repetition exercises and",
    fillHintDuration: "Hold per set",
    fillHintTail: "for timed ones. Persian digits are accepted in every numeric field.",
    recentHeading: "Recent sessions",
    emptyState: "No sessions logged yet — your first set is one form away.",
    deleteButton: "Delete",
    warmupChip: "warm-up",
    assistanceSuffix: "assistance",
    loadSuffix: "load",
    loadTypeLabels: {
      BODYWEIGHT: "Bodyweight",
      BODYWEIGHT_PLUS: "Added load",
      ASSISTED: "Assisted",
    } as Record<LoadType, string>,
    repsUnit: "reps",
    secondsUnit: "s",
    tonnageUnit: "kg",
    footer: "Stored in PostgreSQL via Drizzle · same domain rules as the Warrior Android app.",
    pageTitle: "Warrior · Bodyweight Training Log",
  },
  fa: {
    kicker: "Warrior · قدرتی",
    title: "دفتر ثبت تمرینات با وزن بدن",
    backToDashboard: "داشبورد ریپو",
    languageToggle: "English",
    savedBanner: "جلسه ذخیره شد — سیت‌ها همین حالا در آمار زیر حساب شده‌اند.",
    deletedBanner: "جلسه حذف شد.",
    statSessions: "جلسات",
    statWorkingSets: "سیت‌های کاری",
    statTotalReps: "مجموع تکرارها",
    statTonnage: "تناژ وزنه اضافه",
    tonnageNote:
      "تناژ وزنه اضافه = مجموع (وزنه اضافه × تکرار) فقط برای سیت‌های غیر گرم‌کردنیِ با وزنه اضافه. تمرینِ خالص با وزن بدن و تمرینِ با کمک، صفر حساب می‌شوند — درست مثل اپ اندروید.",
    logHeading: "ثبت جلسه تمرین",
    logIntro: "هر جلسه یک حرکت است؛ فرم، سیت‌ها را یک‌به‌یک برایت می‌سازد.",
    fieldExercise: "حرکت",
    fieldPerformedAt: "زمان انجام",
    fieldLoadType: "نوع بار",
    fieldMeasure: "نوع اندازه‌گیری",
    fieldSets: "تعداد سیت",
    fieldReps: "تکرار هر سیت",
    fieldDuration: "مدت هر سیت (ثانیه)",
    fieldExternalLoad: "وزنه اضافه (کیلوگرم)",
    fieldNotes: "یادداشت",
    notesPlaceholder: "اختیاری — نوع گرفتن دست، رنگ کش، حس تمرین…",
    warmupLabel: "سیت‌های گرم‌کردنی (در آمار حساب نمی‌شوند)",
    saveButton: "ذخیره جلسه",
    measureRepetitions: "تکراری",
    measureDuration: "زمانی (نگه‌داشت)",
    fillHintLead: "برای حرکات تکراری",
    fillHintReps: "تکرار هر سیت",
    fillHintMiddle: "و برای حرکات زمانی",
    fillHintDuration: "مدت هر سیت",
    fillHintTail: "را پر کن. ارقام فارسی در همه فیلدهای عددی قبول است.",
    recentHeading: "جلسات اخیر",
    emptyState: "هنوز جلسه‌ای ثبت نشده — اولین سیت فقط یک فرم با تو فاصله دارد.",
    deleteButton: "حذف",
    warmupChip: "گرم‌کردنی",
    assistanceSuffix: "کمک",
    loadSuffix: "بار",
    loadTypeLabels: {
      BODYWEIGHT: "وزن بدن",
      BODYWEIGHT_PLUS: "با وزنه اضافه",
      ASSISTED: "با کمک",
    } as Record<LoadType, string>,
    repsUnit: "تکرار",
    secondsUnit: "ثانیه",
    tonnageUnit: "کیلوگرم",
    footer: "ذخیره در PostgreSQL با Drizzle · همان قواعد دامنه‌ی اپ اندروید Warrior",
    pageTitle: "Warrior · دفتر تمرینات با وزن بدن",
  },
};

export type Messages = (typeof STRINGS)["en"];

export function t(lang: Lang): Messages {
  return STRINGS[lang];
}

/** Display digits follow the language (DigitSystem AUTO: fa → Persian glyphs). */
export function formatNumber(lang: Lang, value: number, maximumFractionDigits = 2): string {
  return value.toLocaleString(localeOf(lang), { maximumFractionDigits });
}

/** Jalali for fa, Gregorian for en — one Intl call, no lookup tables. */
export function formatSessionDate(lang: Lang, value: Date | string | null): string {
  if (!value) return "—";
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat(localeOf(lang), {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function formatTonnageIntl(lang: Lang, value: number): string {
  return `${formatNumber(lang, value, 1)} ${t(lang).tonnageUnit}`;
}

export function formatSetGroup(lang: Lang, group: SetGroup): string {
  const m = t(lang);
  const core =
    group.reps !== null
      ? `${formatNumber(lang, group.reps, 0)} ${m.repsUnit}`
      : `${formatNumber(lang, group.durationSec ?? 0, 0)} ${m.secondsUnit}`;
  const load =
    group.externalLoadKg > 0
      ? ` · +${lang === "fa" ? formatNumber(lang, group.externalLoadKg) : formatKg(group.externalLoadKg)} ${m.tonnageUnit}`
      : "";
  const base = `${core}${load}`;
  return group.count > 1 ? `${formatNumber(lang, group.count, 0)} × ${base}` : base;
}
