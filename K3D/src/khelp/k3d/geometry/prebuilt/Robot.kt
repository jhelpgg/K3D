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
import khelp.k3d.util.ThreadAnimation
import khelp.math.limit
import khelp.math.moduloInterval
import khelp.math.random
import khelp.util.WHITE
import java.util.concurrent.atomic.AtomicInteger

/**
 * Describe [Robot] joints position
 * @param neckAngleX Neck angle around X axis in `[-45, 45]`
 * @param neckAngleY Neck angle around Y axis in `[-90, 90]`
 * @param neckAngleZ Neck angle around Z axis in `[-22, 22]`
 * @param rightShoulderAngleX Right shoulder angle around X axis
 * @param rightShoulderAngleZ Right shoulder angle around Z axis in `[0, 180]`
 * @param rightElbowAngleX Right elbow angle around X axis in `[-150, 0]`
 * @param leftShoulderAngleX Left shoulder angle around X axis
 * @param leftShoulderAngleZ Left shoulder angle around Z axis in `[-180, 0]`
 * @param leftElbowAngleX Left elbow angle around X axis in `[-150, 0]`
 * @param rightAssAngleX Right ass angle around X axis in `[90, 270]`
 * @param rightAssAngleZ Right ass angle around Z axis in `[-30, 90]`
 * @param rightKneeAngleX Right knee angle around X axis in `[0, 150]`
 * @param leftAssAngleX Left ass angle around X axis in `[90, 270]`
 * @param leftAssAngleZ Left ass angle around Z axis in `[-90, 30]`
 * @param leftKneeAngleX Left knee angle around X axis in `[0, 150]`
 */
class RobotPosition(neckAngleX: Float = 0f, neckAngleY: Float = 0f, neckAngleZ: Float = 0f,
                    rightShoulderAngleX: Float = 180f, rightShoulderAngleZ: Float = 0f,
                    rightElbowAngleX: Float = 0f,
                    leftShoulderAngleX: Float = 180f, leftShoulderAngleZ: Float = 0f,
                    leftElbowAngleX: Float = 0f,
                    rightAssAngleX: Float = 180f, rightAssAngleZ: Float = 0f,
                    rightKneeAngleX: Float = 0f,
                    leftAssAngleX: Float = 180f, leftAssAngleZ: Float = 0f,
                    leftKneeAngleX: Float = 0f)
{
    var neckAngleX = limit(neckAngleX, -45f, 45f)
    var neckAngleY = limit(neckAngleY, -90f, 90f)
    var neckAngleZ = limit(neckAngleZ, -22f, 22f)

    var rightShoulderAngleX = moduloInterval(rightShoulderAngleX, 0f, 360f)
    var rightShoulderAngleZ = limit(rightShoulderAngleZ, 0f, 180f)

    var rightElbowAngleX = limit(rightElbowAngleX, -150f, 0f)

    var leftShoulderAngleX = moduloInterval(leftShoulderAngleX, 0f, 360f)
    var leftShoulderAngleZ = limit(leftShoulderAngleZ, -180f, 0f)

    var leftElbowAngleX = limit(leftElbowAngleX, -150f, 0f)

    var rightAssAngleX = limit(rightAssAngleX, 90f, 270f)
    var rightAssAngleZ = limit(rightAssAngleZ, -30f, 90f)

    var rightKneeAngleX = limit(rightKneeAngleX, 0f, 150f)

    var leftAssAngleX = limit(leftAssAngleX, 90f, 270f)
    var leftAssAngleZ = limit(leftAssAngleZ, -90f, 30f)

    var leftKneeAngleX = limit(leftKneeAngleX, 0f, 150f)
}

