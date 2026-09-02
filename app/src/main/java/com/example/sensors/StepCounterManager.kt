package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepCounterManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _stepCount = MutableStateFlow(0L)
    val stepCount: StateFlow<Long> = _stepCount.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(stepCounterSensor != null || stepDetectorSensor != null)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var initialStepOffset = -1L

    fun startListening() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSensorSteps = event.values[0].toLong()
                if (initialStepOffset < 0) {
                    initialStepOffset = totalSensorSteps
                }
                val sessionSteps = (totalSensorSteps - initialStepOffset).coerceAtLeast(0L)
                if (sessionSteps > _stepCount.value) {
                    _stepCount.value = sessionSteps
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    _stepCount.value = _stepCount.value + 1L
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun addSimulatedSteps(steps: Long) {
        _stepCount.value = _stepCount.value + steps
    }

    fun resetSessionSteps() {
        _stepCount.value = 0L
        initialStepOffset = -1L
    }
}
