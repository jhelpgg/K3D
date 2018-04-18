package khelp.list

/**
 * Cache of elements
 *
 * It create elements if not already computed, remove some if not enough place and recreate them later.
 * @param maximumSize Maximum size of cache
 * @param valueCreator Function for create value from a key
 * @param K Cache key type
 * @param V Cache value type
 */
class Cache<in K, out V>(val maximumSize: Int, private val valueCreator: (K) -> V)
{
    /**
     * Cache element
     * @param lastTime Last time the element was query
     * @param value Element value
     * @param V Value type
     */
    internal data class Element<out V>(var lastTime: Long, val value: V)

    /**Cache map*/
    private val map = HashMap<K, Element<V>>(this.maximumSize)

    init
    {
        if (this.maximumSize <= 0)
        {
            throw IllegalArgumentException("maximumSize must be strictly positive")
        }
    }

    /**
     * Obtain a cache element
     * @param key Cache key
     * @return Value store in cache (May be just created)
     */
    operator fun get(key: K): V
    {
        var element = this.map[key]

        if (element != null)
        {
            element.lastTime = System.currentTimeMillis()
            return element.value
        }

        element = Element(System.currentTimeMillis(), this.valueCreator(key))

        if (this.map.size >= this.maximumSize)
        {
            var time = Long.MAX_VALUE
            var toRemove: K? = null

            this.map.entries.forEach {
                if (it.value.lastTime < time)
                {
                    toRemove = it.key
                    time = it.value.lastTime
                }
            }

            if (toRemove != null)
            {
                this.map.remove(toRemove as K)
            }
        }

        this.map[key] = element
        return element.value
    }
}