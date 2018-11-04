package khelp.database

import khelp.text.between
import khelp.text.createBasicCharactersInterval
import khelp.text.format
import khelp.text.group
import khelp.text.plus
import khelp.text.regex
import khelp.text.regexIn
import khelp.text.zeroOrOne
import java.util.GregorianCalendar
import kotlin.math.max

val YEAR_GROUP = ('-'.regex().zeroOrOne() + createBasicCharactersInterval('0', '9').regexIn().between(1, 4)).group()
val MONTH_GROUP = createBasicCharactersInterval('0', '9').regexIn().between(1, 2).group()
val DAY_GROUP = createBasicCharactersInterval('0', '9').regexIn().between(1, 2).group()
val HOURS_GROUP = createBasicCharactersInterval('0', '9').regexIn().between(1, 2).group()
val MINUTES_GROUP = createBasicCharactersInterval('0', '9').regexIn().between(1, 2).group()
val SECONDS_GROUP = createBasicCharactersInterval('0', '9').regexIn().between(1, 2).group()
val MILLISECONDS_GROUP = createBasicCharactersInterval('0', '9').regexIn().between(1, 3).group()
val TIMESTAMP_REGEX =
        YEAR_GROUP +
                ('/'.regex() + MONTH_GROUP +
                        ('/'.regex() + DAY_GROUP +
                                (':'.regex() + HOURS_GROUP + 'h'.regex() +
                                        (MINUTES_GROUP + 'm'.regex() +
                                                (SECONDS_GROUP + 's'.regex() +
                                                        MILLISECONDS_GROUP.zeroOrOne()
                                                        ).zeroOrOne()
                                                ).zeroOrOne()
                                        ).zeroOrOne()
                                ).zeroOrOne()
                        ).zeroOrOne()
val ELAPSED_TIME_REGEX =
        DAY_GROUP +
                ('D'.regex() + HOURS_GROUP + 'h'.regex() +
                        (MINUTES_GROUP + 'm'.regex() +
                                (SECONDS_GROUP + 's'.regex() +
                                        MILLISECONDS_GROUP.zeroOrOne()
                                        ).zeroOrOne()
                                ).zeroOrOne()
                        ).zeroOrOne()

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

    constructor(date: String)
    {
        val matcher = TIMESTAMP_REGEX.matcher(date)
        val calendar =
                if (matcher.matches())
                {
                    var year = matcher.group(1)?.toInt() ?: 1970
                    var month = matcher.group(2)?.toInt() ?: 1
                    var day = matcher.group(3)?.toInt() ?: 1
                    var hours = matcher.group(4)?.toInt() ?: 0
                    var minutes = matcher.group(5)?.toInt() ?: 0
                    var seconds = matcher.group(6)?.toInt() ?: 0
                    var milliseconds = matcher.group(7)?.toInt() ?: 0
                    val cal = GregorianCalendar(year, month - 1, day, hours, minutes, seconds)
                    cal.set(GregorianCalendar.MILLISECOND, milliseconds)
                    cal
                }
                else
                {
                    try
                    {
                        val cal = GregorianCalendar()
                        cal.timeInMillis = date.toLong()
                        cal
                    }
                    catch (ignored: Exception)
                    {
                        val cal = GregorianCalendar(1970, 0, 1, 0, 0, 0)
                        cal.set(GregorianCalendar.MILLISECOND, 0)
                        cal
                    }
                }

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

    override fun toString() =
            "${this.year}/${this.month.format(2)}/${this.day.format(2)}:${this.hours.format(2)}h${this.minutes.format(
                    2)}m${this.seconds.format(2)}s${this.milliseconds.format(3)}"
}

/**TimeStamp at exact moment of calling the method*/
fun now() = TimeStamp(GregorianCalendar().timeInMillis)

/**
 * Represents a duration
 */
class ElapsedTime
{
    val days: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val milliseconds: Int
    /**Total time in milliseconds*/
    val timeInMilliseconds: Long

    constructor(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0, milliseconds: Int = 0)
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
        this.timeInMilliseconds =
                (((this.days * 24L + this.hours) * 60L + this.minutes) * 60L + this.seconds) * 1000L + this.milliseconds
    }

    constructor(timeInMilliseconds: Long)
    {
        this.timeInMilliseconds = max(0L, timeInMilliseconds)
        this.days = (this.timeInMilliseconds / (24L * 60L * 60L * 1000L)).toInt()
        this.hours = ((this.timeInMilliseconds / (60L * 60L * 1000L)) % 24L).toInt()
        this.minutes = ((this.timeInMilliseconds / (60L * 1000L)) % 60L).toInt()
        this.seconds = ((this.timeInMilliseconds / 1000L) % 60L).toInt()
        this.milliseconds = (this.timeInMilliseconds % 1000L).toInt()
    }

    constructor(time: String)
    {
        var days = 0
        var hours = 0
        var minutes = 0
        var seconds = 0
        var milliseconds = 0
        val matcher = ELAPSED_TIME_REGEX.matcher(time)

        if (matcher.matches())
        {
            days = matcher.group(1)?.toInt() ?: 0
            hours = matcher.group(2)?.toInt() ?: 0
            minutes = matcher.group(3)?.toInt() ?: 0
            seconds = matcher.group(4)?.toInt() ?: 0
            milliseconds = matcher.group(5)?.toInt() ?: 0
        }

        this.days = days
        this.hours = hours
        this.minutes = minutes
        this.seconds = seconds
        this.milliseconds = milliseconds
        this.timeInMilliseconds =
                (((this.days * 24L + this.hours) * 60L + this.minutes) * 60L + this.seconds) * 1000L + this.milliseconds
    }

    operator fun plus(elapsedTime: ElapsedTime) =
            ElapsedTime(this.timeInMilliseconds + elapsedTime.timeInMilliseconds)

    operator fun minus(elapsedTime: ElapsedTime) =
            ElapsedTime(this.timeInMilliseconds - elapsedTime.timeInMilliseconds)

    override fun toString() =
            "${this.days}D${this.hours.format(2)}h${this.minutes.format(2)}m${this.seconds.format(
                    2)}s${this.milliseconds.format(3)}"
}