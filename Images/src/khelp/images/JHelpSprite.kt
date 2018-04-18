package khelp.images

import khelp.images.dynamic.Position
import khelp.images.dynamic.Positionable

/**
 * Sprite is image can be move aver a [JHelpImage], the system take care about image background.
 *
 * To create a sprite use [JHelpImage.createSprite]
 *
 * @param x Sprite X position in image parent
 * @param y Sprite Y position in image parent
 * @param width Sprite width
 * @param height Sprite height
 * @param parent Image parent where sprite is draw
 * @param spriteIndex Sprite index in parent image
 */
class JHelpSprite internal constructor(private var x: Int, private var y: Int,
                                       private var width: Int, private var height: Int,
                                       private var parent: JHelpImage, private var spriteIndex: Int) : Positionable
{
    /**Image draw by sprtie*/
    private val image = JHelpImage(this.width, this.height)
    /**Background image from parent to manage background*/
    private val back = JHelpImage(this.width, this.height)
    /**Sprite visible status*/
    private var visible = false

    /**
     * Create sprite with an image to copy
     * @param x Sprite X position in image parent
     * @param y Sprite Y position in image parent
     * @param source Image to copy on sprite
     * @param parent Image parent where sprite is draw
     * @param spriteIndex Sprite index in parent image
     */
    internal constructor(x: Int, y: Int, source: JHelpImage, parent: JHelpImage, spriteIndex: Int) :
            this(x, y, source.width, source.height, parent, spriteIndex)
    {
        this.image.copy(source)
    }

    /**
     * Change internally the sprite visibility
     *
     * @param visible New visible state
     */
    internal fun changeVisible(visible: Boolean)
    {
        if (visible)
        {
            this.image.refresh()
        }

        if (this.visible != visible)
        {
            if (this.visible)
            {
                this.parent.drawImageOver(this.x, this.y, this.back, 0, 0, this.width, this.height)
            }
            else
            {
                this.back.drawImageOver(0, 0, this.parent, this.x, this.y, this.width, this.height)
                this.parent.drawImageInternal(this.x, this.y, this.image, 0, 0, this.width, this.height, true)
            }

            this.visible = visible
        }
    }

    /**
     * Sprite index in image parent
     */
    internal fun spriteIndex() = this.spriteIndex;

    /**
     * Change sprite index in image parent
     * @param spriteIndex New sprite index
     */
    internal fun spriteIndex(spriteIndex: Int)
    {
        this.spriteIndex = spriteIndex
    }

    /**
     * Sprite X
     */
    fun x() = this.x

    /**
     * Sprite Y
     */
    fun y() = this.y

    /**
     * Sprite width
     */
    fun width() = this.width

    /**
     * Sprite height
     */
    fun height() = this.height

    /**
     * Image to use for draw on sprite
     */
    fun image() = this.image

    /**
     * Image parent
     */
    fun parent() = this.parent

    /**
     * Change parent image
     * @param parent Ne parent
     */
    internal fun parent(parent: JHelpImage)
    {
        this.parent = parent
    }

    /**
     * Indicates if sprite is visible
     */
    fun visible() = this.visible;

    /**
     * Change sprite visibility
     * @param visible New visible status
     */
    fun visible(visible: Boolean) = this.parent.changeSpriteVisibility(this.spriteIndex, visible)

    /**
     * Change sprite position
     * @param x New X
     * @param y New Y
     */
    fun position(x: Int, y: Int)
    {
        if (this.x != x || this.y != y)
        {
            val visible = this.visible

            if (visible)
            {
                this.visible(false)
            }

            this.x = x
            this.y = y

            if (visible)
            {
                this.visible(true)
            }
        }
    }

    /**
     * Sprite current position
     */
    override fun position() = Position(this.x, this.y)

    /**
     * Change sprite position
     * @param position New sprite position
     */
    override fun position(position: Position) = this.position(position.x, position.y)

    /**
     * Translate the sprite
     *
     * @param vx X of translation vector
     * @param vy Y of translation vector
     */
    fun translate(vx: Int, vy: Int) = this.position(vx + this.x, vy + this.y)
}