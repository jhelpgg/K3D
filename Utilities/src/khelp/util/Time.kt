package khelp.util

import khelp.text.StringExtractor
import khelp.text.indexOfFirstString

typealias Milliseconds = Long

val NOW: Milliseconds get() = System.currentTimeMillis()
val Number.milliseconds: Milliseconds get() = this.toLong()
val ONE_MILLISECOND = 1.milliseconds

val Double.seconds: Milliseconds get() = (this * 1000.0).toLong()
val Float.seconds: Milliseconds get() = (this * 1000.0f).toLong()
val Number.seconds: Milliseconds get() = this.toLong() * 1000L
val ONE_SECOND = 1.seconds

val Double.minutes: Milliseconds get() = (this * 60.0 * 1000.0).toLong()
val Float.minutes: Milliseconds get() = (this * 60.0f * 1000.0f).toLong()
val Number.minutes: Milliseconds get() = this.toLong() * 60L * 1000L
val ONE_MINUTE = 1.minutes

val Double.hours: Milliseconds get() = (this * 60.0 * 60.0 * 1000.0).toLong()
val Float.hours: Milliseconds get() = (this * 60.0f * 60.0f * 1000.0f).toLong()
val Number.hours: Milliseconds get() = this.toLong() * 60L * 60L * 1000L
val ONE_HOUR = 1.hours

val Double.days: Milliseconds get() = (this * 24.0 * 60.0 * 60.0 * 1000.0).toLong()
val Float.days: Milliseconds get() = (this * 24.0f * 60.0f * 60.0f * 1000.0f).toLong()
val Number.days: Milliseconds get() = this.toLong() * 24L * 60L * 60L * 1000L
val ONE_DAY = 1.days

val Milliseconds.longString: String
    get() =
        {
            val stringBuilder = StringBuilder()
            var time = this
            var printed = false
            val append =
                    { reference: Milliseconds, name: String ->
                        if (printed)
                        {
                            stringBuilder.append(", ")
                        }

                        printed = true
                        val factor = time / reference
                        stringBuilder.append(factor)
                        stringBuilder.append(" $name${if (factor > 1) "s" else ""}")
                        time = time % reference
                    }

            if (time >= ONE_DAY)
            {
                append(ONE_DAY, "day")
            }

            if (printed || time >= ONE_HOUR)
            {
                append(ONE_HOUR, "hour")
            }

            if (printed || time >= ONE_MINUTE)
            {
                append(ONE_MINUTE, "minute")
            }

            if (printed || time >= ONE_SECOND)
            {
                append(ONE_SECOND, "second")
            }

            if (printed || time >= ONE_MILLISECOND)
            {
                append(ONE_MILLISECOND, "millisecond")
            }

            stringBuilder.toString()
        }()

val Milliseconds.shortString: String
    get() =
        {
            val stringBuilder = StringBuilder()
            var time = this
            var printed = 0
            val append =
                    { reference: Milliseconds, name: String ->
                        val factor = time / reference

                        if (factor > 0)
                        {
                            if (printed > 0)
                            {
                                stringBuilder.append(", ")
                            }

                            stringBuilder.append(factor)
                            stringBuilder.append(" $name${if (factor > 1) "s" else ""}")
                            time = time % reference
                        }

                        printed++
                    }

            if (time >= ONE_DAY)
            {
                append(ONE_DAY, "day")
            }

            if (printed > 0 || time >= ONE_HOUR)
            {
                append(ONE_HOUR, "hour")
            }

            if (printed < 2 && (printed > 0 || time >= ONE_MINUTE))
            {
                append(ONE_MINUTE, "minute")
            }

            if (printed < 2 && (printed > 0 || time >= ONE_SECOND))
            {
                append(ONE_SECOND, "second")
            }

            if (printed < 2 && (printed > 0 || time >= ONE_MILLISECOND))
            {
                append(ONE_MILLISECOND, "millisecond")
            }

            stringBuilder.toString()
        }()

val Milliseconds.compactString: String
    get() =
        {
            val stringBuilder = StringBuilder()
            var time = this
            var printed = false
            val append =
                    { reference: Milliseconds, name: String ->
                        if (printed)
                        {
                            stringBuilder.append(' ')
                        }

                        printed = true
                        val factor = time / reference
                        stringBuilder.append(factor)
                        stringBuilder.append(name)
                        time = time % reference
                    }

            if (time >= ONE_DAY)
            {
                append(ONE_DAY, "d")
            }

            if (time >= ONE_HOUR)
            {
                append(ONE_HOUR, "h")
            }

            if (time >= ONE_MINUTE)
            {
                append(ONE_MINUTE, "m")
            }

            if (time >= ONE_SECOND)
            {
                append(ONE_SECOND, "s")
            }

            if (time >= ONE_MILLISECOND)
            {
                append(ONE_MILLISECOND, "ms")
            }

            stringBuilder.toString()
        }()

private val TIME_LIMITS = arrayOf("millisecond", "second", "minute", "hour", "day", "ms", "s", "m", "h", "d")

val String.time
    get() : Milliseconds
    {
        val stringExtractor = StringExtractor(this, " \n\t\r,;|./+*-:#~")
        var time = 0L
        var part = stringExtractor.next()
        var alreadyEnd = false

        while (part != null)
        {
            val pair = part.indexOfFirstString(TIME_LIMITS, 0, true)
            var left = part

            if (pair != null)
            {
                try
                {
                    val value = part.substring(0, pair.first).toLong()

                    when (pair.second)
                    {
                        "millisecond" -> time += value.milliseconds
                        "second"      -> time += value.seconds
                        "minute"      -> time += value.minutes
                        "hour"        -> time += value.hours
                        "day"         -> time += value.days
                        "ms"          -> time += value.milliseconds
                        "s"           -> time += value.seconds
                        "m"           -> time += value.minutes
                        "h"           -> time += value.hours
                        "d"           -> time += value.days
                    }

                    left = ""
                }
                catch (ignored: Exception)
                {
                }
            }

            if (alreadyEnd)
            {
                part = null
            }
            else
            {
                val follow = stringExtractor.next()
                part = left + (if (follow != null) follow else "")
                alreadyEnd = follow == null
            }
        }

        return time
    }