/**
 * Animation for [Robot].
 *
 * Specifies [RobotPosition] at specific frames and interpolate frames between
 * @param robot Robot to animate
 */
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
    @ThreadAnimation
    override fun interpolateValue(obj: Robot, before: RobotPosition, after: RobotPosition, percent: Float)
    {
        val anti = 1f - percent
        val robotPosition = RobotPosition(before.neckAngleX * anti + after.neckAngleX * percent,
                                          before.neckAngleY * anti + after.neckAngleY * percent,
                                          before.neckAngleZ * anti + after.neckAngleZ * percent,
                                          before.rightShoulderAngleX * anti + after.rightShoulderAngleX * percent,
                                          before.rightShoulderAngleZ * anti + after.rightShoulderAngleZ * percent,
                                          before.rightElbowAngleX * anti + after.rightElbowAngleX * percent,
                                          before.leftShoulderAngleX * anti + after.leftShoulderAngleX * percent,
                                          before.leftShoulderAngleZ * anti + after.leftShoulderAngleZ * percent,
                                          before.leftElbowAngleX * anti + after.leftElbowAngleX * percent,
                                          before.rightAssAngleX * anti + after.rightAssAngleX * percent,
                                          before.rightAssAngleZ * anti + after.rightAssAngleZ * percent,
                                          before.rightKneeAngleX * anti + after.rightKneeAngleX * percent,
                                          before.leftAssAngleX * anti + after.leftAssAngleX * percent,
                                          before.leftAssAngleZ * anti + after.leftAssAngleZ * percent,
                                          before.leftKneeAngleX * anti + after.leftKneeAngleX * percent)
        obj.robotPosition(robotPosition)
    }

    /**
     * Give the actual value for an object
     *
     * @param obj Object we want extract the value
     * @return The actual value
     */
    @ThreadAnimation
    override fun obtainValue(obj: Robot) = obj.robotPosition()

    /**
     * Change object state
     *
     * @param obj Object to change
     * @param value  New state value
     */
    @ThreadAnimation
    override fun setValue(obj: Robot, value: RobotPosition) = obj.robotPosition(value)
}

/**
 * [Robot] head parameters
 * @param leftEye Define the left eye
 * @param rightEye Define the right eye
 * @param mouth Define mouth expression
 */
class Head(leftEye: Eyes = Eyes.EyeGreen2, rightEye: Eyes = Eyes.EyeGreen2, mouth: Mouth = Mouth.Smile2)
{
    companion object
    {
        /**Next head ID*/
        internal val NEXT_ID = AtomicInteger(0)
        /**Hair base image*/
        internal val HAIR = resourcesK3D.obtainJHelpImage("textures/hair1.png")
    }

    /**Texture where head is rendered*/
    internal val texture = Texture("Robot-Head_${Head.NEXT_ID.getAndIncrement()}", 512, 512, WHITE)
    /**Image where head is draw*/
    private val image = JHelpImage(512, 512, WHITE)
    /**Sprite for left eye*/
    private val leftEyeSprite = this.image.createSprite(303 - 32, 128 + 64, leftEye.image)
    /**Sprite for right eye*/
    private val rightEyeSprite = this.image.createSprite(209 - 32, 128 + 64, rightEye.image)
    /**Sprite for hair*/
    private val hairSprite = this.image.createSprite(0, 0, 512, 512)
    /**Sprite for mouth*/
    private val mouthSprite = this.image.createSprite(192, 300, mouth.image)
    /**Current left eye*/
    var leftEye = leftEye
        private set
    /**Current right eye*/
    var rightEye = rightEye
        private set
    /**Current mouth*/
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

    /**
     * Change mouth expression
     * @param mouth New mouth expression
     */
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

    /**
     * Change left eye
     * @param leftEye New left eye
     */
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

    /**
     * Change right eye
     * @param rightEye New right eye
     */
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

    /**
     * Change both eyes in same time
     * @param eye New left an right eyes
     */
    fun bothEyes(eye: Eyes)
    {
        this.leftEye(eye)
        this.rightEye(eye)
    }

    /**
     * Change hair color
     * @param color New color
     */
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

/**
 * Create a *"robot"* that can be animated
 * @param headTexture Head parameters
 */
class Robot(val headTexture: Head = Head())
{
    companion object
    {
        /**Robot next ID*/
        internal val NEXT_ID = AtomicInteger(0)
    }

