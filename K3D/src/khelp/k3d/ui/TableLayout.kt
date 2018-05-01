package khelp.k3d.ui

import java.awt.Dimension
import java.awt.Rectangle

class TableLayout : Layout<TableConstraints>()
{
    private fun cellBounds(components: List<ComponentConstraints>): Rectangle
    {
        var xMin = Int.MAX_VALUE
        var xMax = Int.MIN_VALUE
        var yMin = Int.MAX_VALUE
        var yMax = Int.MIN_VALUE
        var tableConstraints: TableConstraints

        components.forEach {
            tableConstraints = it.constraints
            xMin = Math.min(xMin, tableConstraints.cellX)
            xMax = Math.max(xMax, tableConstraints.cellX + tableConstraints.cellWidth)
            yMin = Math.min(yMin, tableConstraints.cellY)
            yMax = Math.max(yMax, tableConstraints.cellY + tableConstraints.cellHeight)
        }

        return Rectangle(xMin, yMin, xMax - xMin, yMax - yMin)
    }

    override fun layout(parentWidth: Int, parentHeight: Int, components: List<ComponentConstraints>): Dimension
    {
        val cellBounds = cellBounds(components)
        var component: Component
        var tableConstraints: TableConstraints

        components.forEach {
            component = it.component
            tableConstraints = it.constraints
            component.x = ((tableConstraints.cellX - cellBounds.x) * parentWidth) / cellBounds.width
            component.y = ((tableConstraints.cellY - cellBounds.y) * parentHeight) / cellBounds.height
            component.width = (tableConstraints.cellWidth * parentWidth) / cellBounds.width
            component.height = (tableConstraints.cellHeight * parentHeight) / cellBounds.height
        }

        return Dimension(parentWidth, parentHeight)
    }

    override fun preferredSize(components: List<ComponentConstraints>): Dimension
    {
        val cellBounds = cellBounds(components)
        var preferred: Dimension
        var tableConstraints: TableConstraints
        var cellWidth = 1
        var cellHeight = 1

        components.forEach {
            preferred = it.component.preferredSize()
            tableConstraints = it.constraints
            cellWidth = Math.max(cellWidth, preferred.width / tableConstraints.cellWidth)
            cellHeight = Math.max(cellHeight, preferred.height / tableConstraints.cellHeight)
        }

        return Dimension(cellBounds.width * cellWidth, cellBounds.height * cellHeight)
    }
}