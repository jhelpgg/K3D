package khelp.ui

import java.awt.Dimension

/**
 * Component with fixed size
 */
interface WithFixedSize
{
    /**
     * Fixed size
     */
    fun fixedSize(): Dimension
}