package com.aima.habitual.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aima.habitual.MainActivity
import com.aima.habitual.R
import com.aima.habitual.data.HabitualDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Receives scheduled habit reminders and turns them into notifications.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        // Extras used by the scheduler to tell this receiver which habit to remind.
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        private const val CHANNEL_ID = "habitual_reminders"
    }

    // Android delivers the alarm broadcast here.
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Your Ritual"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = HabitualDatabase.getInstance(context)
                val habit = db.habitDao().getHabitById(habitId)
                
                if (habit != null) {
                    val currentDay = LocalDate.now().dayOfWeek.value
                    if (habit.repeatDays.contains(currentDay)) {
                        showNotification(context, habitId, habitTitle)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    // Build and post a high-priority reminder notification for the habit.
    private fun showNotification(context: Context, habitId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O+ requires a notification channel before notifications can be shown.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Notifications for your daily rituals"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tapping the notification returns the user to the main app screen.
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: replace with a proper monochrome silhouette icon if available
            .setContentTitle("Time for: $title")
            .setContentText("Complete your ritual and grow your streak!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Derive a stable, non-negative notification ID from the habit ID.
        val notificationId = habitId.hashCode().and(0x7FFFFFFF)
        notificationManager.notify(notificationId, builder.build())
    }
}