    /**Robot id*/
    val id = Robot.NEXT_ID.getAndIncrement()
    /**Node that contains the whole robot. Use it to place robot in scene*/
    val mainNode = Node()
    /**Head object*/
    private val head = Sphere(22, 22)
    /**Neck node. It it b the joint for move the head*/
    private val neck = Node()
    /**Body object*/
    private val body = Box(CrossUV(5f / 22f, 17f / 22f, 0.1f, 0.5f, 0.6f))
    /**Node for attach something in the back*/
    private val backAttach = Node()
    /**Right shoulder joint*/
    private val rightShoulder = Node()
    /**Left shoulder joint*/
    private val leftShoulder = Node()
    /**Right ass joint*/
    private val rightAss = Node()
    /**Left ass joint*/
    private val leftAss = Node()
    /**Arms/legs part base*/
    private val cylinder = Revolution()
    /**Right before arm object*/
    private val rightBeforeArm = ObjectClone(this.cylinder)
    /**Right after arm object*/
    private val rightAfterArm = ObjectClone(this.cylinder)
    /**Right hand where can be attach something*/
    private val rightHand = Node()
    /**Left before arm object*/
    private val leftBeforeArm = ObjectClone(this.cylinder)
    /**Left after arm object*/
    private val leftAfterArm = ObjectClone(this.cylinder)
    /**Left hand where can be attach something*/
    private val leftHand = Node()
    /**Right before leg object*/
    private val rightBeforeLeg = ObjectClone(this.cylinder)
    /**Right after leg object*/
    private val rightAfterLeg = ObjectClone(this.cylinder)
    /**Left before leg object*/
    private val leftBeforeLeg = ObjectClone(this.cylinder)
    /**Left after leg object*/
    private val leftAfterLeg = ObjectClone(this.cylinder)
    /**Body material*/
    private val materialBody = Material.obtainMaterialOrCreate("RobotBody_${this.id}")
    /**Complete right arm material*/
    private val materialRightArm = Material.obtainMaterialOrCreate("RobotRightArm_${this.id}")
    /**Complete left arm material*/
    private val materialLeftArm = Material.obtainMaterialOrCreate("RobotLeftArm_${this.id}")
    /**Complete right leg material*/
    private val materialRightLeg = Material.obtainMaterialOrCreate("RobotRightLeg_${this.id}")
    /**Complete left leg material*/
    private val materialLeftLeg = Material.obtainMaterialOrCreate("RobotLeftLeg_${this.id}")

