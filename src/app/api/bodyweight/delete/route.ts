import { NextRequest, NextResponse } from "next/server";
import { eq } from "drizzle-orm";
import { db } from "@/db";
import { bodyweightWorkouts } from "@/db/schema";

export const dynamic = "force-dynamic";

export async function POST(request: NextRequest) {
  const url = new URL("/bodyweight", request.url);

  try {
    const form = await request.formData();
    const id = form.get("id");

    if (typeof id !== "string" || id.length === 0) {
      url.searchParams.set("log_error", "Missing session id.");
      return NextResponse.redirect(url, 303);
    }

    // Sets are removed by the ON DELETE CASCADE on bodyweight_sets.workout_id.
    await db.delete(bodyweightWorkouts).where(eq(bodyweightWorkouts.id, id));
  } catch (error) {
    url.searchParams.set(
      "log_error",
      error instanceof Error ? error.message : "Unable to delete the session.",
    );
    return NextResponse.redirect(url, 303);
  }

  url.searchParams.set("deleted", "1");
  return NextResponse.redirect(url, 303);
}
