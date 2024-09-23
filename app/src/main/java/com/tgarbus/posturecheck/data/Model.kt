package com.tgarbus.posturecheck.data

import android.os.Bundle

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
)