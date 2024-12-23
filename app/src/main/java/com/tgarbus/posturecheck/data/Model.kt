package com.tgarbus.posturecheck.data

import android.os.Bundle
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

data class PlannedPostureCheck (
  val id: String,
  val millis: Long,
) {
  constructor(millis: Long) : this(UUID.randomUUID().toString(), millis)

  fun toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString("id", this.id)
    bundle.putLong("millis", this.millis)
    return bundle
  }

  fun withReply(reply: PostureCheckReply): PastPostureCheck {
    return PastPostureCheck(
      planned = this,
      reply = reply
    )
  }

  fun notificationId(): Int {
    return (millis / 1000).toInt()
  }

  fun formatDate(sdf: SimpleDateFormat): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.millis
    return sdf.format(calendar.time)
  }

  fun getTimeOfDay(): TimeOfDay {
    return TimeOfDay.fromMillis(this.millis)
  }

  fun getDay(): Day {
    return Day.fromMillis(this.millis)
  }

  fun isToday(): Boolean {
    val checkCalendar = Calendar.getInstance()
    checkCalendar.timeInMillis = this.millis
    val todayCalendar = Calendar.getInstance()
    todayCalendar.timeInMillis = System.currentTimeMillis()
    return checkCalendar.get(Calendar.DATE) == todayCalendar.get(Calendar.DATE) &&
      checkCalendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
      checkCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
  }

  fun isInPast(): Boolean {
    val checkCalendar = Calendar.getInstance()
    checkCalendar.timeInMillis = this.millis
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = System.currentTimeMillis()
    return checkCalendar < nowCalendar
  }

  override fun toString(): String {
    return "PlannedPostureCheck(${formatDate(SimpleDateFormat("dd-MM-yy HH:mm:ss"))}, ${id})"
  }

  companion object {
    fun fromBundle(bundle: Bundle): PlannedPostureCheck {
      return PlannedPostureCheck(
        bundle.getString("id")!!,
        bundle.getLong("millis")
      )
    }
  }
}

enum class PostureCheckReply {
  NO_ANSWER,
  GOOD,
  BAD,
  NOT_APPLICABLE,
}

data class PastPostureCheck (
  val planned: PlannedPostureCheck,
  val reply: PostureCheckReply
) {
  fun toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString("id", this.planned.id)
    bundle.putLong("millis", this.planned.millis)
    bundle.putString("reply", this.reply.name)
    return bundle
  }

  fun withoutReply(): PlannedPostureCheck {
    return PlannedPostureCheck(
      id = this.planned.id,
      millis = this.planned.millis
    )
  }

  fun notificationId(): Int {
    return (planned.millis / 1000).toInt()
  }

  companion object {
    fun fromBundle(bundle: Bundle): PastPostureCheck {
      return PastPostureCheck(
        PlannedPostureCheck(
          bundle.getString("id")!!,
          bundle.getLong("millis")
        ),
        PostureCheckReply.valueOf(bundle.getString("reply")!!)
      )
    }
  }
}

data class TimeOfDay(
  val hour: Int,
  val minute: Int
): Comparable<TimeOfDay> {
  override fun compareTo(other: TimeOfDay): Int {
    return compareValuesBy(this, other, { it.hour }, { it.minute })
  }

  private fun nextMinute(): TimeOfDay {
    if (minute == 59) {
      if (hour == 23) {
        return TimeOfDay(0, 0)
      }
      return TimeOfDay(hour + 1, 0)
    }
    return TimeOfDay(hour, minute + 1)
  }

  fun rangeTo(end: TimeOfDay): Array<TimeOfDay> {
    var cur: TimeOfDay = this.copy(hour=hour, minute=minute)
    val res = ArrayList<TimeOfDay>()
    while (cur != end) {
      res.add(cur)
      cur = cur.nextMinute()
    }
    return res.toTypedArray()
  }

  fun toPreferencesStorageFormat(): Int {
    return 100 * hour + minute
  }

  override fun toString(): String {
    return "%02d:%02d".format(hour, minute)
  }

  companion object {
    fun fromMillis(millis: Long): TimeOfDay {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = millis
      return TimeOfDay(
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
      )
    }

    fun fromPreferencesStorageFormat(value: Int): TimeOfDay {
      return TimeOfDay(value / 100, value % 100)
    }
  }
}

data class Day(
  val dayOfMonth: Int,
  val month: Int,
  val year: Int,
): Comparable<Day> {

  override fun compareTo(other: Day): Int {
    return compareValuesBy(this, other, { it.year }, { it.month }, { it.dayOfMonth })
  }

  override fun toString(): String {
    return "${dayOfMonth}-${month + 1}-${year}"
  }

  fun toShortString(): String {
    return "${dayOfMonth}.${month + 1}"
  }

  private fun toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.YEAR, year)
    return calendar
  }

  fun toMillis(): Long {
    return toCalendar().timeInMillis
  }

  operator fun plus(days: Int): Day {
    val calendar = toCalendar()
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return fromMillis(calendar.timeInMillis)
  }

  operator fun minus(days: Int): Day {
    val calendar = toCalendar()
    calendar.add(Calendar.DAY_OF_MONTH, -days)
    return fromMillis(calendar.timeInMillis)
  }

  fun getDayOfWeek(): String {
    val cal = toCalendar()
    Log.d("tomek", "Debugging day of week: $this: $cal")
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
      Calendar.MONDAY -> "Mon"
      Calendar.TUESDAY -> "Tue"
      Calendar.WEDNESDAY -> "Wed"
      Calendar.THURSDAY -> "Thu"
      Calendar.FRIDAY -> "Fri"
      Calendar.SATURDAY -> "Sat"
      Calendar.SUNDAY -> "Sun"
      else -> "???"
    }
  }

  companion object {
    fun fromMillis(millis: Long): Day {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = millis
      return Day(
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.YEAR)
      )
    }

    fun parseString(str: String): Day {
      val components = str.split("-")
      return Day(
        dayOfMonth = components[0].toInt(),
        month = components[1].toInt() - 1,
        year = components[2].toInt()
      )
    }

    fun today(): Day {
      return fromMillis(System.currentTimeMillis())
    }

    fun range(firstDay: Day, lastDay: Day): ArrayList<Day> {
      // The range is inclusive on both ends.
      var day = firstDay
      val result = ArrayList<Day>()
      while (day <= lastDay) {
        result.add(day)
        day += 1
      }
      return result
    }
  }
}