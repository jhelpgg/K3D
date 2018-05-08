package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.transferFloat
import org.lwjgl.opengl.GL11

/**
 * Represents a light.
 *
 * To create a light, have to use methods from the lights manager [Lights] get by [Window3D.lights]
 * @param name Light name
 * @param id Light ID
 */
class Light internal constructor(public val name: String, id: Int)
{
    companion object
    {
        /**
         * Light 0 name
         */
        val LIGHT_O = "LIGHT_0"
    }

    /**Light OpenGL ID*/
    private val id = GL11.GL_LIGHT0 + id

    /**Diffuse color*/
    private val diffuse = WHITE.copy()
    /**Ambient color*/
    private val ambient = BLACK.copy()
    /**Specular color*/
    private val specular = GRAY.copy()

    /**Indicates if light as changed*/
    private var asChanged = false
    /**Indicates if light is enable*/
    private var enable = false
    /**Indicates if light need to be refresh*/
    private var needRefresh = false

    /**Attenuation constant part*/
    private var constantAttenuation = 0f
    /**Attenuation linear part*/
    private var linearAttenuation = 0f
    /**Attenuation quadratic part*/
    private var quadraticAttenuation = 0f

    /**Spot cut off*/
    private var spotCutOff = 0
    /**Spot exponent*/
    private var spotExponent = 0
    /**Spot direction*/
    private val spotDirection = Point3D(0f, 0f, 1f)

    /**X position*/
    var x = 0f
        private set
    /**Y position*/
    var y = 0f
        private set
    /**Z position*/
    var z = 0f
        private set
    /**W position*/
    var w = 0f
        private set

    init
    {
        this.reset()
    }

    /**
     * Reset the light to default settings of light 0
     */
    private fun reset0()
    {
        this.enable = true
        this.diffuse.set(1f, 1f, 1f, 1f)
        this.specular.set(1f, 1f, 1f, 1f)
    }

    /**
     * Reset the light to the default setting of all lights except the light 0
     */
    private fun resetOthers()
    {
        this.enable = false
        this.diffuse.set(0f, 0f, 0f, 1f)
        this.specular.set(0f, 0f, 0f, 1f)
    }

    /**
     * Render the light
     */
    @ThreadOpenGL
    internal fun render()
    {
        if (this.asChanged)
        {
            this.asChanged = false

            if (this.needRefresh)
            {
                this.needRefresh = false

                GL11.glLightfv(this.id, GL11.GL_AMBIENT, this.ambient.putInFloatBuffer())
                GL11.glLightfv(this.id, GL11.GL_DIFFUSE, this.diffuse.putInFloatBuffer())
                GL11.glLightfv(this.id, GL11.GL_SPECULAR, this.specular.putInFloatBuffer())

                GL11.glLightfv(this.id, GL11.GL_POSITION, transferFloat(this.x, this.y, this.z, this.w))

                GL11.glLightfv(this.id, GL11.GL_SPOT_DIRECTION,
                               transferFloat(this.spotDirection.x, this.spotDirection.y, this.spotDirection.z))
                GL11.glLighti(this.id, GL11.GL_SPOT_EXPONENT, this.spotExponent)
                GL11.glLighti(this.id, GL11.GL_SPOT_CUTOFF, this.spotCutOff)

                GL11.glLightf(this.id, GL11.GL_CONSTANT_ATTENUATION, this.constantAttenuation)
                GL11.glLightf(this.id, GL11.GL_LINEAR_ATTENUATION, this.linearAttenuation)
                GL11.glLightf(this.id, GL11.GL_QUADRATIC_ATTENUATION, this.quadraticAttenuation)
            }

            if (this.enable)
            {
                GL11.glEnable(this.id)
            }
            else
            {
                GL11.glDisable(this.id)
            }
        }
    }

    /**
     * Ambient color
     */
    fun ambient() = this.ambient.copy()

    /**
     * Change ambient color
     */
    fun ambient(ambient: Color4f)
    {
        this.ambient.set(ambient)
        this.asChanged = true
        this.needRefresh = true
    }

