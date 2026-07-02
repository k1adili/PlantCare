package ir.plantcare.app.util

import java.util.Calendar
import java.util.TimeZone

/**
 * تبدیل تقویم میلادی <-> شمسی (بدون هیچ وابستگی بیرونی).
 * الگوریتم استاندارد و رایج تبدیل تقویم (مبتنی بر شمارش روزهای ژولیوسی).
 * ذخیره‌سازی تاریخ‌ها همیشه به صورت epoch millis (میلادی) انجام می‌شود
 * و این کلاس فقط برای نمایش/ورودی شمسی استفاده می‌شود.
 */
object JalaliCalendar {

    private val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val weekDayNames = arrayOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه")

    private fun div(a: Int, b: Int): Int {
        return Math.floorDiv(a, b)
    }

    private fun isGregorianLeap(gy: Int): Boolean {
        return (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
    }

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): IntArray {
        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo = 365 * gy + div(gy + 3, 4) - div(gy + 99, 100) + div(gy + 399, 400)
        for (i in 0 until gm) gDayNo += gDaysInMonth[i]
        if (gm > 1 && isGregorianLeap(gYear)) gDayNo++
        gDayNo += gd

        var jDayNo = gDayNo - 79

        val jNp = div(jDayNo, 12053)
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * div(jDayNo, 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += div(jDayNo - 1, 365)
            jDayNo = (jDayNo - 1) % 365
        }

        var i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        val jm = i + 1
        val jd = jDayNo + 1

        return intArrayOf(jy, jm, jd)
    }

    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): IntArray {
        val jy = jYear - 979
        val jm = jMonth - 1
        val jd = jDay - 1

        var jDayNo = 365 * jy + div(jy, 33) * 8 + div((jy % 33) + 3, 4)
        for (i in 0 until jm) jDayNo += jDaysInMonth[i]
        jDayNo += jd

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * div(gDayNo, 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * div(gDayNo, 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) gDayNo++ else leap = false
        }

        gy += 4 * div(gDayNo, 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += div(gDayNo, 365)
            gDayNo %= 365
        }

        var i = 0
        var gd = gDayNo
        while (gd >= gDaysInMonth[i] + (if (i == 1 && leap) 1 else 0)) {
            gd -= gDaysInMonth[i] + (if (i == 1 && leap) 1 else 0)
            i++
        }
        val gm = i + 1
        val gDay = gd + 1

        return intArrayOf(gy, gm, gDay)
    }

    private val tz: TimeZone = TimeZone.getTimeZone("Asia/Tehran")

    fun toJalali(epochMillis: Long): IntArray {
        val cal = Calendar.getInstance(tz)
        cal.timeInMillis = epochMillis
        return gregorianToJalali(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    fun toEpochMillis(jYear: Int, jMonth: Int, jDay: Int, hour: Int = 0, minute: Int = 0): Long {
        val g = jalaliToGregorian(jYear, jMonth, jDay)
        val cal = Calendar.getInstance(tz)
        cal.clear()
        cal.set(g[0], g[1] - 1, g[2], hour, minute, 0)
        return cal.timeInMillis
    }

    fun now(): IntArray = toJalali(System.currentTimeMillis())

    fun format(epochMillis: Long, withMonthName: Boolean = true): String {
        val j = toJalali(epochMillis)
        return if (withMonthName) {
            "${j[2]} ${monthNames[j[1] - 1]} ${j[0]}"
        } else {
            "%04d/%02d/%02d".format(j[0], j[1], j[2])
        }
    }

    fun daysInJalaliMonth(jYear: Int, jMonth: Int): Int {
        if (jMonth <= 6) return 31
        if (jMonth <= 11) return 30
        // اسفند: بررسی کبیسه با تبدیل روز ۳۰ به میلادی و مقایسه با فروردین سال بعد
        val g1 = jalaliToGregorian(jYear, 12, 30)
        val g2 = jalaliToGregorian(jYear + 1, 1, 1)
        val cal1 = Calendar.getInstance(tz).apply { clear(); set(g1[0], g1[1] - 1, g1[2]) }
        val cal2 = Calendar.getInstance(tz).apply { clear(); set(g2[0], g2[1] - 1, g2[2]) }
        val diffDays = (cal2.timeInMillis - cal1.timeInMillis) / (24 * 3600 * 1000)
        return if (diffDays == 1L) 30 else 29
    }

    fun addDaysToMillis(epochMillis: Long, days: Int): Long {
        return epochMillis + days.toLong() * 24 * 3600 * 1000
    }
}
