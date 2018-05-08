package khelp.k3d.ui

import khelp.k3d.ui.RelativeHorizontal.LEFT_AND_RIGHT
import khelp.k3d.ui.RelativeVertical.TOP_AND_BOTTOM
import khelp.util.HashCode
import java.util.Stack
import java.util.TreeSet

enum class RelativeHorizontal
{
    LEFT, CENTER, RIGHT, LEFT_AND_RIGHT
}

enum class RelativeVertical
{
    TOP, CENTER, BOTTOM, TOP_AND_BOTTOM
}

data class ConstraintsHorizontal(val thisHorizontal: RelativeHorizontal,
                                 val relativeTo: RelativeConstraints,
                                 val relativeHorizontal: RelativeHorizontal,
                                 val margin: Int) : Comparable<ConstraintsHorizontal>
{
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: ConstraintsHorizontal): Int
    {
        var comparison = this.thisHorizontal.compareTo(other.thisHorizontal)
        if (comparison != 0) return comparison

        comparison = this.relativeTo.compareTo(other.relativeTo)
        if (comparison != 0) return comparison

        comparison = this.relativeHorizontal.compareTo(other.relativeHorizontal)
        if (comparison != 0) return comparison

        return this.margin - other.margin
    }
}

data class ConstraintsVertical(val thisVertical: RelativeVertical,
                               val relativeTo: RelativeConstraints,
                               val relativeVertical: RelativeVertical,
                               val margin: Int) : Comparable<ConstraintsVertical>
{
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: ConstraintsVertical): Int
    {
        var comparison = this.thisVertical.compareTo(other.thisVertical)
        if (comparison != 0) return comparison

        comparison = this.relativeTo.compareTo(other.relativeTo)
        if (comparison != 0) return comparison

        comparison = this.relativeVertical.compareTo(other.relativeVertical)
        if (comparison != 0) return comparison

        return this.margin - other.margin
    }
}

