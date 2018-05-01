package khelp.k3d.geometry

import khelp.k3d.render.Object3D
import khelp.k3d.render.Vertex

/**
 * Describes how UV are put in a box face.
 *
 * The face is covered by the texture.
 * @param minU Left U on face
 * @param maxU Right U on face
 * @param minV Up V on face
 * @param maxV Down V on face
 */
class FaceUV(val minU: Float = 0f, val maxU: Float = 1f, val minV: Float = 0f, val maxV: Float = 1f)

/**
 * Describes how UV are put on each box face.
 *
 * By default texture is repeat on each face.
 * @param face Describes how UV are put on face
 * @param back Describes how UV are put on back
 * @param top Describes how UV are put on top
 * @param bottom Describes how UV are put on bottom
 * @param left Describes how UV are put on left
 * @param right Describes how UV are put on right
 */
open class BoxUV(val face: FaceUV = FaceUV(), val back: FaceUV = FaceUV(),
                 val top: FaceUV = FaceUV(), val bottom: FaceUV = FaceUV(),
                 val left: FaceUV = FaceUV(), val right: FaceUV = FaceUV())

/**
 * UV are place on cross like this:
 *
 *       0   u1  u2  1
 *     0 +---+---+---+
 *       | . | T | . |
 *     v1+---+---+---+
 *       | L | F | R |
 *     v2+---+---+---+
 *       | . | B | . |
 *     v3+---+---+---+
 *       | . | b | . |
 *     1 +---+---+---+
 *
 * Where:
 * * **T** : Applied on top face
 * * **L** : Applied on left face
 * * **F** : Applied on face face
 * * **R** : Applied on right face
 * * **B** : Applied on bottom face
 * * **b** : Applied on back face
 * * Other part of texture are not used
 * @param u1 U border between left and face
 * @param u2 U border between face and right
 * @param v1 V border between top and face
 * @param v2 V border between face and bottom
 * @param v3 V border between bottom and back
 */
class CrossUV(u1: Float = 1f / 3f, u2: Float = 2f / 3f,
              v1: Float = 0.25f, v2: Float = 0.5f, v3: Float = 0.75f) :
        BoxUV(face = FaceUV(u1, u2, v1, v2), back = FaceUV(u1, u2, v3, 1f),
              top = FaceUV(u1, u2, 0f, v1), bottom = FaceUV(u1, u2, v2, v3),
              left = FaceUV(0f, u1, v1, v2), right = FaceUV(u2, 1f, v1, v2))

/**
 * A regular box
 * @param boxUV Describes how UV are put on box
 */
class Box(boxUV: BoxUV = BoxUV()) : Object3D()
{
    init
    {
        // Face
        this.add(Vertex(-0.5f, 0.5f, 0.5f,
                        0f, 0f, -1f,
                        boxUV.face.minU, boxUV.face.minV))
        this.add(Vertex(0.5f, 0.5f, 0.5f,
                        0f, 0f, -1f,
                        boxUV.face.maxU, boxUV.face.minV))
        this.add(Vertex(0.5f, -0.5f, 0.5f,
                        0f, 0f, -1f,
                        boxUV.face.maxU, boxUV.face.maxV))
        this.add(Vertex(-0.5f, -0.5f, 0.5f,
                        0f, 0f, -1f,
                        boxUV.face.minU, boxUV.face.maxV))

        // Top
        this.nextFace()
        this.add(Vertex(-0.5f, 0.5f, -0.5f,
                        0f, -1f, 0f,
                        boxUV.top.minU, boxUV.top.minV))
        this.add(Vertex(0.5f, 0.5f, -0.5f,
                        0f, -1f, 0f,
                        boxUV.top.maxU, boxUV.top.minV))
        this.add(Vertex(0.5f, 0.5f, 0.5f,
                        0f, -1f, 0f,
                        boxUV.top.maxU, boxUV.top.maxV))
        this.add(Vertex(-0.5f, 0.5f, 0.5f,
                        0f, -1f, 0f,
                        boxUV.top.minU, boxUV.top.maxV))

        // Right
        this.nextFace()
        this.add(Vertex(0.5f, -0.5f, 0.5f,
                        -1f, 0f, 0f,
                        boxUV.right.minU, boxUV.right.maxV))
        this.add(Vertex(0.5f, 0.5f, 0.5f,
                        -1f, 0f, 0f,
                        boxUV.right.minU, boxUV.right.minV))
        this.add(Vertex(0.5f, 0.5f, -0.5f,
                        -1f, 0f, 0f,
                        boxUV.right.maxU, boxUV.right.minV))
        this.add(Vertex(0.5f, -0.5f, -0.5f,
                        -1f, 0f, 0f,
                        boxUV.right.maxU, boxUV.right.maxV))

        // Back
        this.nextFace()
        this.add(Vertex(-0.5f, -0.5f, -0.5f,
                        0f, 0f, 1f,
                        boxUV.back.maxU, boxUV.back.maxV))
        this.add(Vertex(0.5f, -0.5f, -0.5f,
                        0f, 0f, 1f,
                        boxUV.back.minU, boxUV.back.maxV))
        this.add(Vertex(0.5f, 0.5f, -0.5f,
                        0f, 0f, 1f,
                        boxUV.back.minU, boxUV.back.minV))
        this.add(Vertex(-0.5f, 0.5f, -0.5f,
                        0f, 0f, 1f,
                        boxUV.back.maxU, boxUV.back.minV))

        // Bottom
        this.nextFace()
        this.add(Vertex(-0.5f, -0.5f, 0.5f,
                        0f, 1f, 0f,
                        boxUV.bottom.minU, boxUV.bottom.minV))
        this.add(Vertex(0.5f, -0.5f, 0.5f,
                        0f, 1f, 0f,
                        boxUV.bottom.maxU, boxUV.bottom.minV))
        this.add(Vertex(0.5f, -0.5f, -0.5f,
                        0f, 1f, 0f,
                        boxUV.bottom.maxU, boxUV.bottom.maxV))
        this.add(Vertex(-0.5f, -0.5f, -0.5f,
                        0f, 1f, 0f,
                        boxUV.bottom.minU, boxUV.bottom.maxV))

        // Left
        this.nextFace()
        this.add(Vertex(-0.5f, -0.5f, -0.5f,
                        1f, 0f, 0f,
                        boxUV.left.minU, boxUV.left.maxV))
        this.add(Vertex(-0.5f, 0.5f, -0.5f,
                        1f, 0f, 0f,
                        boxUV.left.minU, boxUV.left.minV))
        this.add(Vertex(-0.5f, 0.5f, 0.5f,
                        1f, 0f, 0f,
                        boxUV.left.maxU, boxUV.left.minV))
        this.add(Vertex(-0.5f, -0.5f, 0.5f,
                        1f, 0f, 0f,
                        boxUV.left.maxU, boxUV.left.maxV))
    }
}