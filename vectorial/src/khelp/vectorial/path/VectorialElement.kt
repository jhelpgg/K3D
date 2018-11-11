package khelp.vectorial.path

import khelp.util.HashCode
import khelp.util.forEachAsync
import khelp.vectorial.shape.Point

typealias VectorialElementListener = (VectorialElement) -> Unit

abstract class VectorialElement(relative: Boolean)
{
    var relative = relative
        private set
    private val listeners = ArrayList<VectorialElementListener>()

    final fun register(listener: VectorialElementListener) =
            synchronized(this.listeners)
            {
                if (!this.listeners.contains(listener))
                {
                    this.listeners += listener
                }
            }

    final fun unregister(listener: VectorialElementListener) =
            synchronized(this.listeners)
            {
                this.listeners -= listener
            }

    final protected fun fireChanged() =
            synchronized(this.listeners)
            {
                this.listeners.forEachAsync({ it(this) })
            }

    final override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (other == null || other.javaClass != this.javaClass)
        {
            return false
        }

        if (this.relative != (other as VectorialElement).relative)
        {
            return false
        }

        return this.equalsIntern(other)
    }

    protected abstract fun equalsIntern(element: VectorialElement): Boolean

    final override fun hashCode(): Int
    {
        val hashCode = HashCode()
        hashCode += this.javaClass.name
        hashCode += this.relative
        this.hashCodeIntern(hashCode)
        return hashCode.getHashCode()
    }

    protected abstract fun hashCodeIntern(hashCode: HashCode)

    final fun relative(relative: Boolean)
    {
        if (this.relative != relative)
        {
            this.relative = relative
            this.fireChanged()
        }
    }

    abstract fun draw(pathDrawer: PathDrawer, referenceX: Double, referenceY: Double): Point
}