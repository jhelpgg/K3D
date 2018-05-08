package khelp.k3d.ui

import khelp.list.Queue
import khelp.math.maximum
import khelp.util.contains
import java.awt.Dimension

class RelativeLayout : Layout<RelativeConstraints>()
{
    private fun order(components: List<ComponentConstraints>): List<ComponentConstraints>
    {
        val list = ArrayList<ComponentConstraints>()
        val later = Queue<ComponentConstraints>()
        var relativeConstraints: RelativeConstraints
        var toAdd: Boolean

        components.forEach {
            val preferred = it.component.preferredSize()
            it.component.width = preferred.width
            it.component.height = preferred.height
            relativeConstraints = it.constraints
            toAdd = true

            if (relativeConstraints.constraintsHorizontal != null &&
                    !list.contains { it.constraints == relativeConstraints.constraintsHorizontal?.relativeTo })
            {
                toAdd = false
            }

            if (relativeConstraints.constraintsVertical != null &&
                    !list.contains { it.constraints == relativeConstraints.constraintsVertical?.relativeTo })
            {
                toAdd = false
            }

            if (toAdd)
            {
                list.add(it)
            }
            else
            {
                later.inQueue(it)
            }
        }

        var componentConstraints: ComponentConstraints

        while (!later.empty())
        {
            componentConstraints = later.outQueue()
            relativeConstraints = componentConstraints.constraints

            toAdd = true

            if (relativeConstraints.constraintsHorizontal != null &&
                    !list.contains { it.constraints == relativeConstraints.constraintsHorizontal?.relativeTo })
            {
                toAdd = false
            }

            if (relativeConstraints.constraintsVertical != null &&
                    !list.contains { it.constraints == relativeConstraints.constraintsVertical?.relativeTo })
            {
                toAdd = false
            }

            if (toAdd)
            {
                list.add(componentConstraints)
            }
            else
            {
                later.inQueue(componentConstraints)
            }
        }

        return list
    }

