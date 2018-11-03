package khelp.database

import java.util.GregorianCalendar
import kotlin.math.max

/**
 * Time stamp is a date in time
 */
class TimeStamp
{
    /**Time in milliseconds (Number of milliseconds since epoch)*/
    val timeInMilliseconds: Long
    /**Time milliseconds part*/
    val milliseconds: Int
    /**Time seconds part*/
    val seconds: Int
    /**Time minutes part*/
    val minutes: Int
    /**Time hours part*/
    val hours: Int
    /**Date day part*/
    val day: Int
    /**Date month part*/
    val month: Int
    /**Date year part*/
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

/**TimeStamp at exact moment of calling the method*/
fun now() = TimeStamp(GregorianCalendar().timeInMillis)

/**
 * Represents a duration
 */
class ElapsedTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0, milliseconds: Int = 0)
{
    val days: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val milliseconds: Int

    init
    {
        var temp = max(0, milliseconds)
        this.milliseconds = temp % 1000
        temp = max(0, seconds) + temp / 1000
        this.seconds = temp % 60
        temp = max(0, minutes) + temp / 60
        this.minutes = temp % 60
        temp = max(0, hours) + temp / 60
        this.hours = temp % 24
        temp = max(0, days) + temp / 24
        this.days = temp
    }

    constructor(timeInMilliseconds: Long) : this(days = (timeInMilliseconds / (24L * 60L * 60L * 1000L)).toInt(),
                                                 hours = ((timeInMilliseconds / (60L * 60L * 1000L)) % 24L).toInt(),
                                                 minutes = ((timeInMilliseconds / (60L * 1000L)) % 60L).toInt(),
                                                 seconds = ((timeInMilliseconds / 1000L) % 60L).toInt(),
                                                 milliseconds = (timeInMilliseconds % 1000L).toInt())

    /**Total time in milliseconds*/
    val timeInMilliseconds =
            (((this.days * 24L + this.hours) * 60L + this.minutes) * 60L + this.seconds) * 1000L + this.milliseconds

    operator fun plus(elapsedTime: ElapsedTime) =
            ElapsedTime(this.timeInMilliseconds + elapsedTime.timeInMilliseconds)

    operator fun minus(elapsedTime: ElapsedTime) =
            ElapsedTime(this.timeInMilliseconds - elapsedTime.timeInMilliseconds)
}