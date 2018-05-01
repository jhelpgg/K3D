package khelp.k3d.geometry.prebuilt

import khelp.images.JHelpImage
import khelp.images.dynamic.Interpolation
import khelp.images.dynamic.LinearInterpolation
import khelp.k3d.animation.Animation
import khelp.k3d.animation.AnimationKeyFrame
import khelp.k3d.geometry.Box
import khelp.k3d.geometry.CrossUV
import khelp.k3d.geometry.Revolution
import khelp.k3d.geometry.Sphere
import khelp.k3d.render.Color4f
import khelp.k3d.render.Material
import khelp.k3d.render.Node
import khelp.k3d.render.ObjectClone
import khelp.k3d.render.Point2D
import khelp.k3d.render.Texture
import khelp.k3d.resources.Eyes
import khelp.k3d.resources.Mouth
import khelp.k3d.resources.obtainResourceTexture
import khelp.k3d.resources.resourcesK3D
import khelp.math.limit
import khelp.math.moduloInterval
import khelp.math.random
import khelp.util.WHITE
import java.util.concurrent.atomic.AtomicInteger

class RobotPosition(neckAngleX: Float = 0f, neckAngleY: Float = 0f, neckAngleZ: Float = 0f,
                    rightShoulderAngleX: Float = 180f, rightShoulderAngleZ: Float = 0f,
                    rightAfterArmAngleX: Float = 0f,
                    leftShoulderAngleX: Float = 180f, leftShoulderAngleZ: Float = 0f,
                    leftAfterArmAngleX: Float = 0f,
                    rightAssAngleX: Float = 180f, rightAssAngleZ: Float = 0f,
                    rightAfterLegAngleX: Float = 0f,
                    leftAssAngleX: Float = 180f, leftAssAngleZ: Float = 0f,
                    leftAfterLegAngleX: Float = 0f)
{
    var neckAngleX = limit(neckAngleX, -45f, 45f)
    var neckAngleY = limit(neckAngleY, -90f, 90f)
    var neckAngleZ = limit(neckAngleZ, -22f, 22f)

    var rightShoulderAngleX = moduloInterval(rightShoulderAngleX, 0f, 360f)
    var rightShoulderAngleZ = limit(rightShoulderAngleZ, 0f, 180f)

    var rightAfterArmAngleX = limit(rightAfterArmAngleX, -150f, 0f)

    var leftShoulderAngleX = moduloInterval(leftShoulderAngleX, 0f, 360f)
    var leftShoulderAngleZ = limit(leftShoulderAngleZ, -180f, 0f)

    var leftAfterArmAngleX = limit(leftAfterArmAngleX, -150f, 0f)

    var rightAssAngleX = limit(rightAssAngleX, 90f, 270f)
    var rightAssAngleZ = limit(rightAssAngleZ, -30f, 90f)

    var rightAfterLegAngleX = limit(rightAfterLegAngleX, 0f, 150f)

    var leftAssAngleX = limit(leftAssAngleX, 90f, 270f)
    var leftAssAngleZ = limit(leftAssAngleZ, -90f, 30f)

    var leftAfterLegAngleX = limit(leftAfterLegAngleX, 0f, 150f)
}

