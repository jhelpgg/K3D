package khelp.grammar

import khelp.util.HashCode
import java.util.regex.Pattern

class GrammarNode(val rule: GrammarDefinition, val text: String)
{
    private val children = ArrayList<GrammarNode>()
    internal fun add(node: GrammarNode)
    {
        if (this != node)
        {
            this.children.add(node)
        }
    }

    val size: Int get() = this.children.size
    operator fun get(index: Int) = this.children[index]
    override fun toString() = "${this.rule} => ${this.text.replace("\n", "\\n").replace("\t", "\\t")}"
    internal fun fillString(stringBuilder: StringBuilder, header: String)
    {
        stringBuilder.append(header)
        stringBuilder.append('-')

        if (this.size == 0)
        {
            stringBuilder.append('-')
        }
        else
        {
            stringBuilder.append('+')
        }

        stringBuilder.append("${this.rule}: ${this.text.replace("\n", "\\n").replace("\t", "\\t")}\n")
        val subHeader = "$header |"
        this.children.forEach { it.fillString(stringBuilder, subHeader) }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
        {
            return true
        }

        if (null == other || other !is GrammarNode)
        {
            return false
        }

        return this.rule == other.rule && this.text == other.text
    }

    override fun hashCode() = HashCode.computeHashCode(this.rule, this.text)
}

class GrammarTree(text: String, grammar: Grammar)
{
    val root = GrammarNode(GrammarDefinition("", DescriptionRegex("(?:.|[\\n\\t])*")), text)

    init
    {
        this.fill(this.root, text, grammar, grammar[0])
    }

    private fun fill(node: GrammarNode, text: String, grammar: Grammar, definition: GrammarDefinition)
    {
        val rule = definition.name
        val descritpion = definition.element
        val regex = grammar.regexOf(rule)

        if (regex.length == 0)
        {
            return
        }

        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(text)

        while (matcher.find())
        {
            if (matcher.start() < matcher.end())
            {
                val subText = text.substring(matcher.start(), matcher.end())
                val child = GrammarNode(definition, subText)
                node.add(child)
                val pair = grammar.matchDirect(subText, rule)

                if (pair != null)
                {
                    val groupRule = pair.first
                    val groupMatcher = pair.second
                    val groupCount = groupMatcher.groupCount()
                    val groupDefinition = grammar[groupRule]
                    val groupDescription = groupDefinition.element

                    if (groupCount > 0)
                    {
                        child.add(GrammarNode(groupDefinition, groupMatcher.group(0)))
                    }

                    when (groupDescription)
                    {
                        is DescriptionComposition ->
                            (1..groupCount).forEach {
                                val groupText = groupMatcher.group(it)
                                val part = groupDescription[it - 1]

                                if (!part.isRegex)
                                {
                                    this.fill(child, groupText, grammar, grammar[part.value])
                                }
                            }
                        is DescriptionChoice      ->
                            (1..groupCount).forEach {
                                val groupText = groupMatcher.group(it)
                                val part = groupDescription[it - 1]
                                this.fill(child, groupText, grammar, grammar[part])
                            }
                        is DescriptionRepetition  ->
                        {
                            val repeat = subText//groupMatcher.group(1)
                            val repeatPattern = Pattern.compile(grammar.regexOf(groupDescription.name))
                            val repeatMatcher = repeatPattern.matcher(repeat)

                            while (repeatMatcher.find())
                            {
                                if (repeatMatcher.start() < repeatMatcher.end())
                                {
                                    this.fill(child, repeat.substring(repeatMatcher.start(), repeatMatcher.end()),
                                              grammar, grammar[groupDescription.name])
                                }
                            }
                        }
                    }
                }
                //                else
                //                {
                //                    val description = grammar[rule].element
                //
                //                    when (description)
                //                    {
                //                        is DescriptionComposition ->
                //                            description.forEach {
                //                                if (!it.isRegex)
                //                                {
                //                                    this.fill(child, subText, grammar, it.value)
                //                                }
                //                            }
                //                        is DescriptionChoice      ->
                //                            description.forEach {
                //                                this.fill(child, subText, grammar, it)
                //                            }
                //                        is DescriptionRepetition  ->
                //                            this.fill(child, subText, grammar, description.name)
                //                    }
                //                }
            }
        }
    }

    fun fillString(stringBuilder: StringBuilder, header: String = "")
    {
        this.root.fillString(stringBuilder, header)
    }

    override fun toString(): String
    {
        val stringBuilder = StringBuilder()
        this.fillString(stringBuilder)
        return stringBuilder.toString()
    }
}