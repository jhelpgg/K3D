package khelp.k3d.render

import khelp.k3d.util.ThreadOpenGL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13

/**
 * Cube map.<br>
 * A cube map is compose on six textures, place on each face of a cube.<br>
 * It use for having reflection, or simulate "mirror environment" in objects
 *
 * @author JHelp
 */
class CubeMap()
{
    companion object
    {
        /**
         * For place a texture in the "back" face of the cube
         */
        val BACK = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
        /**
         * For place a texture in the "bottom" face of the cube
         */
        val BOTTOM = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
        /**
         * For place a texture in the "face" face of the cube
         */
        val FACE = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z
        /**
         * For place a texture in the "left" face of the cube
         */
        val LEFT = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
        /**
         * For place a texture in the "right" face of the cube
         */
        val RIGHT = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X
        /**
         * For place a texture in the "top" face of the cube
         */
        val TOP = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
    }

    private var videoMemoryID = -1
    private var needRefresh = true
    private var crossTexture: Texture = Texture.DUMMY
    var xNegative: Texture = Texture.DUMMY
    var xPositive: Texture = Texture.DUMMY
    var yNegative: Texture = Texture.DUMMY
    var yPositive: Texture = Texture.DUMMY
    var zNegative: Texture = Texture.DUMMY
    var zPositive: Texture = Texture.DUMMY
    /**
     * Apply the cube map.<br></br>
     * If the cube map is not complete, nothing is done
     */
    @ThreadOpenGL
    fun bind()
    {
        //        if (this.videoMemoryID < 0)
        //        {
        //            // Not in video memory, so put it in
        //            try (MemoryStack stack = MemoryStack.stackPush())
        //            {
        //                final IntBuffer cubeMapID = stack.mallocInt(1);
        //                GL11.glGenTextures(cubeMapID);
        //                this.videoMemoryID = cubeMapID.get();
        //            }
        //        }

        //        if (this.needRefresh)
        //        {
        //            // If the cube map need to be refresh, refresh it
        //            GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, this.videoMemoryID);
        //
        //            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL11.GL_RGBA, this.xPositive.width,
        //                              this.xPositive.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        //                              BufferUtils.transferByte(this.xPositive.pixels));
        //            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL11.GL_RGBA, this.xNegative.width,
        //                              this.xNegative.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        //                              BufferUtils.transferByte(this.xNegative.pixels));
        //
        //            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL11.GL_RGBA, this.yPositive.width,
        //                              this.yPositive.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        //                              BufferUtils.transferByte(this.yPositive.pixels));
        //            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL11.GL_RGBA, this.yNegative.width,
        //                              this.yNegative.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        //                              BufferUtils.transferByte(this.yNegative.pixels));
        //
        //            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL11.GL_RGBA, this.zPositive.width,
        //                              this.zPositive.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        //                              BufferUtils.transferByte(this.zPositive.pixels));
        //            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL11.GL_RGBA, this.zNegative.width,
        //                              this.zNegative.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        //                              BufferUtils.transferByte(this.zNegative.pixels));
        //
        //            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        //            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        //            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL11.GL_REPEAT);
        //            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        //            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        //
        //            // Cube map has been refresh
        //            this.needRefresh = false;
        //        }

        // Activate cube map
        //        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, this.videoMemoryID);
        //        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GL11.glEnable(GL13.GL_TEXTURE_CUBE_MAP)
        GL11.glEnable(GL11.GL_TEXTURE_GEN_S)
        GL11.glEnable(GL11.GL_TEXTURE_GEN_T)
        GL11.glEnable(GL11.GL_TEXTURE_GEN_R)
        GL11.glTexGeni(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL13.GL_REFLECTION_MAP)
        GL11.glTexGeni(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL13.GL_REFLECTION_MAP)
        GL11.glTexGeni(GL11.GL_R, GL11.GL_TEXTURE_GEN_MODE, GL13.GL_REFLECTION_MAP)
        GL11.glEnable(GL11.GL_TEXTURE_2D)

        //Strange ????
        // TODO Check the cube map !!!!
        this.crossTexture.bind()
    }

    /**
     * Cut a cross texture for fill the cube map.<br></br>
     * Cross suppose be like : `<br></br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+X<br></br>
     * &nbsp;+Y&nbsp;&nbsp;+Z&nbsp;&nbsp;-Y<br></br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-X<br></br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-Z<br></br>
    ` *
     *
     * @param texture Texture to cut
     */
    fun crossTexture(texture: Texture)
    {
        this.crossTexture = texture
        val width = texture.width / 3
        val height = texture.height shr 2

        // X positive
        this.xPositive = Texture.obtainTexture(texture.textureName() + "_CUBE_MAP_X_POSITIVE")
                ?: texture.obtainParcel(width, 0, width, height, "_CUBE_MAP_X_POSITIVE")

        // X negative
        this.xNegative = Texture.obtainTexture(texture.textureName() + "_CUBE_MAP_X_NEGATIVE")
                ?: texture.obtainParcel(width, height shl 1, width, height, "_CUBE_MAP_X_NEGATIVE")

        // Y positive
        this.yPositive = Texture.obtainTexture(texture.textureName() + "_CUBE_MAP_Y_POSITIVE")
                ?: texture.obtainParcel(0, height, width, height, "_CUBE_MAP_Y_POSITIVE")

        // Y negative
        this.yNegative = Texture.obtainTexture(texture.textureName() + "_CUBE_MAP_Y_NEGATIVE")
                ?: texture.obtainParcel(width shl 1, height, width, height, "_CUBE_MAP_Y_NEGATIVE")

        // Z positive
        this.zPositive = Texture.obtainTexture(texture.textureName() + "_CUBE_MAP_Z_POSITIVE")
                ?: texture.obtainParcel(width, height, width, height, "_CUBE_MAP_Z_POSITIVE")

        // Z negative
        this.zNegative = Texture.obtainTexture(texture.textureName() + "_CUBE_MAP_Z_NEGATIVE")
                ?: texture.obtainParcel(width, height * 3, width, height, "_CUBE_MAP_Z_NEGATIVE")

        this.needRefresh = true
    }

    fun crossTexture() = this.crossTexture

    /**
     * End application of cube map
     */
    @ThreadOpenGL
    fun endCubeMap()
    {
        GL11.glDisable(GL13.GL_TEXTURE_CUBE_MAP)
        GL11.glDisable(GL11.GL_TEXTURE_GEN_S)
        GL11.glDisable(GL11.GL_TEXTURE_GEN_T)
        GL11.glDisable(GL11.GL_TEXTURE_GEN_R)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
    }

    /**
     * Flush last changes.<br></br>
     * Use it if you have modified one of its texture pixels
     */
    fun flush()
    {
        this.needRefresh = true
    }
}