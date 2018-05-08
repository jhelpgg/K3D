package khelp.samples.k3d

import khelp.k3d.k2d.Object2D
import khelp.k3d.render.Window3D
import khelp.k3d.ui.Label
import khelp.k3d.ui.RelativeConstraints
import khelp.k3d.ui.RelativeHorizontal
import khelp.k3d.ui.RelativeLayout
import khelp.k3d.ui.RelativeVertical
import khelp.k3d.ui.TextureFrame

fun main(args: Array<String>)
{
    val window3D = Window3D.createSizedWindow(800, 600, "SampleUI", true)
    val guI2D = window3D.gui2d()
    val textureFrame = TextureFrame("Frame", RelativeLayout())
    val frame = Object2D((800 - 512) shr 1, (600 - 512) shr 1, 512, 512)
    val topLeft = RelativeConstraints(alignParentTop = true, alignParentLeft = true)
    textureFrame.layout[topLeft] = Label("Top\nLeft")
    val bottomRight = RelativeConstraints()
    bottomRight.horizontal(RelativeHorizontal.LEFT, topLeft, RelativeHorizontal.RIGHT)
    bottomRight.vertical(RelativeVertical.TOP, topLeft, RelativeVertical.BOTTOM)
    textureFrame.layout[bottomRight] = Label("Bottom\nRight")
    frame.texture(textureFrame)
    guI2D.addUnder3D(frame)
}