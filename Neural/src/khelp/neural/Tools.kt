package khelp.neural

infix fun DoubleArray.DOT(doubleArray: DoubleArray): Double
{
    if (this.size != doubleArray.size)
    {
        throw IllegalArgumentException("Given array not same size as this array")
    }

    return (0 until this.size).sumByDouble { this[it] * doubleArray[it] }
}

operator fun DoubleArray.plus(double: Double): DoubleArray
{
    (0 until this.size).forEach { this[it] += double }
    return this
}