package khelp.samples.k3d

import khelp.alphabet.AlphabetOrange16x16
import khelp.k3d.geometry.Sphere
import khelp.k3d.k2d.Object2D
import khelp.k3d.render.Color4f
import khelp.k3d.render.DARK_GRAY
import khelp.k3d.render.Material
import khelp.k3d.render.RED
import khelp.k3d.render.TextureAlphabetText
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ActionCode
import khelp.k3d.render.event.ActionCode.ACTION_DOWN
import khelp.k3d.render.event.ActionCode.ACTION_EXIT
import khelp.k3d.render.event.ActionCode.ACTION_LEFT
import khelp.k3d.render.event.ActionCode.ACTION_RIGHT
import khelp.k3d.render.event.ActionCode.ACTION_UP
import khelp.k3d.render.event.ActionListener
import khelp.math.limit
import khelp.text.JHelpTextAlign.LEFT
import khelp.util.BLACK_ALPHA_MASK

/**
 * Here there some interaction:
 * * Up arrow key or joystick vertical axis negative: Increase the shininess
 * * Down arrow key or joystick vertical axis positive: Decrease the shininess
 * * Left arrow key or joystick horizontal axis negative: Decrease the specular level
 * * Right arrow key or joystick horizontal axis positive: Increase the specular level
 * * Escape key : Exit the application
 *
 * Try by example:
 * * Specular level around 0.7 and shininess 5.
 * * Put shininess at 10 and try different specular level values
 * * ... (Let express your imagination)
 */
fun main(args: Array<String>)
{
    // 1) Create window that will show the 3D
    val window3D = Window3D.createFullWidow("Light on object")

    // 2) Get window associated scene to add 3D elements
    val scene = window3D.scene()

    // 3) Create a Sphere
    val node = Sphere()

    // 4) Add sphere to the scene
    scene.add(node)

    // 5) Place sphere to able see it
    node.position(0f, 0f, -5f)

    // 6) Create a material
    val material = Material.obtainMaterialOrCreate("LightEffect")

    // 7) Apply material to sphere
    node.material(material)

    // 8) Initialize material light settings
    material.colorDiffuse(DARK_GRAY)
    material.colorEmissive(Color4f(0.2f, 0.3f, 0.4f))
    material.colorAmbient(Color4f(0.25f, 0.5f, 0.75f))
    material.colorSpecular(RED)
    material.specularLevel = 5f
    material.shininess(25)

    // 9) Add 2D information to show shininess and specular level current value
    // See explanations about 2D for more information
    val gui2d = window3D.gui2d()
    val informationWidth = 512
    val informationHeight = 64
    val informationShower = Object2D(8, 8, informationWidth, informationHeight)
    gui2d.addOver3D(informationShower)
    val textureInformation = TextureAlphabetText(AlphabetOrange16x16,
                                                 informationWidth / 16, informationHeight / 16,
                                                 "shininess=${material.shininess()}\n\nspecular level=${material.specularLevel}",
                                                 LEFT, BLACK_ALPHA_MASK, 0)
    informationShower.texture(textureInformation)

    // 10) Associate application user actions to tasks
    // See explanations about key/joystick management for more information
    var shininess = material.shininess().toFloat()
    val actionManager = window3D.actionManager()
    actionManager.registerActionListener(
            object : ActionListener
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
                            ACTION_UP    ->
                            {
                                shininess += 0.1f
                                material.shininess(shininess.toInt())
                                textureInformation.text(
                                        "shininess=${material.shininess()}\n\nspecular level=${material.specularLevel}")
                            }
                            ACTION_DOWN  ->
                            {
                                shininess -= 0.1f
                                material.shininess(shininess.toInt())
                                textureInformation.text(
                                        "shininess=${material.shininess()}\n\nspecular level=${material.specularLevel}")
                            }
                            ACTION_LEFT  ->
                            {
                                material.specularLevel = limit(material.specularLevel - 0.01f, 0f, 10f)
                                textureInformation.text(
                                        "shininess=${material.shininess()}\n\nspecular level=${material.specularLevel}")
                            }
                            ACTION_RIGHT ->
                            {
                                material.specularLevel = limit(material.specularLevel + 0.01f, 0f, 10f)
                                textureInformation.text(
                                        "shininess=${material.shininess()}\n\nspecular level=${material.specularLevel}")
                            }
                            ACTION_EXIT  -> window3D.close()
                        }
                    }
                }
            })
}