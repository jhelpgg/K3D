package khelp.samples.k3d

import khelp.k3d.k2d.Object2D
import khelp.k3d.render.Window3D
import khelp.k3d.ui.BorderConstraints
import khelp.k3d.ui.BorderLayout
import khelp.k3d.ui.Label
import khelp.k3d.ui.TextureFrame

fun main(args: Array<String>)
{
    val window3D = Window3D.createSizedWindow(800, 600, "SampleUI", true)
    val guI2D = window3D.gui2d()
    val textureFrame = TextureFrame("Frame", BorderLayout())
    val frame = Object2D((800 - 512) shr 1, (600 - 512) shr 1, 512, 512)
    textureFrame.layout[BorderConstraints.TOP_LEFT] = Label("TOP_LEFT")
    textureFrame.layout[BorderConstraints.TOP] = Label("TOP")
    textureFrame.layout[BorderConstraints.TOP_RIGHT] = Label("TOP_RIGHT")
    textureFrame.layout[BorderConstraints.LEFT] = Label("LEFT")
    textureFrame.layout[BorderConstraints.CENTER] = Label("CENTER")
    textureFrame.layout[BorderConstraints.RIGHT] = Label("RIGHT")
    textureFrame.layout[BorderConstraints.BOTTOM_LEFT] = Label("BOTTOM_LEFT")
    textureFrame.layout[BorderConstraints.BOTTOM] = Label("BOTTOM")
    textureFrame.layout[BorderConstraints.BOTTOM_RIGHT] = Label("BOTTOM_RIGHT")
    frame.texture(textureFrame)
    guI2D.addUnder3D(frame)
}