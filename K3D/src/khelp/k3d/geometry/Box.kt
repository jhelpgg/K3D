package khelp.k3d.geometry

import khelp.k3d.render.Object3D
import khelp.k3d.render.Vertex

class Box : Object3D()
{
    init
    {
        this.add(Vertex(-0.5f, 0.5f, 0.5f,
                        0f, 0f, -1f,
                        0f, 0f))
        this.add(Vertex(0.5f, 0.5f, 0.5f,
                        0f, 0f, -1f,
                        1f, 0f))
        this.add(Vertex(0.5f, -0.5f, 0.5f,
                        0f, 0f, -1f,
                        1f, 1f))
        this.add(Vertex(-0.5f, -0.5f, 0.5f,
                        0f, 0f, -1f,
                        0f, 1f))

        this.nextFace()
        this.add(Vertex(-0.5f, 0.5f, -0.5f,
                        0f, -1f, 0f,
                        1f, 1f))
        this.add(Vertex(0.5f, 0.5f, -0.5f,
                        0f, -1f, 0f,
                        0f, 1f))
        this.add(Vertex(0.5f, 0.5f, 0.5f,
                        0f, -1f, 0f,
                        0f, 0f))
        this.add(Vertex(-0.5f, 0.5f, 0.5f,
                        0f, -1f, 0f,
                        1f, 0f))

        this.nextFace()
        this.add(Vertex(0.5f, -0.5f, 0.5f,
                        -1f, 0f, 0f,
                        0f, 1f))
        this.add(Vertex(0.5f, 0.5f, 0.5f,
                        -1f, 0f, 0f,
                        0f, 0f))
        this.add(Vertex(0.5f, 0.5f, -0.5f,
                        -1f, 0f, 0f,
                        1f, 0f))
        this.add(Vertex(0.5f, -0.5f, -0.5f,
                        -1f, 0f, 0f,
                        1f, 1f))

        this.nextFace()
        this.add(Vertex(-0.5f, -0.5f, -0.5f,
                        0f, 0f, 1f,
                        1f, 1f))
        this.add(Vertex(0.5f, -0.5f, -0.5f,
                        0f, 0f, 1f,
                        0f, 1f))
        this.add(Vertex(0.5f, 0.5f, -0.5f,
                        0f, 0f, 1f,
                        0f, 0f))
        this.add(Vertex(-0.5f, 0.5f, -0.5f,
                        0f, 0f, 1f,
                        1f, 0f))

        this.nextFace()
        this.add(Vertex(-0.5f, -0.5f, 0.5f,
                        0f, 1f, 0f,
                        0f, 0f))
        this.add(Vertex(0.5f, -0.5f, 0.5f,
                        0f, 1f, 0f,
                        1f, 0f))
        this.add(Vertex(0.5f, -0.5f, -0.5f,
                        0f, 1f, 0f,
                        1f, 1f))
        this.add(Vertex(-0.5f, -0.5f, -0.5f,
                        0f, 1f, 0f,
                        0f, 1f))

        this.nextFace()
        this.add(Vertex(-0.5f, -0.5f, -0.5f,
                        1f, 0f, 0f,
                        0f, 1f))
        this.add(Vertex(-0.5f, 0.5f, -0.5f,
                        1f, 0f, 0f,
                        0f, 0f))
        this.add(Vertex(-0.5f, 0.5f, 0.5f,
                        1f, 0f, 0f,
                        1f, 0f))
        this.add(Vertex(-0.5f, -0.5f, 0.5f,
                        1f, 0f, 0f,
                        1f, 1f))
    }
}