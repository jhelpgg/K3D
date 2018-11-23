package khelp.math

import khelp.thread.parallel
import khelp.util.HashCode
import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicInteger

/**
 * Matrix that paralleling in thread long operation, so multiplication, take adjacent, take inverse are fast.
 *
 * For determinant, the used algorithm is already fast
 */
class Matrix(private val width: Int, private val height: Int)
{
    companion object
    {
        /**
         * Precision used in computing
         */
        const val PRECISION = 1e-5

        /**
         * Indicates if 2 real are enough near to be considered as equals
         *
         * @param real1 First
         * @param real2 Second
         * @return `true` if equals
         */
        fun equals(real1: Double, real2: Double) = Math.abs(real1 - real2) <= Matrix.PRECISION

        /**
         * Indicates if a real is enough small to be consider as zero
         *
         * @param real Real to test
         * @return `true` if consider as zero
         */
        fun isNul(real: Double) = Math.abs(real) <= Matrix.PRECISION
    }

    /**
     * Parameters for adjacent task
     * @param count Number task left
     * @param adjacent Adjacent matrix where write
     * @param sign Sign factor
     * @param index Adjacent computed cell index
     * @param x Adjacent cell X
     * @param height Matrix height
     */
    internal class TaskAdjacent(val count: AtomicInteger,
                                val adjacent: DoubleArray,
                                val sign: Double,
                                val index: Int,
                                val x: Int)

    /**
     * Compute an adjacent cell
     */
    private val taskAdjacent: (TaskAdjacent) -> Unit =
            {
                var index = it.index
                var sign = it.sign
                // Compute adjacent line
                for (y in 0 until this.height)
                {
                    it.adjacent[index++] = sign * this.determinantSubMatrix(it.x, y)
                    sign *= -1.0
                }

                // Task is done, if its last one say its finish
                synchronized(it.count)
                {
                    if (it.count.decrementAndGet() <= 0)
                    {
                        (it.count as Object).notify()
                    }
                }
            }

    /**
     * Parameters for multiplication task
     * @param count Number task left
     * @param x Cell X
     * @param y Cell Y
     * @param firstWidth First matrix width
     * @param secondWidth Second matrix width
     * @param resultWidth Result matrix width
     * @param result Result matrix data
     * @param first First matrix data
     * @param second Second matrix data
     */
    internal class TaskMultiplicationCell(val count: AtomicInteger,
                                          val x: Int,
                                          val y: Int,
                                          val firstWidth: Int,
                                          val secondWidth: Int,
                                          val resultWidth: Int,
                                          val result: DoubleArray,
                                          val first: DoubleArray,
                                          val second: DoubleArray)

    /**
     * Multiplication task
     */
    private val taskMultiplicationCell: (TaskMultiplicationCell) -> Unit =
            {
                var indexFirst = it.y * it.firstWidth
                var indexSecond = it.x
                var res = 0.0

                // Compute cell value
                for (i in 0 until it.firstWidth)
                {
                    res += it.first[indexFirst] * it.second[indexSecond]
                    indexFirst++
                    indexSecond += it.secondWidth
                }

                it.result[it.x + it.y * it.resultWidth] = res;

                // The task is done, if its the last one, say its finish
                synchronized(it.count)
                {
                    if (it.count.decrementAndGet() <= 0)
                    {
                        (it.count as Object).notify()
                    }
                }
            }

    /**Matrix size*/
    private val size: Int
    /**Matrix data*/
    private val matrix: DoubleArray
    /**Indicates if determinant is known*/
    private var determinantKnown: Boolean
    /**Matrix determinant, have meaning only if [determinantKnown] is **`true`** */
    private var determinant: Double

    init
    {
        if (this.width < 1 || this.height < 1)
        {
            throw IllegalArgumentException("Width and height must be > 0 not " + this.width + "x" + this.height)
        }

        this.size = this.width * this.height
        this.matrix = DoubleArray(this.size)
        this.determinantKnown = this.width == this.height
        this.determinant = 0.0
    }

