package khelp.k3d.ui

import khelp.thread.Mutex
import khelp.util.contains
import java.awt.Dimension

abstract class Layout<C : Constraints> : Iterable<Component>
{
    internal inner class ComponentsIterator() : Iterator<Component>
    {
        var index = 0
        /**
         * Returns `true` if the iteration has more elements.
         */
        override fun hasNext() = mutex.playInCriticalSection { this.index < components.size }

        /**
         * Returns the next element in the iteration.
         */
        override fun next() =
                mutex.playInCriticalSection {
                    if (this.index < components.size) components[this.index++].component
                    else DummyComponent
                }
    }

    internal inner class ComponentConstraints(var component: Component, val constraints: C)

    private val mutex = Mutex()
    private val components = ArrayList<ComponentConstraints>()

    internal fun obtainComponentConstraints(constraints: C) =
            this.mutex.playInCriticalSection { this.components.firstOrNull { it.constraints == constraints } }

    fun remove(component: Component) =
            this.mutex.playInCriticalSection { this.components.removeIf { it.component == component } }

    fun remove(constraints: C) =
            this.mutex.playInCriticalSection { this.components.removeIf { it.constraints == constraints } }

    operator fun set(constraints: C, component: Component)
    {
        this.remove(component)
        var componentConstraints = this.obtainComponentConstraints(constraints)

        this.mutex.playInCriticalSectionVoid {
            if (componentConstraints == null)
            {
                this.components.add(ComponentConstraints(component, constraints))
            }
            else
            {
                componentConstraints.component = component
            }
        }
    }

    operator fun get(constraints: C) =
            this.mutex.playInCriticalSection { this.obtainComponentConstraints(constraints)?.component }

    fun obtainConstraints(component: Component) =
            this.mutex.playInCriticalSection { (this.components.firstOrNull { it.component == component })?.constraints }

    fun defined(constraints: C) =
            this.mutex.playInCriticalSection { this.components.contains { it.constraints == constraints } }

    fun contains(component: Component) =
            this.mutex.playInCriticalSection { this.components.contains { it.component == component } }

    fun layout(parentWidth: Int, parentHeight: Int) =
            this.mutex.playInCriticalSection {
                this.layout(parentWidth, parentHeight, java.util.Collections.unmodifiableList(this.components))
            }

    abstract internal fun layout(parentWidth: Int, parentHeight: Int, components: List<ComponentConstraints>): Dimension

    fun preferredSize() =
            this.mutex.playInCriticalSection {
                this.preferredSize(java.util.Collections.unmodifiableList(this.components))
            }

    abstract internal fun preferredSize(components: List<ComponentConstraints>): Dimension

    fun clear() = this.mutex.playInCriticalSection { this.components.clear() }

    override final fun iterator(): Iterator<Component> = ComponentsIterator()
}