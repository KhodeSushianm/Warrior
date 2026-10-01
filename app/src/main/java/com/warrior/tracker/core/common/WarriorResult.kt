package com.warrior.tracker.core.common

/**
 * Domain result wrapper (ARCHITECTURE.md sec.15 — Validation & Error Handling).
 * Failures carry a machine-readable reason, never raw exceptions to the UI.
 */
sealed interface WarriorResult<out T> {
    data class Success<T>(val data: T) : WarriorResult<T>
    data class Failure(val error: WarriorError) : WarriorResult<Nothing>
}

sealed interface WarriorError {
    /** Input outside allowed ranges (see sec.15 validation table). */
    data class Validation(val field: String, val reason: Reason) : WarriorError

    /** Referenced entity does not exist or was archived/locked. */
    data class NotFound(val entity: String, val id: String) : WarriorError

    /** DB / IO failure while persisting. */
    data class Persistence(val cause: Throwable?) : WarriorError

    enum class Reason {
        REQUIRED, TOO_SMALL, TOO_LARGE, INVALID_FORMAT, INCONSISTENT_STATE
    }
}