    init
    {
        val joint = Sphere(7, 7)
        this.cylinder.appendLine(Point2D(0.3f, 2f), 0f, Point2D(0.3f, 0f), 1f)
        this.cylinder.refreshRevolution(0f, 1f)

        var material = Material.obtainMaterialOrCreate(this.headTexture.texture.textureName())
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
        val joint1 = ObjectClone(joint)
        joint1.scale(0.3f)
        this.rightShoulder.addChild(joint1)
        this.rightShoulder.addChild(this.rightBeforeArm)
        this.rightShoulder.position(-1.5f, 1.7f, 0f)
        this.rightShoulder.angleX(180f)
        this.rightShoulder.limitAngleY(0f, 0f)
        this.rightShoulder.limitAngleZ(0f, 180f)
        this.mainNode.addChild(this.rightShoulder)

        val joint2 = ObjectClone(joint)
        joint2.scale(0.3f)
        joint2.position(0f, 2f, 0f)
        this.rightBeforeArm.addChild(joint2)
        this.rightAfterArm.position(0f, 2f, 0f)
        this.rightAfterArm.limitAngleX(-150f, 0f)
        this.rightAfterArm.limitAngleY(0f, 0f)
        this.rightAfterArm.limitAngleZ(0f, 0f)
        this.rightBeforeArm.addChild(this.rightAfterArm)

        val joint3 = ObjectClone(joint)
        joint3.scale(0.3f)
        joint3.position(0f, 2f, 0f)
        this.rightAfterArm.addChild(joint3)

        this.rightHand.position(0f, 2.1f, 0f)
        this.rightAfterArm.addChild(this.rightHand)

        this.rightShoulder.applyMaterialHierarchically(this.materialRightArm)
        this.rightArmColor()

        //Left arm
        val joint4 = ObjectClone(joint)
        joint4.scale(0.3f)
        this.leftShoulder.addChild(joint4)
        this.leftShoulder.addChild(this.leftBeforeArm)
        this.leftShoulder.position(1.5f, 1.7f, 0f)
        this.leftShoulder.angleX(180f)
        this.leftShoulder.limitAngleY(0f, 0f)
        this.leftShoulder.limitAngleZ(-180f, 0f)
        this.mainNode.addChild(this.leftShoulder)

        val joint5 = ObjectClone(joint)
        joint5.scale(0.3f)
        joint5.position(0f, 2f, 0f)
        this.leftBeforeArm.addChild(joint5)
        this.leftAfterArm.position(0f, 2f, 0f)
        this.leftAfterArm.limitAngleX(-150f, 0f)
        this.leftAfterArm.limitAngleY(0f, 0f)
        this.leftAfterArm.limitAngleZ(0f, 0f)
        this.leftBeforeArm.addChild(this.leftAfterArm)

        val joint6 = ObjectClone(joint)
        joint6.scale(0.3f)
        joint6.position(0f, 2f, 0f)
        this.leftAfterArm.addChild(joint6)

        this.leftHand.position(0f, 2.1f, 0f)
        this.leftAfterArm.addChild(this.leftHand)

        this.leftShoulder.applyMaterialHierarchically(this.materialLeftArm)
        this.leftArmColor()

        // Right leg
        val joint7 = ObjectClone(joint)
        joint7.scale(0.3f)
        this.rightAss.addChild(joint7)
        this.rightAss.addChild(this.rightBeforeLeg)
        this.rightAss.position(-0.6f, -2.2f, 0f)
        this.rightAss.angleX(180f)
        this.rightAss.limitAngleX(90f, 270f)
        this.rightAss.limitAngleY(0f, 0f)
        this.rightAss.limitAngleZ(-30f, 90f)
        this.mainNode.addChild(this.rightAss)

        val joint8 = ObjectClone(joint)
        joint8.scale(0.3f)
        joint8.position(0f, 2f, 0f)
        this.rightBeforeLeg.addChild(joint8)
        this.rightAfterLeg.position(0f, 2f, 0f)
        this.rightAfterLeg.limitAngleX(0f, 150f)
        this.rightAfterLeg.limitAngleY(0f, 0f)
        this.rightAfterLeg.limitAngleZ(0f, 0f)
        this.rightBeforeLeg.addChild(this.rightAfterLeg)

        val joint9 = ObjectClone(joint)
        joint9.scale(0.3f)
        joint9.position(0f, 2f, 0f)
        this.rightAfterLeg.addChild(joint9)

        this.rightAss.applyMaterialHierarchically(this.materialRightLeg)
        this.rightLegColor()

        // Left leg
        val joint10 = ObjectClone(joint)
        joint10.scale(0.3f)
        this.leftAss.addChild(joint10)
        this.leftAss.addChild(this.leftBeforeLeg)
        this.leftAss.position(0.6f, -2.2f, 0f)
        this.leftAss.angleX(180f)
        this.leftAss.limitAngleX(90f, 270f)
        this.leftAss.limitAngleY(0f, 0f)
        this.leftAss.limitAngleZ(-90f, 30f)
        this.mainNode.addChild(this.leftAss)

        val joint11 = ObjectClone(joint)
        joint11.scale(0.3f)
        joint11.position(0f, 2f, 0f)
        this.leftBeforeLeg.addChild(joint11)
        this.leftAfterLeg.position(0f, 2f, 0f)
        this.leftAfterLeg.limitAngleX(0f, 150f)
        this.leftAfterLeg.limitAngleY(0f, 0f)
        this.leftAfterLeg.limitAngleZ(0f, 0f)
        this.leftBeforeLeg.addChild(this.leftAfterLeg)

        val joint12 = ObjectClone(joint)
        joint12.scale(0.3f)
        joint12.position(0f, 2f, 0f)
        this.leftAfterLeg.addChild(joint12)

        this.leftAss.applyMaterialHierarchically(this.materialLeftLeg)
        this.leftLegColor()
    }

