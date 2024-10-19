package com.tgarbus.posturecheck.data

import android.os.Bundle
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

  fun isToday(): Boolean {
    val checkCalendar = Calendar.getInstance()
    checkCalendar.timeInMillis = this.millis
    val todayCalendar = Calendar.getInstance()
    todayCalendar.timeInMillis = System.currentTimeMillis()
    return checkCalendar.get(Calendar.DATE) == todayCalendar.get(Calendar.DATE) &&
      checkCalendar.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
      checkCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
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

  companion object {
    fun fromMillis(millis: Long): TimeOfDay {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = millis
      return TimeOfDay(
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
      )
    }
  }
}