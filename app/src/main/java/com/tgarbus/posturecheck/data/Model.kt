package com.tgarbus.posturecheck.data

import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Calendar

data class PlannedPostureCheck (
  val id: String,
  val millis: Long,
) {
  fun toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString("id", this.id)
    bundle.putLong("millis", this.millis)
    return bundle
  }

  fun withReply(reply: PostureCheckReply): PastPostureCheck {
    return PastPostureCheck(
      id = this.id,
      millis = this.millis,
      reply = reply
    )
  }

  fun notificationId(): Int {
    return (millis / 1000).toInt()
  }

  fun formatDate(sdf: SimpleDateFormat): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.millis
    return sdf.format(calendar)
  }

  fun getTimeOfDay(): TimeOfDay {
    return TimeOfDay.fromMillis(this.millis)
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
  val id: String,
  val millis: Long,
  val reply: PostureCheckReply
) {
  fun toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString("id", this.id)
    bundle.putLong("millis", this.millis)
    bundle.putString("reply", this.reply.name)
    return bundle
  }

  fun withoutReply(): PlannedPostureCheck {
    return PlannedPostureCheck(
      id = this.id,
      millis = this.millis
    )
  }

  fun notificationId(): Int {
    return (millis / 1000).toInt()
  }

  companion object {
    fun fromBundle(bundle: Bundle): PastPostureCheck {
      return PastPostureCheck(
        bundle.getString("id")!!,
        bundle.getLong("millis"),
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