    /**
     * Put robot at random position
     */
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

    /**
     * Change left knee position
     * @param angleX Angle around X axis
     */
    fun leftKnee(angleX: Float = 0f)
    {
        this.leftAfterLeg.angleX(angleX)
    }

    /**
     * Rotate left knee
     * @param rotateX Angle around X axis to add
     */
    fun rotateLeftKnee(rotateX: Float = 0f)
    {
        this.leftAfterLeg.rotateAngleX(rotateX);
    }

    /**
     * Change left ass position
     * @param angleX Angle around X axis
     * @param angleZ Angle around Z axis
     */
    fun leftAss(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.leftAss.angleX(angleX);
        this.leftAss.angleZ(angleZ);
    }

    /**
     * Rotate left ass position
     * @param rotateX Angle around X axis to add
     * @param rotateZ Angle around Z axis to add
     */
    fun rotateLeftAss(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.leftAss.rotateAngleX(rotateX);
        this.leftAss.rotateAngleZ(rotateZ);
    }

    /**
     * Change right knee position
     * @param angleX Angle around X axis
     */
    fun rightKnee(angleX: Float = 0f)
    {
        this.rightAfterLeg.angleX(angleX)
    }

    /**
     * Rotate right knee
     * @param rotateX Angle around X axis to add
     */
    fun rotateRightKnee(rotateX: Float = 0f)
    {
        this.rightAfterLeg.rotateAngleX(rotateX);
    }

    /**
     * Change right ass position
     * @param angleX Angle around X axis
     * @param angleZ Angle around Z axis
     */
    fun rightAss(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.rightAss.angleX(angleX);
        this.rightAss.angleZ(angleZ);
    }

    /**
     * Rotate right ass position
     * @param rotateX Angle around X axis to add
     * @param rotateZ Angle around Z axis to add
     */
    fun rotateRightAss(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.rightAss.rotateAngleX(rotateX);
        this.rightAss.rotateAngleZ(rotateZ);
    }

    /**
     * Change left elbow position
     * @param angleX Angle around X axis
     */
    fun leftElbow(angleX: Float = 0f)
    {
        this.leftAfterArm.angleX(angleX)
    }

    /**
     * Rotate left elbow
     * @param rotateX Angle around X axis to add
     */
    fun rotateLeftElbow(rotateX: Float = 0f)
    {
        this.leftAfterArm.rotateAngleX(rotateX);
    }

    /**
     * Change left shoulder position
     * @param angleX Angle around X axis
     * @param angleZ Angle around Z axis
     */
    fun leftShoulder(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.leftShoulder.angleX(angleX);
        this.leftShoulder.angleZ(angleZ);
    }

    /**
     * Rotate left shoulder position
     * @param rotateX Angle around X axis to add
     * @param rotateZ Angle around Z axis to add
     */
    fun rotateLeftShoulder(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.leftShoulder.rotateAngleX(rotateX);
        this.leftShoulder.rotateAngleZ(rotateZ);
    }

    /**
     * Change right elbow position
     * @param angleX Angle around X axis
     */
    fun rightElbow(angleX: Float = 0f)
    {
        this.rightAfterArm.angleX(angleX)
    }

    /**
     * Rotate right elbow
     * @param rotateX Angle around X axis to add
     */
    fun rotateRightElbow(rotateX: Float = 0f)
    {
        this.rightAfterArm.rotateAngleX(rotateX);
    }

    /**
     * Change right shoulder position
     * @param angleX Angle around X axis
     * @param angleZ Angle around Z axis
     */
    fun rightShoulder(angleX: Float = 180f, angleZ: Float = 0f)
    {
        this.rightShoulder.angleX(angleX);
        this.rightShoulder.angleZ(angleZ);
    }