    /**
     * Check if position is inside the matrix
     *
     * @param x X
     * @param y Y
     * @throws IllegalArgumentException If position outside the matrix
     */
    private fun check(x: Int, y: Int)
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
        {
            throw IllegalArgumentException(
                    "x must be in [0, " + this.width +
                            "[ and y in [0, " + this.height +
                            "[ not (" + x + ", " + y + ")")
        }
    }

    /**
     * Compute the determinant (Here we have already checked that determinant need to be computed and matrix is square)
     *
     * @return Determinant
     */
    private fun determinantInternal(): Double
    {
        if (this.width == 1)
        {
            this.determinantKnown = true
            this.determinant = this.matrix[0]
            return this.determinant
        }

        if (this.width == 2)
        {
            this.determinantKnown = true
            this.determinant = this.matrix[0] * this.matrix[3] - this.matrix[2] * this.matrix[1]
            return this.determinant
        }

        return this.determinantInternalMore2()
    }

    /**
     * Compute determinant for matrix bigger than 2x2
     *
     * @return Computed determinant
     */
    private fun determinantInternalMore2(): Double
    {
        val work = this.copy()
        var determinant = 1.0
        var y2: Int
        var diagonal = 0
        val more = this.width + 1
        var diagonalValue: Double

        for (y in 0 until this.height)
        {
            y2 = y
            diagonalValue = work.matrix[diagonal]

            while (Matrix.isNul(diagonalValue))
            {
                y2++

                if (y2 >= this.height)
                {
                    this.determinantKnown = true
                    this.determinant = 0.0
                    return 0.0
                }

                determinant *= -1.0
                work.interExchangeRowInternal(y, y2)
                diagonalValue = work.matrix[diagonal]
            }

            determinant *= diagonalValue
            work.pivotInternal(y)
            diagonal += more
        }

        this.determinantKnown = true
        this.determinant = determinant
        return determinant
    }

    /**
     * Exchange 2 row.
     *
     * The determinant not change except by the sign (y1 and y2 are inside matrix and different)
     *
     * @param y1 First row
     * @param y2 Second row
     */
    private fun interExchangeRowInternal(y1: Int, y2: Int)
    {
        val line1 = y1 * this.width
        val line2 = y2 * this.width
        val temp = DoubleArray(this.width)
        System.arraycopy(this.matrix, line1, temp, 0, this.width)
        System.arraycopy(this.matrix, line2, this.matrix, line1, this.width)
        System.arraycopy(temp, 0, this.matrix, line2, this.width)
        this.determinant *= -1.0
    }

    /**
     * Pivot matrix from one diagonal point
     *
     * @param xy X and Y coordinate of diagonal point
     */
    private fun pivotInternal(xy: Int)
    {
        val startY = xy * this.width
        var y = startY + this.width
        var destination: Int
        var source: Int
        var coefficient: Double
        val div = this.matrix[startY + xy]

        if (Matrix.isNul(div))
        {
            return
        }

        for (yy in xy + 1 until this.height)
        {
            coefficient = this.matrix[y + xy]

            if (!Matrix.isNul(coefficient))
            {
                destination = y
                source = startY
                coefficient /= div

                for (xx in 0 until this.width)
                {
                    this.matrix[destination++] -= coefficient * this.matrix[source++]
                }
            }

            y += this.width
        }

        this.determinantKnown = false
    }

    /**
     * Extract a sub matrix by remove one column and one row
     *
     * @param removedColumn Column to remove
     * @param removedRow    Row to remove
     * @return Extracted matrix
     */
    private fun subMatrix(removedColumn: Int, removedRow: Int): Matrix
    {
        val w = this.width - 1
        val result = Matrix(w, this.height - 1)
        var lineSource = 0
        var lineDestination = 0
        val x = removedColumn + 1
        val left = w - removedColumn

        for (y in 0 until removedRow)
        {
            System.arraycopy(this.matrix, lineSource, result.matrix, lineDestination, removedColumn)
            System.arraycopy(this.matrix, lineSource + x, result.matrix, lineDestination + removedColumn, left)
            lineSource += this.width
            lineDestination += w
        }

        lineSource += this.width

        for (y in removedRow + 1 until this.height)
        {
            System.arraycopy(this.matrix, lineSource, result.matrix, lineDestination, removedColumn)
            System.arraycopy(this.matrix, lineSource + x, result.matrix, lineDestination + removedColumn, left)
            lineSource += this.width
            lineDestination += w
        }

        result.determinantKnown = false
        return result
    }

    /**
     * Compute the determinant of a sub matrix (The matrix with one column and one row removed)
     *
     * @param x Column to remove
     * @param y Row to remove
     * @return Determinant computed
     */
    private fun determinantSubMatrix(x: Int, y: Int): Double
    {
        if (this.width == 2)
        {
            return this.matrix[1 - x + (1 - y) * this.width]
        }

        if (this.width == 3)
        {
            when (x)
            {
                0 ->
                    when (y)
                    {
                        0 -> return this.matrix[4] * this.matrix[8] - this.matrix[5] * this.matrix[7]
                        1 -> return this.matrix[1] * this.matrix[8] - this.matrix[2] * this.matrix[7]
                        2 -> return this.matrix[1] * this.matrix[5] - this.matrix[2] * this.matrix[4]
                    }
                1 ->
                    when (y)
                    {
                        0 -> return this.matrix[3] * this.matrix[8] - this.matrix[5] * this.matrix[6]
                        1 -> return this.matrix[0] * this.matrix[8] - this.matrix[2] * this.matrix[6]
                        2 -> return this.matrix[0] * this.matrix[5] - this.matrix[2] * this.matrix[3]
                    }
                2 ->
                    when (y)
                    {
                        0 -> return this.matrix[3] * this.matrix[7] - this.matrix[4] * this.matrix[6]
                        1 -> return this.matrix[0] * this.matrix[7] - this.matrix[1] * this.matrix[6]
                        2 -> return this.matrix[0] * this.matrix[4] - this.matrix[1] * this.matrix[3]
                    }
            }
        }

        return this.subMatrix(x, y).determinantInternalMore2()
    }

    /**
     * Addition wit an other matrix
     *
     * @param matrix Matrix to add
     * @throws IllegalArgumentException If given matrix haven't same width and same height as this matrix
     */
    fun addition(matrix: Matrix)
    {
        if (this.width != matrix.width || this.height != matrix.height)
        {
            throw IllegalArgumentException("Matrix must have same size !")
        }

        for (i in 0 until this.size)
        {
            this.matrix[i] += matrix.matrix[i]
        }

        this.determinantKnown = false
    }

    /**
     * Addition wit an other matrix
     *
     * @param matrix Matrix to add
     * @throws IllegalArgumentException If given matrix haven't same width and same height as this matrix
     */
    operator fun plusAssign(matrix: Matrix) = this.addition(matrix)

    /**
     * Addition wit an other matrix
     *
     * @param matrix Matrix to add
     * @return result matrix
     * @throws IllegalArgumentException If given matrix haven't same width and same height as this matrix
     */
    operator fun plus(matrix: Matrix): Matrix
    {
        val copy = this.copy()
        copy += matrix
        return copy
    }

    /**
     * Compute adjacent matrix
     *
     * @return Adjacent matrix
     * @throws IllegalStateException If matrix is not square
     */
    fun adjacent(): Matrix
    {
        if (this.width != this.height)
        {
            throw IllegalStateException("Adjacent only for square matrix")
        }

        if (this.width == 1)
        {
            return this.copy()
        }

        if (this.width == 2)
        {
            val adjacent = Matrix(2, 2)
            adjacent.matrix[0] = this.matrix[3]
            adjacent.matrix[1] = -this.matrix[1]
            adjacent.matrix[2] = -this.matrix[2]
            adjacent.matrix[3] = this.matrix[0]
            adjacent.determinantKnown = this.determinantKnown
            adjacent.determinant = this.determinant
            return adjacent
        }

        val adjacent = Matrix(this.width, this.height)
        var index = 0
        var signMain = 1.0
        val count = AtomicInteger(this.width)

        for (x in 0 until this.width)
        {
            this.taskAdjacent.parallel(TaskAdjacent(count, adjacent.matrix, signMain, index, x))
            signMain *= -1.0
            index += this.width
        }


        synchronized(count)
        {
            while (count.get() > 0)
            {
                try
                {
                    (count as Object).wait()
                }
                catch (ignored: Exception)
                {
                }
            }
        }

        adjacent.determinantKnown = this.determinantKnown
        adjacent.determinant = Math.pow(this.determinant, (this.width - 1).toDouble())
        return adjacent
    }

    /**
     * Indicates if matrix can be invert
     *
     * @return `true` If matrix can be invert
     */
    fun canBeInvert(): Boolean
    {
        return this.isSquare() && !Matrix.isNul(this.determinant())
    }

    /**
     * Copy the matrix
     *
     * @return Matrix copy
     */
    fun copy(): Matrix
    {
        val matrix = Matrix(this.width, this.height)
        System.arraycopy(this.matrix, 0, matrix.matrix, 0, this.size)
        matrix.determinantKnown = this.determinantKnown
        matrix.determinant = this.determinant
        return matrix
    }

    /**
     * Compute matrix determinant
     *
     * @return Matrix determinant
     * @throws IllegalStateException If matrix is not square
     */
    fun determinant(): Double
    {
        if (this.width != this.height)
        {
            throw IllegalStateException("Matrix must be square")
        }

        if (this.determinantKnown)
        {
            return this.determinant
        }

        return this.determinantInternal()
    }

    /**
     * Obtain a matrix cell
     *
     * @param x X
     * @param y Y
     * @return Cell value
     * @throws IllegalArgumentException If coordinate outside the matrix
     */
    operator fun get(x: Int, y: Int): Double
    {
        this.check(x, y)
        return this.matrix[x + y * this.width]
    }

    /**
     * Matrix width
     *
     * @return Matrix width
     */
    fun width() = this.width

    /**
     * Matrix height
     *
     * @return Matrix height
     */
    fun height() = this.height

    /**
     * Matrix hash code
     * @return Matrix hash code
     * @see Object.hashCode
     */
    override fun hashCode(): Int
    {
        return HashCode.computeHashCode(this.width, this.height, this.matrix)
    }

    /**
     * Compare the matrix to an other
     * @param other Object to compare with
     * @return `true` if other is equals to this matrix
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null)
        {
            return false
        }

        if (other !is Matrix)
        {
            return false
        }

        if (this.height != other.height)
        {
            return false
        }

        if (this.width != other.width)
        {
            return false
        }

        return (0 until this.size).all { Matrix.equals(this.matrix[it], other.matrix[it]) }
    }

    /**
     * String representation
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        val stringBuilder = StringBuilder("Matrix ")
        stringBuilder.append(this.width)
        stringBuilder.append('x')
        stringBuilder.append(this.height)

        if (this.determinantKnown)
        {
            stringBuilder.append(" determinant=")
            stringBuilder.append(this.determinant)
        }

        stringBuilder.append(" : ")
        var w = 0
        val texts = Array(this.size, { "" })
        val numberFormat = NumberFormat.getInstance()
        numberFormat.minimumFractionDigits = 5
        numberFormat.maximumFractionDigits = 5
        var length: Int

        for (i in 0 until this.size)
        {
            texts[i] = numberFormat.format(this.matrix[i])
            length = texts[i].length
            w = Math.max(length, w)
        }

        for (i in 0 until this.size)
        {
            if (i % this.width == 0)
            {
                stringBuilder.append("\n     ")
            }

            length = texts[i].length

            for (s in length until w)
            {
                stringBuilder.append(' ')
            }

            stringBuilder.append(texts[i])
            stringBuilder.append(' ')
        }

        return stringBuilder.toString()
    }

    /**
     * Exchange two matrix row
     *
     * @param y1 First row
     * @param y2 Second row
     * @throws IllegalArgumentException If at least one specified rows is outside the matrix
     */
    fun interExchangeRow(y1: Int, y2: Int)
    {
        if (y1 < 0 || y1 >= this.height || y2 < 0 || y2 >= this.height)
        {
            throw IllegalArgumentException(
                    "It is a " + this.width + "x" + this.height + " matrix, but y1=" + y1 + " and y2=" + y2)
        }

        if (y1 == y2)
        {
            return
        }

        this.interExchangeRowInternal(y1, y2)
    }

    /**
     * Invert the matrix
     *
     * @return Matrix invert
     * @throws IllegalStateException    If the matrix is not square
     * @throws IllegalArgumentException If the matrix determinant is zero
     */
    fun invert(): Matrix
    {
        if (this.width != this.height)
        {
            throw IllegalStateException("Only square matrix can be invert")
        }

        val determinant = this.determinant()

        if (Matrix.isNul(determinant))
        {
            throw IllegalArgumentException("Matrix determinant is zero, so have no invert")
        }

        val invert = this.adjacent()

        for (i in 0 until this.size)
        {
            invert.matrix[i] /= determinant
        }

        invert.determinantKnown = true
        invert.determinant = 1 / determinant

        return invert
    }

    /**
     * Indicates if matrix is the identity matrix
     *
     * @return `true` if matrix is the identity one
     */
    fun isIdentity(): Boolean
    {
        val diagonal = this.width + 1
        val max = this.width * this.width

        for (i in 0 until this.size)
        {
            if (i < max && i % diagonal == 0)
            {
                if (!Matrix.equals(1.0, this.matrix[i]))
                {
                    return false
                }
            }
            else if (!Matrix.isNul(this.matrix[i]))
            {
                return false
            }
        }

        this.determinantKnown = this.width == this.height
        this.determinant = 1.0
        return true
    }

    /**
     * Indicates if matrix is square
     *
     * @return `true` if matrix is square
     */
    fun isSquare() = this.width == this.height

    /**
     * Indicates if matrix is zero matrix
     *
     * @return `true` if matrix is zero matrix
     */
    fun isZero(): Boolean
    {
        for (i in 0 until this.size)
        {
            if (!Matrix.isNul(this.matrix[i]))
            {
                return false
            }
        }

        this.determinantKnown = this.width == this.height
        this.determinant = 0.0
        return true
    }

    /**
     * Multiply all matrix cell by a value
     *
     * @param factor Value to multiply with
     */
    fun multiplication(factor: Double)
    {
        for (i in 0 until this.size)
        {
            this.matrix[i] *= factor
        }

        this.determinant *= Math.pow(factor, this.width.toDouble())
    }

    /**
     * Multiply all matrix cell by a value
     *
     * @param factor Value to multiply with
     */
    operator fun timesAssign(factor: Double) = this.multiplication(factor)

    /**
     * Multiply all matrix cell by a value
     *
     * @param factor Value to multiply with
     * @return multiplied matrix
     */
    operator fun times(factor: Double): Matrix
    {
        val copy = this.copy()
        copy *= factor
        return copy
    }

    /**
     * Multiply by an other matrix
     *
     * @param matrix Matrix to multiply with
     * @return Matrix result
     * @throws IllegalArgumentException if given matrix width isn't this matrix height OR given matrix height isn't this
     * matrix width
     */
    fun multiplication(matrix: Matrix): Matrix
    {
        if (this.width != matrix.height || this.height != matrix.width)
        {
            throw IllegalArgumentException(
                    "The multiplied matrix must have size : " + this.height + "x" + this.width)
        }

        val result = Matrix(matrix.width, this.height)
        val count = AtomicInteger(result.size)

        for (y in 0 until this.height)
        {
            for (x in 0 until matrix.width)
            {
                this.taskMultiplicationCell.parallel(
                        TaskMultiplicationCell(count, x, y, this.width, matrix.width, matrix.width, result.matrix,
                                               this.matrix, matrix.matrix))
            }
        }


        synchronized(count)
        {
            while (count.get() > 0)
            {
                try
                {
                    (count as Object).wait()
                }
                catch (ignored: Exception)
                {
                }

            }
        }

        if (this.determinantKnown && matrix.determinantKnown)
        {
            result.determinantKnown = true
            result.determinant = this.determinant * matrix.determinant
        }
        else
        {
            result.determinantKnown = false
        }

        return result
    }

    /**
     * Multiply by an other matrix
     *
     * @param matrix Matrix to multiply with
     * @return Matrix result
     * @throws IllegalArgumentException if given matrix width isn't this matrix height OR given matrix height isn't this
     * matrix width
     */
    operator fun times(matrix: Matrix) = this.multiplication(matrix)

    /**
     * Pivot matrix from one diagonal
     *
     * @param xy X and Y diagonal coordinate
     * @throws IllegalArgumentException If diagonal outside the matrix
     */
    fun pivot(xy: Int)
    {
        if (xy < 0 || xy >= this.width || xy >= this.height)
        {
            throw IllegalArgumentException("It is a " + this.width + "x" + this.height + " matrix, but xy=" + xy)
        }

        if (Matrix.isNul(this.matrix[xy + xy * this.width]))
        {
            return
        }

        this.pivotInternal(xy)
    }

    /**
     * Change a cell value
     *
     * @param x     X
     * @param y     Y
     * @param value new value
     * @throws IllegalArgumentException If coordinate outside the matrix
     */
    operator fun set(x: Int, y: Int, value: Double)
    {
        this.check(x, y)
        this.matrix[x + y * this.width] = value
        this.determinantKnown = false
    }

    /**
     * Push list of values inside the matrix, from up left, left to right, then up to down.
     *
     * If not enough value are given, the rest of the matrix is fill by 0.
     *
     * If to much value is given, the matrix is fill and other values are ignored
     *
     * @param values Values for fill the matrix
     */
    fun setValues(vararg values: Double)
    {
        val nb = Math.min(this.size, values.size)
        System.arraycopy(values, 0, this.matrix, 0, nb)

        for (i in nb until this.size)
        {
            this.matrix[i] = 0.0
        }

        this.determinantKnown = false
    }

    /**
     * Subtract a matrix to current one
     *
     * @param matrix Matrix to subtract
     * @throws IllegalArgumentException If given matrix haven't this matrix size
     */
    fun subtraction(matrix: Matrix)
    {
        if (this.width != matrix.width || this.height != matrix.height)
        {
            throw IllegalArgumentException("Matrix must have same size !")
        }

        for (i in 0 until this.size)
        {
            this.matrix[i] -= matrix.matrix[i]
        }

        this.determinantKnown = false
    }

    /**
     * Subtract a matrix to current one
     *
     * @param matrix Matrix to subtract
     * @throws IllegalArgumentException If given matrix haven't this matrix size
     */
    operator fun minusAssign(matrix: Matrix) = this.subtraction(matrix)

    /**
     * Subtract a matrix to current one
     *
     * @param matrix Matrix to subtract
     * @return subtracted matrix
     * @throws IllegalArgumentException If given matrix haven't this matrix size
     */
    operator fun minus(matrix: Matrix): Matrix
    {
        val copy = this.copy()
        copy -= matrix
        return copy
    }

    /**
     * Opposite of each value of the matrix
     * @return Negative matrix
     */
    operator fun unaryMinus(): Matrix = this * -1.0

    /**
     * Transform to matrix to identity one
     */
    fun toIdentity()
    {
        this.toZero()

        val number = this.width * Math.min(this.width, this.height)

        var p = 0
        while (p < number)
        {
            this.matrix[p] = 1.0
            p += this.width + 1
        }

        this.determinantKnown = this.width == this.height
        this.determinant = 1.0
    }

    /**
     * Transform this matrix to zero matrix (Matrix fill of zero)
     */
    fun toZero()
    {
        for (i in 0 until this.size)
        {
            this.matrix[i] = 0.0
        }

        this.determinantKnown = this.width == this.height
        this.determinant = 0.0
    }

    /**
     * Compute transpose matrix
     *
     * @return Transpose matrix
     */
    fun transpose(): Matrix
    {
        val transpose = Matrix(this.height, this.width)
        var index = 0
        var pos: Int

        for (y in 0 until this.height)
        {
            pos = y
            for (x in 0 until this.width)
            {
                transpose.matrix[pos] = this.matrix[index++]
                pos += this.height
            }
        }

        transpose.determinantKnown = this.determinantKnown
        transpose.determinant = this.determinant

        return transpose
    }
}