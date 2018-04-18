package khelp.k3d.render

import java.util.Collections

data class Triangle(public val first: Vertex = Vertex(), public val second: Vertex = Vertex(),
                    public val third: Vertex = Vertex())

class Triangles
{
    private val triangles = ArrayList<Triangle>()

    fun addTriangle(triangle: Triangle) = this.triangles.add(triangle)
    fun addTriangle(first: Vertex, second: Vertex, third: Vertex) = this.triangles.add(Triangle(first, second, third))
    /**
     * Add a convex polygon in traingle set
     *
     * @param polygon Points of the convex polygon
     */
    fun convertInTriangles(vararg polygon: Vertex)
    {
        if (polygon.size < 3)
        {
            return
        }

        val length = polygon.size
        val first = polygon[0]

        for (i in 2 until length)
        {
            this.triangles.add(Triangle(first, polygon[i - 1], polygon[i]))
        }
    }

    fun obtainTriangleList() = Collections.unmodifiableList(this.triangles)
}