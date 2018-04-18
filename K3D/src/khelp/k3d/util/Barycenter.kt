package khelp.k3d.util

/**
 * Barycenter of several values
 */
class Barycenter
{
    /**Actual barycenter. Have meaning if at least one value was add*/
    var barycenter = 0.0
        private set
    /**Number points actually added*/
    private var count = 0
    /**Indicates if no point was added*/
    val empty: Boolean get() = this.count == 0

    /**
     * Add a value to the set
     *
     * @param value Value add
     */
    fun add(value: Double)
    {
        if (this.count == 0)
        {
            this.barycenter = value
            this.count = 1
            return
        }

        this.barycenter = (this.count * this.barycenter + value) / (this.count + 1.0)
        this.count++
    }
}