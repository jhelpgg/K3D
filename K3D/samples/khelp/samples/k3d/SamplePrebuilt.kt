package khelp.samples.k3d

import khelp.debug.debug
import khelp.debug.mark
import khelp.k3d.animation.Animation
import khelp.k3d.animation.AnimationList
import khelp.k3d.animation.AnimationPause
import khelp.k3d.animation.AnimationProduce
import khelp.k3d.geometry.prebuilt.Dice
import khelp.k3d.geometry.prebuilt.DiceChangeListener
import khelp.k3d.geometry.prebuilt.Robot
import khelp.k3d.geometry.prebuilt.RobotAnimation
import khelp.k3d.geometry.prebuilt.RobotPosition
import khelp.k3d.geometry.prebuilt.Sword
import khelp.k3d.render.GREEN
import khelp.k3d.render.Window3D
import khelp.k3d.render.event.ActionCode
import khelp.k3d.render.event.ActionListener
import khelp.k3d.resources.Eyes
import khelp.k3d.resources.Mouth

/**
 * https://youtu.be/OsOjPZVLkF0
 */
fun main(args: Array<String>)
{
    val window3D = Window3D.createFullWidow("Robot")
    window3D.showFPS(true)
    val scene = window3D.scene()
    val robot = Robot()
    val mainNode = robot.mainNode
    scene.add(mainNode)
    mainNode.position(0f, 0f, -15f)
    val actionManager = window3D.actionManager()
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
                                                         ActionCode.ACTION_UP       -> mainNode.translate(0f, 0.1f, 0f)
                                                         ActionCode.ACTION_DOWN     -> mainNode.translate(0f, -0.1f, 0f)
                                                         ActionCode.ACTION_LEFT     -> mainNode.translate(-0.1f, 0f, 0f)
                                                         ActionCode.ACTION_RIGHT    -> mainNode.translate(0.1f, 0f, 0f)
                                                         ActionCode.ACTION_BUTTON_5 -> mainNode.translate(0f, 0f, 0.1f)
                                                         ActionCode.ACTION_BUTTON_6 -> mainNode.translate(0f, 0f, -0.1f)
                                                         ActionCode.ACTION_BUTTON_1 -> mainNode.rotateAngleX(1f)
                                                         ActionCode.ACTION_BUTTON_2 -> mainNode.rotateAngleY(-1f)
                                                         ActionCode.ACTION_BUTTON_3 -> mainNode.rotateAngleY(1f)
                                                         ActionCode.ACTION_BUTTON_4 -> mainNode.rotateAngleX(-1f)
                                                         ActionCode.ACTION_EXIT     -> window3D.close()
                                                     }
                                                 }
                                             }
                                         })

    val animationList = AnimationList()
    animationList.addAnimation(robot.run(12, 50))
    window3D.playAnimation(animationList)

    val animationFace = object : Animation
    {
        private var startAbsoluteFrame = 0f

        /**
         * Call by the renderer each time the animation is refresh on playing
         *
         * @param absoluteFrame Actual ABSOLUTE frame
         * @return `true` if the animation need to be refresh one more time. `false` if the animation is end
         */
        override fun animate(absoluteFrame: Float): Boolean
        {
            var index = ((absoluteFrame - this.startAbsoluteFrame).toInt() / 100) % Eyes.values().size
            robot.headTexture.bothEyes(Eyes.values()[index])
            index = ((absoluteFrame - this.startAbsoluteFrame).toInt() / 100) % Mouth.values().size
            robot.headTexture.mouth(Mouth.values()[index])
            return true
        }

        /**
         * Call by the renderer to indicates the start ABSOLUTE frame
         *
         * @param startAbsoluteFrame Start ABSOLUTE frame
         */
        override fun startAbsoluteFrame(startAbsoluteFrame: Float)
        {
            this.startAbsoluteFrame = startAbsoluteFrame
        }
    }

    window3D.playAnimation(animationFace)

    val dice = Dice()
    dice.color(GREEN)
    debug("Dice N°", dice.id, " value=", dice.value)
    mark("GO")
    dice.position(-5f, 5f, -15f)
    scene.add(dice)
    dice.registerDiceChangeListener(object : DiceChangeListener
                                    {
                                        override fun diceChanged(dice: Dice)
                                        {
                                            debug("Dice N°", dice.id, " value=", dice.value)
                                        }
                                    })

    val animationDice = AnimationList()

    (0..10).forEach {
        animationDice.addAnimation(AnimationPause(50))
        animationDice.addAnimation(dice.roll())
    }

    window3D.playAnimation(animationDice)

    val sword = Sword()
    sword.positionForBack()
    robot.putOnBack(sword)

    scene.refresh()

    var animationTakeSword = RobotAnimation(robot)
    animationTakeSword.addFrame(100, RobotPosition(rightShoulderAngleX = 0f, rightElbowAngleX = -150f))
    animationList.addAnimation(animationTakeSword)
    animationList.addAnimation(AnimationProduce<Unit>({
                                                          sword.positionForHand()
                                                          robot.putOnRightHand(sword)
                                                      }))
    animationTakeSword = RobotAnimation(robot)
    animationTakeSword.addFrame(50, RobotPosition(rightShoulderAngleX = 45f, rightElbowAngleX = 0f))
    animationList.addAnimation(animationTakeSword)
}