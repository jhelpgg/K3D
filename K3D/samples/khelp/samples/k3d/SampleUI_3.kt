package khelp.samples.k3d

import khelp.k3d.k2d.Object2D
import khelp.k3d.render.Window3D
import khelp.k3d.ui.Label
import khelp.k3d.ui.TableConstraints
import khelp.k3d.ui.TableLayout
import khelp.k3d.ui.TextureFrame
import khelp.thread.parallel

/**25FPS => 1000ms/25 = 40ms wait between each frame*/
const val TIME = 40L

fun count(count: Int, label: Label)
{
    val now = count - 1
    label.text("$now")

    if (now > 0)
    {
        ({ count(now, label) }).parallel(TIME)
    }
}

fun main(args: Array<String>)
{
    val window3D = Window3D.createSizedWindow(800, 600, "SampleUI", true)
    val guI2D = window3D.gui2d()
    val textureFrame = TextureFrame("Frame", TableLayout())
    val frame = Object2D((800 - 512) shr 1, (600 - 512) shr 1, 512, 512)
    textureFrame.layout[TableConstraints(0, 0)] = Label("(0,0)\n1x1")
    textureFrame.layout[TableConstraints(1, 0, 2, 3)] = Label("(1,0)\n2x3")
    textureFrame.layout[TableConstraints(0, 1, 1, 2)] = Label("(0,1)\n1x2")
    textureFrame.layout[TableConstraints(0, 3, 3, 1)] = Label("(0,3)\n3x1")
    val label = Label("(2,2)\n1x1");
    textureFrame.layout[TableConstraints(2, 2, 1, 1)] = label
    frame.texture(textureFrame)
    guI2D.addUnder3D(frame)
    ({ count(1025, label) }).parallel(TIME)
}