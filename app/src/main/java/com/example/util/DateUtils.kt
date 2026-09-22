package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val BANGKOK_TZ = TimeZone.getTimeZone("Asia/Bangkok")

    /**
     * Calculates Work Date in Asia/Bangkok timezone (format: YYYY-MM-DD).
     * Never relies on server/device default timezone.
     */
    fun getWorkDate(timestampMs: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = BANGKOK_TZ
        return sdf.format(Date(timestampMs))
    }

    /**
     * Formats workDate into Thai friendly text: e.g. "21 กันยายน 2569" / "21 ก.ย. 2026"
     */
    fun formatThaiDate(workDateStr: String): String {
        return try {
            val parts = workDateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: 2026
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                val thaiMonths = arrayOf(
                    "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
                    "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
                )
                val mStr = if (month in 1..12) thaiMonths[month - 1] else parts[1]
                "$day $mStr $year"
            } else {
                workDateStr
            }
        } catch (e: Exception) {
            workDateStr
        }
    }

    fun formatThaiDateTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy HH:mm น.", Locale("th", "TH"))
        sdf.timeZone = BANGKOK_TZ
        return sdf.format(Date(timestampMs))
    }

    fun formatThaiTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm น.", Locale("th", "TH"))
        sdf.timeZone = BANGKOK_TZ
        return sdf.format(Date(timestampMs))
    }
}
