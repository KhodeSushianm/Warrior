import {
  boolean,
  integer,
  numeric,
  pgTable,
  text,
  timestamp,
  uuid,
} from "drizzle-orm/pg-core";

/**
 * Bodyweight training log — the web counterpart of the Android domain model
 * (ARCHITECTURE.md sec.10). Mirrored design decisions:
 * - rows are keyed by UUID so a future JSON backup merge stays possible;
 * - times are UTC `timestamptz`, weight is always kg (display-layer conversion only);
 * - `load_type` / `measure_type` hold the Android enum names as TEXT, never free-form
 *   strings at the type level (`LoadType` / `MeasureType` in `src/lib/bodyweight.ts`);
 * - a workout is a single bodyweight exercise session and owns its sets through a
 *   cascading foreign key — like `activities -> sets`, with no extra middle layer.
 */
export const bodyweightWorkouts = pgTable("bodyweight_workouts", {
  id: uuid("id").primaryKey().defaultRandom(),
  exerciseKey: text("exercise_key").notNull(),
  exerciseNameEn: text("exercise_name_en").notNull(),
  exerciseNameFa: text("exercise_name_fa").notNull(),
  loadType: text("load_type").notNull(),
  measureType: text("measure_type").notNull(),
  performedAt: timestamp("performed_at", { withTimezone: true }).notNull().defaultNow(),
  notes: text("notes"),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const bodyweightSets = pgTable("bodyweight_sets", {
  id: uuid("id").primaryKey().defaultRandom(),
  workoutId: uuid("workout_id")
    .notNull()
    .references(() => bodyweightWorkouts.id, { onDelete: "cascade" }),
  setNumber: integer("set_number").notNull(),
  isWarmup: boolean("is_warmup").notNull().default(false),
  reps: integer("reps"),
  durationSec: integer("duration_sec"),
  externalLoadKg: numeric("external_load_kg", {
    precision: 7,
    scale: 2,
    mode: "number",
  })
    .notNull()
    .default(0),
});

export type BodyweightWorkoutRow = typeof bodyweightWorkouts.$inferSelect;
export type BodyweightSetRow = typeof bodyweightSets.$inferSelect;