class RobotAnimation(robot: Robot) : AnimationKeyFrame<Robot, RobotPosition>(robot)
{
    /**
     * Interpolate a value and change the obj state
     *
     * @param obj  Object to change
     * @param before  Value just before the wanted state
     * @param after   Value just after the wanted state
     * @param percent Percent of invoke
     */
    override fun interpolateValue(obj: Robot, before: RobotPosition, after: RobotPosition, percent: Float)
    {
        val anti = 1f - percent
        val robotPosition = RobotPosition(before.neckAngleX * anti + after.neckAngleX * percent,
                                          before.neckAngleY * anti + after.neckAngleY * percent,
                                          before.neckAngleZ * anti + after.neckAngleZ * percent,
                                          before.rightShoulderAngleX * anti + after.rightShoulderAngleX * percent,
                                          before.rightShoulderAngleZ * anti + after.rightShoulderAngleZ * percent,
                                          before.rightAfterArmAngleX * anti + after.rightAfterArmAngleX * percent,
                                          before.leftShoulderAngleX * anti + after.leftShoulderAngleX * percent,
                                          before.leftShoulderAngleZ * anti + after.leftShoulderAngleZ * percent,
                                          before.leftAfterArmAngleX * anti + after.leftAfterArmAngleX * percent,
                                          before.rightAssAngleX * anti + after.rightAssAngleX * percent,
                                          before.rightAssAngleZ * anti + after.rightAssAngleZ * percent,
                                          before.rightAfterLegAngleX * anti + after.rightAfterLegAngleX * percent,
                                          before.leftAssAngleX * anti + after.leftAssAngleX * percent,
                                          before.leftAssAngleZ * anti + after.leftAssAngleZ * percent,
                                          before.leftAfterLegAngleX * anti + after.leftAfterLegAngleX * percent)
        obj.robotPosition(robotPosition)
    }

    /**
     * Give the actual value for an object
     *
     * @param obj Object we want extract the value
     * @return The actual value
     */
    override fun obtainValue(obj: Robot) = obj.robotPosition()

    /**
     * Change object state
     *
     * @param obj Object to change
     * @param value  New state value
     */
    override fun setValue(obj: Robot, value: RobotPosition) = obj.robotPosition(value)
}

class Head(leftEye: Eyes = Eyes.EyeGreen2, rightEye: Eyes = Eyes.EyeGreen2, mouth: Mouth = Mouth.Smile2)
{
    companion object
    {
        internal val NEXT_ID = AtomicInteger(0)
        internal val HAIR = resourcesK3D.obtainJHelpImage("textures/hair1.png")
    }

    internal val texture = Texture("Robot-Head_${Head.NEXT_ID.getAndIncrement()}", 512, 512, WHITE)
    private val image = JHelpImage(512, 512, WHITE)
    private val leftEyeSprite = this.image.createSprite(303 - 32, 128 + 64, leftEye.image)
    private val rightEyeSprite = this.image.createSprite(209 - 32, 128 + 64, rightEye.image)
    private val hairSprite = this.image.createSprite(0, 0, 512, 512)
    private val mouthSprite = this.image.createSprite(192, 300, mouth.image)
    var leftEye = leftEye
        private set
    var rightEye = rightEye
        private set
    var mouth = mouth
        private set

    init
    {
        this.leftEyeSprite.visible(true)
        this.rightEyeSprite.visible(true)
        this.hairSprite.visible(true)
        this.mouthSprite.visible(true)
        this.hairColor()
        this.texture.setImage(this.image)
    }

    fun mouth(mouth: Mouth)
    {
        if (this.mouth == mouth)
        {
            return
        }

        this.mouth = mouth
        val image = this.mouthSprite.image()
        this.mouthSprite.visible(false)
        image.startDrawMode()
        image.clear(0)
        image.drawImage(0, 0, mouth.image)
        image.endDrawMode()
        this.mouthSprite.visible(true)
        this.texture.setImage(this.image)
    }

    fun leftEye(leftEye: Eyes)
    {
        if (this.leftEye == leftEye)
        {
            return
        }

        this.leftEye = leftEye
        val image = this.leftEyeSprite.image()
        this.leftEyeSprite.visible(false)
        image.startDrawMode()
        image.clear(0)
        image.drawImage(0, 0, leftEye.image)
        image.endDrawMode()
        this.leftEyeSprite.visible(true)
        this.texture.setImage(this.image)
    }

    fun rightEye(rightEye: Eyes)
    {
        if (this.rightEye == rightEye)
        {
            return
        }

        this.rightEye = rightEye
        val image = this.rightEyeSprite.image()
        this.rightEyeSprite.visible(false)
        image.startDrawMode()
        image.clear(0)
        image.drawImage(0, 0, rightEye.image)
        image.endDrawMode()
        this.rightEyeSprite.visible(true)
        this.texture.setImage(this.image)
    }

