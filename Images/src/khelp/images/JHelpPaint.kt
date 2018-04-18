package khelp.images

/**
 * Paint for fill a shape.
 *
 * When the paint is used, first its called [JHelpPaint.initializePaint] to give to the paint information about size of
 * the bounding box of the shape it will be filled. Then for each pixel inside the shape it will call
 * [JHelpPaint.obtainColor] to know the color to use for a specific point inside the shape. Coordinate are relative to the
 * shape, in other words (0, 0) is the upper left corner of the bonding box of the shape
 */
interface JHelpPaint
{
    /**
     * Initialize the shape.<br></br>
     * It is called just before fill shape with this paint.
     *
     * @param width  Shape bounding box width
     * @param height Shape bounding box height
     */
    abstract fun initializePaint(width: Int, height: Int)

    /**
     * Compute color to use for a specific point.<br></br>
     * Coordinate are relative to the bounding box upper left corner
     *
     * @param x X of the pixel coordinate
     * @param y Y of the pixel coordinate
     * @return Computed color
     */
    abstract fun obtainColor(x: Int, y: Int): Int
}