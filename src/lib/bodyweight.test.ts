import { describe, expect, it } from "vitest";
import {
  groupSets,
  normalizeDigits,
  parseLogForm,
  setTonnageKg,
} from "@/lib/bodyweight";
import {
  formatNumber,
  formatSessionDate,
  formatSetGroup,
  formatTonnageIntl,
  resolveLang,
} from "@/lib/bodyweight-i18n";

function form(fields: Record<string, string>): FormData {
  const data = new FormData();
  for (const [key, value] of Object.entries(fields)) {
    data.set(key, value);
  }
  return data;
}

const VALID_REPS = {
  exerciseKey: "pull_up",
  loadType: "BODYWEIGHT",
  measureType: "REPS",
  setCount: "3",
  reps: "12",
  externalLoadKg: "0",
};

describe("normalizeDigits", () => {
  it("passes Latin digits through", () => {
    expect(normalizeDigits("123")).toBe("123");
  });

  it("converts Persian digits", () => {
    expect(normalizeDigits("۱۲۳")).toBe("123");
  });

  it("converts Arabic-Indic digits", () => {
    expect(normalizeDigits("٤٥٦")).toBe("456");
  });

  it("turns ٫ and ، into a decimal point", () => {
    expect(normalizeDigits("۱۲٫۵")).toBe("12.5");
    expect(normalizeDigits("۱۲،۵")).toBe("12.5");
  });
});

describe("parseLogForm", () => {
  it("accepts a valid repetition session", () => {
    const result = parseLogForm(form(VALID_REPS));
    expect(result.ok).toBe(true);
    if (result.ok) {
      expect(result.value.reps).toBe(12);
      expect(result.value.setCount).toBe(3);
      expect(result.value.isWarmup).toBe(false);
      expect(result.value.notes).toBeNull();
      expect(result.value.exercise.nameEn).toBe("Pull-Up");
    }
  });

  it("accepts Persian digits everywhere", () => {
    const result = parseLogForm(form({ ...VALID_REPS, reps: "۱۲", setCount: "۳" }));
    expect(result.ok).toBe(true);
    if (result.ok) {
      expect(result.value.reps).toBe(12);
      expect(result.value.setCount).toBe(3);
    }
  });

  it("accepts a valid hold session", () => {
    const result = parseLogForm(
      form({
        exerciseKey: "plank",
        loadType: "BODYWEIGHT_PLUS",
        measureType: "DURATION",
        setCount: "2",
        durationSec: "60",
        externalLoadKg: "20",
      }),
    );
    expect(result.ok).toBe(true);
    if (result.ok) {
      expect(result.value.durationSec).toBe(60);
      expect(result.value.reps).toBeNull();
      expect(result.value.externalLoadKg).toBe(20);
    }
  });

  it("rejects an unknown exercise", () => {
    const result = parseLogForm(form({ ...VALID_REPS, exerciseKey: "bench_press" }));
    expect(result.ok).toBe(false);
  });

  it("rejects an unknown load type", () => {
    const result = parseLogForm(form({ ...VALID_REPS, loadType: "WEIGHTED" }));
    expect(result.ok).toBe(false);
  });

  it("requires reps for REPS and duration for DURATION", () => {
    expect(parseLogForm(form({ ...VALID_REPS, reps: "" })).ok).toBe(false);
    expect(
      parseLogForm(form({ ...VALID_REPS, measureType: "DURATION", reps: "" })).ok,
    ).toBe(false);
  });

  it("enforces the sec.15 range table", () => {
    expect(parseLogForm(form({ ...VALID_REPS, reps: "0" })).ok).toBe(false);
    expect(parseLogForm(form({ ...VALID_REPS, reps: "1000" })).ok).toBe(false);
    expect(parseLogForm(form({ ...VALID_REPS, setCount: "0" })).ok).toBe(false);
    expect(parseLogForm(form({ ...VALID_REPS, setCount: "100" })).ok).toBe(false);
    expect(
      parseLogForm(
        form({
          exerciseKey: "wall_sit",
          loadType: "BODYWEIGHT",
          measureType: "DURATION",
          setCount: "1",
          durationSec: "3601",
        }),
      ).ok,
    ).toBe(false);
  });

  it("enforces the external load bounds and two-decimal precision", () => {
    expect(parseLogForm(form({ ...VALID_REPS, externalLoadKg: "-1" })).ok).toBe(false);
    expect(parseLogForm(form({ ...VALID_REPS, externalLoadKg: "1000.01" })).ok).toBe(false);
    expect(parseLogForm(form({ ...VALID_REPS, externalLoadKg: "12.345" })).ok).toBe(false);
    const ok = parseLogForm(form({ ...VALID_REPS, externalLoadKg: "12.5" }));
    expect(ok.ok).toBe(true);
    if (ok.ok) expect(ok.value.externalLoadKg).toBe(12.5);
  });

  it("toggles warm-up only from the checkbox value", () => {
    const warm = parseLogForm(form({ ...VALID_REPS, isWarmup: "on" }));
    expect(warm.ok && warm.value.isWarmup).toBe(true);
    const notWarm = parseLogForm(form({ ...VALID_REPS, isWarmup: "yes" }));
    expect(notWarm.ok && notWarm.value.isWarmup).toBe(false);
  });

  it("trims notes and enforces the length cap", () => {
    const trimmed = parseLogForm(form({ ...VALID_REPS, notes: "  easy day  " }));
    expect(trimmed.ok && trimmed.value.notes).toBe("easy day");
    const tooLong = parseLogForm(form({ ...VALID_REPS, notes: "x".repeat(501) }));
    expect(tooLong.ok).toBe(false);
  });

  it("rejects an invalid performed-at date", () => {
    expect(parseLogForm(form({ ...VALID_REPS, performedAt: "not-a-date" })).ok).toBe(false);
  });
});

