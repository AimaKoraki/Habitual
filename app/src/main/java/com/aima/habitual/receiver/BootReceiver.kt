package com.aima.habitual.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.utils.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reschedules all active habit reminders after a device reboot.
 * Android clears all AlarmManager alarms on reboot, so this receiver
 * reads habits from the Room database and re-registers them.
 */
class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, rescheduling habit reminders...")
            rescheduleAllAlarms(context)
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        val dao = HabitualDatabase.getInstance(context).habitDao()

        // Use goAsync() pattern for BroadcastReceivers that need coroutine work
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = dao.getAllHabits().first()
                var rescheduledCount = 0
                for (habit in habits) {
                    if (habit.isReminderEnabled && habit.reminderTime != null) {
                        ReminderManager.scheduleReminder(context, habit)
                        rescheduledCount++
                    }
                }
                Log.d(TAG, "Successfully rescheduled $rescheduledCount habits")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule habits", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
