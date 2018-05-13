package khelp.samples.k3d.interpolations

import khelp.alphabet.AlphabetOrange16x16
import khelp.k3d.k2d.GUI2D
import khelp.k3d.k2d.Object2D
import khelp.k3d.render.TextureAlphabetText
import khelp.k3d.render.Window3D
import khelp.text.JHelpTextAlign.CENTER
import khelp.util.BLACK_ALPHA_MASK

/**
 * Manage 2D interface
 * @param window3D Window where scene is draw
 */
class UIManager(val window3D: Window3D)
{
    /**Texture where information is print*/
    private val textureInformation = TextureAlphabetText(AlphabetOrange16x16, 32, 1, "Not started", CENTER,
                                                         BLACK_ALPHA_MASK, 0)
    /**Listener of animation state change*/
    val informationChangeListener: (String) -> Unit = { this.textureInformation.text(it) }

    init
    {
        this.initUI(this.window3D.gui2d())
    }

    /**
     * Initialize the UI
     * @param guI2D UI manager
     */
    private fun initUI(guI2D: GUI2D)
    {
        val information = Object2D((this.window3D.width - this.textureInformation.width) / 2, 8,
                                   this.textureInformation.width, this.textureInformation.height)
        information.texture(this.textureInformation)
        guI2D.addOver3D(information)
    }
}