    /**
     * Diffuse color
     */
    fun diffuse() = this.diffuse.copy()

    /**
     * change diffuse color
     */
    fun diffuse(diffuse: Color4f)
    {
        this.diffuse.set(diffuse)
        this.asChanged = true
        this.needRefresh = true
    }

    /**
     * Specular color
     */
    fun specular() = this.specular.copy()

    /**
     * Change specular color
     */
    fun specular(specular: Color4f)
    {
        this.specular.set(specular)
        this.asChanged = true
        this.needRefresh = true
    }

    /**
     * Constant attenuation part
     */
    fun constantAttenuation() = this.constantAttenuation

    /**
     * Change attenuation constant part
     * @param constantAttenuation Attenuation constant part must be positive or nul
     * @throws IllegalArgumentException If attenuation constant part is negative
     */
    @Throws(IllegalArgumentException::class)
    fun constantAttenuation(constantAttenuation: Float)
    {
        if (constantAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        this.constantAttenuation = constantAttenuation
        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Linear attenuation part
     */
    fun linearAttenuation() = this.linearAttenuation

    /**
     * Change attenuation linear part
     * @param linearAttenuation Attenuation linear part must be positive or nul
     * @throws IllegalArgumentException If attenuation linear part is negative
     */
    fun linearAttenuation(linearAttenuation: Float)
    {
        if (linearAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        this.linearAttenuation = linearAttenuation
        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Quadratic attenuation part
     */
    fun quadraticAttenuation() = this.quadraticAttenuation

    /**
     * Change attenuation quadratic part
     * @param quadraticAttenuation Attenuation quadratic part must be positive or nul
     * @throws IllegalArgumentException If attenuation quadratic part is negative
     */
    fun quadraticAttenuation(quadraticAttenuation: Float)
    {
        if (quadraticAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        this.quadraticAttenuation = quadraticAttenuation
        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Indicates if light is enable (on)
     */
    fun enable() = this.enable

    /**
     * Enable/disable the light. Switch it on or off
     */
    fun enable(enable: Boolean)
    {
        if (this.enable != enable)
        {
            this.enable = enable
            this.asChanged = true
        }
    }

    /**
     * Change the position/direction
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @param w W
     */
    fun position(x: Float, y: Float, z: Float, w: Float)
    {
        this.x = x
        this.y = y
        this.z = z
        this.w = w

        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Spot cut off angle
     */
    fun spotCutOff() = this.spotCutOff

    /**
     * Change spot cut off angle
     * @param spotCutOff Spot cut off angle. Must be in `[0, 90]` or `180`
     * @throws IllegalArgumentException if the angle not in `[0, 90]' and not `180`
     */
    @Throws(IllegalArgumentException::class)
    fun spotCutOff(spotCutOff: Int)
    {
        if ((spotCutOff < 0 || spotCutOff > 90) && spotCutOff != 180)
        {
            throw IllegalArgumentException("Spot cut off must be in [0, 90] or the special 180, not $spotCutOff")
        }

        if (this.spotCutOff != spotCutOff)
        {
            this.spotCutOff = spotCutOff

            this.needRefresh = true
            this.asChanged = true
        }
    }

    /**
     * Spot direction
     */
    fun spotDirection() = Point3D(this.spotDirection)

    /**
     * Change spot direction
     */
    fun spotDirection(spotDirection: Point3D)
    {
        this.spotDirection.set(spotDirection)
        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Spot exponent
     */
    fun spotExponent() = this.spotExponent

    /**
     * Change spot exponent
     * @param spotExponent New spot exponent. Must be in `[0, 128]`
     * @throws IllegalArgumentException If exponent not in `[0, 128]`
     */
    @Throws(IllegalArgumentException::class)
    fun spotExponent(spotExponent: Int)
    {
        if (spotExponent < 0 || spotExponent > 128)
        {
            throw IllegalArgumentException("Spot exponent must be in [0, 128], not $spotExponent")
        }

        if (this.spotExponent != spotExponent)
        {
            this.spotExponent = spotExponent
            this.needRefresh = true
            this.asChanged = true
        }
    }

    /**
     * Set the light to be a directional light
     *
     * @param direction Direction
     */
    fun makeDirectional(direction: Point3D)
    {
        this.x = direction.x
        this.y = direction.y
        this.z = direction.z
        this.w = 0f

        this.spotDirection.set(0f, 0f, -1f)

        this.spotExponent = 0
        this.spotCutOff = 180

        this.constantAttenuation = 1f
        this.linearAttenuation = 0f
        this.quadraticAttenuation = 0f

        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Set the light to be a punctual light
     *
     * @param position Position
     * @param exponent Exponent attenuation. Must be in `[0, 128]`
     * @param constantAttenuation Constant attenuation. Must be >=0
     * @param linearAttenuation Linear attenuation. Must be >=0
     * @param quadraticAttenuation Quadratic attenuation. Must be >=0
     * @throws IllegalArgumentException If one of constraints not full fill
     */
    @Throws(IllegalArgumentException::class)
    fun makePunctualLight(position: Point3D, exponent: Int,
                          constantAttenuation: Float, linearAttenuation: Float, quadraticAttenuation: Float)
    {
        if (exponent < 0 || exponent > 128)
        {
            throw IllegalArgumentException("exponent must be in [0, 128], not $exponent")
        }

        if (constantAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        if (linearAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        if (quadraticAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        this.x = position.x
        this.y = position.y
        this.z = position.z
        this.w = 1f

        this.spotDirection.set(0f, 0f, -1f)

        this.spotExponent = exponent
        this.spotCutOff = 180

        this.constantAttenuation = constantAttenuation
        this.linearAttenuation = linearAttenuation
        this.quadraticAttenuation = quadraticAttenuation

        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Set the light to be a spot
     *
     * @param position Position
     * @param direction Light direction
     * @param exponent Exponent attenuation. Must be in `[0, 128]`
     * @param cutOff Cut off. Must be in `[0, 90]` or `128`
     * @param constantAttenuation Constant attenuation. Must be >=0
     * @param linearAttenuation Linear attenuation. Must be >=0
     * @param quadraticAttenuation Quadratic attenuation. Must be >=0
     * @throws IllegalArgumentException If one of constraints not full fill
     */
    @Throws(IllegalArgumentException::class)
    fun makeSpot(position: Point3D, direction: Point3D, exponent: Int, cutOff: Int,
                 constantAttenuation: Float, linearAttenuation: Float, quadraticAttenuation: Float)
    {
        if (exponent < 0 || exponent > 128)
        {
            throw IllegalArgumentException("exponent must be in [0, 128], not $exponent")
        }

        if (constantAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        if (linearAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        if (quadraticAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        if ((cutOff < 0 || cutOff > 90) && cutOff != 180)
        {
            throw IllegalArgumentException("cutOff must be in [0, 90] or the special 180, not $cutOff")
        }

        this.x = position.x
        this.y = position.y
        this.z = position.z
        this.w = 1f

        this.spotDirection.set(direction)

        this.spotExponent = exponent
        this.spotCutOff = cutOff

        this.constantAttenuation = constantAttenuation
        this.linearAttenuation = linearAttenuation
        this.quadraticAttenuation = quadraticAttenuation

        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Reset the light to default settings
     */
    fun reset()
    {
        this.ambient.set(0f, 0f, 0f, 1f)

        this.x = 0f
        this.y = 0f
        this.z = 1f
        this.w = 0f

        this.spotDirection.set(0f, 0f, -1f)

        this.spotExponent = 0
        this.spotCutOff = 180

        this.constantAttenuation = 1f
        this.linearAttenuation = 0f
        this.quadraticAttenuation = 0f

        if (this.id == GL11.GL_LIGHT0)
        {
            this.reset0()
        }
        else
        {
            this.resetOthers()
        }

        this.needRefresh = true
        this.asChanged = true
    }
}