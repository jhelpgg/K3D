package khelp.images

/**
 * Describe part of line to draw
 * @param text String represented by the given mask
 * @param x Position X relative where draw the part
 * @param y Position Y relative where draw the part
 * @param width Part width
 * @param height Part height
 * @param mask Image to draw. It represents the text. It can be used as mask
 * @param endOfLine Indicates if the part is a end of line of original complete text
 */
data class JHelpTextLineAlpha(val text: String,
                              var x: Int, val y: Int, val width: Int, val height: Int,
                              val mask: JHelpImage, val endOfLine: Boolean)