    /**
     * Rotate right shoulder position
     * @param rotateX Angle around X axis to add
     * @param rotateZ Angle around Z axis to add
     */
    fun rotateRightShoulder(rotateX: Float = 0f, rotateZ: Float = 0f)
    {
        this.rightShoulder.rotateAngleX(rotateX);
        this.rightShoulder.rotateAngleZ(rotateZ);
    }

    /**
     * Change neck position
     * @param angleX Angle around X axis
     * @param angleY Angle around Y axis
     * @param angleZ Angle around Z axis
     */
    fun neck(angleX: Float = 0f, angleY: Float = 0f, angleZ: Float = 0f)
    {
        this.neck.angleX(angleX);
        this.neck.angleY(angleY);
        this.neck.angleZ(angleZ);
    }

    /**
     * Rotate neck position
     * @param rotateX Angle around X axis to add
     * @param rotateY Angle around Y axis to add
     * @param rotateZ Angle around Z axis to add
     */
    fun rotateNeck(rotateX: Float = 0f, rotateY: Float = 0f, rotateZ: Float = 0f)
    {
        this.neck.rotateAngleX(rotateX);
        this.neck.rotateAngleY(rotateY);
        this.neck.rotateAngleZ(rotateZ);
    }

    /**
     * Current robot position
     * @return Current robot position
     */
    fun robotPosition() = RobotPosition(this.neck.angleX(), this.neck.angleY(), this.neck.angleZ(),
                                        this.rightShoulder.angleX(), this.rightShoulder.angleZ(),
                                        this.rightAfterArm.angleX(),
                                        this.leftShoulder.angleX(), this.leftShoulder.angleZ(),
                                        this.leftAfterArm.angleX(),
                                        this.rightAss.angleX(), this.rightAss.angleZ(),
                                        this.rightAfterLeg.angleX(),
                                        this.leftAss.angleX(), this.leftAss.angleZ(),
                                        this.leftAfterLeg.angleX())

    /**
     * Change robot position
     * @param robotPosition New robot position
     */
    fun robotPosition(robotPosition: RobotPosition)
    {
        this.neck.angleX(robotPosition.neckAngleX)
        this.neck.angleY(robotPosition.neckAngleY)
        this.neck.angleZ(robotPosition.neckAngleZ)

        this.rightShoulder.angleX(robotPosition.rightShoulderAngleX)
        this.rightShoulder.angleZ(robotPosition.rightShoulderAngleZ)

        this.rightAfterArm.angleX(robotPosition.rightElbowAngleX)

        this.leftShoulder.angleX(robotPosition.leftShoulderAngleX)
        this.leftShoulder.angleZ(robotPosition.leftShoulderAngleZ)

        this.leftAfterArm.angleX(robotPosition.leftElbowAngleX)

        this.rightAss.angleX(robotPosition.rightAssAngleX)
        this.rightAss.angleZ(robotPosition.rightAssAngleZ)

        this.rightAfterLeg.angleX(robotPosition.rightKneeAngleX)

        this.leftAss.angleX(robotPosition.leftAssAngleX)
        this.leftAss.angleZ(robotPosition.leftAssAngleZ)

        this.leftAfterLeg.angleX(robotPosition.leftKneeAngleX)
    }

    /**
     * Create animation that make the robot to return at it start position
     * @param numberFrame Number frame to return to start position
     * @param interpolation Interpolation to use
     * @return Created animation
     */
    fun startPosition(numberFrame: Int = 1, interpolation: Interpolation = LinearInterpolation): Animation
    {
        val animation = RobotAnimation(this)
        animation.addFrame(Math.max(1, numberFrame), RobotPosition(), interpolation)
        return animation
    }

    /**
     * Create animation that make robot to walk
     * @param numberFramePerStep Number frame to make a step
     * @param numberStep Number step to make
     * @return Created animation
     */
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

