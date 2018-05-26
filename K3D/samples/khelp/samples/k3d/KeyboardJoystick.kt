package khelp.samples.k3d

import khelp.k3d.geometry.Box
import khelp.k3d.render.BLUE
import khelp.k3d.render.Material
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ActionCode
import khelp.k3d.render.event.ActionListener

/**
 * Keyboard and Joystick sample.
 *
 * Plug Joystick before launch the sample
 */
fun main(args: Array<String>)
{
    // 1) Create 3D object to move
    val window3D = Window3D.createFullWidow("KeyBoard/Joystick")
    val scene = window3D.scene()
    val node = Box()
    node.position(0f, 0f, -5f)
    node.angleX(12f)
    node.angleY(25f)
    val material = Material.obtainMaterialOrCreate("box")
    material.colorDiffuse(BLUE)
    node.material(material)
    scene.add(node)

    // 2) Obtain the action manager
    val actionManager = window3D.actionManager()

    // 3) Associate action event to action task
    actionManager.registerActionListener(object : ActionListener
                                         {
                                             /**
                                              * Called each time current actions updates.
                                              *
                                              * Note if their no current active action, the method is not called
                                              *
                                              * @param actionCodes Current active action code list.
                                              */
                                             override fun actionsActive(vararg actionCodes: ActionCode)
                                             {
                                                 actionCodes.forEach {
                                                     when (it)
                                                     {
                                                     // By default UP, DOWN, LEFT and RIGHT are mapped to corresponding arrow keys
                                                     // and to main Joystick paddle
                                                         ActionCode.ACTION_UP       -> node.translate(0f, 0.1f, 0f)
                                                         ActionCode.ACTION_DOWN     -> node.translate(0f, -0.1f, 0f)
                                                         ActionCode.ACTION_LEFT     -> node.translate(-0.1f, 0f, 0f)
                                                         ActionCode.ACTION_RIGHT    -> node.translate(0.1f, 0f, 0f)

                                                     // By default button 5 map to E and 6 to D
                                                     // In Joystick, its the 5th button and 6th button
                                                         ActionCode.ACTION_BUTTON_5 -> node.translate(0f, 0f, 0.1f)
                                                         ActionCode.ACTION_BUTTON_6 -> node.translate(0f, 0f, -0.1f)

                                                     // By default button 1 map to K, 2 to J, 3 to L and 4 to I
                                                     // For joystick the number corresponds to button number
                                                         ActionCode.ACTION_BUTTON_1 -> node.rotateAngleX(1f)
                                                         ActionCode.ACTION_BUTTON_2 -> node.rotateAngleY(-1f)
                                                         ActionCode.ACTION_BUTTON_3 -> node.rotateAngleY(1f)
                                                         ActionCode.ACTION_BUTTON_4 -> node.rotateAngleX(-1f)

                                                     // By default exit action map to Escape button
                                                     // No default Joystick association
                                                         ActionCode.ACTION_EXIT     -> window3D.close()
                                                     }
                                                 }
                                             }
                                         })
}