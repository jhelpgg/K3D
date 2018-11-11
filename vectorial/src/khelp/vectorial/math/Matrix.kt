package khelp.vectorial.math

import khelp.vectorial.shape.Point

class Matrix
{
    var determinant = 0.0
        private set
    /**
     * Matrix position 0,0
     */
    private var matrix00 = 0.0
    /**
     * Matrix position 0,1
     */
    private var matrix01 = 0.0
    /**
     * Matrix position 0,2
     */
    private var matrix02 = 0.0
    /**
     * Matrix position 1,0
     */
    private var matrix10 = 0.0
    /**
     * Matrix position 1,1
     */
    private var matrix11 = 0.0
    /**
     * Matrix position 1,2
     */
    private var matrix12 = 0.0
    /**
     * Matrix position 2,0
     */
    private var matrix20 = 0.0
    /**
     * Matrix position 2,1
     */
    private var matrix21 = 0.0
    /**
     * Matrix position 2,2
     */
    private var matrix22 = 0.0

    init
    {
        this.toIdentity()
    }

    fun toIdentity()
    {
        this.matrix00 = 1.0
        this.matrix10 = 0.0
        this.matrix20 = 0.0
        this.matrix01 = 0.0
        this.matrix11 = 1.0
        this.matrix21 = 0.0
        this.matrix02 = 0.0
        this.matrix12 = 0.0
        this.matrix22 = 1.0
        this.determinant = 1.0
    }

    fun toRotate(centerX: Double, centerY: Double, angleInRadian: Double)
    {
        /**
         * <pre>
         * x=centerX;
         * y=centerY;
         * c=cos(angleInRadian)
         * s=sin(angleInRadian)
         *
         *     |1 0 -x| | c s 0| |1 0 x|
         * R = |0 1 -y|x|-s c 0|x|0 1 y|
         *     |0 0  1| | 0 0 1| |0 0 1|
         *
         *     | c s -x| |1 0 x|
         * R = |-s c -y|x|0 1 y|
         *     | 0 0  1| |0 0 1|
         *
         *     | c s cx+sy-x|
         * R = |-s c cy-sx-y|
         *     | 0 0    1   |
         *
         * </pre>
         */
        val cos = Math.cos(angleInRadian)
        val sin = Math.sin(angleInRadian)
        this.matrix00 = cos
        this.matrix10 = sin
        this.matrix20 = cos * centerX + sin * centerY - centerX
        this.matrix01 = -sin
        this.matrix11 = cos
        this.matrix21 = cos * centerY - sin * centerX - centerY
        this.matrix02 = 0.0
        this.matrix12 = 0.0
        this.matrix22 = 1.0
        this.determinant = 1.0
    }

    fun transform(x: Double, y: Double) = Point(
            (this.matrix00 * x) + (this.matrix10 * y) + this.matrix20,
            (this.matrix01 * x) + (this.matrix11 * y) + this.matrix21)

}

fun obtainRotateMatrix(centerX: Double, centerY: Double, angleInRadian: Double): Matrix
{
    val matrix = Matrix()
    matrix.toRotate(centerX, centerY, angleInRadian)
    return matrix
}