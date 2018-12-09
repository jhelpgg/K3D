package khelp.list

import java.util.Stack
import kotlin.math.abs

class BTree<K : Comparable<K>, V>(key: K, value: V)
{
    companion object
    {
        private fun <KK : Comparable<KK>, VV> put(treeToAdd: BTree<KK, VV>, treeWhereAdd: BTree<KK, VV>)
        {
            val stack = Stack<Pair<BTree<KK, VV>, BTree<KK, VV>>>()
            stack.push(Pair(treeToAdd, treeWhereAdd))

            while (stack.isNotEmpty())
            {
                val (source, destination) = stack.pop()
                val comparison = source.key.compareTo(destination.key)

                when
                {
                    comparison == 0 ->
                    {
                        destination.value = source.value
                        source.smaller?.let { stack.push(Pair(it, destination)) }
                        source.bigger?.let { stack.push(Pair(it, destination)) }
                    }
                    comparison < 0  ->
                        if (destination.smaller == null)
                        {
                            destination.smaller = source
                        }
                        else
                        {
                            stack.push(Pair(source, destination.smaller!!))
                        }
                    else            ->
                        if (destination.bigger == null)
                        {
                            destination.bigger = source
                        }
                        else
                        {
                            stack.push(Pair(source, destination.bigger!!))
                        }
                }
            }
        }

        private fun <KK : Comparable<KK>, VV> equilibrating(treeToModify: BTree<KK, VV>)
        {
            var tree: BTree<KK, VV>
            var smallWeight: Int
            var bigWeight: Int
            val stack = Stack<BTree<KK, VV>>()
            var newMainTree: BTree<KK, VV>
            var temp: BTree<KK, VV>?
            stack.push(treeToModify)

            while (stack.isNotEmpty())
            {
                tree = stack.pop()
                smallWeight = BTree.weight(tree.smaller)
                bigWeight = BTree.weight(tree.bigger)

                while (abs(smallWeight - bigWeight) > 1)
                {
                    if (smallWeight > bigWeight)
                    {
                        newMainTree = tree.smaller!!
                        temp = tree.bigger
                    }
                    else
                    {
                        newMainTree = tree.bigger!!
                        temp = tree.smaller
                    }

                    val k = tree.key
                    val v = tree.value

                    tree.key = newMainTree.key
                    tree.value = newMainTree.value
                    tree.smaller = newMainTree.smaller
                    tree.bigger = newMainTree.bigger
                    BTree.put(BTree(k, v), tree)
                    temp?.let { BTree.put(it, tree) }

                    smallWeight = BTree.weight(tree.smaller)
                    bigWeight = BTree.weight(tree.bigger)
                }

                tree.smaller?.let { stack.push(it) }
                tree.bigger?.let { stack.push(it) }
            }
        }

        private fun <KK : Comparable<KK>, VV> weight(treeToGetWeight: BTree<KK, VV>?): Int
        {
            if (treeToGetWeight == null)
            {
                return 0
            }

            var weight = 0
            var tree: BTree<KK, VV>
            val stack = Stack<BTree<KK, VV>>()
            stack.push(treeToGetWeight)

            while (stack.isNotEmpty())
            {
                tree = stack.pop()
                weight++
                tree.smaller?.let { stack.push(it) }
                tree.bigger?.let { stack.push(it) }
            }

            return weight
        }

        private fun <KK : Comparable<KK>, VV> remove(key: KK, fromTree: BTree<KK, VV>): Boolean
        {
            var smallWeight: Int
            var bigWeight: Int
            var comparison: Int
            var tree = fromTree
            var temp: BTree<KK, VV>?

            while (true)
            {
                comparison = key.compareTo(tree.key)

                when
                {
                    comparison == 0 ->
                    {
                        smallWeight = BTree.weight(tree.smaller)
                        bigWeight = BTree.weight(tree.bigger)

                        if (smallWeight >= bigWeight)
                        {
                            if (smallWeight == 0)
                            {
                                return false
                            }
                            else
                            {
                                temp = tree.bigger
                                tree.key = tree.smaller!!.key
                                tree.value = tree.smaller!!.value
                                tree.bigger = tree.smaller!!.bigger
                                tree.smaller = tree.smaller!!.smaller
                                temp?.let { BTree.put(it, tree) }
                                return true
                            }
                        }
                        else
                        {
                            temp = tree.smaller
                            tree.key = tree.bigger!!.key
                            tree.value = tree.bigger!!.value
                            tree.smaller = tree.bigger!!.smaller
                            tree.bigger = tree.bigger!!.bigger
                            temp?.let { BTree.put(it, tree) }
                            return true
                        }
                    }
                    comparison < 0  -> tree.smaller?.let { tree = it } ?: return false
                    comparison > 0  -> tree.bigger?.let { tree = it } ?: return false
                    else            -> return false
                }
            }
        }

        private fun <KK : Comparable<KK>, VV> get(key: KK, fromTree: BTree<KK, VV>): VV?
        {
            var tree = fromTree
            var comparison: Int

            while (true)
            {
                comparison = key.compareTo(tree.key)

                when
                {
                    comparison == 0                        -> return tree.value
                    comparison < 0 && tree.smaller != null -> tree = tree.smaller!!
                    comparison > 0 && tree.bigger != null  -> tree = tree.bigger!!
                    else                                   -> return null
                }
            }
        }

        private fun <KK : Comparable<KK>, VV> fill(header: String, stringBuilder: StringBuilder,
                                                   treeToPrint: BTree<KK, VV>)
        {
            val stack = Stack<Pair<BTree<KK, VV>, String>>()
            stack.push(Pair(treeToPrint, header))

            while (stack.isNotEmpty())
            {
                var (tree, currentHeader) = stack.pop()
                stringBuilder.append("$currentHeader--${tree.key}=${tree.value}")
                currentHeader = "$currentHeader |"
                tree.smaller?.let { stack.push(Pair(it, currentHeader)) }
                tree.bigger?.let { stack.push(Pair(it, currentHeader)) }
            }
        }
    }

    var key = key
        private set
    var value = value
        private set
    private var smaller: BTree<K, V>? = null
    private var bigger: BTree<K, V>? = null

    operator fun plusAssign(tree: BTree<K, V>)
    {
        BTree.put(tree, this)
        BTree.equilibrating(this)
    }

    operator fun set(key: K, value: V)
    {
        this += BTree<K, V>(key, value)
    }

    operator fun minusAssign(key: K)
    {
        if (BTree.remove(key, this))
        {
            BTree.equilibrating(this)
        }
    }

    operator fun get(key: K) = BTree.get(key, this)

    operator fun contains(key: K) = this[key] != null

    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        BTree.fill("\n", stringBuilder, this)
        return stringBuilder.toString()
    }
}
