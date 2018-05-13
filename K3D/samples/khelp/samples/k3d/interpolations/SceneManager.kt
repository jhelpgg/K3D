package khelp.samples.k3d.interpolations

import khelp.images.dynamic.AccelerationInterpolation
import khelp.images.dynamic.AnticipateInterpolation
import khelp.images.dynamic.AnticipateOvershootInterpolation
import khelp.images.dynamic.BounceInterpolation
import khelp.images.dynamic.BouncingInterpolation
import khelp.images.dynamic.CosinusInterpolation
import khelp.images.dynamic.CubicInterpolation
import khelp.images.dynamic.DecelerationInterpolation
import khelp.images.dynamic.ExponentialInterpolation
import khelp.images.dynamic.HesitateInterpolation
import khelp.images.dynamic.Interpolation
import khelp.images.dynamic.LinearInterpolation
import khelp.images.dynamic.LogarithmInterpolation
import khelp.images.dynamic.OvershootInterpolation
import khelp.images.dynamic.QuadraticInterpolation
import khelp.images.dynamic.RandomInterpolation
import khelp.images.dynamic.SinusInterpolation
import khelp.images.dynamic.SquareInterpolation
import khelp.images.dynamic.SquareRootInterpolation
import khelp.k3d.animation.Animation
import khelp.k3d.animation.AnimationList
import khelp.k3d.animation.AnimationParallel
import khelp.k3d.animation.AnimationPause
import khelp.k3d.animation.AnimationPositionNode
import khelp.k3d.animation.AnimationTask
import khelp.k3d.animation.PositionNode
import khelp.k3d.geometry.Box
import khelp.k3d.geometry.Plane
import khelp.k3d.render.Color4f
import khelp.k3d.render.Material
import khelp.k3d.render.NodeWithMaterial
import khelp.k3d.render.ObjectClone
import khelp.k3d.render.Scene
import khelp.k3d.render.TwoSidedState.FORCE_TWO_SIDE
import khelp.k3d.render.Window3D
import khelp.util.scramble

/**Pause time in frame*/
const val PAUSE_FRAME = 25
/**Animation time in frame for go from start to end and for end to start*/
const val ANIMATION_FRAME = 75
/**Number of turn*/
const val REPEAT = 128
/**X position for start and end*/
private const val X = 2.2f

/**Interpolations show*/
private val INTERPOLATIONS = arrayOf(LinearInterpolation, SinusInterpolation, CosinusInterpolation,
                                     ExponentialInterpolation, LogarithmInterpolation, SquareInterpolation,
                                     SquareRootInterpolation, BounceInterpolation, HesitateInterpolation,
                                     RandomInterpolation, AccelerationInterpolation(3f), AnticipateInterpolation(3f),
                                     AnticipateOvershootInterpolation(3f), BouncingInterpolation(5),
                                     CubicInterpolation(-0.1f, 1.1f), DecelerationInterpolation(3f),
                                     OvershootInterpolation(3f), QuadraticInterpolation(1.1f))

/**
 * Manage the 3D scene.
 *
 * Draw the 3D, create the animation
 * @param window3D Window where scene is draw
 */
class SceneManager(val window3D: Window3D)
{
    /**Animated box*/
    private val movedBox = Box()

    init
    {
        this.initializeScene(this.window3D.scene())
    }

    /**
     * Create a material with given diffuse color and apply it to a node
     * @param nodeWithMaterial Node to put the created material
     * @param name Created material name
     * @param color Diffuse to apply
     */
    private fun diffuse(nodeWithMaterial: NodeWithMaterial, name: String, color: Color4f)
    {
        val material = Material.obtainMaterialOrCreate(name)
        material.colorDiffuse(color)
        nodeWithMaterial.material(material)
    }

    /**
     * Initialize the 3D scene
     */
    private fun initializeScene(scene: Scene)
    {
        // Create start red position
        val startPlane = Plane()
        startPlane.angleX(70f)
        startPlane.position(-X, -0.6f, -5f)
        scene.add(startPlane)
        this.diffuse(startPlane, "start", khelp.k3d.render.RED)

        // Create end green position
        val endPlane = ObjectClone(startPlane)
        endPlane.twoSidedState = FORCE_TWO_SIDE
        endPlane.angleX(70f)
        endPlane.position(X, -0.6f, -5f)
        scene.add(endPlane)
        this.diffuse(endPlane, "end", khelp.k3d.render.GREEN)

        // Position the moved blue box at start position
        this.movedBox.position(-X, 0f, -5f)
        this.movedBox.angleX(70f)
        scene.add(this.movedBox)
        this.diffuse(this.movedBox, "move", khelp.k3d.render.BLUE)
    }

    /**
     * Create an animation for an interpolation type
     * @param interpolation Interpolation to use
     * @param changeAnimationListener Listener to call when animation start. The parameter is the interpolation name
     * @return Create animation
     */
    private fun createAnimation(interpolation: Interpolation, changeAnimationListener: (String) -> Unit): Animation
    {
        val animation = AnimationParallel()
        // Alert listener that animation for this interpolation start
        animation.addAnimation(AnimationTask<String, Unit>(changeAnimationListener, interpolation.javaClass.simpleName))

        // Add animation with the interpolation
        val animationMove = AnimationPositionNode(this.movedBox)
        // Start to end
        animationMove.addFrame(ANIMATION_FRAME, PositionNode(X, 0f, -5f, angleX = 70f), interpolation)
        // End to start
        animationMove.addFrame(ANIMATION_FRAME * 2, PositionNode(-X, 0f, -5f, angleX = 70f), interpolation)
        animation.addAnimation(animationMove)

        return animation
    }

    /**
     * Create the complete animation
     * @param changeAnimationListener Listener to callback on animation status change. The parameter is the state description
     * @return Created animation
     */
    fun createAnimation(changeAnimationListener: (String) -> Unit): Animation
    {
        val animation = AnimationList()

        (1..REPEAT).forEach {
            // Signal the current turn
            animation.addAnimation(AnimationTask<String, Unit>(changeAnimationListener, "Turn $it on $REPEAT"))
            animation.addAnimation(AnimationPause(PAUSE_FRAME))

            //Add interpolations animation
            INTERPOLATIONS.scramble()

            INTERPOLATIONS.forEach {
                //Add current interpolation animation
                animation.addAnimation(this.createAnimation(it, changeAnimationListener))
                // Make a pause between each animation
                animation.addAnimation(AnimationPause(PAUSE_FRAME))
            }
        }

        return animation
    }
}