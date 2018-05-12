package khelp.samples.k3d

import khelp.k3d.animation.AnimationEquation
import khelp.k3d.geometry.Sphere
import khelp.k3d.render.BLUE
import khelp.k3d.render.Material
import khelp.k3d.render.ObjectClone
import khelp.k3d.render.Window3D
import khelp.k3d.render.YELLOW
import khelp.k3d.util.PI
import khelp.math.formal.toFunction

fun main(args: Array<String>)
{
    // 1) Create 3D scene with objects
    val window3D = Window3D.createSizedWindow(800, 600, "Animation equation", true)
    val scene = window3D.scene()
    scene.background(0f, 0f, 0f)
    scene.position(0f, 0f, -8f)

    val star = Sphere()
    scene.add(star)
    var material = Material.obtainMaterialOrCreate("star")
    material.colorDiffuse(YELLOW)
    star.material(material)

    val planet = ObjectClone(star)
    planet.scale(0.25f)
    scene.add(planet)
    material = Material.obtainMaterialOrCreate("planet")
    material.colorDiffuse(BLUE)
    planet.material(material)

    // 2) Create animation and play it
    val animationEquation = AnimationEquation("sin(t) + 2 * sin(2*t)".toFunction(),
                                              "cos(t) - 2 * cos(2*t)".toFunction(),
                                              "-sin(3*t)".toFunction(),
                                              -2 * PI, 2 * PI, 400, planet)

    window3D.playAnimation(animationEquation)
}