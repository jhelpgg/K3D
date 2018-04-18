package khelp.k3d.render

import khelp.math.isNul

/**
 * Represents a rotation throw a quaternion
 * @param w Quaternion w
 * @param x Quaternion x
 * @param y Quaternion y
 * @param z Quaternion z
 */
class Rotf(private var w: Float = 1f, private var x: Float = 0f, private var y: Float = 0f, private var z: Float = 0f)
{
    companion object
    {
        /**Computing precision*/
        private val EPSILON = 1.0E-7f
    }

    /**
     * Create a rotation copy the given one
     * @param rotf Rotation to copy
     */
    constructor(rotf: Rotf) : this(rotf.w, rotf.x, rotf.y, rotf.z)

    /**
     * Create a rotation with a rotation axis and rotation angle
     * @param axis Rotation axis
     * @param angle Rotation angle in radian
     */
    constructor(axis: Vec3f, angle: Float) : this()
    {
        this.set(axis, angle)
    }

    /**
     * Create rotation with two vectors.
     *
     * The constructed rotation will be able to transform the **`source`** vector to the **`destination`** vector.
     *
     * So the rotation angle will be the angle between the **`source`** and the **`destination`**
     * and the axis will be the cross vector of the given ones.
     * @param source Vector source
     * @param destination Vector destination
     */
    constructor(source: Vec3f, destination: Vec3f) : this()
    {
        this.set(source, destination)
    }

    /**
     * Make the rotation to identity
     */
    fun identity()
    {
        this.w = 1f
        this.x = 0f
        this.y = 0f
        this.z = 0f
    }

    /**
     * Fill the rotation axis inside given vector and return the rotation angle
     * @param axis Vector to set with the rotation axis
     * @return Rotation angle in radian
     */
    fun get(axis: Vec3f): Float
    {
        val angle = (2.0 * Math.acos(this.w.toDouble())).toFloat();
        axis.set(this.x, this.y, this.z);
        val length = axis.length();

        if (isNul(length))
        {
            axis.set(0.0F, 0.0F, 1.0F);
        }
        else
        {
            axis.scale(1.0F / length);
        }

        return angle;
    }

    /**
     * Compute the opposite of rotation
     * @return Rotation opposite
     */
    operator fun unaryMinus(): Rotf
    {
        val inverted = Rotf(this)
        inverted.opposite()
        return inverted
    }

    /**
     * Make rotation to its opposite
     */
    fun opposite()
    {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
    }

    /**
     * Rotation length
     * @return Rotation length
     */
    fun length(): Float
    {
        return Math.sqrt(this.lengthSquared().toDouble()).toFloat()
    }

    /**
     * Rotation length squared
     * @return Rotation length squared
     */
    fun lengthSquared(): Float
    {
        return this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z
    }

    /**
     * Affect this rotation as the result of combination of two rotations (First then second).
     * @param rotation1 First rotation
     * @param rotation2 Second rotation
     */
    fun multiply(rotation1: Rotf, rotation2: Rotf)
    {
        this.w = rotation1.w * rotation2.w - rotation1.x * rotation2.x - rotation1.y * rotation2.y - rotation1.z * rotation2.z
        this.x = rotation1.w * rotation2.x + rotation1.x * rotation2.w + rotation1.y * rotation2.z - rotation1.z * rotation2.y
        this.y = rotation1.w * rotation2.y + rotation1.y * rotation2.w - rotation1.x * rotation2.z + rotation1.z * rotation2.x
        this.z = rotation1.w * rotation2.z + rotation1.z * rotation2.w + rotation1.x * rotation2.y - rotation1.y * rotation2.x
    }

    /**
     * Normalize the rotation
     */
    fun normalize()
    {
        val length = this.length()
        this.w /= length
        this.x /= length
        this.y /= length
        this.z /= length
    }

    /**
     * Rotate a vector by this rotation and fill the result in a given vector
     * @param toRotate Vector to rotate
     * @param rotationResult Vector where fill the rotation result
     * @return Rotation result
     */
    fun rotateVector(toRotate: Vec3f, rotationResult: Vec3f = Vec3f()): Vec3f
    {
        val axis = Vec3f(this.x, this.y, this.z)
        val cross = axis.cross(toRotate)
        val projected = cross.cross(axis)
        cross.scale(2.0f * this.w)
        projected.scale(-2.0f)
        rotationResult.add(toRotate, cross)
        rotationResult.add(rotationResult, projected)
        return rotationResult
    }

    /**
     * Copy a rotation
     * @param rotation Rotation to copy
     */
    fun copy(rotation: Rotf)
    {
        this.w = rotation.w;
        this.x = rotation.x;
        this.y = rotation.y;
        this.z = rotation.z;
    }

    /**
     * Become rotation of two vectors.
     *
     * The rotation will be able to transform the **`source`** vector to the **`destination`** vector.
     *
     * So the rotation angle will be the angle between the **`source`** and the **`destination`**
     * and the axis will be the cross vector of the given ones.
     * @param source Vector source
     * @param destination Vector destination
     */
    fun set(source: Vec3f, destination: Vec3f)
    {
        val axis = source.cross(destination)

        if (axis.lengthSquared() < Rotf.EPSILON)
        {
            this.identity()
        }
        else
        {
            var arcCosinus = source.dot(destination)
            val length = source.length() * destination.length()

            if (length < Rotf.EPSILON)
            {
                this.identity()
            }
            else
            {
                arcCosinus /= length
                this.set(axis, Math.acos(arcCosinus.toDouble()).toFloat())
            }
        }
    }

    /**
     * Become rotation with specified rotation axis and rotation angle
     * @param axis Rotation axis
     * @param angle Rotation angle in radian
     */
    fun set(axis: Vec3f, angle: Float)
    {
        val semiAngle = angle / 2.0F;
        this.w = Math.cos(semiAngle.toDouble()).toFloat();
        val sinus = Math.sin(semiAngle.toDouble()).toFloat();
        val axisCopy = Vec3f(axis);
        axisCopy.normalize();
        this.x = axisCopy.x * sinus;
        this.y = axisCopy.y * sinus;
        this.z = axisCopy.z * sinus;
    }

    /**
     * Combine this rotation with given one (this then given)
     * @param rotation Rotation to combine with
     * @return Combination result
     */
    operator fun times(rotation: Rotf): Rotf
    {
        val rotationResult = Rotf();
        rotationResult.multiply(this, rotation);
        return rotationResult;
    }
}