class RelativeConstraints(alignParentTop: Boolean = false,
                          alignParentLeft: Boolean = false,
                          alignParentRight: Boolean = false,
                          alignParentBottom: Boolean = false,
                          margin: Int = 0) : Constraints, Comparable<RelativeConstraints>
{
    var alignParentTop = if (alignParentTop) Math.max(0, margin) else -1
        private set

    fun alignParentTop(alignParentTop: Int)
    {
        if (this.constraintsVertical != null)
        {
            throw IllegalStateException(
                    "Can't align relatively to parent top. It is already link vertically relative to a constraints")
        }

        this.alignParentTop = Math.max(0, alignParentTop)
    }

    fun freeAlignParentTop()
    {
        this.alignParentTop = -1
    }

    var alignParentLeft = if (alignParentLeft) Math.max(0, margin) else -1
        private set

    fun alignParentLeft(alignParentLeft: Int)
    {
        if (this.constraintsHorizontal != null)
        {
            throw IllegalStateException(
                    "Can't align relatively to parent left. It is already link horizontally relative to a constraints")
        }

        this.alignParentLeft = Math.max(0, alignParentLeft)
    }

    fun freeAlignParentLeft()
    {
        this.alignParentLeft = -1
    }

    var alignParentRight = if (alignParentRight) Math.max(0, margin) else -1
        private set

    fun alignParentRight(alignParentRight: Int)
    {
        if (this.constraintsHorizontal != null)
        {
            throw IllegalStateException(
                    "Can't align relatively to parent right. It is already link horizontally relative to a constraints")
        }

        this.alignParentRight = Math.max(0, alignParentRight)
    }

    fun freeAlignParentRight()
    {
        this.alignParentRight = -1
    }

    var alignParentBottom = if (alignParentBottom) Math.max(0, margin) else -1
        private set

    fun alignParentBottom(alignParentDown: Int)
    {
        if (this.constraintsVertical != null)
        {
            throw IllegalStateException(
                    "Can't align relatively to parent bottom. It is already link vertically relative to a constraints")
        }

        this.alignParentBottom = Math.max(0, alignParentDown)
    }

    fun freeAlignParentBottom()
    {
        this.alignParentBottom = -1
    }

    var constraintsHorizontal: ConstraintsHorizontal? = null
        private set

    var constraintsVertical: ConstraintsVertical? = null
        private set

    private fun checkRecursiveLoop()
    {
        val alreadySeen = TreeSet<RelativeConstraints>()
        val stack = Stack<RelativeConstraints>()
        stack.push(this)
        var relativeConstraints: RelativeConstraints

        while (!stack.empty())
        {
            relativeConstraints = stack.pop()

            if (!alreadySeen.add(relativeConstraints))
            {
                throw IllegalArgumentException("Recursive reference detected!")
            }

            if (relativeConstraints.constraintsHorizontal != null)
            {
                stack.push(relativeConstraints.constraintsHorizontal?.relativeTo)
            }

            if (relativeConstraints.constraintsVertical != null &&
                    (relativeConstraints.constraintsHorizontal == null ||
                            relativeConstraints.constraintsHorizontal?.relativeTo != relativeConstraints.constraintsVertical?.relativeTo))
            {
                stack.push(relativeConstraints.constraintsVertical?.relativeTo)
            }
        }
    }

    fun horizontal(thisHorizontal: RelativeHorizontal,
                   relativeTo: RelativeConstraints, relativeHorizontal: RelativeHorizontal = thisHorizontal,
                   margin: Int = 0)
    {
        if (this.alignParentLeft >= 0 || this.alignParentRight >= 0)
        {
            throw IllegalArgumentException(
                    "Can't align horizontally relative to a component. It linked to parent left and/or right")
        }

        if ((thisHorizontal == LEFT_AND_RIGHT && relativeHorizontal != LEFT_AND_RIGHT) || (thisHorizontal != LEFT_AND_RIGHT && relativeHorizontal == LEFT_AND_RIGHT))
        {
            throw IllegalArgumentException(
                    "LEFT_AND_RIGHT can be only use with LEFT_AND_RIGHT. thisHorizontal=$thisHorizontal, relativeHorizontal=$relativeHorizontal")
        }

        this.constraintsHorizontal = ConstraintsHorizontal(thisHorizontal, relativeTo, relativeHorizontal,
                                                           Math.max(0, margin))
        this.checkRecursiveLoop()
    }

    fun freeHorizontal()
    {
        this.constraintsHorizontal = null
    }

    fun vertical(thisVertical: RelativeVertical,
                 relativeTo: RelativeConstraints, relativeVertical: RelativeVertical = thisVertical,
                 margin: Int = 0)
    {
        if (this.alignParentTop >= 0 || this.alignParentBottom >= 0)
        {
            throw IllegalArgumentException(
                    "Can't align vertically relative to a component. It linked to parent top and/or bottom")
        }

        if ((thisVertical == TOP_AND_BOTTOM && relativeVertical != TOP_AND_BOTTOM) || (thisVertical != TOP_AND_BOTTOM && relativeVertical == TOP_AND_BOTTOM))
        {
            throw IllegalArgumentException(
                    "TOP_AND_BOTTOM can be only use with TOP_AND_BOTTOM. thisHorizontal=$thisVertical, relativeHorizontal=$relativeVertical")
        }

        this.constraintsVertical = ConstraintsVertical(thisVertical, relativeTo, relativeVertical, Math.max(0, margin))
        this.checkRecursiveLoop()
    }

    fun freeVertical()
    {
        this.constraintsVertical = null
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is RelativeConstraints)
        {
            return false
        }

        return this.alignParentTop == other.alignParentTop &&
                this.alignParentLeft == other.alignParentLeft &&
                this.alignParentRight == other.alignParentRight &&
                this.alignParentBottom == other.alignParentBottom &&
                this.constraintsHorizontal == other.constraintsHorizontal &&
                this.constraintsVertical == other.constraintsVertical
    }

    override fun hashCode() =
            HashCode.computeHashCode(this.alignParentTop, this.alignParentLeft,
                                     this.alignParentRight, this.alignParentBottom,
                                     this.constraintsHorizontal, this.constraintsVertical)

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: RelativeConstraints): Int
    {
        var comparison = this.alignParentTop - other.alignParentTop
        if (comparison != 0) return comparison

        comparison = this.alignParentLeft - other.alignParentLeft
        if (comparison != 0) return comparison

        comparison = this.alignParentRight - other.alignParentRight
        if (comparison != 0) return comparison

        comparison = this.alignParentBottom - other.alignParentBottom
        if (comparison != 0) return comparison

        val thisConstraintsHorizontal = this.constraintsHorizontal
        val otherConstraintsHorizontal = other.constraintsHorizontal

        if (thisConstraintsHorizontal != null)
        {
            if (otherConstraintsHorizontal != null)
            {
                comparison = thisConstraintsHorizontal.compareTo(otherConstraintsHorizontal)
                if (comparison != 0) return comparison
            }
            else
            {
                return 1
            }
        }
        else if (otherConstraintsHorizontal != null)
        {
            return -1
        }

        val thisConstraintsVertical = this.constraintsVertical
        val otherConstraintsVertical = other.constraintsVertical

        if (thisConstraintsVertical != null)
        {
            if (otherConstraintsVertical != null)
            {
                comparison = thisConstraintsVertical.compareTo(otherConstraintsVertical)
                if (comparison != 0) return comparison
            }
            else
            {
                return 1
            }
        }
        else if (otherConstraintsVertical != null)
        {
            return -1
        }

        return 0
    }
}