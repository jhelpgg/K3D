package khelp.k3d.ui

import khelp.k3d.ui.BorderConstraints.ALL_BOTTOM
import khelp.k3d.ui.BorderConstraints.ALL_LEFT
import khelp.k3d.ui.BorderConstraints.ALL_RIGHT
import khelp.k3d.ui.BorderConstraints.ALL_TOP
import khelp.k3d.ui.BorderConstraints.CENTER
import khelp.k3d.ui.BorderConstraints.BOTTOM
import khelp.k3d.ui.BorderConstraints.BOTTOM_LEFT
import khelp.k3d.ui.BorderConstraints.BOTTOM_RIGHT
import khelp.k3d.ui.BorderConstraints.LEFT
import khelp.k3d.ui.BorderConstraints.RIGHT
import khelp.k3d.ui.BorderConstraints.TOP
import khelp.k3d.ui.BorderConstraints.TOP_LEFT
import khelp.k3d.ui.BorderConstraints.TOP_RIGHT
import java.awt.Dimension

class BorderLayout : Layout<BorderConstraints>()
{
    override fun layout(parentWidth: Int, parentHeight: Int, components: List<ComponentConstraints>): Dimension
    {
        var upHeight = 0
        var leftWidth = 0
        var rightWidth = 0
        var downHeight = 0
        var width = 1
        var height = 1

        components.forEach {
            val preferred = it.component.preferredSize()

            when (it.constraints)
            {
                TOP_LEFT     ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                TOP          -> upHeight = Math.max(upHeight, preferred.height)
                TOP_RIGHT    ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                ALL_TOP      ->
                {
                    width = Math.max(width, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                LEFT         -> leftWidth = Math.max(leftWidth, preferred.width)
                ALL_LEFT     ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    height = Math.max(height, preferred.height)
                }
                CENTER       -> Unit
                RIGHT        -> rightWidth = Math.max(rightWidth, preferred.width)
                ALL_RIGHT    ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    height = Math.max(height, preferred.height)
                }
                BOTTOM_LEFT  ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
                BOTTOM       -> downHeight = Math.max(downHeight, preferred.height)
                BOTTOM_RIGHT ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
                ALL_BOTTOM   ->
                {
                    width = Math.max(width, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
            }
        }

        val centerWidth = parentWidth - leftWidth - rightWidth
        val centerHeight = parentHeight - upHeight - downHeight
        val xLeft = leftWidth
        val xRight = parentWidth - rightWidth
        val yUp = upHeight
        val yDown = parentHeight - downHeight

        components.forEach {
            val component = it.component

            when (it.constraints)
            {
                TOP_LEFT     ->
                {
                    component.x = 0
                    component.y = 0
                    component.width = leftWidth
                    component.height = upHeight
                }
                TOP          ->
                {
                    component.x = xLeft
                    component.y = 0
                    component.width = centerWidth
                    component.height = upHeight
                }
                TOP_RIGHT    ->
                {
                    component.x = xRight
                    component.y = 0
                    component.width = rightWidth
                    component.height = upHeight
                }
                ALL_TOP      ->
                {
                    component.x = 0
                    component.y = 0
                    component.width = parentWidth
                    component.height = upHeight
                }
                LEFT         ->
                {
                    component.x = 0
                    component.y = yUp
                    component.width = leftWidth
                    component.height = centerHeight
                }
                ALL_LEFT     ->
                {
                    component.x = 0
                    component.y = 0
                    component.width = leftWidth
                    component.height = parentHeight
                }
                CENTER       ->
                {
                    component.x = xLeft
                    component.y = yUp
                    component.width = centerWidth
                    component.height = centerHeight
                }
                RIGHT        ->
                {
                    component.x = xRight
                    component.y = yUp
                    component.width = rightWidth
                    component.height = centerHeight
                }
                ALL_RIGHT    ->
                {
                    component.x = xRight
                    component.y = 0
                    component.width = rightWidth
                    component.height = parentHeight
                }
                BOTTOM_LEFT  ->
                {
                    component.x = 0
                    component.y = yDown
                    component.width = leftWidth
                    component.height = downHeight
                }
                BOTTOM       ->
                {
                    component.x = xLeft
                    component.y = yDown
                    component.width = centerWidth
                    component.height = downHeight
                }
                BOTTOM_RIGHT ->
                {
                    component.x = xRight
                    component.y = yDown
                    component.width = rightWidth
                    component.height = downHeight
                }
                ALL_BOTTOM   ->
                {
                    component.x = 0
                    component.y = yDown
                    component.width = parentWidth
                    component.height = downHeight
                }
            }
        }

        return Dimension(parentWidth, parentHeight)
    }

    override fun preferredSize(components: List<ComponentConstraints>): Dimension
    {
        var upHeight = 0
        var leftWidth = 0
        var centerWidth = 0
        var centerHeight = 0
        var rightWidth = 0
        var downHeight = 0
        var width = 1
        var height = 1

        components.forEach {
            val preferred = it.component.preferredSize()

            when (it.constraints)
            {
                TOP_LEFT     ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                TOP          ->
                {
                    centerWidth = Math.max(centerWidth, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                TOP_RIGHT    ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                ALL_TOP      ->
                {
                    width = Math.max(width, preferred.width)
                    upHeight = Math.max(upHeight, preferred.height)
                }
                LEFT         ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    centerHeight = Math.max(centerHeight, preferred.height)
                }
                ALL_LEFT     ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    height = Math.max(height, preferred.height)
                }
                CENTER       ->
                {
                    centerWidth = Math.max(centerWidth, preferred.width)
                    centerHeight = Math.max(centerHeight, preferred.height)
                }
                RIGHT        ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    centerHeight = Math.max(centerHeight, preferred.height)
                }
                ALL_RIGHT    ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    height = Math.max(height, preferred.height)
                }
                BOTTOM_LEFT  ->
                {
                    leftWidth = Math.max(leftWidth, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
                BOTTOM       ->
                {
                    centerWidth = Math.max(centerWidth, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
                BOTTOM_RIGHT ->
                {
                    rightWidth = Math.max(rightWidth, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
                ALL_BOTTOM   ->
                {
                    width = Math.max(width, preferred.width)
                    downHeight = Math.max(downHeight, preferred.height)
                }
            }
        }

        return Dimension(Math.max(width, leftWidth + centerWidth + rightWidth),
                         Math.max(height, upHeight + centerHeight + downHeight))
    }
}