    override fun layout(parentWidth: Int, parentHeight: Int, components: List<ComponentConstraints>): Dimension
    {
        val ordered = this.order(components)

        ordered.forEach {
            val relativeConstraints = it.constraints
            val top = relativeConstraints.alignParentTop
            val left = relativeConstraints.alignParentLeft
            val right = relativeConstraints.alignParentRight
            val bottom = relativeConstraints.alignParentBottom
            val horizontal = relativeConstraints.constraintsHorizontal
            val vertical = relativeConstraints.constraintsVertical
            val component = it.component

            if (top >= 0 || bottom >= 0 || vertical == null)
            {
                if (top >= 0)
                {
                    component.y = top

                    if (bottom >= 0)
                    {
                        component.height = parentHeight - top - bottom
                    }
                }
                else if (bottom >= 0)
                {
                    component.y = parentHeight - component.height - bottom
                }
                else
                {
                    component.y = (parentHeight - component.height) shr 1
                }
            }
            else
            {
                val relativeComponent = this.obtainComponentUnsafe(vertical.relativeTo)!!

                when (vertical.thisVertical)
                {
                    RelativeVertical.TOP_AND_BOTTOM ->
                    {
                        //By construction vertical.relativeVertical == TOP_AND_BOTTOM also
                        component.y = relativeComponent.y + vertical.margin
                        component.height = relativeComponent.height - (vertical.margin shl 1)
                    }
                    RelativeVertical.TOP            ->
                        component.y = when (vertical.relativeVertical)
                        {
                            RelativeVertical.TOP    -> relativeComponent.y + vertical.margin
                            RelativeVertical.CENTER -> relativeComponent.y + (relativeComponent.height shr 1)
                            RelativeVertical.BOTTOM -> relativeComponent.y + relativeComponent.height + vertical.margin
                            else                    -> component.y
                        }
                    RelativeVertical.CENTER         ->
                        component.y = when (vertical.relativeVertical)
                        {
                            RelativeVertical.TOP    -> relativeComponent.y - (component.height shr 1)
                            RelativeVertical.CENTER -> relativeComponent.y + ((relativeComponent.height - component.height) shr 1)
                            RelativeVertical.BOTTOM -> relativeComponent.y + relativeComponent.height - (component.height shr 1)
                            else                    -> component.y
                        }
                    RelativeVertical.BOTTOM         ->
                        component.y = when (vertical.relativeVertical)
                        {
                            RelativeVertical.TOP    -> relativeComponent.y - vertical.margin - component.height
                            RelativeVertical.CENTER -> relativeComponent.y + (relativeComponent.height shr 1) - component.height
                            RelativeVertical.BOTTOM -> relativeComponent.y + relativeComponent.height - component.height
                            else                    -> component.y
                        }
                }
            }

            if (left >= 0 || right >= 0 || horizontal == null)
            {
                if (left >= 0)
                {
                    component.x = left

                    if (right >= 0)
                    {
                        component.width = parentWidth - left - right
                    }
                }
                else if (right >= 0)
                {
                    component.x = parentWidth - component.width - right
                }
                else
                {
                    component.x = (parentWidth - component.width) shr 1
                }
            }
            else
            {
                val relativeComponent = this.obtainComponentUnsafe(horizontal.relativeTo)!!

                when (horizontal.thisHorizontal)
                {
                    RelativeHorizontal.LEFT_AND_RIGHT ->
                    {
                        //By construction horizontal.relativeHorizontal == LEFT_AND_RIGHT also
                        component.x = relativeComponent.x + horizontal.margin
                        component.width = relativeComponent.width - (horizontal.margin shl 1)
                    }
                    RelativeHorizontal.LEFT           ->
                        component.x = when (horizontal.relativeHorizontal)
                        {
                            RelativeHorizontal.LEFT   -> relativeComponent.x + horizontal.margin
                            RelativeHorizontal.CENTER -> relativeComponent.x + (relativeComponent.width shr 1)
                            RelativeHorizontal.RIGHT  -> relativeComponent.x + relativeComponent.width + horizontal.margin
                            else                      -> component.x
                        }
                    RelativeHorizontal.CENTER         ->
                        component.x = when (horizontal.relativeHorizontal)
                        {
                            RelativeHorizontal.LEFT   -> relativeComponent.x - (component.width shr 1)
                            RelativeHorizontal.CENTER -> relativeComponent.x + ((relativeComponent.width - component.width) shr 1)
                            RelativeHorizontal.RIGHT  -> relativeComponent.x + relativeComponent.width - (component.width shr 1)
                            else                      -> component.x
                        }
                    RelativeHorizontal.RIGHT          ->
                        component.x = when (horizontal.relativeHorizontal)
                        {
                            RelativeHorizontal.LEFT   -> relativeComponent.x - horizontal.margin - component.width
                            RelativeHorizontal.CENTER -> relativeComponent.x + (relativeComponent.width shr 1) - component.width
                            RelativeHorizontal.RIGHT  -> relativeComponent.x + relativeComponent.width - component.width
                            else                      -> component.x
                        }
                }
            }
        }

        return Dimension(parentWidth, parentHeight)
    }

