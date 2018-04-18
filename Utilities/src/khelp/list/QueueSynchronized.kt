package khelp.list

/**
 * Link between tow elements in the queue <br></br>
 */
internal class Link<E>(val element: E)
{
    /**
     * Next link
     */
    var next: Link<E>? = null
}

/**
 * Synchronized queue
 * @param E Element type
 */
class QueueSynchronized<E>
{
    /**Queue head*/
    private var head: Link<E>? = null
    /**Queue tail*/
    private var tail: Link<E>? = null
    /**Queue size*/
    private var size = 0

    /**
     * In queue an element
     *
     * @param element Element to in queue
     */
    @Synchronized
    fun inQueue(element: E)
    {
        if (this.head == null)
        {
            this.head = Link<E>(element)
            this.tail = this.head
            this.size = 1
            return
        }

        this.tail?.next = Link<E>(element)
        this.tail = this.tail?.next
        this.size++
    }

    /**
     * Indicates if queue is empty
     */
    fun empty() = this.head == null

    /**
     * Look the next element in the queue
     *
     * @return Element look
     */
    @Synchronized
    fun peek(): E
    {
        if (this.head == null)
        {
            throw IllegalStateException("The queue is empty !")
        }

        return this.head!!.element
    }

    /**
     * Out queue element
     *
     * @return Element out queue
     */
    @Synchronized
    fun outQueue(): E
    {
        val element: E

        if (this.head == null)
        {
            throw IllegalStateException("The queue is empty !")
        }

        element = this.head!!.element
        this.head = this.head!!.next

        if (this.head == null)
        {
            this.tail = null
        }

        this.size--
        return element
    }

    /**
     * Queue size
     *
     * @return Queue size
     */
    @Synchronized
    fun size(): Int = this.size

    /**
     * String representation
     *
     * @return String representation
     * @see Object.toString
     */
    override fun toString(): String
    {
        val stringBuffer: StringBuffer
        var link: Link<E>?

        stringBuffer = StringBuffer("Queue : [")

        link = this.head

        while (link != null)
        {
            stringBuffer.append(link.element)
            link = link.next

            if (link != null)
            {
                stringBuffer.append(" | ")
            }
        }

        stringBuffer.append(']')
        return stringBuffer.toString()
    }
}