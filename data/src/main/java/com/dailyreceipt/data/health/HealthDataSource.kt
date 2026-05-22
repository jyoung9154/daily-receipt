package com.dailyreceipt.data.health

import android.content.Context
import android.content.Intent
import com.dailyreceipt.domain.model.HealthData
import com.dailyreceipt.domain.model.HeartRateSample
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataSourcesResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource for health data using Google Fit API.
 * Requires Google Sign-In and fitness permissions.
 */
@Singleton
class HealthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val FITNESS_OPTIONS = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_ACTIVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
            .build()
    }

    private val fitnessClient by lazy {
        Fitness.getRecordingClient(context)
    }

    private val sensorsClient by lazy {
        Fitness.getSensorsClient(context)
    }

    private val historyClient by lazy {
        Fitness.getHistoryClient(context)
    }

    /**
     * Check if user is signed in with Google.
     */
    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Check if the app has fitness permissions granted.
     */
    fun hasPermissions(): Boolean {
        val account = getSignedInAccount() ?: return false
        return Fitness.hasPermissions(account, FITNESS_OPTIONS)
    }

    /**
     * Get the Google Sign-In intent for fitness permissions.
     */
    fun getSignInIntent(): Intent {
        val account = getSignedInAccount()
        return GoogleSignIn.getClient(
            context,
            GoogleSignIn.getGoogleSignInOptions(account)
        ).signInIntent
    }

    /**
     * Get the Google Sign-In options with fitness permissions.
     */
    private fun getGoogleSignInOptions(existingAccount: GoogleSignInAccount?): GoogleSignInOptions {
        return GoogleSignInOptions.Builder()
            .addExtension(FITNESS_OPTIONS)
            .apply {
                if (existingAccount != null) {
                    setAccount(existingAccount.account)
                }
            }
            .build()
    }

    /**
     * Get health data for a specific date.
     */
    suspend fun getHealthDataForDate(date: LocalDateTime): HealthData =
        withContext(Dispatchers.IO) {
            val account = getSignedInAccount()
                ?: return@withContext HealthData()

            val startMillis = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val endMillis = date.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            try {
                // Read all data types in parallel
                val stepCount = readStepCount(account, startMillis, endMillis)
                val distance = readDistance(account, startMillis, endMillis)
                val activeMinutes = readActiveMinutes(account, startMillis, endMillis)
                val calories = readCalories(account, startMillis, endMillis)
                val heartRateSamples = readHeartRateSamples(account, startMillis, endMillis)

                HealthData(
                    steps = stepCount,
                    distanceMeters = distance,
                    activeMinutes = activeMinutes,
                    caloriesBurned = calories,
                    heartRateSamples = heartRateSamples,
                    recordedAt = LocalDateTime.now()
                )
            } catch (e: Exception) {
                // Return empty health data if there's an error
                HealthData()
            }
        }

    /**
     * Get today's health data.
     */
    suspend fun getTodayHealthData(): HealthData {
        return getHealthDataForDate(LocalDateTime.now())
    }

    /**
     * Read step count for the time range.
     */
    private suspend fun readStepCount(
        account: GoogleSignInAccount,
        startMillis: Long,
        endMillis: Long
    ): Int {
        return try {
            val request = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
                .build()

            val response = historyClient.readData(request).await()
            response.buckets.flatMap { it.dataSets }
                .flatMap { it.dataPoints }
                .sumOf { it.getValue(Field.FIELD_STEPS).asInt() }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Read distance for the time range.
     */
    private suspend fun readDistance(
        account: GoogleSignInAccount,
        startMillis: Long,
        endMillis: Long
    ): Float {
        return try {
            val request = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
                .build()

            val response = historyClient.readData(request).await()
            response.buckets.flatMap { it.dataSets }
                .flatMap { it.dataPoints }
                .sumOf { it.getValue(Field.FIELD_DISTANCE).asFloat().toDouble() }
                .toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Read active minutes for the time range.
     */
    private suspend fun readActiveMinutes(
        account: GoogleSignInAccount,
        startMillis: Long,
        endMillis: Long
    ): Int {
        return try {
            val request = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVE_MINUTES)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
                .build()

            val response = historyClient.readData(request).await()
            response.buckets.flatMap { it.dataSets }
                .flatMap { it.dataPoints }
                .sumOf { it.getValue(Field.FIELD_DURATION).asInt() }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Read calories for the time range.
     */
    private suspend fun readCalories(
        account: GoogleSignInAccount,
        startMillis: Long,
        endMillis: Long
    ): Float {
        return try {
            val request = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
                .build()

            val response = historyClient.readData(request).await()
            response.buckets.flatMap { it.dataSets }
                .flatMap { it.dataPoints }
                .sumOf { it.getValue(Field.FIELD_CALORIES).asFloat().toDouble() }
                .toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Read heart rate samples for the time range.
     */
    private suspend fun readHeartRateSamples(
        account: GoogleSignInAccount,
        startMillis: Long,
        endMillis: Long
    ): List<HeartRateSample> {
        return try {
            val request = DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startMillis, endMillis, TimeUnit.MILLISECONDS)
                .build()

            val response = historyClient.readData(request).await()
            response.dataSets.flatMap { it.dataPoints }.map { dataPoint ->
                val timestamp = dataPoint.getTimestamp(java.time.Instant::class.java)
                HeartRateSample(
                    bpm = dataPoint.getValue(Field.FIELD_BPM).asInt(),
                    recordedAt = LocalDateTime.ofInstant(
                        timestamp,
                        ZoneId.systemDefault()
                    )
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Subscribe to real-time heart rate updates.
     */
    fun subscribeToHeartRate(): Flow<Int> = callbackFlow {
        val account = getSignedInAccount()
        if (account == null) {
            close()
            return@callbackFlow
        }

        val listener = object : com.google.android.gms.fitness.SensorListener {
            override fun onSensorChanged(datapoint: com.google.android.gms.fitness.data.DataPoint) {
                trySend(datapoint.getValue(Field.FIELD_BPM).asInt())
            }

            override fun onAccuracyChanged(sensor: com.google.android.gms.fitness.data.DataSource?, accuracy: Int) {
                // Not needed
            }
        }

        try {
            val dataSourcesRequest = DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                .setDataSourcesEnabled(true)
                .build()

            sensorsClient.findDataSources(dataSourcesRequest)
                .addOnSuccessListener { dataSources ->
                    val heartRateSource = dataSources.firstOrNull { source ->
                        source.dataType == DataType.TYPE_HEART_RATE_BPM
                    }

                    heartRateSource?.let { source ->
                        val sensorRequest = SensorRequest.Builder()
                            .setDataSource(source)
                            .setDataType(DataType.TYPE_HEART_RATE_BPM)
                            .setSamplingRate(5, TimeUnit.SECONDS)
                            .build()

                        sensorsClient.add(listener, sensorRequest)
                    }
                }

        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            sensorsClient.remove(listener)
        }
    }

    /**
     * Subscribe to real-time step count updates.
     */
    fun subscribeToStepCount(): Flow<Int> = callbackFlow {
        val request = SensorRequest.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setSamplingRate(1, TimeUnit.SECONDS)
            .build()

        val listener = object : com.google.android.gms.fitness.SensorListener {
            override fun onSensorChanged(datapoint: com.google.android.gms.fitness.data.DataPoint) {
                trySend(datapoint.getValue(Field.FIELD_STEPS).asInt())
            }

            override fun onAccuracyChanged(sensor: com.google.android.gms.fitness.data.DataSource?, accuracy: Int) {
                // Not needed
            }
        }

        sensorsClient.add(listener, request)

        awaitClose {
            sensorsClient.remove(listener)
        }
    }
}

// Extension for GoogleSignInOptions builder
private fun GoogleSignIn.getGoogleSignInOptions(existingAccount: com.google.android.gms.auth.api.signin.GoogleSignInAccount?): com.google.android.gms.auth.api.signin.GoogleSignInOptions {
    return GoogleSignInOptions.Builder()
        .addExtension(FITNESS_OPTIONS)
        .apply {
            existingAccount?.let { setAccount(it.account) }
        }
        .build()
}
