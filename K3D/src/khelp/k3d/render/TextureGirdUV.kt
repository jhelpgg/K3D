package khelp.k3d.render

import khelp.util.ColorInt
import java.awt.Color
import java.awt.Font
import java.awt.Polygon

/**
 * Texture with a gird computed with [Mesh]/[Object3D] UV polygons ([createGird]).
 *
 * This texture can be used to see how UV are applied to an object.
 * @param name Texture unique name
 * @param width Texture width
 * @param height Texture height
 */
class TextureGirdUV(name: String, width: Int, height: Int) : Texture(name, width, height, khelp.util.WHITE)
{
    /**
     * Shape description
     * @param polygon Shape polygon
     */
    internal class Shape(internal val polygon: Polygon)
    {
        /**
         * Color to fill the shape
         */
        var color: ColorInt = 0
    }

    /**Grid shapes*/
    private val shapes = ArrayList<Shape>()
    /**Background color*/
    var backgroundColor = Color.WHITE
        private set

    /**
     * Change background color
     */
    fun backgroundColor(color: Color)
    {
        this.backgroundColor = color
        this.refreshShapes()
    }

    /**Border color*/
    var borderColor = Color.BLACK
        private set

    /**
     * Change border color
     */
    fun borderColor(color: Color)
    {
        this.borderColor = color
        this.refreshShapes()
    }

    /**
     * Obtain a shape's color
     * @param shape Shape index
     * @return Shape color
     */
    fun colorOnShape(shape: Int) = this.shapes[shape].color

    /**
     * Change color on a shape
     * @param shape Shape index
     * @param color New color
     */
    fun colorOnShape(shape: Int, color: ColorInt)
    {
        this.shapes[shape].color = color
        this.refreshShapes()
    }

    /**
     * Create grid from mesh
     *
     * @param mesh Mesh to "extract" grid
     */
    fun createGird(mesh: Mesh)
    {
        this.shapes.clear()
        mesh.obtainUVshapes(this.shapes, this.width, this.height)
        this.refreshShapes()
    }

    /**
     * Create grid from 3D object
     */
    fun createGird(object3D: Object3D) = this.createGird(object3D.mesh)

    /**
     * Number of shape in the grid
     */
    fun numberOfShape() = this.shapes.size

    /**
     * Obtain a shape for a position
     *
     * @param x X
     * @param y Y
     * @return Shape index under the position or -1
     */
    fun obtainShape(x: Int, y: Int): Int
    {
        val nb = this.shapes.size

        for (i in 0 until nb)
        {
            if (this.shapes[i].polygon.contains(x, y))
            {
                return i
            }
        }

        return -1
    }

    /**
     * Refresh shapes drawing
     */
    fun refreshShapes()
    {
        this.fillRect(0, 0, this.width, this.height, this.backgroundColor, false)
        val font = Font("Courier", Font.BOLD, 12)
        var index = 0

        for (shape in this.shapes)
        {
            this.fill(shape.polygon, Color(shape.color, true), true)
            this.draw(shape.polygon, this.borderColor, false, 1)
            val bounds = shape.polygon.bounds
            this.drawString(bounds.x + (bounds.width shr 3), bounds.y + (bounds.height shr 3), index.toString(),
                            this.borderColor, font, true, 5)
            index++
        }

        this.flush()
    }
}