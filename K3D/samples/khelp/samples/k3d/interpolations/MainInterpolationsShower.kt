package khelp.samples.k3d.interpolations

import khelp.k3d.render.Window3D

/**
 * Show the different type of interpolations
 *
 * To exit press the escape key
 */
fun main(args: Array<String>)
{
    //The 3D window
    val window3D = Window3D.createFullWidow("Interpolations")
    // Manage 3D scene
    val sceneManager = SceneManager(window3D)
    // Manage 2D interface
    val uiManager = UIManager(window3D)
    // Manage user interaction
    val managerAction = ManagerAction(window3D)
    val actionManager = window3D.actionManager()
    actionManager.registerActionListener(managerAction)
    // Create and launch the animation
    val animation = sceneManager.createAnimation(uiManager.informationChangeListener)
    window3D.playAnimation(animation)
}