package com.warrior.tracker.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warrior.tracker.core.common.AppLanguage
import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult
import com.warrior.tracker.core.settings.SettingsRepository
import com.warrior.tracker.core.time.NumberInputParser
import com.warrior.tracker.domain.model.StrengthExercise
import com.warrior.tracker.domain.model.WorkoutDraft
import com.warrior.tracker.domain.model.WorkoutSummary
import com.warrior.tracker.domain.repository.WorkoutLoggingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WorkoutMessage {
    WORKOUT_READY,
    EXERCISE_ADDED,
    SET_ADDED,
    SET_REMOVED,
    EXERCISE_REMOVED,
    WORKOUT_FINISHED,
    INVALID_INPUT,
    EMPTY_WORKOUT,
    OPERATION_FAILED,
}

data class WorkoutLoggingUiState(
    val draft: WorkoutDraft? = null,
    val exercises: List<StrengthExercise> = emptyList(),
    val recentWorkouts: List<WorkoutSummary> = emptyList(),
    val usePersianNames: Boolean = false,
    val useJalali: Boolean = false,
    val usePersianDigits: Boolean = false,
    val isBusy: Boolean = false,
    val message: WorkoutMessage? = null,
)

private data class TransientState(
    val isBusy: Boolean = false,
    val message: WorkoutMessage? = null,
)

@HiltViewModel
class WorkoutLoggingViewModel @Inject constructor(
    private val repository: WorkoutLoggingRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val transient = MutableStateFlow(TransientState())

    val state: StateFlow<WorkoutLoggingUiState> = combine(
        repository.observeDraft(),
        repository.observeExercises(),
        repository.observeRecentWorkouts(),
        settingsRepository.resolved,
        transient,
    ) { draft, exercises, recent, settings, action ->
        WorkoutLoggingUiState(
            draft = draft,
            exercises = exercises,
            recentWorkouts = recent,
            usePersianNames = settings.language == AppLanguage.PERSIAN,
            useJalali = settings.useJalali,
            usePersianDigits = settings.usePersianDigits,
            isBusy = action.isBusy,
            message = action.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutLoggingUiState(),
    )

    init {
        viewModelScope.launch {
            val result = repository.initialize()
            if (result is WarriorResult.Failure) {
                transient.value = TransientState(message = WorkoutMessage.OPERATION_FAILED)
            }
        }
    }

    fun startOrResumeWorkout() = runAction(WorkoutMessage.WORKOUT_READY) {
        repository.startOrResumeWorkout()
    }

    fun addExercise(exerciseId: String) = runAction(WorkoutMessage.EXERCISE_ADDED) {
        repository.addExercise(exerciseId)
    }

    fun addSet(activityId: String, rawValue: String) {
        val value = NumberInputParser.toIntOrNull(rawValue)
        if (value == null) {
            transient.value = TransientState(message = WorkoutMessage.INVALID_INPUT)
            return
        }
        runAction(WorkoutMessage.SET_ADDED) {
            repository.addCompletedSet(activityId, value)
        }
    }

    fun removeSet(setId: String) = runAction(WorkoutMessage.SET_REMOVED) {
        repository.removeSet(setId)
    }

    fun removeActivity(activityId: String) = runAction(WorkoutMessage.EXERCISE_REMOVED) {
        repository.removeActivity(activityId)
    }

    fun finishWorkout() = runAction(WorkoutMessage.WORKOUT_FINISHED) {
        repository.finishWorkout()
    }

    fun consumeMessage() {
        transient.value = transient.value.copy(message = null)
    }

    private fun <T> runAction(
        successMessage: WorkoutMessage,
        action: suspend () -> WarriorResult<T>,
    ) {
        if (transient.value.isBusy) return
        viewModelScope.launch {
            transient.value = TransientState(isBusy = true)
            val result = action()
            transient.value = when (result) {
                is WarriorResult.Success -> TransientState(message = successMessage)
                is WarriorResult.Failure -> TransientState(message = result.error.toMessage())
            }
        }
    }

    private fun WarriorError.toMessage(): WorkoutMessage = when (this) {
        is WarriorError.Validation -> if (field == "workout") {
            WorkoutMessage.EMPTY_WORKOUT
        } else {
            WorkoutMessage.INVALID_INPUT
        }
        is WarriorError.NotFound,
        is WarriorError.Persistence,
        -> WorkoutMessage.OPERATION_FAILED
    }
}
