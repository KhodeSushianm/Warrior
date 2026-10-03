/**
 * Bodyweight training log domain — mirrors the Android app 1:1 where it matters:
 * - `LoadType` / `MeasureType` enum names (core/common/Enums.kt, ARCHITECTURE.md sec.6.7);
 * - the 19-exercise bilingual seed catalogue (data/seed/BodyweightExerciseCatalog.kt);
 * - the sec.15 input range table (core/validation/InputValidator.kt);
 * - the digit normalization rules (core/time/NumberInputParser.kt);
 * - the tonnage rule: SUM(external_load_kg × reps), BODYWEIGHT and ASSISTED contribute 0,
 *   warm-up sets excluded (README · Metrics).
 */

export const LOAD_TYPES = ["BODYWEIGHT", "BODYWEIGHT_PLUS", "ASSISTED"] as const;
export type LoadType = (typeof LOAD_TYPES)[number];

export const MEASURE_TYPES = ["REPS", "DURATION"] as const;
export type MeasureType = (typeof MEASURE_TYPES)[number];

export const LOAD_TYPE_LABELS: Record<LoadType, string> = {
  BODYWEIGHT: "Bodyweight",
  BODYWEIGHT_PLUS: "Added load",
  ASSISTED: "Assisted",
};

export type ExerciseDifficulty = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";

export type BodyweightExercise = {
  key: string;
  nameEn: string;
  nameFa: string;
  measureType: MeasureType;
  difficulty: ExerciseDifficulty;
  equipment: string;
};

/** The exact seed catalogue from android-app `BodyweightExerciseCatalog.kt` (push → pull → legs → core). */
export const BODYWEIGHT_EXERCISES: BodyweightExercise[] = [
  { key: "push_up", nameEn: "Push-Up", nameFa: "شنا سوئدی", measureType: "REPS", difficulty: "BEGINNER", equipment: "NONE" },
  { key: "wide_push_up", nameEn: "Wide Push-Up", nameFa: "شنا دست باز", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "NONE" },
  { key: "diamond_push_up", nameEn: "Diamond Push-Up", nameFa: "شنا الماسی", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "NONE" },
  { key: "decline_push_up", nameEn: "Decline Push-Up", nameFa: "شنا پا بالا", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "BENCH" },
  { key: "pike_push_up", nameEn: "Pike Push-Up", nameFa: "شنا سرشانه پایک", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "NONE" },
  { key: "dip", nameEn: "Dip", nameFa: "دیپ", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "PARALLEL_BARS" },
  { key: "pull_up", nameEn: "Pull-Up", nameFa: "بارفیکس دست باز", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "PULL_UP_BAR" },
  { key: "chin_up", nameEn: "Chin-Up", nameFa: "بارفیکس دست جمع", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "PULL_UP_BAR" },
  { key: "inverted_row", nameEn: "Inverted Row", nameFa: "پارویی معکوس", measureType: "REPS", difficulty: "BEGINNER", equipment: "PULL_UP_BAR" },
  { key: "bodyweight_squat", nameEn: "Bodyweight Squat", nameFa: "اسکوات وزن بدن", measureType: "REPS", difficulty: "BEGINNER", equipment: "NONE" },
  { key: "reverse_lunge", nameEn: "Reverse Lunge", nameFa: "لانج معکوس", measureType: "REPS", difficulty: "BEGINNER", equipment: "NONE" },
  { key: "bulgarian_split_squat", nameEn: "Bulgarian Split Squat", nameFa: "اسکوات بلغاری", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "BENCH" },
  { key: "single_leg_calf_raise", nameEn: "Single-Leg Calf Raise", nameFa: "ساق پا تک‌پا", measureType: "REPS", difficulty: "INTERMEDIATE", equipment: "NONE" },
  { key: "glute_bridge", nameEn: "Glute Bridge", nameFa: "پل باسن", measureType: "REPS", difficulty: "BEGINNER", equipment: "NONE" },
  { key: "plank", nameEn: "Plank", nameFa: "پلانک", measureType: "DURATION", difficulty: "BEGINNER", equipment: "NONE" },
  { key: "side_plank", nameEn: "Side Plank", nameFa: "پلانک بغل", measureType: "DURATION", difficulty: "INTERMEDIATE", equipment: "NONE" },
  { key: "hollow_hold", nameEn: "Hollow Body Hold", nameFa: "هالو هولد", measureType: "DURATION", difficulty: "INTERMEDIATE", equipment: "NONE" },
  { key: "wall_sit", nameEn: "Wall Sit", nameFa: "وال سیت", measureType: "DURATION", difficulty: "BEGINNER", equipment: "NONE" },
  { key: "handstand_hold", nameEn: "Handstand Hold", nameFa: "هندستند هولد", measureType: "DURATION", difficulty: "ADVANCED", equipment: "NONE" },
];