    override fun preferredSize(components: List<ComponentConstraints>): Dimension
    {
        var width = 1
        var height = 1
        val ordered = this.order(components)

        ordered.forEach {
            val relativeConstraints = it.constraints
            val top = relativeConstraints.alignParentTop
            val left = relativeConstraints.alignParentLeft
            val right = relativeConstraints.alignParentRight
            val bottom = relativeConstraints.alignParentBottom
            val horizontal = relativeConstraints.constraintsHorizontal
            val vertical = relativeConstraints.constraintsVertical
            val component = it.component
            var componentWidth = component.width
            var componentHeight = component.height

            if (top >= 0 || bottom >= 0 || vertical == null)
            {
                height = Math.max(height, componentHeight + Math.max(0, top) + Math.max(0, bottom))
            }
            else
            {
                val relativeComponent = this.obtainComponentUnsafe(vertical.relativeTo)!!
                val relativeHeight = relativeComponent.height
                var computedHeight = componentHeight

                when
                {
                    vertical.thisVertical == RelativeVertical.TOP_AND_BOTTOM                                                            ->
                    {
                        //By construction vertical.relativeVertical == TOP_AND_BOTTOM also
                        computedHeight = relativeHeight - (vertical.margin shl 1)
                        height = Math.max(height, relativeHeight)
                    }
                    (vertical.thisVertical == RelativeVertical.TOP && vertical.relativeVertical == RelativeVertical.TOP) ||
                            (vertical.thisVertical == RelativeVertical.BOTTOM && vertical.relativeVertical == RelativeVertical.BOTTOM)  ->
                    {
                        height = maximum(height, relativeHeight, vertical.margin + componentHeight)
                    }
                    (vertical.thisVertical == RelativeVertical.TOP && vertical.relativeVertical == RelativeVertical.BOTTOM) ||
                            (vertical.thisVertical == RelativeVertical.BOTTOM && vertical.relativeVertical == RelativeVertical.TOP)     ->
                    {
                        height = Math.max(height, relativeHeight + vertical.margin + componentHeight)
                    }
                    vertical.thisVertical == RelativeVertical.CENTER && vertical.relativeVertical == RelativeVertical.CENTER            ->
                    {
                        height = maximum(height, relativeHeight, componentHeight)
                    }
                    (vertical.thisVertical == RelativeVertical.TOP || vertical.thisVertical == RelativeVertical.BOTTOM) &&
                            vertical.relativeVertical == RelativeVertical.CENTER                                                        ->
                    {
                        val relativeHeight2 = relativeHeight shr 1
                        height = Math.max(height,
                                          relativeHeight2 + Math.max(relativeHeight2,
                                                                     vertical.margin + componentHeight))
                    }
                    vertical.thisVertical == RelativeVertical.CENTER &&
                            (vertical.relativeVertical == RelativeVertical.TOP || vertical.relativeVertical == RelativeVertical.BOTTOM) ->
                    {
                        val relativeHeight2 = relativeHeight shr 1
                        height = Math.max(height,
                                          relativeHeight2 + Math.max(relativeHeight2,
                                                                     componentHeight))
                    }
                    else                                                                                                                ->
                        throw RuntimeException("Should never goes here: vertical=$vertical")
                }

                component.height = computedHeight
            }

            if (left >= 0 || right >= 0 || horizontal == null)
            {
                width = Math.max(width, componentWidth + Math.max(0, left) + Math.max(0, right))
            }
            else
            {
                val relativeComponent = this.obtainComponentUnsafe(horizontal.relativeTo)!!
                val relativeWidth = relativeComponent.width
                var computedWidth = componentWidth

                when
                {
                    horizontal.thisHorizontal == RelativeHorizontal.LEFT_AND_RIGHT                                                                  ->
                    {
                        //By construction vertical.relativeHorizontal == LEFT_AND_RIGHT also
                        computedWidth = relativeWidth - (horizontal.margin shl 1)
                        width = Math.max(width, relativeWidth)
                    }
                    (horizontal.thisHorizontal == RelativeHorizontal.LEFT && horizontal.relativeHorizontal == RelativeHorizontal.LEFT) ||
                            (horizontal.thisHorizontal == RelativeHorizontal.RIGHT && horizontal.relativeHorizontal == RelativeHorizontal.RIGHT)    ->
                    {
                        width = maximum(width, relativeWidth, horizontal.margin + computedWidth)
                    }
                    (horizontal.thisHorizontal == RelativeHorizontal.LEFT && horizontal.relativeHorizontal == RelativeHorizontal.RIGHT) ||
                            (horizontal.thisHorizontal == RelativeHorizontal.RIGHT && horizontal.relativeHorizontal == RelativeHorizontal.LEFT)     ->
                    {
                        width = Math.max(width, relativeWidth + horizontal.margin + computedWidth)
                    }
                    horizontal.thisHorizontal == RelativeHorizontal.CENTER && horizontal.relativeHorizontal == RelativeHorizontal.CENTER            ->
                    {
                        width = maximum(width, relativeWidth, computedWidth)
                    }
                    (horizontal.thisHorizontal == RelativeHorizontal.LEFT || horizontal.thisHorizontal == RelativeHorizontal.RIGHT) &&
                            horizontal.relativeHorizontal == RelativeHorizontal.CENTER                                                              ->
                    {
                        val relativeWidth2 = relativeWidth shr 1
                        width = Math.max(width,
                                         relativeWidth2 + Math.max(relativeWidth2,
                                                                   horizontal.margin + computedWidth))
                    }
                    horizontal.thisHorizontal == RelativeHorizontal.CENTER &&
                            (horizontal.relativeHorizontal == RelativeHorizontal.LEFT || horizontal.relativeHorizontal == RelativeHorizontal.RIGHT) ->
                    {
                        val relativeWidth2 = relativeWidth shr 1
                        width = Math.max(width,
                                         relativeWidth2 + Math.max(relativeWidth2,
                                                                   computedWidth))
                    }
                    else                                                                                                                            ->
                        throw RuntimeException("Should never goes here: horizontal=$horizontal")
                }

                component.width = computedWidth
            }
        }

        return Dimension(width, height)
    }
}