    fun bothEyes(eye: Eyes)
    {
        this.leftEye(eye)
        this.rightEye(eye)
    }

    fun hairColor(color: Int = 0xFFA0661C.toInt())
    {
        val xs = intArrayOf(0, 512, 512, 342, 333, 322, 342, 333, 321, 300, 250, 170, 150, 160, 170, 0)
        val ys = intArrayOf(0, 0, 512, 512, 256, 300, 170, 150, 160, 170, 155, 170, 250, 300, 512, 512)
        val size = Math.min(xs.size, ys.size)
        val image = this.hairSprite.image()
        val hair = Head.HAIR.copy()
        hair.startDrawMode()
        hair.tint(color)
        hair.endDrawMode()
        this.hairSprite.visible(false)
        image.startDrawMode()
        image.clear(0)
        image.fillPolygon(xs, 0, ys, 0, size, hair, false)
        image.endDrawMode()
        this.hairSprite.visible(true)
        this.texture.setImage(this.image)
    }
}

class Robot(val headTexture: Head = Head())
{
    companion object
    {
        internal val NEXT_ID = AtomicInteger(0)
    }

    private val id = Robot.NEXT_ID.getAndIncrement()
    val mainNode = Node()
    private val head = Sphere(22, 22)
    private val neck = Node()
    private val body = Box(CrossUV(5f / 22f, 17f / 22f, 0.1f, 0.5f, 0.6f))
    private val backAttach = Node()
    private val rightShoulder = Node()
    private val leftShoulder = Node()
    private val rightAss = Node()
    private val leftAss = Node()
    private val cylinder = Revolution()
    private val rightBeforeArm = ObjectClone(this.cylinder)
    private val rightAfterArm = ObjectClone(this.cylinder)
    private val rightHand = Node()
    private val leftBeforeArm = ObjectClone(this.cylinder)
    private val leftAfterArm = ObjectClone(this.cylinder)
    private val leftHand = Node()
    private val rightBeforeLeg = ObjectClone(this.cylinder)
    private val rightAfterLeg = ObjectClone(this.cylinder)
    private val leftBeforeLeg = ObjectClone(this.cylinder)
    private val leftAfterLeg = ObjectClone(this.cylinder)
    private val materialBody = Material("RobotBody_${this.id}")
    private val materialRightArm = Material("RobotRightArm_${this.id}")
    private val materialLeftArm = Material("RobotLeftArm_${this.id}")
    private val materialRightLeg = Material("RobotRightLeg_${this.id}")
    private val materialLeftLeg = Material("RobotLeftLeg_${this.id}")

