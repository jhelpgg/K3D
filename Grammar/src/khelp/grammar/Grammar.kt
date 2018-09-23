package khelp.grammar

import khelp.util.HashCode
import khelp.util.transform
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class DescriptionElement
{
    abstract override fun toString(): String
    abstract operator fun contains(rule: String): Boolean
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}

class DescriptionRepetition(val name: String, val minimum: Int, val maximum: Int) : DescriptionElement()
{
    override fun toString() = "${this.name}{${this.minimum},${this.maximum}}"
    override operator fun contains(rule: String) = rule == this.name
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is DescriptionRepetition)
        {
            return false
        }

        return this.name == other.name && this.minimum == other.minimum && this.maximum == maximum
    }

    override fun hashCode() = HashCode.computeHashCode(this.name, this.minimum, this.maximum)
}

class DescriptionChoice : DescriptionElement(), Iterable<String>
{
    private val names = ArrayList<String>()
    fun add(name: String)
    {
        this.names += name
    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.names.iterator()

    override operator fun contains(rule: String) = rule in this.names
    operator fun get(index: Int) = this.names[index]
    val size: Int get() = this.names.size
    fun clear()
    {
        this.names.clear()
    }

    override fun toString() = this.names.joinToString("|", "", "")
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is DescriptionChoice)
        {
            return false
        }

        return this.names == other.names
    }

    override fun hashCode() = this.names.hashCode()
}

class DescriptionRegex(val regex: String) : DescriptionElement()
{
    val pattern = Pattern.compile(this.regex)

    override fun toString() = "{${this.regex}}"
    override operator fun contains(rule: String) = false
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is DescriptionRegex)
        {
            return false
        }

        return this.regex == other.regex
    }

    override fun hashCode() = this.regex.hashCode()
}

class DescriptionNameOrRegex(val value: String, val isRegex: Boolean) : DescriptionElement()
{
    val pattern: Pattern by lazy {
        if (this.isRegex) Pattern.compile(this.value)
        else throw IllegalStateException("It represents a name not a regex")
    }

    override fun toString() = if (this.isRegex) "{${this.value}}" else this.value
    override operator fun contains(rule: String) = !this.isRegex && this.value == rule
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is DescriptionNameOrRegex)
        {
            return false
        }

        return this.isRegex == other.isRegex && this.value == other.value
    }

    override fun hashCode() = HashCode.computeHashCode(this.isRegex, this.value)
}

class DescriptionComposition : DescriptionElement(), Iterable<DescriptionNameOrRegex>
{
    private val nameOrRegexList = ArrayList<DescriptionNameOrRegex>()
    fun add(name: DescriptionNameOrRegex)
    {
        this.nameOrRegexList += name
    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.nameOrRegexList.iterator()

    operator fun contains(name: DescriptionNameOrRegex) = name in this.nameOrRegexList
    override operator fun contains(rule: String) = this.nameOrRegexList.any { rule in it }
    operator fun get(index: Int) = this.nameOrRegexList[index]
    val size: Int get() = this.nameOrRegexList.size
    fun clear()
    {
        this.nameOrRegexList.clear()
    }

    override fun toString() = this.nameOrRegexList.joinToString("", transform = { it.toString() })
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is DescriptionComposition)
        {
            return false
        }

        return this.nameOrRegexList == other.nameOrRegexList
    }

    override fun hashCode() = this.nameOrRegexList.hashCode()
}

class GrammarDefinition(val name: String, val element: DescriptionElement)
{
    operator fun contains(rule: String) = rule in this.element
    override fun toString() = "${this.name}:=${this.element}"
    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is GrammarDefinition)
        {
            return false
        }

        return this.name == other.name && this.element == other.element
    }

    override fun hashCode() = HashCode.computeHashCode(this.name, this.element)
}

class Grammar : Iterable<GrammarDefinition>
{
    private val definitions = ArrayList<GrammarDefinition>()
    private val regexMap = HashMap<String, String>()
    private val regexMapDirect = HashMap<String, Pattern>()
    fun add(definition: GrammarDefinition)
    {
        this.definitions += definition
    }

    private fun computeRepetition(description: DescriptionRepetition, stringBuilder: StringBuilder)
    {
        when
        {
            description.minimum == 0 && description.maximum == 1             -> stringBuilder.append("?")
            description.minimum == 0 && description.maximum == Int.MAX_VALUE -> stringBuilder.append("*")
            description.minimum == 1 && description.maximum == Int.MAX_VALUE -> stringBuilder.append("+")
            description.minimum == description.maximum                       ->
            {
                stringBuilder.append("{")
                stringBuilder.append(description.minimum)
                stringBuilder.append("}")
            }
            description.maximum == Int.MAX_VALUE                             ->
            {
                stringBuilder.append("{")
                stringBuilder.append(description.minimum)
                stringBuilder.append(",}")
            }
            else                                                             ->
            {
                stringBuilder.append("{")
                stringBuilder.append(description.minimum)
                stringBuilder.append(",")
                stringBuilder.append(description.maximum)
                stringBuilder.append("}")
            }
        }
    }

    private fun computeRegexRepetition(description: DescriptionRepetition, stringBuilder: StringBuilder)
    {
        if (description.minimum == 0 && description.maximum == 0 || description.minimum > description.maximum)
        {
            return
        }

        stringBuilder.append("(?:")
        stringBuilder.append(this.regexOf(description.name))
        stringBuilder.append(")")
        this.computeRepetition(description, stringBuilder)
    }

