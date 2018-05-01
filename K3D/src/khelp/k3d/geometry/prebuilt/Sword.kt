package khelp.k3d.geometry.prebuilt

import khelp.k3d.geometry.Revolution
import khelp.k3d.geometry.Sphere
import khelp.k3d.render.Color4f
import khelp.k3d.render.LIGHT_GRAY
import khelp.k3d.render.Material
import khelp.k3d.render.Node
import khelp.k3d.render.Object3D
import khelp.k3d.render.ObjectClone
import khelp.k3d.render.Point2D
import khelp.k3d.render.Vertex
import khelp.math.limit
import khelp.util.ORANGE
import java.util.concurrent.atomic.AtomicInteger

class Sword(size: Float = 3.3f) : Node()
{
    companion object
    {
        internal val NEXT_ID = AtomicInteger(0)
    }

    val id = Sword.NEXT_ID.getAndIncrement()
    val baseMaterial = Material("SwordBase${this.id}")
    val bladeMaterial = Material("SwordBlade${this.id}")

    init
    {
        var size = limit(size, 2f, 5f)
        this.baseMaterial.colorDiffuse(Color4f(ORANGE))
        this.bladeMaterial.colorDiffuse(LIGHT_GRAY)

        val base = Revolution()
        base.appendLine(Point2D(0.1f, 0.4f), 0f, Point2D(0.1f, -0.4f), 1f)
        base.refreshRevolution(0f, 1f)
        base.material(this.baseMaterial)
        this.addChild(base)

        val sphere = Sphere(5, 5)
        sphere.scale(0.12f)
        sphere.position(0f, -0.4f, 0f)
        sphere.material(this.baseMaterial)
        this.addChild(sphere)

        val guard = Revolution()
        guard.appendQuadratic(Point2D(0.1f, 0.4f), 0f,
                              Point2D(0.2f, 0f), 0.5f,
                              Point2D(0.1f, -0.4f), 1f)
        guard.refreshRevolution(0f, 1f)
        guard.angleX(90f)
        guard.position(0f, 0.4f, 0f)
        guard.material(this.baseMaterial)
        this.addChild(guard)

        var clone = ObjectClone(sphere)
        clone.scale(0.12f)
        clone.position(0f, 0.4f, -0.4f)
        clone.material(this.baseMaterial)
        this.addChild(clone)

        clone = ObjectClone(sphere)
        clone.scale(0.12f)
        clone.position(0f, 0.4f, 0.4f)
        clone.material(this.baseMaterial)
        this.addChild(clone)

        val z = Math.min(size / 10f, 0.39f)
        val blade = Object3D()
        blade.add(Vertex(0.1f, 0.4f, 0f, 0f, 0f, -1f, 0.5f, 1f))
        blade.add(Vertex(0f, 0.4f, z, 0f, 0f, -1f, 1f, 1f))
        blade.add(Vertex(0f, size, 0f, 0f, 0f, -1f, 0.5f, 0f))
        blade.nextFace()
        blade.add(Vertex(0f, size, 0f, 0f, 0f, -1f, 0.5f, 0f))
        blade.add(Vertex(0f, 0.4f, z, 0f, 0f, -1f, 1f, 1f))
        blade.add(Vertex(-0.1f, 0.4f, 0f, 0f, 0f, -1f, 0.5f, 1f))
        blade.nextFace()
        blade.add(Vertex(0f, size, 0f, 0f, 0f, 1f, 0.5f, 0f))
        blade.add(Vertex(0f, 0.4f, -z, 0f, 0f, 1f, 0f, 1f))
        blade.add(Vertex(0.1f, 0.4f, 0f, 0f, 0f, 1f, 0.5f, 1f))
        blade.nextFace()
        blade.add(Vertex(-0.1f, 0.4f, 0f, 0f, 0f, 1f, 0.5f, 1f))
        blade.add(Vertex(0f, 0.4f, -z, 0f, 0f, 1f, 0f, 1f))
        blade.add(Vertex(0f, size, 0f, 0f, 0f, 1f, 0.5f, 0f))
        blade.material(this.bladeMaterial)
        this.addChild(blade)
    }

    fun positionForBack()
    {
        this.angleX(180f)
        this.angleY(90f)
        this.angleZ(0f)
        this.position(0f, -0.4f, -0.05f)
    }

    fun positionForHand()
    {
        this.angleX(-90f)
        this.angleY(90f)
        this.angleZ(0f)
        this.position(0f, 0f, 0f)
    }
}