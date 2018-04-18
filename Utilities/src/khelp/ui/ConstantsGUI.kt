package khelp.ui

import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

/**
 * Identity transform
 */
val AFFINE_TRANSFORM = AffineTransform()
/**
 * Flatness to use
 */
val FLATNESS = 0.01
/**
 * Font render context
 */
val FONT_RENDER_CONTEXT = FontRenderContext(AFFINE_TRANSFORM, true, true)