    /**
     * Create animation that make robot to run
     * @param numberFramePerStep Number frame to make a step
     * @param numberStep Number step to make
     * @return Created animation
     */
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
                                           rightElbowAngleX = -90f, leftElbowAngleX = -90f,
                                           rightAssAngleX = 180f + angle, leftAssAngleX = 90f,
                                           rightKneeAngleX = 0f, leftKneeAngleX = 90f)
        val robotPosition2 = RobotPosition(rightShoulderAngleX = 144f, leftShoulderAngleX = 216f,
                                           rightElbowAngleX = -90f, leftElbowAngleX = -90f,
                                           rightAssAngleX = 216f, leftAssAngleX = 144f)
        val robotPosition3 = RobotPosition(rightShoulderAngleX = 180f + angle, leftShoulderAngleX = 180f - angle,
                                           rightElbowAngleX = -90f, leftElbowAngleX = -90f,
                                           rightAssAngleX = 90f, leftAssAngleX = 180f + angle,
                                           rightKneeAngleX = 90f, leftKneeAngleX = 0f)
        val robotPosition4 = RobotPosition(rightShoulderAngleX = 216f, leftShoulderAngleX = 144f,
                                           rightElbowAngleX = -90f, leftElbowAngleX = -90f,
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

    /**
     * Remove any object in the right hand
     */
    fun freeRightHand() = this.rightHand.removeAllChildren()

    /**
     * Put something in the right hand
     * @param node Main node of the thing to carry
     */
    fun putOnRightHand(node: Node)
    {
        this.freeRightHand()
        this.rightHand.addChild(node)
    }

    /**
     * Remove any object in the left hand
     */
    fun freeLeftHand() = this.leftHand.removeAllChildren()

    /**
     * Put something in the left hand
     * @param node Main node of the thing to carry
     */
    fun putOnLeftHand(node: Node)
    {
        this.freeLeftHand()
        this.leftHand.addChild(node)
    }

    /**
     * Remove any object attach on the back
     */
    fun freeBack() = this.backAttach.removeAllChildren()

    /**
     * Attach something in the back
     * @param node Main node of the thing to attach
     */
    fun putOnBack(node: Node)
    {
        this.freeBack()
        this.backAttach.addChild(node)
    }

    /**
     * Change body texture.
     *
     * To create a good body cross texture, take example from resources:
     * `textures/BoyCostume.png` and its source `textures/BodyCostume.xcf`. Template at `textures/BodytextureBase.xcf`
     * @param texture Body texture
     */
    fun bodyTexture(texture: Texture)
    {
        this.materialBody.textureDiffuse = texture
    }

    /**
     * Change body texture base on image.
     *
     * To create a good body cross texture, take example from resources:
     * `textures/BoyCostume.png` and its source `textures/BodyCostume.xcf`. Template at `textures/BodytextureBase.xcf`
     * @param textureImage Image to use
     */
    fun bodyTexture(textureImage: JHelpImage)
    {
        val texture = Texture.obtainTexture(this.materialBody.name()) ?: Texture(this.materialBody.name(),
                                                                                 textureImage.width,
                                                                                 textureImage.height)
        texture.setImage(textureImage)
        this.materialBody.textureDiffuse = texture
    }

    /**
     * Change body texture from a resource
     * @param resourcePath Resource relative path
     */
    fun bodyTexture(resourcePath: String) = bodyTexture(obtainResourceTexture(resourcePath))

    /**
     * Change right arm color
     * @param color New color
     */
    fun rightArmColor(color: Color4f = Color4f(0xFF828105.toInt())) = this.materialRightArm.colorDiffuse(color)

    /**
     * Change left arm color
     * @param color New color
     */
    fun leftArmColor(color: Color4f = Color4f(0xFF828105.toInt())) = this.materialLeftArm.colorDiffuse(color)

    /**
     * Change right leg color
     * @param color New color
     */
    fun rightLegColor(color: Color4f = Color4f(0xFF020260.toInt())) = this.materialRightLeg.colorDiffuse(color)

    /**
     * Change left left color
     * @param color New color
     */
    fun leftLegColor(color: Color4f = Color4f(0xFF020260.toInt())) = this.materialLeftLeg.colorDiffuse(color)
}