package com.dailyreceipt.data.health

import android.content.Context
import com.dailyreceipt.domain.model.HealthData
import com.dailyreceipt.domain.model.HeartRateSample
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * DataSource for health data.
 * 
 * NOTE: Google Fit API dependencies are commented out in build.gradle.kts
 * due to ARM64 build issues on CI. This implementation uses mock data.
 * 
 * To enable Google Fit:
 * 1. Uncomment the dependencies in data/build.gradle.kts
 * 2. Uncomment the Google Fit code below
 * 3. Run on a physical device with Google Fit installed
 */
@Singleton
class HealthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Get health data for a specific date.
     * Currently returns mock data. Replace with Google Fit API calls when enabled.
     */
    suspend fun getHealthDataForDate(dateTime: LocalDateTime): HealthData {
        return withContext(Dispatchers.IO) {
            // Generate consistent mock data based on date
            val seed = dateTime.dayOfYear + dateTime.monthValue * 31
            val random = Random(seed.toLong())
            
            HealthData(
                steps = 5000 + random.nextInt(5000),
                distanceMeters = (6000 + random.nextInt(4000)).toFloat(),
                activeMinutes = 30 + random.nextInt(90),
                caloriesBurned = 1500f + random.nextInt(1000),
                heartRateSamples = generateMockHeartRateSamples(random),
                recordedAt = dateTime
            )
        }
    }
    
    private fun generateMockHeartRateSamples(random: Random): List<HeartRateSample> {
        val samples = mutableListOf<HeartRateSample>()
        val now = LocalDateTime.now()
        
        // Generate 6 samples throughout the day
        for (i in 0 until 6) {
            samples.add(
                HeartRateSample(
                    bpm = 60 + random.nextInt(40),
                    recordedAt = now.minusHours((5 - i).toLong()).withMinute(random.nextInt(60))
                )
            )
        }
        return samples
    }
    
    /*
     * TODO: Enable Google Fit API when running on physical device
     * 
     * import com.google.android.gms.auth.api.signin.GoogleSignIn
     * import com.google.android.gms.auth.api.signin.GoogleSignInAccount
     * import com.google.android.gms.fitness.Fitness
     * import com.google.android.gms.fitness.FitnessOptions
     * import com.google.android.gms.fitness.data.DataType
     * import com.google.android.gms.fitness.request.DataReadRequest
     * import kotlinx.coroutines.tasks.await
     * 
     * companion object {
     *     private val FITNESS_OPTIONS = FitnessOptions.builder()
     *         .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
     *         .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
     *         .build()
     * }
     * 
     * private suspend fun getGoogleFitData(dateTime: LocalDateTime): HealthData {
     *     val account = GoogleSignIn.getAccountForExtension(context, FITNESS_OPTIONS)
     *     
     *     val readRequest = DataReadRequest.Builder()
     *         .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
     *         .bucketByTime(1, TimeUnit.DAYS)
     *         .setTimeRange(
     *             dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
     *             dateTime.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
     *         )
     *         .build()
     *     
     *     val response = Fitness.getHistoryClient(context, account)
     *         .readData(readRequest)
     *         .await()
     *     
     *     // Process response...
     * }
     */
}
