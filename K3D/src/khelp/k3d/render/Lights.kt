package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL

class Lights internal constructor(public val maximumNumberOfLights: Int)
{
    private val lights = ArrayList<Light>()
    val actualNumberOfLights: Int get() = this.lights.size

    init
    {
        this.lights.add(Light(Light.LIGHT_O, 0))
    }

    /**
     * Create a new light
     *
     * @param name Light name
     * @return Created light index
     */
    private fun createNewLight(name: String = "LIGHT_" + this.actualNumberOfLights): Int
    {
        var name = name

        if (!this.canCreateNewLight())
        {
            throw IllegalStateException("Maximum of lights is reach")
        }

        this.lights.add(Light(name, this.lights.size))
        return this.lights.size - 1
    }

    /**
     * Render the lights
     */
    @ThreadOpenGL
    internal fun render() = this.lights.forEach { it.render() }

    /**
     * Indicates if we are able to create a new light
     *
     * @return `true` if able to create a new light
     */
    fun canCreateNewLight() = this.lights.size < this.maximumNumberOfLights

    /**
     * Create a new directional light
     *
     * @param name      Light name
     * @param ambient   Ambient color
     * @param diffuse   Diffuse color
     * @param specular  Specular color
     * @param direction Direction of light
     * @return Created light index
     */
    fun createNewDirectional(name: String = "LIGHT_" + this.actualNumberOfLights,
                             ambient: Color4f, diffuse: Color4f, specular: Color4f,
                             direction: Point3D): Int
    {
        val id = this.createNewLight(name)
        val light = this.lights[id]

        light.ambient(ambient)
        light.diffuse(diffuse)
        light.specular(specular)

        light.makeDirectional(direction)

        return id
    }

    /**
     * Create a new ponctual light
     *
     * @param name                Light name
     * @param ambient             Ambient color
     * @param diffuse             Diffuse color
     * @param specular            Specular color
     * @param position            Position
     * @param exponent            Exponent in [0, 128]
     * @param constantAttenuation Constant attenuation
     * @param linearAttenuation   Linear attenuation
     * @param quadricAttenuation  Quadric attenuation
     * @return Created light index
     */
    fun createNewPonctual(name: String = "LIGHT_" + this.actualNumberOfLights,
                          ambient: Color4f, diffuse: Color4f, specular: Color4f,
                          position: Point3D, exponent: Int,
                          constantAttenuation: Float, linearAttenuation: Float, quadricAttenuation: Float): Int
    {
        val id = this.createNewLight(name)
        val light = this.lights[id]

        light.ambient(ambient)
        light.diffuse(diffuse)
        light.specular(specular)

        light.makePonctualLight(position, exponent, constantAttenuation, linearAttenuation, quadricAttenuation)

        return id
    }

    /**
     * Create a new spot light
     *
     * @param name                Light name
     * @param ambient             Ambient color
     * @param diffuse             Diffuse color
     * @param specular            Specular color
     * @param position            Position
     * @param direction           Spot direction
     * @param exponent            Exponent in [0, 128]
     * @param cutOff              Cut off in [0, 90] or special 180
     * @param constantAttenuation Constant attenuation
     * @param linearAttenuation   Linear attenuation
     * @param quadricAttenuation  Quadric attenuation
     * @return Created light index
     */
    fun createNewSpot(name: String = "LIGHT_" + this.actualNumberOfLights,
                      ambient: Color4f, diffuse: Color4f, specular: Color4f,
                      position: Point3D, direction: Point3D,
                      exponent: Int, cutOff: Int,
                      constantAttenuation: Float, linearAttenuation: Float, quadricAttenuation: Float): Int
    {
        val id = this.createNewLight(name)

        val light = this.lights[id]

        light.ambient(ambient)
        light.diffuse(diffuse)
        light.specular(specular)

        light.makeSpot(position, direction, exponent, cutOff, constantAttenuation, linearAttenuation,
                       quadricAttenuation)

        return id
    }

    /**
     * Obtain light by its index
     *
     * @param id Light index
     * @return Light
     */
    fun obtainLight(id: Int): Light
    {
        if (id < 0 || id >= this.lights.size)
        {
            throw IllegalArgumentException("id must be in [0, " + this.lights.size + "[ not " + id)
        }

        return this.lights[id]
    }

    /**
     * Obtain light by its name
     *
     * @param name Light name
     * @return Light or `null`
     */
    fun obtainLight(name: String): Light?
    {
        for (i in 0 until this.actualNumberOfLights)
        {
            if (this.lights[i].name == name)
            {
                return this.lights[i]
            }
        }

        return null
    }
}