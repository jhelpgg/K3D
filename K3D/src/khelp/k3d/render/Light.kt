package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.transferFloat
import org.lwjgl.opengl.GL11

class Light internal constructor(public val name: String, id: Int)
{
    companion object
    {
        /**
         * Light 0 name
         */
        val LIGHT_O = "LIGHT_0"
    }

    private val id = GL11.GL_LIGHT0 + id

    private val diffuse = WHITE.copy()
    private val ambient = BLACK.copy()
    private val specular = GRAY.copy()

    private var asChanged = false
    private var enable = false
    private var needRefresh = false

    private var constantAttenuation = 0f
    private var linearAttenuation = 0f
    private var quadricAttenuation = 0f

    private var spotCutOff = 0
    private var spotExponent = 0
    private val spotDirection = Point3D(0f, 0f, 1f)

    var x = 0f
        private set
    var y = 0f
        private set
    var z = 0f
        private set
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
                GL11.glLightf(this.id, GL11.GL_QUADRATIC_ATTENUATION, this.quadricAttenuation)
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

    fun ambient() = this.ambient.copy()
    fun ambient(ambient: Color4f)
    {
        this.ambient.set(ambient)
        this.asChanged = true
        this.needRefresh = true
    }

    fun diffuse() = this.diffuse.copy()
    fun diffuse(diffuse: Color4f)
    {
        this.diffuse.set(diffuse)
        this.asChanged = true
        this.needRefresh = true
    }

    fun specular() = this.specular.copy()
    fun specular(specular: Color4f)
    {
        this.specular.set(specular)
        this.asChanged = true
        this.needRefresh = true
    }

    fun constantAttenuation() = this.constantAttenuation
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

    fun linearAttenuation() = this.linearAttenuation
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

    fun quadricAttenuation() = this.quadricAttenuation
    fun quadricAttenuation(quadricAttenuation: Float)
    {
        if (quadricAttenuation < 0)
        {
            throw IllegalArgumentException("No negative value")
        }

        this.quadricAttenuation = quadricAttenuation
        this.needRefresh = true
        this.asChanged = true
    }

    fun enable() = this.enable
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

    fun spotCutOff() = this.spotCutOff
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

    fun spotDirection() = Point3D(this.spotDirection)
    fun spotDirection(spotDirection: Point3D)
    {
        this.spotDirection.set(spotDirection)
        this.needRefresh = true
        this.asChanged = true
    }

    fun spotExponent() = this.spotExponent
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
        this.quadricAttenuation = 0f

        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Set the light to be a ponctual light
     *
     * @param position            Position
     * @param exponent            Exponent attenuation
     * @param constantAttenuation Constant attenuation
     * @param linearAttenuation   Linear attenuation
     * @param quadricAttenuation  Quadric attenuation
     */
    fun makePonctualLight(position: Point3D, exponent: Int,
                          constantAttenuation: Float, linearAttenuation: Float, quadricAttenuation: Float)
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

        if (quadricAttenuation < 0)
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
        this.quadricAttenuation = quadricAttenuation

        this.needRefresh = true
        this.asChanged = true
    }

    /**
     * Set the light to be a spot
     *
     * @param position            Position
     * @param direction           Light direction
     * @param exponent            Exponent attenuation
     * @param cutOff              Cut off
     * @param constantAttenuation Constant attenuation
     * @param linearAttenuation   Linear attenuation
     * @param quadricAttenuation  Quadric attenuation
     */
    fun makeSpot(position: Point3D, direction: Point3D, exponent: Int, cutOff: Int,
                 constantAttenuation: Float, linearAttenuation: Float, quadricAttenuation: Float)
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

        if (quadricAttenuation < 0)
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
        this.quadricAttenuation = quadricAttenuation

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
        this.quadricAttenuation = 0f

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