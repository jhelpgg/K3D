package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import khelp.k3d.util.equal
import khelp.list.EnumerationIterator
import khelp.math.limit
import khelp.text.computeNotInsideName
import org.lwjgl.opengl.GL11
import java.util.Hashtable
import java.util.concurrent.atomic.AtomicInteger

class Material(private var name: String)
{
    companion object
    {
        /**
         * New material default header name
         */
        private val NEW_MATERIAL_HEADER = "MATERIAL_"
        /**
         * Materials table
         */
        private var hashtableMaterials = Hashtable<String, Material>()
        /**
         * Material use for pick UV
         */
        val materialForPickUV: Material by lazy {
            val material = Material("JHELP_MATERIAL_FOR_PICK_UV")
            material.colorEmissive().set(1f)
            material.specularLevel = 1f
            material.shininess(128)
            material.colorDiffuse().set(1f)
            material.colorSpecular().set()
            material.colorAmbient().set(1f)
            material.twoSided = true
            material.textureDiffuse = Texture.textureForPickUV
            material
        }
        /**
         * Next default ID name
         */
        private var nextID = AtomicInteger(0)
        /**
         * Default material
         */
        val DEFAULT_MATERIAL = Material("Default")

        /**
         * Name for material 2D
         */
        val MATERIAL_FOR_2D_NAME = "MATERIAL_FOR_2D"

        /**
         * New default named material
         *
         * @return New default named material
         */
        fun createNewMaterial(): Material
        {
            if (Material.hashtableMaterials == null)
            {
                Material.hashtableMaterials = Hashtable()
            }

            var name = Material.NEW_MATERIAL_HEADER + Material.nextID.getAndIncrement()

            while (Material.hashtableMaterials!!.containsKey(name) == true)
            {
                name = Material.NEW_MATERIAL_HEADER + Material.nextID.getAndIncrement()
            }

            return Material(name)
        }

        /**
         * Create a new material with a specific base name.<br></br>
         * Note if the name is alred given to an other material, the name is little changed to be unique
         *
         * @param name Base name. If `null` or empty, a name is automatic given
         * @return Created material
         */
        fun createNewMaterial(name: String): Material
        {
            var name = name.trim { it <= ' ' }

            if (name.length == 0)
            {
                name = Material.NEW_MATERIAL_HEADER + "0"
            }

            name = computeNotInsideName(name, Material.hashtableMaterials.keys)
            return Material(name)
        }

        /**
         * Obtain material with its name
         *
         * @param name Material name
         * @return The material or [.DEFAULT_MATERIAL] if the material not exists
         */
        fun obtainMaterial(name: String) = this.hashtableMaterials[name] ?: DEFAULT_MATERIAL

        /**
         * Obtain a material or create a new one if not exists
         *
         * @param name Material name
         * @return Searched material or newly created
         */
        fun obtainMaterialOrCreate(name: String) = this.hashtableMaterials[name] ?: Material(name)

        /**
         * Force refresh all materials
         */
        fun refreshAllMaterials()
        {
            for (material in EnumerationIterator<Material>(Material.hashtableMaterials.elements()))
            {
                material.textureDiffuse?.flush()
                material.textureSpheric?.flush()
                material.cubeMap?.flush()
            }
        }

        /**
         * Register a material
         *
         * @param material Material to register
         */
        private fun registerMaterial(material: Material)
        {
            Material.hashtableMaterials[material.name] = material
        }

        /**
         * Rename a material
         *
         * @param material Material to rename
         * @param newName  New name
         */
        fun rename(material: Material, newName: String)
        {
            var newName = newName.trim { it <= ' ' }

            if (newName.length < 1)
            {
                throw IllegalArgumentException("newName mustn't be empty")
            }

            if (material.name == newName)
            {
                return
            }

            Material.hashtableMaterials.remove(material.name)
            material.name = newName
            Material.hashtableMaterials[newName] = material
        }
    }

    private var colorAmbient = BLACK.copy()
    private var colorDiffuse = GRAY.copy()
    private var colorEmissive = DARK_GRAY.copy()
    private var colorSpecular = LIGHT_GRAY.copy()
    var specularLevel = 0.1f
    private var shininess = 12
    var transparency = 1f
    var twoSided = false
    var sphericRate = 1f
    var cubeMapRate = 1f
    var cubeMap: CubeMap? = null
    var textureDiffuse: Texture? = null
    var textureSpheric: Texture? = null

    init
    {
        Material.registerMaterial(this)
    }

    fun name() = this.name

    /**
     * Render the material for a 3D object
     *
     * @param object3D Object to render
     */
    @ThreadOpenGL
    internal fun renderMaterial(object3D: Object3D)
    {
        this.prepareMaterial()
        //
        if (this.textureDiffuse != null)
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            this.textureDiffuse?.bind()
            object3D.drawObject()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
        }
        else
        {
            object3D.drawObject()
        }

