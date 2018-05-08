package khelp.k3d.render

import java.util.Collections

/**
 * Triangle is composed of 3 vertex
 * @param first First vertex
 * @param second Second vertex
 * @param third Third vertex
 */
data class Triangle(val first: Vertex = Vertex(), val second: Vertex = Vertex(), val third: Vertex = Vertex())

/**
 * Triangles list
 */
class Triangles
{
    /**Triangles list*/
    private val triangles = ArrayList<Triangle>()

    /**
     * Add a triangle
     * @param triangle Triangle to add
     */
    fun addTriangle(triangle: Triangle) = this.triangles.add(triangle)

    /**
     * Add triangle describes by it's vertex
     * @param first First vertex
     * @param second Second vertex
     * @param third Third vertex
     */
    fun addTriangle(first: Vertex, second: Vertex, third: Vertex) = this.triangles.add(Triangle(first, second, third))

    /**
     * Add a convex polygon in triangle set
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

    /**
     * List of triangles
     */
    fun obtainTriangleList(): List<Triangle> = Collections.unmodifiableList(this.triangles)
}