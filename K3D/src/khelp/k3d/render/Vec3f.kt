package khelp.k3d.render

import com.sun.javafx.geom.Vec3d

val NEG_X_AXIS = Vec3f(-1.0f, 0.0f, 0.0f)
val NEG_Y_AXIS = Vec3f(0.0f, -1.0f, 0.0f)
val NEG_Z_AXIS = Vec3f(0.0f, 0.0f, -1.0f)
val X_AXIS = Vec3f(1.0f, 0.0f, 0.0f)
val Y_AXIS = Vec3f(0.0f, 1.0f, 0.0f)
val Z_AXIS = Vec3f(0.0f, 0.0f, 1.0f)

class Vec3f(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f)
{
    constructor(vec3f: Vec3f) : this(vec3f.x, vec3f.y, vec3f.z)

    fun add(var1: Vec3f)
    {
        this.add(this, var1)
    }

    fun add(var1: Vec3f, var2: Vec3f)
    {
        this.x = var1.x + var2.x
        this.y = var1.y + var2.y
        this.z = var1.z + var2.z
    }

    fun addScaled(var1: Float, var2: Vec3f): Vec3f
    {
        val var3 = Vec3f()
        var3.addScaled(this, var1, var2)
        return var3
    }

    fun addScaled(var1: Vec3f, var2: Float, var3: Vec3f)
    {
        this.x = var1.x + var2 * var3.x
        this.y = var1.y + var2 * var3.y
        this.z = var1.z + var2 * var3.z
    }

    fun componentMul(var1: Vec3f)
    {
        this.x *= var1.x
        this.y *= var1.y
        this.z *= var1.z
    }

    fun copy(): Vec3f
    {
        return Vec3f(this)
    }

    fun cross(var1: Vec3f): Vec3f
    {
        val var2 = Vec3f()
        var2.cross(this, var1)
        return var2
    }

    fun cross(var1: Vec3f, var2: Vec3f)
    {
        this.x = var1.y * var2.z - var1.z * var2.y
        this.y = var1.z * var2.x - var1.x * var2.z
        this.z = var1.x * var2.y - var1.y * var2.x
    }

    fun dot(var1: Vec3f): Float
    {
        return this.x * var1.x + this.y * var1.y + this.z * var1.z
    }

    operator fun get(var1: Int): Float
    {
        when (var1)
        {
            0    -> return this.x
            1    -> return this.y
            2    -> return this.z
            else -> throw IndexOutOfBoundsException()
        }
    }

    fun length(): Float
    {
        return Math.sqrt(this.lengthSquared().toDouble()).toFloat()
    }

    fun lengthSquared(): Float
    {
        return this.dot(this)
    }

    fun minus(var1: Vec3f): Vec3f
    {
        var var2 = Vec3f();
        var2.sub(this, var1);
        return var2;
    }

    fun normalize()
    {
        val var1 = this.length()
        if (var1 != 0.0f)
        {
            this.scale(1.0f / var1)
        }
    }

    fun plus(var1: Vec3f): Vec3f
    {
        var var2 = Vec3f();
        var2.add(this, var1);
        return var2;
    }

    fun scale(var1: Float)
    {
        this.x *= var1;
        this.y *= var1;
        this.z *= var1;
    }

    fun set(var1: Vec3f)
    {
        this.set(var1.x, var1.y, var1.z);
    }

    fun set(var1: Float, var2: Float, var3: Float)
    {
        this.x = var1
        this.y = var2
        this.z = var3
    }

    operator fun set(var1: Int, var2: Float)
    {
        when (var1)
        {
            0    -> this.x = var2
            1    -> this.y = var2
            2    -> this.z = var2
            else -> throw IndexOutOfBoundsException()
        }
    }

    fun sub(var1: Vec3f)
    {
        this.sub(this, var1)
    }

    fun sub(var1: Vec3f, var2: Vec3f)
    {
        this.x = var1.x - var2.x
        this.y = var1.y - var2.y
        this.z = var1.z - var2.z
    }

    operator fun times(var1: Float): Vec3f
    {
        val var2 = Vec3f(this)
        var2.scale(var1)
        return var2
    }

    fun toDouble(): Vec3d
    {
        return Vec3d(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
    }
}