        if (this.textureSpheric != null)
        {
            val transparency = this.transparency
            this.transparency *= this.sphericRate
            //
            this.prepareMaterial()
            GL11.glDepthFunc(GL11.GL_LEQUAL)
            //
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_TEXTURE_GEN_S)
            GL11.glEnable(GL11.GL_TEXTURE_GEN_T)
            //
            GL11.glTexGeni(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_SPHERE_MAP)
            GL11.glTexGeni(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_SPHERE_MAP)
            //
            this.textureSpheric?.bind()
            object3D.drawObject()
            //
            GL11.glDisable(GL11.GL_TEXTURE_GEN_T)
            GL11.glDisable(GL11.GL_TEXTURE_GEN_S)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            //
            GL11.glDepthFunc(GL11.GL_LESS)
            //
            this.transparency = transparency
        }

        if (this.cubeMap != null)
        {
            val transparency = this.transparency
            this.transparency *= this.cubeMapRate

            this.prepareMaterial()
            GL11.glDepthFunc(GL11.GL_LEQUAL)

            this.cubeMap?.bind()
            object3D.drawObject()
            this.cubeMap?.endCubeMap()

            GL11.glDepthFunc(GL11.GL_LESS)
            this.transparency = transparency
        }

        if (object3D.showWire)
        {
            GL11.glDisable(GL11.GL_LIGHTING)
            object3D.wireColor.glColor4f()
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE.toFloat())
            object3D.drawObject()
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE.toFloat())
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    fun colorAmbient() = this.colorAmbient
    fun colorAmbient(colorAmbient: Color4f)
    {
        if (colorAmbient.defaultColor())
        {
            this.colorAmbient = colorAmbient.copy()
        }
        else
        {
            this.colorAmbient = colorAmbient
        }
    }

    fun colorDiffuse() = this.colorDiffuse
    fun colorDiffuse(colorDiffuse: Color4f)
    {
        if (colorDiffuse.defaultColor())
        {
            this.colorDiffuse = colorDiffuse.copy()
        }
        else
        {
            this.colorDiffuse = colorDiffuse
        }
    }

    fun colorEmissive() = this.colorEmissive
    fun colorEmissive(colorEmissive: Color4f)
    {
        if (colorEmissive.defaultColor())
        {
            this.colorEmissive = colorEmissive.copy()
        }
        else
        {
            this.colorEmissive = colorEmissive
        }
    }

    fun colorSpecular() = this.colorSpecular
    fun colorSpecular(colorSpecular: Color4f)
    {
        if (colorSpecular.defaultColor())
        {
            this.colorSpecular = colorSpecular.copy()
        }
        else
        {
            this.colorSpecular = colorSpecular
        }
    }

    /**
     * Indicates if an Object is the same as the material
     *
     * @param `other` Object to compare
     * @return `true` if an Object is the same as the material
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (super.equals(other))
        {
            return true
        }

        if (other !is Material)
        {
            return false
        }

        if (other.name == this.name)
        {
            return true
        }
        if (!other.colorAmbient.equals(this.colorAmbient))
        {
            return false
        }
        if (!other.colorDiffuse.equals(this.colorDiffuse))
        {
            return false
        }
        if (!other.colorEmissive.equals(this.colorEmissive))
        {
            return false
        }

        if (!other.colorSpecular.equals(this.colorSpecular))
        {
            return false
        }

        if (other.shininess != this.shininess)
        {
            return false
        }

        if (other.twoSided != this.twoSided)
        {
            return false
        }

        if (!equal(other.specularLevel, this.specularLevel))
        {
            return false
        }

        if (!equal(other.sphericRate, this.sphericRate))
        {
            return false
        }

        if (!equal(other.transparency, this.transparency))
        {
            return false
        }

        if (this.textureDiffuse == null && other.textureDiffuse != null || this.textureDiffuse != null && other.textureDiffuse == null)
        {
            return false
        }

        if (this.textureDiffuse != null && !this.textureDiffuse!!.equals(other.textureDiffuse))
        {
            return false
        }

        return if (this.textureSpheric == null && other.textureSpheric != null || this.textureSpheric != null && other.textureSpheric == null)
        {
            false
        }
        else this.textureSpheric == null || this.textureSpheric!!.equals(other.textureSpheric)
    }

    /**
     * Reset all settings to put as default
     */
    fun originalSettings()
    {
        this.colorAmbient = BLACK.copy()
        this.colorDiffuse = GRAY.copy()
        this.colorEmissive = DARK_GRAY.copy()
        this.colorSpecular = LIGHT_GRAY.copy()
        this.specularLevel = 0.1f
        this.shininess = 12
        this.transparency = 1f
        this.twoSided = false
        this.sphericRate = 1f
        this.cubeMapRate = 1f
    }

    /**
     * Prepare material for OpenGL render.<br></br>
     * Use by the renderer, don't call it directly
     */
    @ThreadOpenGL
    fun prepareMaterial()
    {
        if (this.twoSided)
        {
            GL11.glDisable(GL11.GL_CULL_FACE)
        }
        else
        {
            GL11.glEnable(GL11.GL_CULL_FACE)
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        val alpha = this.colorDiffuse.alpha()
        this.colorDiffuse.alpha(this.transparency)
        this.colorDiffuse.glColor4f()
        this.colorDiffuse.alpha(alpha)
        //
        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, this.colorDiffuse.putInFloatBuffer())
        //
        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION, this.colorEmissive.putInFloatBuffer())
        //
        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR,
                          this.colorSpecular.putInFloatBuffer(this.specularLevel))
        //
        GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, this.colorAmbient.putInFloatBuffer())
        //
        GL11.glMateriali(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, this.shininess)
    }

    /**
     * Do settings for 2D
     */
    fun settingAsFor2D()
    {
        this.colorEmissive.set(1f)
        this.specularLevel = 1f
        this.shininess = 128
        this.colorDiffuse.set(1f)
        this.colorSpecular.set()
        this.colorAmbient.set(1f)
        this.twoSided = true
    }

    fun shininess() = this.shininess
    /**
     * Change shininess (0 <-> 128)
     *
     * @param shininess New shininess (0 <-> 128)
     */
    fun shininess(shininess: Int)
    {
        this.shininess = limit(shininess, 0, 128)
    }
}