describe("setTonnageKg (README · Metrics)", () => {
  it("counts added-load sets only", () => {
    const set = { reps: 8, isWarmup: false, externalLoadKg: 20 };
    expect(setTonnageKg("BODYWEIGHT_PLUS", set)).toBe(160);
    expect(setTonnageKg("BODYWEIGHT", set)).toBe(0);
    expect(setTonnageKg("ASSISTED", set)).toBe(0);
  });

  it("excludes warm-up sets and reps-less holds", () => {
    expect(setTonnageKg("BODYWEIGHT_PLUS", { reps: 8, isWarmup: true, externalLoadKg: 20 })).toBe(0);
    expect(setTonnageKg("BODYWEIGHT_PLUS", { reps: null, isWarmup: false, externalLoadKg: 20 })).toBe(0);
  });
});

describe("groupSets", () => {
  const set = (setNumber: number, reps: number | null) => ({
    setNumber,
    reps,
    durationSec: null,
    externalLoadKg: 0,
  });

  it("condenses identical consecutive sets", () => {
    const groups = groupSets([set(1, 12), set(2, 12), set(3, 12)]);
    expect(groups).toEqual([{ count: 3, reps: 12, durationSec: null, externalLoadKg: 0 }]);
  });

  it("keeps different sets as separate groups and re-sorts by set number", () => {
    const groups = groupSets([set(3, 8), set(1, 12), set(2, 12)]);
    expect(groups.map((g) => [g.count, g.reps])).toEqual([
      [2, 12],
      [1, 8],
    ]);
  });
});

describe("i18n helpers", () => {
  it("resolves the language from the query param", () => {
    expect(resolveLang("fa")).toBe("fa");
    expect(resolveLang("en")).toBe("en");
    expect(resolveLang(null)).toBe("en");
  });

  it("renders set chips in English", () => {
    expect(formatSetGroup("en", { count: 3, reps: 12, durationSec: null, externalLoadKg: 0 })).toBe(
      "3 × 12 reps",
    );
    expect(formatSetGroup("en", { count: 2, reps: null, durationSec: 60, externalLoadKg: 20 })).toBe(
      "2 × 60 s · +20 kg",
    );
  });

  it("renders set chips in Persian with Persian digits", () => {
    expect(formatSetGroup("fa", { count: 3, reps: 12, durationSec: null, externalLoadKg: 0 })).toBe(
      "۳ × ۱۲ تکرار",
    );
  });

  it("formats tonnage per language", () => {
    expect(formatTonnageIntl("en", 60)).toBe("60 kg");
    expect(formatTonnageIntl("fa", 60)).toContain("۶۰");
    expect(formatTonnageIntl("fa", 60)).toContain("کیلوگرم");
  });

  it("formats session dates in Jalali for fa and Gregorian for en", () => {
    const date = new Date("2026-10-03T12:00:00Z");
    expect(formatSessionDate("en", date)).toContain("2026");
    const faDate = formatSessionDate("fa", date);
    expect(faDate).toContain("مهر");
    expect(faDate).toContain("۱۴۰۵");
  });

  it("formats numbers with language-specific digits", () => {
    expect(formatNumber("en", 1234, 0)).toBe("1,234");
    expect(formatNumber("fa", 1234, 0)).toBe("۱٬۲۳۴");
  });
});