    init
    {
        val articulation = Sphere(7, 7)
        this.cylinder.appendLine(Point2D(0.3f, 2f), 0f, Point2D(0.3f, 0f), 1f)
        this.cylinder.refreshRevolution(0f, 1f)

        var material = Material(this.headTexture.texture.textureName())
        material.textureDiffuse = this.headTexture.texture
        this.head.material(material)

        //Head
        this.head.movePoint(this.head.northPole, 0f, -0.25f, 0f, 0.87f, 1)
        this.head.position(0f, 0.75f, 0f)
        this.neck.addChild(this.head)
        this.neck.limitAngleX(-45f, 45f)
        this.neck.limitAngleY(-90f, 90f)
        this.neck.limitAngleZ(-22f, 22f)
        this.neck.position(0f, 2.5f, 0f)
        this.mainNode.addChild(this.neck)

        //Body
        this.body.scale(2.4f, 4f, 1f)
        this.body.material(this.materialBody)
        this.mainNode.addChild(this.body)
        this.bodyTexture("textures/BodyCostume.png")

        this.backAttach.position(0f, 2f, -0.6f)
        this.mainNode.addChild(this.backAttach)

        //Right arm
        val articulation1 = ObjectClone(articulation)
        articulation1.scale(0.3f)
        this.rightShoulder.addChild(articulation1)
        this.rightShoulder.addChild(this.rightBeforeArm)
        this.rightShoulder.position(-1.5f, 1.7f, 0f)
        this.rightShoulder.angleX(180f)
        this.rightShoulder.limitAngleY(0f, 0f)
        this.rightShoulder.limitAngleZ(0f, 180f)
        this.mainNode.addChild(this.rightShoulder)

        val articulation2 = ObjectClone(articulation)
        articulation2.scale(0.3f)
        articulation2.position(0f, 2f, 0f)
        this.rightBeforeArm.addChild(articulation2)
        this.rightAfterArm.position(0f, 2f, 0f)
        this.rightAfterArm.limitAngleX(-150f, 0f)
        this.rightAfterArm.limitAngleY(0f, 0f)
        this.rightAfterArm.limitAngleZ(0f, 0f)
        this.rightBeforeArm.addChild(this.rightAfterArm)

        val articulation3 = ObjectClone(articulation)
        articulation3.scale(0.3f)
        articulation3.position(0f, 2f, 0f)
        this.rightAfterArm.addChild(articulation3)

        this.rightHand.position(0f, 2.1f, 0f)
        this.rightAfterArm.addChild(this.rightHand)

        this.rightShoulder.applyMaterialHierarchically(this.materialRightArm)
        this.rightArmColor()

        //Left arm
        val articulation4 = ObjectClone(articulation)
        articulation4.scale(0.3f)
        this.leftShoulder.addChild(articulation4)
        this.leftShoulder.addChild(this.leftBeforeArm)
        this.leftShoulder.position(1.5f, 1.7f, 0f)
        this.leftShoulder.angleX(180f)
        this.leftShoulder.limitAngleY(0f, 0f)
        this.leftShoulder.limitAngleZ(-180f, 0f)
        this.mainNode.addChild(this.leftShoulder)

        val articulation5 = ObjectClone(articulation)
        articulation5.scale(0.3f)
        articulation5.position(0f, 2f, 0f)
        this.leftBeforeArm.addChild(articulation5)
        this.leftAfterArm.position(0f, 2f, 0f)
        this.leftAfterArm.limitAngleX(-150f, 0f)
        this.leftAfterArm.limitAngleY(0f, 0f)
        this.leftAfterArm.limitAngleZ(0f, 0f)
        this.leftBeforeArm.addChild(this.leftAfterArm)

        val articulation6 = ObjectClone(articulation)
        articulation6.scale(0.3f)
        articulation6.position(0f, 2f, 0f)
        this.leftAfterArm.addChild(articulation6)

        this.leftHand.position(0f, 2.1f, 0f)
        this.leftAfterArm.addChild(this.leftHand)

        this.leftShoulder.applyMaterialHierarchically(this.materialLeftArm)
        this.leftArmColor()

        // Right leg
        val articulation7 = ObjectClone(articulation)
        articulation7.scale(0.3f)
        this.rightAss.addChild(articulation7)
        this.rightAss.addChild(this.rightBeforeLeg)
        this.rightAss.position(-0.6f, -2.2f, 0f)
        this.rightAss.angleX(180f)
        this.rightAss.limitAngleX(90f, 270f)
        this.rightAss.limitAngleY(0f, 0f)
        this.rightAss.limitAngleZ(-30f, 90f)
        this.mainNode.addChild(this.rightAss)

        val articulation8 = ObjectClone(articulation)
        articulation8.scale(0.3f)
        articulation8.position(0f, 2f, 0f)
        this.rightBeforeLeg.addChild(articulation8)
        this.rightAfterLeg.position(0f, 2f, 0f)
        this.rightAfterLeg.limitAngleX(0f, 150f)
        this.rightAfterLeg.limitAngleY(0f, 0f)
        this.rightAfterLeg.limitAngleZ(0f, 0f)
        this.rightBeforeLeg.addChild(this.rightAfterLeg)

        val articulation9 = ObjectClone(articulation)
        articulation9.scale(0.3f)
        articulation9.position(0f, 2f, 0f)
        this.rightAfterLeg.addChild(articulation9)

        this.rightAss.applyMaterialHierarchically(this.materialRightLeg)
        this.rightLegColor()

        // Left leg
        val articulation10 = ObjectClone(articulation)
        articulation10.scale(0.3f)
        this.leftAss.addChild(articulation10)
        this.leftAss.addChild(this.leftBeforeLeg)
        this.leftAss.position(0.6f, -2.2f, 0f)
        this.leftAss.angleX(180f)
        this.leftAss.limitAngleX(90f, 270f)
        this.leftAss.limitAngleY(0f, 0f)
        this.leftAss.limitAngleZ(-90f, 30f)
        this.mainNode.addChild(this.leftAss)

        val articulation11 = ObjectClone(articulation)
        articulation11.scale(0.3f)
        articulation11.position(0f, 2f, 0f)
        this.leftBeforeLeg.addChild(articulation11)
        this.leftAfterLeg.position(0f, 2f, 0f)
        this.leftAfterLeg.limitAngleX(0f, 150f)
        this.leftAfterLeg.limitAngleY(0f, 0f)
        this.leftAfterLeg.limitAngleZ(0f, 0f)
        this.leftBeforeLeg.addChild(this.leftAfterLeg)

        val articulation12 = ObjectClone(articulation)
        articulation12.scale(0.3f)
        articulation12.position(0f, 2f, 0f)
        this.leftAfterLeg.addChild(articulation12)

        this.leftAss.applyMaterialHierarchically(this.materialLeftLeg)
        this.leftLegColor()
    }