export function findExercise(key: string | null): BodyweightExercise | null {
  if (!key) return null;
  return BODYWEIGHT_EXERCISES.find((exercise) => exercise.key === key) ?? null;
}

// ---- bounds, exactly as tabulated in ARCHITECTURE.md sec.15 ----

export const MIN_REPS = 1;
export const MAX_REPS = 999;

export const MIN_LOAD_KG = 0;
export const MAX_LOAD_KG = 1000;

export const MIN_HOLD_SEC = 1;
export const MAX_HOLD_SEC = 3600;

/** Set count per logged session is a web-form bound; a single session never legitimately exceeds 99. */
export const MIN_SETS = 1;
export const MAX_SETS = 99;

export const MAX_NOTES_LENGTH = 500;

// ---- digit normalization (mirror of core/time/NumberInputParser.kt) ----

/**
 * Accepts Latin, Persian and Arabic-Indic digits plus `٫`/`،` decimal separators,
 * so a Persian keyboard never yields "invalid number".
 */
export function normalizeDigits(input: string): string {
  let out = "";
  for (const ch of input) {
    const code = ch.codePointAt(0) ?? 0;
    if (code >= 0x06f0 && code <= 0x06f9) {
      out += String.fromCharCode(0x30 + (code - 0x06f0)); // ۰-۹
    } else if (code >= 0x0660 && code <= 0x0669) {
      out += String.fromCharCode(0x30 + (code - 0x0660)); // ٠-٩
    } else if (ch === "٫" || ch === "،") {
      out += ".";
    } else {
      out += ch;
    }
  }
  return out;
}

function toIntOrNull(input: string | null): number | null {
  if (input === null) return null;
  const normalized = normalizeDigits(input).trim();
  if (!/^-?\d+$/.test(normalized)) return null;
  return Number.parseInt(normalized, 10);
}

function toFloatOrNull(input: string | null): number | null {
  if (input === null) return null;
  const normalized = normalizeDigits(input).trim();
  if (!/^-?\d+(\.\d+)?$/.test(normalized)) return null;
  return Number.parseFloat(normalized);
}

function isTwoDecimalPrecision(value: number): boolean {
  return Math.abs(value * 100 - Math.round(value * 100)) < 1e-6;
}

// ---- log form parsing + validation (mirror of core/validation/InputValidator.kt) ----

export type ParsedLog = {
  exercise: BodyweightExercise;
  loadType: LoadType;
  measureType: MeasureType;
  setCount: number;
  reps: number | null;
  durationSec: number | null;
  externalLoadKg: number;
  isWarmup: boolean;
  performedAt: Date;
  notes: string | null;
};

export type ParseResult = { ok: true; value: ParsedLog } | { ok: false; error: string };

function readField(form: FormData, name: string): string | null {
  const value = form.get(name);
  return typeof value === "string" ? value : null;
}