    private fun computeRegexChoice(description: DescriptionChoice, stringBuilder: StringBuilder)
    {
        val length = description.size

        if (length == 0)
        {
            return
        }

        stringBuilder.append("(?:")
        stringBuilder.append(this.regexOf(description[0]))
        stringBuilder.append(")")

        (1 until length).forEach {
            stringBuilder.append("|(?:")
            stringBuilder.append(this.regexOf(description[it]))
            stringBuilder.append(")")
        }
    }

    private fun computeRegexRegex(description: DescriptionRegex, stringBuilder: StringBuilder)
    {
        stringBuilder.append(description.regex)
    }

    private fun computeRegexOrName(description: DescriptionNameOrRegex, stringBuilder: StringBuilder)
    {
        if (description.isRegex)
        {
            stringBuilder.append(description.value)
        }
        else
        {
            stringBuilder.append("(?:")
            stringBuilder.append(this.regexOf(description.value))
            stringBuilder.append(")")
        }
    }

    private fun computeRegexComposition(description: DescriptionComposition, stringBuilder: StringBuilder)
    {
        val length = description.size

        if (length == 0)
        {
            return
        }

        this.computeRegexOrName(description[0], stringBuilder)

        (1 until length).forEach {
            this.computeRegexOrName(description[it], stringBuilder)
        }
    }

    private fun computeRegex(definition: GrammarDefinition, stringBuilder: StringBuilder)
    {
        val description = definition.element

        when
        {
            description is DescriptionRepetition  -> this.computeRegexRepetition(description, stringBuilder)
            description is DescriptionChoice      -> this.computeRegexChoice(description, stringBuilder)
            description is DescriptionRegex       -> this.computeRegexRegex(description, stringBuilder)
            description is DescriptionComposition -> this.computeRegexComposition(description, stringBuilder)
        }
    }

    private fun computeRegex(definition: GrammarDefinition): String
    {
        val stringBuilder = StringBuilder()
        this.computeRegex(definition, stringBuilder)
        return stringBuilder.toString()
    }

    private fun regexFor(definition: GrammarDefinition): () -> String =
            {
                this.computeRegex(definition)
            }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator() = this.definitions.iterator()

    operator fun contains(rule: String) = this.definitions.any { it.name == rule }
    operator fun contains(definition: GrammarDefinition) = definition in this.definitions
    operator fun get(rule: String) = this.definitions.first { it.name == rule }
    operator fun get(index: Int) = this.definitions[index]
    val size: Int get() = this.definitions.size
    fun clear()
    {
        this.definitions.clear()
        this.regexMap.clear()
        this.regexMapDirect.clear()
    }

    fun regexOf(rule: String): String
    {
        val definition = this.definitions.firstOrNull { it.name == rule }

        if (definition == null)
        {
            return ""
        }

        return this.regexMap.getOrPut(rule, this.regexFor(definition))
    }

    fun matchDirect(text: String, exclude: String = ""): Pair<String, Matcher>?
    {
        this.definitions.forEach {
            if (exclude !in it)
            {
                val pattern = this.regexMapDirect.getOrPut(it.name,
                                                           { Pattern.compile(this.regexWithDirectGroup(it.name)) })
                val matcher = pattern.matcher(text)

                if (matcher.matches())
                {
                    var noNull = true
                    var group = matcher.groupCount()

                    while (group > 0 && noNull)
                    {
                        noNull = matcher.group(group) != null
                        group--
                    }

                    if (noNull)
                    {
                        return Pair(it.name, matcher)
                    }
                }
            }
        }

        return null
    }

    private fun regexWithDirectGroup(rule: String): String
    {
        val definition = this.definitions.first { it.name == rule }
        val description = definition.element

        return when (description)
        {
            is DescriptionRepetition  ->
            {
                val stringBuilder = StringBuilder()
                stringBuilder.append('(')
                stringBuilder.append(this.regexOf(description.name))
                stringBuilder.append(')')
                this.computeRepetition(description, stringBuilder)
                stringBuilder.toString()
            }
            is DescriptionChoice      ->
            {
                val stringBuilder = StringBuilder()
                val length = description.size

                if (length > 0)
                {
                    stringBuilder.append('(')
                    stringBuilder.append(this.regexOf(description[0]))
                    stringBuilder.append(')')

                    (1 until length).forEach {
                        stringBuilder.append("|(")
                        stringBuilder.append(this.regexOf(description[it]))
                        stringBuilder.append(')')
                    }
                }

                stringBuilder.toString()
            }
            is DescriptionComposition ->
            {
                val stringBuilder = StringBuilder()
                val length = description.size

                if (length > 0)
                {
                    stringBuilder.append('(')

                    if (description[0].isRegex)
                    {
                        stringBuilder.append(description[0].value)
                    }
                    else
                    {
                        stringBuilder.append(this.regexOf(description[0].value))
                    }

                    stringBuilder.append(')')

                    (1 until length).forEach {
                        stringBuilder.append('(')

                        if (description[it].isRegex)
                        {
                            stringBuilder.append(description[it].value)
                        }
                        else
                        {
                            stringBuilder.append(this.regexOf(description[it].value))
                        }

                        stringBuilder.append(')')
                    }
                }

                stringBuilder.toString()
            }
            is DescriptionRegex       ->
            {
                val stringBuilder = StringBuilder()
                stringBuilder.append('(')
                stringBuilder.append(description.regex)
                stringBuilder.append(')')
                stringBuilder.toString()
            }
            else                      -> this.regexOf(rule)
        }
    }

    fun rules() = this.definitions.transform { it.name }
    override fun toString() = this.definitions.joinToString("\n")
}