    fun randomPosition()
    {
        this.neck.angleX(random(-45f, 45f))
        this.neck.angleY(random(-90f, 90f))
        this.neck.angleZ(random(-22f, 22f))
        this.rightShoulder.angleX(random(0f, 360f))
        this.rightShoulder.angleZ(random(0f, 180f))
        this.rightAfterArm.angleX(random(-150f, 0f))
        this.leftShoulder.angleX(random(0f, 360f))
        this.leftShoulder.angleZ(random(-180f, 0f))
        this.leftAfterArm.angleX(random(-150f, 0f))
        this.rightAss.angleX(random(90f, 270f))
        this.rightAss.angleZ(random(-30f, 90f))
        this.rightAfterLeg.angleX(random(0f, 150f))
        this.leftAss.angleX(random(90f, 270f))
        this.leftAss.angleZ(random(-90f, 30f))
        this.leftAfterLeg.angleX(random(0f, 150f))
    }

    fun leftAfterLeg(angleX: Float = 0f)
    {
        this.leftAfterLeg.angleX(angleX)
    }

    fun rotateLeftAfterLeg(rotateX: Float = 0f)
    {
        this.leftAfterLeg.rotateAngleX(rotateX);
    }

    fun leftAss(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.leftAss.angleX(angleX);
        this.leftAss.angleZ(angleZ);
    }

    fun rotateLeftAss(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.leftAss.rotateAngleX(rotateX);
        this.leftAss.rotateAngleZ(rotateZ);
    }

    fun rightAfterLeg(angleX: Float = 0f)
    {
        this.rightAfterLeg.angleX(angleX)
    }

    fun rotateRightAfterLeg(rotateX: Float = 0f)
    {
        this.rightAfterLeg.rotateAngleX(rotateX);
    }

    fun rightAss(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.rightAss.angleX(angleX);
        this.rightAss.angleZ(angleZ);
    }

    fun rotateRightAss(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.rightAss.rotateAngleX(rotateX);
        this.rightAss.rotateAngleZ(rotateZ);
    }

    fun leftAfterArm(angleX: Float = 0f)
    {
        this.leftAfterArm.angleX(angleX)
    }

    fun rotateLeftAfterArm(rotateX: Float = 0f)
    {
        this.leftAfterArm.rotateAngleX(rotateX);
    }

    fun leftShoulder(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.leftShoulder.angleX(angleX);
        this.leftShoulder.angleZ(angleZ);
    }

    fun rotateLeftShoulder(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.leftShoulder.rotateAngleX(rotateX);
        this.leftShoulder.rotateAngleZ(rotateZ);
    }

    fun rightAfterArm(angleX: Float = 0f)
    {
        this.rightAfterArm.angleX(angleX)
    }

