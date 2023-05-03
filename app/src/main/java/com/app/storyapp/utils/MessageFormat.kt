package com.app.storyapp.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object MessageFormat {
    fun dateCreated(dateString: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString.toString()) ?: return ""
        val now = Calendar.getInstance()
        val dateTime = Calendar.getInstance().apply {
            timeInMillis = date.time
        }
        val timeDiff = now.timeInMillis - dateTime.timeInMillis

        return when {
            timeDiff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            timeDiff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(timeDiff)} minutes ago"
            timeDiff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(timeDiff)} hours ago"
            timeDiff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(timeDiff)} days ago"
            else -> {
                val years = timeDiff / (1000L * 60 * 60 * 24 * 365)
                if (years > 0) "$years year${if (years > 1) "s" else ""} ago" else ""
            }
        }
    }
}