package khelp.k3d.render

class Vec4f(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f, var w: Float = 0f)
{
    constructor(vec4f: Vec4f) : this(vec4f.x, vec4f.y, vec4f.z, vec4f.w)

    fun add(var1: Vec4f)
    {
        this.add(this, var1)
    }

    fun add(var1: Vec4f, var2: Vec4f)
    {
        this.x = var1.x + var2.x
        this.y = var1.y + var2.y
        this.z = var1.z + var2.z
        this.w = var1.w + var2.w
    }

    fun addScaled(var1: Float, var2: Vec4f): Vec4f
    {
        val var3 = Vec4f()
        var3.addScaled(this, var1, var2)
        return var3
    }

    fun addScaled(var1: Vec4f, var2: Float, var3: Vec4f)
    {
        this.x = var1.x + var2 * var3.x
        this.y = var1.y + var2 * var3.y
        this.z = var1.z + var2 * var3.z
        this.w = var1.w + var2 * var3.w
    }

    fun componentMul(var1: Vec4f)
    {
        this.x *= var1.x
        this.y *= var1.y
        this.z *= var1.z
        this.w *= var1.w
    }

    fun copy(): Vec4f
    {
        return Vec4f(this)
    }

    fun dot(var1: Vec4f): Float
    {
        return this.x * var1.x + this.y * var1.y + this.z * var1.z + this.w * var1.w
    }

    operator fun get(var1: Int): Float
    {
        when (var1)
        {
            0    -> return this.x
            1    -> return this.y
            2    -> return this.z
            3    -> return this.w
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

    fun minus(var1: Vec4f): Vec4f
    {
        val var2 = Vec4f();
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

    fun plus(var1: Vec4f): Vec4f
    {
        val var2 = Vec4f();
        var2.add(this, var1);
        return var2;
    }

    fun scale(var1: Float)
    {
        this.x *= var1
        this.y *= var1
        this.z *= var1
        this.w *= var1
    }

    fun set(var1: Vec4f)
    {
        this.set(var1.x, var1.y, var1.z, var1.w);
    }

    fun set(var1: Float, var2: Float, var3: Float, var4: Float)
    {
        this.x = var1
        this.y = var2
        this.z = var3
        this.w = var4
    }

    operator fun set(var1: Int, var2: Float)
    {
        when (var1)
        {
            0    -> this.x = var2
            1    -> this.y = var2
            2    -> this.z = var2
            3    -> this.w = var2
            else -> throw IndexOutOfBoundsException()
        }
    }

    fun sub(var1: Vec4f)
    {
        this.sub(this, var1)
    }

    fun sub(var1: Vec4f, var2: Vec4f)
    {
        this.x = var1.x - var2.x
        this.y = var1.y - var2.y
        this.z = var1.z - var2.z
        this.w = var1.w - var2.w
    }

    operator fun times(var1: Float): Vec4f
    {
        val var2 = Vec4f(this)
        var2.scale(var1)
        return var2
    }
}