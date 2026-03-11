package com.aima.habitual.model

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * LightSensorManager: Interface for the device's ambient light sensor (TYPE_LIGHT).
 * Implements SensorEventListener to receive real-time lux (brightness) readings.
 * No special permission is required — TYPE_LIGHT is a non-protected sensor.
 */
class LightSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Function to notify the ViewModel when a new lux reading is available
    private var onLuxUpdate: ((Float) -> Unit)? = null

    /**
     * Registers the sensor listener and starts measuring ambient brightness.
     * [callback] returns the current illuminance level in lux.
     *
     * The SENSOR_DELAY_NORMAL rate is sufficient — we only need one reading when
     * the user opens the sleep dialog, not continuous high-frequency updates.
     */
    fun startListening(callback: (Float) -> Unit) {
        if (lightSensor == null) {
            Log.w("LightSensor", "No ambient light sensor found on this device.")
            return
        }
        onLuxUpdate = callback
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * Unregisters the listener to conserve battery when sensor data is not needed.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        onLuxUpdate = null
    }

    /**
     * Triggered by the Android system whenever the ambient light level changes.
     * event.values[0] = current illuminance in lux (SI unit).
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                val lux = it.values[0]
                onLuxUpdate?.invoke(lux)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed for light sensor accuracy changes
    }
}
