package com.warrior.tracker.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warrior.tracker.core.common.AppLanguage
import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult
import com.warrior.tracker.core.settings.SettingsRepository
import com.warrior.tracker.core.time.NumberInputParser
import com.warrior.tracker.domain.model.BodyweightExercise
import com.warrior.tracker.domain.model.BodyweightWorkoutDraft
import com.warrior.tracker.domain.model.BodyweightWorkoutSummary
import com.warrior.tracker.domain.repository.BodyweightWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BodyweightMessage {
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

data class BodyweightWorkoutUiState(
    val draft: BodyweightWorkoutDraft? = null,
    val exercises: List<BodyweightExercise> = emptyList(),
    val recentWorkouts: List<BodyweightWorkoutSummary> = emptyList(),
    val usePersianNames: Boolean = false,
    val useJalali: Boolean = false,
    val usePersianDigits: Boolean = false,
    val isBusy: Boolean = false,
    val message: BodyweightMessage? = null,
)

private data class TransientState(
    val isBusy: Boolean = false,
    val message: BodyweightMessage? = null,
)

@HiltViewModel
class BodyweightWorkoutViewModel @Inject constructor(
    private val repository: BodyweightWorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val transient = MutableStateFlow(TransientState())

    val state: StateFlow<BodyweightWorkoutUiState> = combine(
        repository.observeDraft(),
        repository.observeExercises(),
        repository.observeRecentWorkouts(),
        settingsRepository.resolved,
        transient,
    ) { draft, exercises, recent, settings, action ->
        BodyweightWorkoutUiState(
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
        initialValue = BodyweightWorkoutUiState(),
    )

    init {
        viewModelScope.launch {
            val result = repository.initialize()
            if (result is WarriorResult.Failure) {
                transient.value = TransientState(message = BodyweightMessage.OPERATION_FAILED)
            }
        }
    }

    fun startOrResumeWorkout() = runAction(BodyweightMessage.WORKOUT_READY) {
        repository.startOrResumeWorkout()
    }

    fun addExercise(exerciseId: String) = runAction(BodyweightMessage.EXERCISE_ADDED) {
        repository.addExercise(exerciseId)
    }

    fun addSet(activityId: String, rawValue: String) {
        val value = NumberInputParser.toIntOrNull(rawValue)
        if (value == null) {
            transient.value = TransientState(message = BodyweightMessage.INVALID_INPUT)
            return
        }
        runAction(BodyweightMessage.SET_ADDED) {
            repository.addCompletedSet(activityId, value)
        }
    }

    fun removeSet(setId: String) = runAction(BodyweightMessage.SET_REMOVED) {
        repository.removeSet(setId)
    }

    fun removeActivity(activityId: String) = runAction(BodyweightMessage.EXERCISE_REMOVED) {
        repository.removeActivity(activityId)
    }

    fun finishWorkout() = runAction(BodyweightMessage.WORKOUT_FINISHED) {
        repository.finishWorkout()
    }

    fun consumeMessage() {
        transient.value = transient.value.copy(message = null)
    }

    private fun <T> runAction(
        successMessage: BodyweightMessage,
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

    private fun WarriorError.toMessage(): BodyweightMessage = when (this) {
        is WarriorError.Validation -> if (field == "workout") {
            BodyweightMessage.EMPTY_WORKOUT
        } else {
            BodyweightMessage.INVALID_INPUT
        }
        is WarriorError.NotFound,
        is WarriorError.Persistence,
        -> BodyweightMessage.OPERATION_FAILED
    }
}
