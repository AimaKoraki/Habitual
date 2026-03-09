package com.aima.habitual.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aima.habitual.model.Habit
import com.aima.habitual.receiver.ReminderReceiver
import java.util.Calendar

object ReminderManager {

    private const val TAG = "ReminderManager"

    fun scheduleReminder(context: Context, habit: Habit) {
        if (!habit.isReminderEnabled || habit.reminderTime == null) {
            cancelReminder(context, habit.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(ReminderReceiver.EXTRA_HABIT_TITLE, habit.title)
        }

        val requestCode = habit.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = habit.reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // We use setRepeating instead of exact alarms for daily habits unless exact time is critical
        // For Android 12+, inexact repeating is safer without SCHEDULE_EXACT_ALARM permission
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d(TAG, "Scheduled reminder for ${habit.title} at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm: permission denied", e)
        }
    }

    fun cancelReminder(context: Context, habitId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)

        val requestCode = habitId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        // Only cancel if it was already created
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled reminder for habitId $habitId")
        }
    }
}