    fun rotateRightAfterArm(rotateX: Float = 0f)
    {
        this.rightAfterArm.rotateAngleX(rotateX);
    }

    fun rightShoulder(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.rightShoulder.angleX(angleX);
        this.rightShoulder.angleZ(angleZ);
    }

    fun rotateRightShoulder(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.rightShoulder.rotateAngleX(rotateX);
        this.rightShoulder.rotateAngleZ(rotateZ);
    }

    fun neck(angleX: Float = 0f, angleY: Float = 0f, angleZ: Float = 0f)
    {
        this.neck.angleX(angleX);
        this.neck.angleY(angleY);
        this.neck.angleZ(angleZ);
    }

    fun rotateNeck(rotateX: Float = 0f, rotateY: Float = 0f, rotateZ: Float = 0f)
    {
        this.neck.rotateAngleX(rotateX);
        this.neck.rotateAngleY(rotateY);
        this.neck.rotateAngleZ(rotateZ);
    }

    fun robotPosition() = RobotPosition(this.neck.angleX(), this.neck.angleY(), this.neck.angleZ(),
                                        this.rightShoulder.angleX(), this.rightShoulder.angleZ(),
                                        this.rightAfterArm.angleX(),
                                        this.leftShoulder.angleX(), this.leftShoulder.angleZ(),
                                        this.leftAfterArm.angleX(),
                                        this.rightAss.angleX(), this.rightAss.angleZ(),
                                        this.rightAfterLeg.angleX(),
                                        this.leftAss.angleX(), this.leftAss.angleZ(),
                                        this.leftAfterLeg.angleX())

    fun robotPosition(robotPosition: RobotPosition)
    {
        this.neck.angleX(robotPosition.neckAngleX)
        this.neck.angleY(robotPosition.neckAngleY)
        this.neck.angleZ(robotPosition.neckAngleZ)

        this.rightShoulder.angleX(robotPosition.rightShoulderAngleX)
        this.rightShoulder.angleZ(robotPosition.rightShoulderAngleZ)

        this.rightAfterArm.angleX(robotPosition.rightAfterArmAngleX)

        this.leftShoulder.angleX(robotPosition.leftShoulderAngleX)
        this.leftShoulder.angleZ(robotPosition.leftShoulderAngleZ)

        this.leftAfterArm.angleX(robotPosition.leftAfterArmAngleX)

        this.rightAss.angleX(robotPosition.rightAssAngleX)
        this.rightAss.angleZ(robotPosition.rightAssAngleZ)

        this.rightAfterLeg.angleX(robotPosition.rightAfterLegAngleX)

        this.leftAss.angleX(robotPosition.leftAssAngleX)
        this.leftAss.angleZ(robotPosition.leftAssAngleZ)

        this.leftAfterLeg.angleX(robotPosition.leftAfterLegAngleX)
    }

    fun startPosition(numberFrame: Int = 1, interpolation: Interpolation = LinearInterpolation): Animation
    {
        val animation = RobotAnimation(this)
        animation.addFrame(Math.max(1, numberFrame), RobotPosition(), interpolation)
        return animation
    }

    fun walk(numberFramePerStep: Int = 2, numberStep: Int = 1): Animation
    {
        val frame = Math.max(2, numberFramePerStep);
        val semiFrame = frame shr 1
        val stepMax = Math.max(1, numberStep)
        val animation = RobotAnimation(this)
        val robotPosition1 = RobotPosition(rightShoulderAngleX = 144f, leftShoulderAngleX = 216f,
                                           rightAssAngleX = 216f, leftAssAngleX = 144f)
        val robotPosition2 = RobotPosition(rightShoulderAngleX = 216f, leftShoulderAngleX = 144f,
                                           rightAssAngleX = 144f, leftAssAngleX = 216f)
        var key = semiFrame
        animation.addFrame(key, robotPosition1)
        var step = 1

        while (step < stepMax)
        {
            key += frame
            animation.addFrame(key, if ((step and 1) == 0) robotPosition1 else robotPosition2)
            step++
        }

        key += semiFrame
        animation.addFrame(key, RobotPosition())
        return animation
    }

