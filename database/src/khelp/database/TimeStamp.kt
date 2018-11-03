package khelp.database

import java.util.GregorianCalendar

class TimeStamp
{
    val timeInMilliseconds: Long
    val milliseconds: Int
    val seconds: Int
    val minutes: Int
    val hours: Int
    val day: Int
    val month: Int
    val year: Int

    constructor(timeInMilliseconds: Long)
    {
        this.timeInMilliseconds = timeInMilliseconds
        val calendar = GregorianCalendar()
        calendar.timeInMillis = this.timeInMilliseconds
        this.milliseconds = calendar.get(GregorianCalendar.MILLISECOND)
        this.seconds = calendar.get(GregorianCalendar.SECOND)
        this.minutes = calendar.get(GregorianCalendar.MINUTE)
        this.hours = calendar.get(GregorianCalendar.HOUR_OF_DAY)
        this.day = calendar.get(GregorianCalendar.DAY_OF_MONTH)
        this.month = calendar.get(GregorianCalendar.MONTH) + 1
        this.year = calendar.get(GregorianCalendar.YEAR)
    }

    constructor(year: Int = 1970, month: Int = 1, day: Int = 1,
                hours: Int = 0, minutes: Int = 0, seconds: Int = 0, milliseconds: Int = 0)
    {
        val calendar = GregorianCalendar(year, month - 1, day, hours, minutes, seconds)
        calendar.set(GregorianCalendar.MILLISECOND, milliseconds)
        this.timeInMilliseconds = calendar.timeInMillis
        this.milliseconds = calendar.get(GregorianCalendar.MILLISECOND)
        this.seconds = calendar.get(GregorianCalendar.SECOND)
        this.minutes = calendar.get(GregorianCalendar.MINUTE)
        this.hours = calendar.get(GregorianCalendar.HOUR_OF_DAY)
        this.day = calendar.get(GregorianCalendar.DAY_OF_MONTH)
        this.month = calendar.get(GregorianCalendar.MONTH) + 1
        this.year = calendar.get(GregorianCalendar.YEAR)
    }

    operator fun plus(timeStamp: TimeStamp) =
            TimeStamp(this.timeInMilliseconds + timeStamp.timeInMilliseconds)

    operator fun minus(timeStamp: TimeStamp) =
            TimeStamp(this.timeInMilliseconds - timeStamp.timeInMilliseconds)

    operator fun plus(elapsedTime: ElapsedTime) =
            TimeStamp(this.timeInMilliseconds + elapsedTime.timeInMilliseconds)

    operator fun minus(elapsedTime: ElapsedTime) =
            TimeStamp(this.timeInMilliseconds - elapsedTime.timeInMilliseconds)
}

fun now() = TimeStamp(GregorianCalendar().timeInMillis)

class ElapsedTime(val days: Int = 0, val hours: Int = 0, val minutes: Int = 0, val seconds: Int = 0,
                  val milliseconds: Int = 0)
{
    constructor(timeInMilliseconds: Long) : this(days = (timeInMilliseconds / (24L * 60L * 60L * 1000L)).toInt(),
                                                 hours = ((timeInMilliseconds / (60L * 60L * 1000L)) % 24L).toInt(),
                                                 minutes = ((timeInMilliseconds / (60L * 1000L)) % 60L).toInt(),
                                                 seconds = ((timeInMilliseconds / 1000L) % 60L).toInt(),
                                                 milliseconds = (timeInMilliseconds % 1000L).toInt())

    val timeInMilliseconds =
            (((this.days * 24L + this.hours) * 60L + this.minutes) * 60L + this.seconds) * 1000L + this.milliseconds

    operator fun plus(elapsedTime: ElapsedTime) =
            ElapsedTime(this.timeInMilliseconds + elapsedTime.timeInMilliseconds)

    operator fun minus(elapsedTime: ElapsedTime) =
            ElapsedTime(this.timeInMilliseconds - elapsedTime.timeInMilliseconds)
}