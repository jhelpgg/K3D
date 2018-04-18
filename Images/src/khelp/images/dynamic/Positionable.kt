package khelp.images.dynamic

/**
 * Object with a position in 2D space
 */
interface Positionable
{
    /**
     * Current position
     * @return Current position
     */
    fun position(): Position

    /**
     * Change position
     * @param position New position
     */
    fun position(position: Position)
}