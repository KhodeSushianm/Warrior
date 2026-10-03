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
import androidx.compose.foundation.layout.weight
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
import com.warrior.tracker.core.common.MeasureType
import com.warrior.tracker.core.time.toJalali
import com.warrior.tracker.core.time.toPersianDigits
import com.warrior.tracker.domain.model.BodyweightActivity
import com.warrior.tracker.domain.model.BodyweightExercise
import com.warrior.tracker.domain.model.BodyweightWorkoutDraft
import com.warrior.tracker.domain.model.BodyweightWorkoutSummary
import java.time.LocalDate
import java.util.Locale

/** End-to-end bodyweight strength logger: draft, exercises, completed sets and recent history. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyweightWorkoutsScreen(
    viewModel: BodyweightWorkoutViewModel = hiltViewModel(),
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
        topBar = { TopAppBar(title = { Text(stringResource(R.string.bodyweight_title)) }) },
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
                    text = stringResource(R.string.bodyweight_subtitle),
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
                    text = stringResource(R.string.bodyweight_recent),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            if (state.recentWorkouts.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.bodyweight_no_history),
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
    draft: BodyweightWorkoutDraft?,
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
                    Text(stringResource(R.string.bodyweight_start))
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
                    text = stringResource(R.string.bodyweight_active),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.bodyweight_date,
                        formatDate(draft.localDate, useJalali, usePersianDigits),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.bodyweight_set_count,
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
                        Text(stringResource(R.string.bodyweight_add_exercise))
                    }
                    Button(
                        onClick = onFinish,
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.bodyweight_finish))
                    }
                }
            }
        }

        if (draft.activities.isEmpty()) {
            Text(
                text = stringResource(R.string.bodyweight_no_exercises),
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
    activity: BodyweightActivity,
    usePersianNames: Boolean,
    usePersianDigits: Boolean,
    isBusy: Boolean,
    onAddSet: (String) -> Unit,
    onRemoveSet: (String) -> Unit,
    onRemoveActivity: () -> Unit,
) {
    var value by rememberSaveable(activity.id) { mutableStateOf("") }
    val unit = if (activity.measureType == MeasureType.REPS) {
        stringResource(R.string.bodyweight_reps)
    } else {
        stringResource(R.string.bodyweight_seconds)
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
                        text = stringResource(
                            if (activity.measureType == MeasureType.REPS) {
                                R.string.bodyweight_reps_label
                            } else {
                                R.string.bodyweight_timed_label
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemoveActivity, enabled = !isBusy) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.bodyweight_remove_exercise),
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
                            R.string.bodyweight_set_row,
                            localizeNumber(set.setNumber, usePersianDigits),
                            localizeNumber(setValue, usePersianDigits),
                            unit,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onRemoveSet(set.id) }, enabled = !isBusy) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.bodyweight_remove_set),
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
                    label = { Text(stringResource(R.string.bodyweight_value_hint, unit)) },
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
                    Text(stringResource(R.string.bodyweight_log_set))
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<BodyweightExercise>,
    usePersianNames: Boolean,
    onSelect: (BodyweightExercise) -> Unit,
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
        title = { Text(stringResource(R.string.bodyweight_choose_exercise)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.bodyweight_search_exercise)) },
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
                                text = stringResource(
                                    if (exercise.measureType == MeasureType.REPS) {
                                        R.string.bodyweight_reps_label
                                    } else {
                                        R.string.bodyweight_timed_label
                                    }
                                ),
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
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.bodyweight_close)) }
        },
    )
}

@Composable
private fun WorkoutSummaryCard(
    workout: BodyweightWorkoutSummary,
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
                    R.string.bodyweight_summary_exercises_sets,
                    localizeNumber(workout.exerciseCount, usePersianDigits),
                    localizeNumber(workout.setCount, usePersianDigits),
                ),
            )
            Text(
                text = stringResource(
                    R.string.bodyweight_summary_reps_time,
                    localizeNumber(workout.totalReps, usePersianDigits),
                    formatDuration(workout.totalDurationSec.toLong(), usePersianDigits),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.bodyweight_summary_duration,
                    formatDuration(workout.activeDurationSec, usePersianDigits),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BodyweightMessage?.localizedText(): String = when (this) {
    BodyweightMessage.WORKOUT_READY -> stringResource(R.string.bodyweight_message_ready)
    BodyweightMessage.EXERCISE_ADDED -> stringResource(R.string.bodyweight_message_exercise_added)
    BodyweightMessage.SET_ADDED -> stringResource(R.string.bodyweight_message_set_added)
    BodyweightMessage.SET_REMOVED -> stringResource(R.string.bodyweight_message_set_removed)
    BodyweightMessage.EXERCISE_REMOVED -> stringResource(R.string.bodyweight_message_exercise_removed)
    BodyweightMessage.WORKOUT_FINISHED -> stringResource(R.string.bodyweight_message_finished)
    BodyweightMessage.INVALID_INPUT -> stringResource(R.string.bodyweight_message_invalid)
    BodyweightMessage.EMPTY_WORKOUT -> stringResource(R.string.bodyweight_message_empty)
    BodyweightMessage.OPERATION_FAILED -> stringResource(R.string.bodyweight_message_failed)
    null -> ""
}

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
