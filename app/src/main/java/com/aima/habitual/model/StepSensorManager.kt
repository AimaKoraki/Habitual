package com.aima.habitual.model

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * StepSensorManager: Interface for the device's physical Step Counter sensor.
 * Implements SensorEventListener to receive real-time updates from the hardware.
 */
class StepSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // Function to notify the ViewModel when new step data is available
    private var onStepUpdate: ((Int) -> Unit)? = null

    /**
     * Registers the sensor listener and starts tracking movement.
     * [callback] returns the total steps recorded by the device since its last reboot.
     */
    fun startListening(callback: (Int) -> Unit) {
        if (stepSensor == null) {
            Log.e("Sensor", "No Step Counter Sensor found on this device.")
            return
        }
        onStepUpdate = callback
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    /**
     * Unregisters the listener to save battery when tracking is not required.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Triggered automatically by the Android system whenever a new step is detected.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                // event.values[0] holds the cumulative steps since the device was last turned on.
                val totalStepsSinceBoot = it.values[0].toInt()
                onStepUpdate?.invoke(totalStepsSinceBoot)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Required by interface but not necessary for basic step counting.
    }
}