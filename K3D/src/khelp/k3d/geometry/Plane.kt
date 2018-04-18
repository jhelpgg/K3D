package khelp.k3d.geometry

import khelp.debug.debug
import khelp.k3d.render.Object3D
import khelp.k3d.render.Point2D
import khelp.k3d.render.Point3D
import khelp.k3d.render.Vertex

class Plane(horizontal: Int = 1, vertical: Int = 1,
            val invertU: Boolean = false, val invertV: Boolean = false) : Object3D()
{
    val horizontal = Math.max(1, horizontal)
    val vertical = Math.max(1, vertical)

    init
    {
        val v = vertical.toFloat()
        val h = horizontal.toFloat()
        val vertex = Vertex(normal = Point3D(0f, 0f, -1f))

        // For each vertical part
        var y = 0
        var x: Int
        var xx: Float
        var yy: Float
        var xx1: Float
        var yy1: Float

        while (y < vertical)
        {
            // Compute Y
            yy = y / v
            yy1 = (y + 1f) / v

            // For each horizontal part
            x = 0

            while (x < horizontal)
            {
                // Compute X
                xx = x / h
                xx1 = (x + 1f) / h

                // Add the face
                vertex.position(Point3D(xx - 0.5f, yy - 0.5f, 0f))
                vertex.uv(this.makeUV(xx, yy, invertU, invertV))
                this.add(vertex)
                vertex.position(Point3D(xx - 0.5f, yy1 - 0.5f, 0f))
                vertex.uv(this.makeUV(xx, yy1, invertU, invertV))
                this.add(vertex)
                vertex.position(Point3D(xx1 - 0.5f, yy1 - 0.5f, 0f))
                vertex.uv(this.makeUV(xx1, yy1, invertU, invertV))
                this.add(vertex)
                vertex.position(Point3D(xx1 - 0.5f, yy - 0.5f, 0f))
                vertex.uv(this.makeUV(xx1, yy, invertU, invertV))
                this.add(vertex)
                //
                this.nextFace()
                x++
            }
            y++
        }

        this.reconstructTheList()
    }

    /**
     * Compute real UV
     *
     * @param u       Actual U
     * @param v       Actual V
     * @param invertU Indicates if U are inverted
     * @param invertV Indicates if V are inverted
     * @return Rel UV
     */
    private fun makeUV(u: Float, v: Float, invertU: Boolean, invertV: Boolean) =
            Point2D(if (invertU) 1f - u else u,
                    if (invertV) 1f - v else v)
}