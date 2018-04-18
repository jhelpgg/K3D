package khelp.k3d.render

import java.awt.Color
import java.awt.Polygon

class TextureGirdUV(name: String, width: Int, height: Int) : Texture(name, width, height, 0xFFFFFFFF.toInt())
{
    /**
     * Shape description <br></br>
     *
     * Constructs Shape
     */
    internal class Shape(internal val polygon: Polygon)
    {
        /**
         * Color to fill
         */
        var color: Int = 0
    }

    private val shapes = ArrayList<Shape>()
    var backgroundColor = Color.WHITE
        set(value)
        {
            this.backgroundColor = value
            this.refreshShapes()
        }
    var borderColor = Color.BLACK
        set(value)
        {
            this.borderColor = value
            this.refreshShapes()
        }

    fun colorOnShape(shape:Int) = this.shapes[shape].color

    fun colorOnShape(shape:Int,color:Int)
    {
        this.shapes[shape].color = color
        this.refreshShapes()
    }

    /**
     * Create grid from mesh
     *
     * @param mesh Mesh to "extract" grid
     */
    fun createGird( mesh: Mesh)
    {
        this.shapes.clear()
         mesh.obtainUVshapes(this.shapes, this.width, this.height)
        this.refreshShapes()
    }

    fun createGird(object3D:Object3D) = this.createGird(object3D.mesh)

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

        for (shape in this.shapes)
        {
            this.draw(shape.polygon, this.borderColor, false, 1)
            this.fill(shape.polygon, Color(shape.color, true), true)
        }

        this.flush()
    }
}