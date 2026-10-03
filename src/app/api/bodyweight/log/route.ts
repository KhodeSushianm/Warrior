import { NextRequest, NextResponse } from "next/server";
import { db } from "@/db";
import { bodyweightSets, bodyweightWorkouts } from "@/db/schema";
import { parseLogForm } from "@/lib/bodyweight";

export const dynamic = "force-dynamic";

export async function POST(request: NextRequest) {
  const url = new URL("/bodyweight", request.url);

  let form: FormData;
  try {
    form = await request.formData();
  } catch {
    url.searchParams.set("log_error", "The submitted form could not be read.");
    return NextResponse.redirect(url, 303);
  }

  // Keep the caller in their language after the redirect (?lang=fa renders RTL Persian).
  const lang = form.get("lang");
  if (lang === "fa") {
    url.searchParams.set("lang", "fa");
  }

  const parsed = parseLogForm(form);
  if (!parsed.ok) {
    url.searchParams.set("log_error", parsed.error);
    return NextResponse.redirect(url, 303);
  }

  const { value } = parsed;

  try {
    await db.transaction(async (tx) => {
      const [workout] = await tx
        .insert(bodyweightWorkouts)
        .values({
          exerciseKey: value.exercise.key,
          exerciseNameEn: value.exercise.nameEn,
          exerciseNameFa: value.exercise.nameFa,
          loadType: value.loadType,
          measureType: value.measureType,
          performedAt: value.performedAt,
          notes: value.notes,
        })
        .returning({ id: bodyweightWorkouts.id });

      await tx.insert(bodyweightSets).values(
        Array.from({ length: value.setCount }, (_, index) => ({
          workoutId: workout.id,
          setNumber: index + 1,
          isWarmup: value.isWarmup,
          reps: value.reps,
          durationSec: value.durationSec,
          externalLoadKg: value.externalLoadKg,
        })),
      );
    });
  } catch (error) {
    url.searchParams.set(
      "log_error",
      error instanceof Error ? error.message : "Unable to save the session.",
    );
    return NextResponse.redirect(url, 303);
  }

  url.searchParams.set("logged", "1");
  return NextResponse.redirect(url, 303);
}
