package com.warrior.tracker.feature.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warrior.tracker.R
import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType
import com.warrior.tracker.core.time.toJalali
import com.warrior.tracker.core.time.toPersianDigits
import com.warrior.tracker.domain.model.StrengthActivity
import com.warrior.tracker.domain.model.StrengthExercise
import com.warrior.tracker.domain.model.WorkoutDraft
import com.warrior.tracker.domain.model.WorkoutSummary
import java.time.LocalDate
import java.util.Locale

/** Existing Workouts flow with offline Strength logging and recent history. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutLoggingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showExercisePicker by rememberSaveable { mutableStateOf(false) }

    val messageText = state.message.localizedText()
    LaunchedEffect(state.message, messageText) {
        if (state.message != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.workout_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.workout_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                ActiveWorkoutSection(
                    draft = state.draft,
                    usePersianNames = state.usePersianNames,
                    useJalali = state.useJalali,
                    usePersianDigits = state.usePersianDigits,
                    isBusy = state.isBusy,
                    onStart = viewModel::startOrResumeWorkout,
                    onAddExercise = { showExercisePicker = true },
                    onAddSet = viewModel::addSet,
                    onRemoveSet = viewModel::removeSet,
                    onRemoveActivity = viewModel::removeActivity,
                    onFinish = viewModel::finishWorkout,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.workout_recent),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            if (state.recentWorkouts.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.workout_no_history),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.recentWorkouts, key = { it.id }) { workout ->
                    WorkoutSummaryCard(
                        workout = workout,
                        useJalali = state.useJalali,
                        usePersianDigits = state.usePersianDigits,
                    )
                }
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            exercises = state.exercises,
            usePersianNames = state.usePersianNames,
            onSelect = { exercise ->
                showExercisePicker = false
                viewModel.addExercise(exercise.id)
            },
            onDismiss = { showExercisePicker = false },
        )
    }
}

@Composable
private fun ActiveWorkoutSection(
    draft: WorkoutDraft?,
    usePersianNames: Boolean,
    useJalali: Boolean,
    usePersianDigits: Boolean,
    isBusy: Boolean,
    onStart: () -> Unit,
    onAddExercise: () -> Unit,
    onAddSet: (String, String) -> Unit,
    onRemoveSet: (String) -> Unit,
    onRemoveActivity: (String) -> Unit,
    onFinish: () -> Unit,
) {
    if (draft == null) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
                Button(onClick = onStart, enabled = !isBusy) {
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.workout_start))
                }
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.workout_active),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.workout_date,
                        formatDate(draft.localDate, useJalali, usePersianDigits),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.workout_set_count,
                        localizeNumber(draft.completedSetCount, usePersianDigits),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onAddExercise,
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.workout_add_exercise))
                    }
                    Button(
                        onClick = onFinish,
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.workout_finish))
                    }
                }
            }
        }

        if (draft.activities.isEmpty()) {
            Text(
                text = stringResource(R.string.workout_no_exercises),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        draft.activities.forEach { activity ->
            ActivityCard(
                activity = activity,
                usePersianNames = usePersianNames,
                usePersianDigits = usePersianDigits,
                isBusy = isBusy,
                onAddSet = { value -> onAddSet(activity.id, value) },
                onRemoveSet = onRemoveSet,
                onRemoveActivity = { onRemoveActivity(activity.id) },
            )
        }
    }
}

@Composable
private fun ActivityCard(
    activity: StrengthActivity,
    usePersianNames: Boolean,
    usePersianDigits: Boolean,
    isBusy: Boolean,
    onAddSet: (String) -> Unit,
    onRemoveSet: (String) -> Unit,
    onRemoveActivity: () -> Unit,
) {
    var value by rememberSaveable(activity.id) { mutableStateOf("") }
    val unit = if (activity.measureType == MeasureType.REPS) {
        stringResource(R.string.workout_reps)
    } else {
        stringResource(R.string.workout_seconds)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (usePersianNames) activity.nameFa else activity.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${loadTypeLabel(activity.loadType)} • ${stringResource(
                            if (activity.measureType == MeasureType.REPS) {
                                R.string.workout_reps_label
                            } else {
                                R.string.workout_timed_label
                            }
                        )}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemoveActivity, enabled = !isBusy) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.workout_remove_exercise),
                    )
                }
            }

            activity.sets.forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val setValue = set.reps ?: set.durationSec ?: 0
                    Text(
                        text = stringResource(
                            R.string.workout_set_row,
                            localizeNumber(set.setNumber, usePersianDigits),
                            localizeNumber(setValue, usePersianDigits),
                            unit,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onRemoveSet(set.id) }, enabled = !isBusy) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.workout_remove_set),
                        )
                    }
                }
                HorizontalDivider()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.workout_value_hint, unit)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = if (activity.measureType == MeasureType.REPS) {
                                Icons.Filled.FitnessCenter
                            } else {
                                Icons.Filled.Timer
                            },
                            contentDescription = null,
                        )
                    },
                )
                Button(
                    onClick = { onAddSet(value) },
                    enabled = value.isNotBlank() && !isBusy,
                ) {
                    Text(stringResource(R.string.workout_log_set))
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<StrengthExercise>,
    usePersianNames: Boolean,
    onSelect: (StrengthExercise) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(exercises, query) {
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) exercises else exercises.filter { exercise ->
            exercise.nameEn.lowercase(Locale.ROOT).contains(normalized) ||
                exercise.nameFa.contains(query.trim())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_choose_exercise)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.workout_search_exercise)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(filtered, key = { it.id }) { exercise ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(exercise) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                        ) {
                            Text(
                                text = if (usePersianNames) exercise.nameFa else exercise.nameEn,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${loadTypeLabel(exercise.loadType)} • ${stringResource(
                                    if (exercise.measureType == MeasureType.REPS) {
                                        R.string.workout_reps_label
                                    } else {
                                        R.string.workout_timed_label
                                    }
                                )}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.workout_close)) }
        },
    )
}

@Composable
private fun WorkoutSummaryCard(
    workout: WorkoutSummary,
    useJalali: Boolean,
    usePersianDigits: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = formatDate(workout.localDate, useJalali, usePersianDigits),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.workout_summary_exercises_sets,
                    localizeNumber(workout.exerciseCount, usePersianDigits),
                    localizeNumber(workout.setCount, usePersianDigits),
                ),
            )
            Text(
                text = stringResource(
                    R.string.workout_summary_reps_time,
                    localizeNumber(workout.totalReps, usePersianDigits),
                    formatDuration(workout.totalDurationSec.toLong(), usePersianDigits),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.workout_summary_duration,
                    formatDuration(workout.activeDurationSec, usePersianDigits),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WorkoutMessage?.localizedText(): String = when (this) {
    WorkoutMessage.WORKOUT_READY -> stringResource(R.string.workout_message_ready)
    WorkoutMessage.EXERCISE_ADDED -> stringResource(R.string.workout_message_exercise_added)
    WorkoutMessage.SET_ADDED -> stringResource(R.string.workout_message_set_added)
    WorkoutMessage.SET_REMOVED -> stringResource(R.string.workout_message_set_removed)
    WorkoutMessage.EXERCISE_REMOVED -> stringResource(R.string.workout_message_exercise_removed)
    WorkoutMessage.WORKOUT_FINISHED -> stringResource(R.string.workout_message_finished)
    WorkoutMessage.INVALID_INPUT -> stringResource(R.string.workout_message_invalid)
    WorkoutMessage.EMPTY_WORKOUT -> stringResource(R.string.workout_message_empty)
    WorkoutMessage.OPERATION_FAILED -> stringResource(R.string.workout_message_failed)
    null -> ""
}

@Composable
private fun loadTypeLabel(loadType: LoadType): String = stringResource(
    when (loadType) {
        LoadType.BODYWEIGHT -> R.string.workout_load_bodyweight
        LoadType.WEIGHTED -> R.string.workout_load_weighted
        LoadType.ASSISTED -> R.string.workout_load_assisted
        LoadType.BODYWEIGHT_PLUS -> R.string.workout_load_bodyweight_plus
    }
)

private fun formatDate(isoDate: String, useJalali: Boolean, persianDigits: Boolean): String =
    runCatching {
        val date = LocalDate.parse(isoDate)
        if (useJalali) date.toJalali().format(persianDigits) else {
            if (persianDigits) isoDate.toPersianDigits() else isoDate
        }
    }.getOrDefault(isoDate)

private fun formatDuration(totalSeconds: Long, persianDigits: Boolean): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3_600
    val minutes = (safe % 3_600) / 60
    val seconds = safe % 60
    val text = if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
    return if (persianDigits) text.toPersianDigits() else text
}

private fun localizeNumber(value: Int, persianDigits: Boolean): String {
    val text = value.toString()
    return if (persianDigits) text.toPersianDigits() else text
}
