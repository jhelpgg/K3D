package khelp.k3d.ui

data class TableConstraints(val cellX: Int, val cellY: Int,
                            val cellWidth: Int = 1, val cellHeight: Int = 1) : Constraints
{
    init
    {
        if (this.cellWidth < 1 || this.cellHeight < 1)
        {
            throw IllegalArgumentException(
                    "cellWidth and cellHeight must be at least 1, but here: cellWidth=${this.cellWidth} and cellHeight=${this.cellHeight}")
        }
    }
}