    fun run(numberFramePerStep: Int = 1, numberStep: Int = 1): Animation
    {
        val frame = Math.max(1, numberFramePerStep);
        val semiFrame = frame shr 1
        val portion = 5
        val part = frame / portion
        val left = frame - part
        val angle = (36 * portion) / (portion + 1)
        val stepMax = Math.max(1, numberStep)
        val animation = RobotAnimation(this)
        val robotPosition1 = RobotPosition(rightShoulderAngleX = 180f - angle, leftShoulderAngleX = 180f + angle,
                                           rightAfterArmAngleX = -90f, leftAfterArmAngleX = -90f,
                                           rightAssAngleX = 180f + angle, leftAssAngleX = 90f,
                                           rightAfterLegAngleX = 0f, leftAfterLegAngleX = 90f)
        val robotPosition2 = RobotPosition(rightShoulderAngleX = 144f, leftShoulderAngleX = 216f,
                                           rightAfterArmAngleX = -90f, leftAfterArmAngleX = -90f,
                                           rightAssAngleX = 216f, leftAssAngleX = 144f)
        val robotPosition3 = RobotPosition(rightShoulderAngleX = 180f + angle, leftShoulderAngleX = 180f - angle,
                                           rightAfterArmAngleX = -90f, leftAfterArmAngleX = -90f,
                                           rightAssAngleX = 90f, leftAssAngleX = 180f + angle,
                                           rightAfterLegAngleX = 90f, leftAfterLegAngleX = 0f)
        val robotPosition4 = RobotPosition(rightShoulderAngleX = 216f, leftShoulderAngleX = 144f,
                                           rightAfterArmAngleX = -90f, leftAfterArmAngleX = -90f,
                                           rightAssAngleX = 144f, leftAssAngleX = 216f)
        var key = semiFrame
        animation.addFrame(key, robotPosition1)
        var step = 1

        while (step < stepMax)
        {
            key += left
            animation.addFrame(key, if ((step and 1) == 0) robotPosition1 else robotPosition3)
            key += part
            animation.addFrame(key, if ((step and 1) == 0) robotPosition2 else robotPosition4)
            step++
        }

        key += semiFrame
        animation.addFrame(key, RobotPosition())
        return animation
    }

    fun freeRightHand() = this.rightHand.removeAllChildren()

    fun putOnRightHand(node: Node)
    {
        this.freeRightHand()
        this.rightHand.addChild(node)
    }

    fun freeLeftHand() = this.leftHand.removeAllChildren()

    fun putOnLeftHand(node: Node)
    {
        this.freeLeftHand()
        this.leftHand.addChild(node)
    }

    fun freeBack() = this.backAttach.removeAllChildren()

    fun putOnBack(node: Node)
    {
        this.freeBack()
        this.backAttach.addChild(node)
    }

    fun bodyTexture(texture: Texture)
    {
        this.materialBody.textureDiffuse = texture
    }

    fun bodyTexture(textureImage: JHelpImage)
    {
        val texture = Texture.obtainTexture(this.materialBody.name()) ?: Texture(this.materialBody.name(),
                                                                                 textureImage.width,
                                                                                 textureImage.height)
        texture.setImage(textureImage)
        this.materialBody.textureDiffuse = texture
    }

    fun bodyTexture(resourcePath: String) = bodyTexture(obtainResourceTexture(resourcePath))

    fun rightArmColor(color: Color4f = Color4f(0xFF828105.toInt())) = this.materialRightArm.colorDiffuse(color)
    fun leftArmColor(color: Color4f = Color4f(0xFF828105.toInt())) = this.materialLeftArm.colorDiffuse(color)
    fun rightLegColor(color: Color4f = Color4f(0xFF020260.toInt())) = this.materialRightLeg.colorDiffuse(color)
    fun leftLegColor(color: Color4f = Color4f(0xFF020260.toInt())) = this.materialLeftLeg.colorDiffuse(color)
}