export function parseLogForm(form: FormData): ParseResult {
  const exercise = findExercise(readField(form, "exerciseKey"));
  if (!exercise) {
    return { ok: false, error: "Choose a bodyweight exercise from the list." };
  }

  const loadTypeRaw = readField(form, "loadType");
  const loadType = LOAD_TYPES.find((type) => type === loadTypeRaw);
  if (!loadType) {
    return { ok: false, error: "Load type must be BODYWEIGHT, BODYWEIGHT_PLUS or ASSISTED." };
  }

  const measureTypeRaw = readField(form, "measureType");
  const measureType = MEASURE_TYPES.find((type) => type === measureTypeRaw);
  if (!measureType) {
    return { ok: false, error: "Measure type must be REPS or DURATION." };
  }

  const setCount = toIntOrNull(readField(form, "setCount"));
  if (setCount === null) {
    return { ok: false, error: "Sets count is required." };
  }
  if (setCount < MIN_SETS) {
    return { ok: false, error: `Sets count is too small — minimum is ${MIN_SETS}.` };
  }
  if (setCount > MAX_SETS) {
    return { ok: false, error: `Sets count is too large — maximum is ${MAX_SETS}.` };
  }

  let reps: number | null = null;
  let durationSec: number | null = null;
  if (measureType === "REPS") {
    reps = toIntOrNull(readField(form, "reps"));
    if (reps === null) {
      return { ok: false, error: "Reps is required for a repetition exercise." };
    }
    if (reps < MIN_REPS) {
      return { ok: false, error: `Reps is too small — minimum is ${MIN_REPS}.` };
    }
    if (reps > MAX_REPS) {
      return { ok: false, error: `Reps is too large — maximum is ${MAX_REPS}.` };
    }
  } else {
    durationSec = toIntOrNull(readField(form, "durationSec"));
    if (durationSec === null) {
      return { ok: false, error: "Hold duration is required for a timed exercise." };
    }
    if (durationSec < MIN_HOLD_SEC) {
      return { ok: false, error: `Hold duration is too small — minimum is ${MIN_HOLD_SEC} s.` };
    }
    if (durationSec > MAX_HOLD_SEC) {
      return { ok: false, error: `Hold duration is too large — maximum is ${MAX_HOLD_SEC} s.` };
    }
  }

  const externalLoadKg = toFloatOrNull(readField(form, "externalLoadKg")) ?? 0;
  if (Number.isNaN(externalLoadKg)) {
    return { ok: false, error: "Added load is not a valid number." };
  }
  if (externalLoadKg < MIN_LOAD_KG) {
    return { ok: false, error: `Added load is too small — minimum is ${MIN_LOAD_KG} kg.` };
  }
  if (externalLoadKg > MAX_LOAD_KG) {
    return { ok: false, error: `Added load is too large — maximum is ${MAX_LOAD_KG} kg.` };
  }
  if (!isTwoDecimalPrecision(externalLoadKg)) {
    return { ok: false, error: "Added load accepts at most two decimals." };
  }

  const performedAtRaw = readField(form, "performedAt");
  const performedAt = performedAtRaw ? new Date(performedAtRaw) : new Date();
  if (Number.isNaN(performedAt.getTime())) {
    return { ok: false, error: "Performed-at date is not valid." };
  }

  const notesRaw = readField(form, "notes")?.trim() ?? "";
  if (notesRaw.length > MAX_NOTES_LENGTH) {
    return { ok: false, error: `Notes are too long — maximum is ${MAX_NOTES_LENGTH} characters.` };
  }

  return {
    ok: true,
    value: {
      exercise,
      loadType,
      measureType,
      setCount,
      reps,
      durationSec,
      externalLoadKg,
      isWarmup: readField(form, "isWarmup") === "on",
      performedAt,
      notes: notesRaw.length > 0 ? notesRaw : null,
    },
  };
}

// ---- metrics (README · Metrics) ----

export type SetLike = {
  reps: number | null;
  isWarmup: boolean;
  externalLoadKg: number;
};

/**
 * Tonnage = SUM(external_load_kg × reps). BODYWEIGHT and ASSISTED contribute 0 and
 * warm-up sets never count — the exact rule the Android VolumeCalculator implements.
 */
export function setTonnageKg(loadType: LoadType, set: SetLike): number {
  if (loadType !== "BODYWEIGHT_PLUS" || set.isWarmup || set.reps === null) {
    return 0;
  }
  return set.externalLoadKg * set.reps;
}

export type SetChip = { count: number; label: string };

/** Condenses identical consecutive sets ("3 × 12 reps") so fast-input sessions stay readable. */
export function summarizeSets(
  sets: { setNumber: number; reps: number | null; durationSec: number | null; externalLoadKg: number }[],
): SetChip[] {
  const sorted = [...sets].sort((a, b) => a.setNumber - b.setNumber);
  const chips: SetChip[] = [];
  for (const set of sorted) {
    const load = set.externalLoadKg > 0 ? ` · +${formatKg(set.externalLoadKg)} kg` : "";
    const label =
      set.reps !== null ? `${set.reps} reps${load}` : `${set.durationSec ?? 0} s${load}`;
    const last = chips[chips.length - 1];
    if (last && last.label === label) {
      last.count += 1;
    } else {
      chips.push({ count: 1, label });
    }
  }
  return chips.map((chip) =>
    chip.count > 1 ? { count: chip.count, label: `${chip.count} × ${chip.label}` } : chip,
  );
}

export function formatKg(value: number): string {
  return value.toLocaleString("en-US", { maximumFractionDigits: 2 });
}

export function formatTonnage(value: number): string {
  return `${value.toLocaleString("en-US", { maximumFractionDigits: 1 })} kg`;
}
