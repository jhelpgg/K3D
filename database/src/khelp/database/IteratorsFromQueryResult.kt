package khelp.database

internal class StringIteratorFromQueryResult(val queryResult: QueryResult) : Iterator<String>
{
    private var queryColumn = queryResult.next()
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            return false
        }

        return true
    }

    /**
     * Returns the next element in the iteration.
     */
    override fun next(): String
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            throw IllegalStateException("No next element")
        }

        val value = this.queryColumn!!.string(0)
        this.queryColumn = queryResult.next()

        if (this.queryColumn == null)
        {
            this.queryResult.close()
        }

        return value
    }
}

internal class IntIteratorFromQueryResult(val queryResult: QueryResult, val isID: Boolean) : IntIterator()
{
    private var queryColumn = queryResult.next()
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            return false
        }

        return true
    }

    /**
     * Returns the next element in the iteration.
     */
    override fun nextInt(): Int
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            throw IllegalStateException("No next element")
        }

        val value =
                if (this.isID)
                {
                    this.queryColumn!!.id(0)
                }
                else
                {
                    this.queryColumn!!.integer(0)
                }

        this.queryColumn = queryResult.next()

        if (this.queryColumn == null)
        {
            this.queryResult.close()
        }

        return value
    }
}

internal class LongIteratorFromQueryResult(val queryResult: QueryResult) : LongIterator()
{
    private var queryColumn = queryResult.next()
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            return false
        }

        return true
    }

    /**
     * Returns the next element in the iteration.
     */
    override fun nextLong(): Long
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            throw IllegalStateException("No next element")
        }

        val value = this.queryColumn!!.long(0)
        this.queryColumn = queryResult.next()

        if (this.queryColumn == null)
        {
            this.queryResult.close()
        }

        return value
    }
}

internal class BooleanIteratorFromQueryResult(val queryResult: QueryResult) : BooleanIterator()
{
    private var queryColumn = queryResult.next()
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            return false
        }

        return true
    }

    /**
     * Returns the next element in the iteration.
     */
    override fun nextBoolean(): Boolean
    {
        if (this.queryColumn == null)
        {
            this.queryResult.close()
            throw IllegalStateException("No next element")
        }

        val value = this.queryColumn!!.boolean(0)
        this.queryColumn = queryResult.next()

        if (this.queryColumn == null)
        {
            this.queryResult.close()
        }

        return value
    }
}