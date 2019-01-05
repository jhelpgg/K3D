package khelp.ui.textEditor.lineNumber

import khelp.debug.debug
import khelp.images.JHelpFont
import khelp.images.JHelpImage
import khelp.util.ColorInt
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.text.AttributeSet
import javax.swing.text.Element
import javax.swing.text.ParagraphView
import javax.swing.text.StyleConstants

/** Margin to respect between normal text and additional one */
public const val ADDITIONAL_MARGIN = 32
/** [AttributeSet] property key for specify the additional text */
public const val ATTRIBUTE_ADDITIONAL_TEXT = "khelp.ui.textEditor.lineNumber.LineNumberParagraphView.ADDITIONAL_TEXT"
/** [AttributeSet] property key for specify background for line number */
public const val ATTRIBUTE_NUMBER_BACKGROUND = "khelp.ui.textEditor.lineNumber.LineNumberParagraphView.ATTRIBUTE_NUMBER_BACKGROUND"
/** [AttributeSet] property key for specify font for line number */
public const val ATTRIBUTE_NUMBER_FONT = "khelp.ui.textEditor.lineNumber.LineNumberParagraphView.ATTRIBUTE_NUMBER_FONT"
/**[AttributeSet] property key for specify foreground for line number */
public const val ATTRIBUTE_NUMBER_FOREGROUND = "khelp.ui.textEditor.lineNumber.LineNumberParagraphView.ATTRIBUTE_NUMBER_FOREGROUND"
/** Default font use for line number */
public val DEFAULT_NUMBER_FONT = JHelpFont("Courier", 18)

fun toColor(value: Any, default: ColorInt = 0): ColorInt =
        when (value)
        {
            is Color   -> value.rgb
            is Integer -> value.toInt()
            is Int     -> value
            else       -> default
        }

class LineNumberParagraphView(element: Element) : ParagraphView(element)
{
    private var foreground = 0xFF000000.toInt()
    private var background = 0
    private var numberFont: JHelpFont? = null
    private var numberSize = Dimension()
    private var additionalText: JHelpImage? = null

    init
    {
        // If not already initialize by call during parent constructor we initialize the font here
        if (this.numberFont == null)
        {
            this.numberFont(DEFAULT_NUMBER_FONT)
        }
    }

    private fun numberFont(numberFont: JHelpFont)
    {
        this.numberFont = numberFont
        this.numberSize = numberFont.stringSize("99999")
    }

    private fun indexInParent(): Int
    {
        val parent = this.parent
        val count = parent.viewCount
        var index = 0

        while (index < count && this != parent.getView(index))
        {
            index++
        }

        return index
    }

    override fun paintChild(g: Graphics, alloc: Rectangle, index: Int)
    {
        // Draw the child normally
        super.paintChild(g, alloc, index)

        // We want add the number and additional text only on first child
        // Because we want the real number of lines (\n) not the visually number of lines
        // And we don't want repeat the same number and information on each child
        if (index > 0)
        {
            return
        }

        // We add one, because line number start at 1 not 0
        val lineNumber = this.indexInParent() + 1
        val number = this.numberFont!!.createImage(lineNumber.toString(), this.foreground, this.background)
        // We center the number inside the reserved area
        g.drawImage(number.image,
                    alloc.x - (this.leftInset + number.width shr 1),
                    alloc.y + (alloc.height - number.height shr 1),
                    null)

        this.additionalText?.let { additionalText ->
            g.drawImage(additionalText.image,
                        alloc.x + alloc.width + ADDITIONAL_MARGIN,
                        alloc.y + (alloc.height - additionalText.height shr 1),
                        null)
        }
    }

    override fun setInsets(top: Short, left: Short, bottom: Short, right: Short)
    {
        if (this.numberFont == null)
        {
            // Number size may be wrong due call during the parent class constructor
            this.numberFont(DEFAULT_NUMBER_FONT)
        }

        // May look strange but it looks for some reason this.numberSize.width becomes 0 times to times
        // So we recompute it to be sure
        this.numberFont(this.numberFont!!)

        val more = this.additionalText?.let { it.width + ADDITIONAL_MARGIN } ?: 0
        super.setInsets(top,
                        (left + this.numberSize.width).toShort(),
                        bottom,
                        (right + more).toShort())
    }

    override fun setParagraphInsets(attributes: AttributeSet) =
            this.setInsets(StyleConstants.getSpaceAbove(attributes).toShort(),
                           StyleConstants.getLeftIndent(attributes).toShort(),
                           StyleConstants.getSpaceBelow(attributes).toShort(),
                           StyleConstants.getRightIndent(attributes).toShort());

    override fun setPropertiesFromAttributes()
    {
        super.setPropertiesFromAttributes()

        // Collect our additional attributes and update if need
        val attributeSet = this.getAttributes()

        if (attributeSet != null)
        {
            var value = attributeSet.getAttribute(ATTRIBUTE_NUMBER_FONT)

            if (value != null)
            {
                when (value)
                {
                    is JHelpFont -> this.numberFont(value)
                    is Font      -> this.numberFont(JHelpFont(value, false))
                }
            }

            value = attributeSet.getAttribute(ATTRIBUTE_NUMBER_FOREGROUND)

            if (value != null)
            {
                this.foreground = toColor(value, this.foreground)
            }

            value = attributeSet.getAttribute(ATTRIBUTE_NUMBER_BACKGROUND)

            if (value != null)
            {
                this.background = toColor(value, this.background)
            }

            value = attributeSet.getAttribute(ATTRIBUTE_ADDITIONAL_TEXT)

            if (value != null && value is CharSequence)
            {
                val text = value.toString().trim()

                if (text.isNotEmpty())
                {
                    if (this.numberFont == null)
                    {
                        // Number font may be null due call during the parent class constructor
                        this.numberFont(DEFAULT_NUMBER_FONT)
                    }

                    this.additionalText = this.numberFont!!.createImage(text, this.foreground, 0x40A0A0A0.toInt())
                }
                else
                {
                    this.additionalText = null
                }
            }
            else
            {
                this.additionalText = null
            }
        }
    }
}