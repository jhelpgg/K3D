package khelp.bytecode.editor.description

import khelp.text.StringExtractor
import khelp.util.toDescriptiveString
import java.util.regex.Pattern

private val groupRegex = Pattern.compile("<([a-zA-Z0-9_\\-]+)>")

class BytecodeInformation(string: String)
{
    val shortDescription: String
    private val regex: Pattern
    private val groups: Array<Pair<String, Int>>
    private val stackEffects: Array<StackEffect>

    init
    {
        val stringExtractor = StringExtractor(string, "|", "", "", false)
        stringExtractor.addOpenCloseIgnore('(', ')', true)
        this.shortDescription = stringExtractor.next()!!
        val regexBuilder = StringBuilder()
        val regex = stringExtractor.next()!!
        val length = regex.length
        val matcher = groupRegex.matcher(regex)
        val groups = ArrayList<Pair<String, Int>>()
        var start = 0
        var groupID = 1

        while (matcher.find())
        {
            val end = matcher.start()

            if (start < end)
            {
                regexBuilder.append(regex.substring(start, end))
            }

            groups += Pair(matcher.group(1), groupID)
            groupID++
            start = matcher.end()
        }

        if (start < length)
        {
            regexBuilder.append(regex.substring(start))
        }

        this.regex = Pattern.compile(regexBuilder.toString())
        this.groups = groups.toTypedArray()
        val stackEffects = ArrayList<StackEffect>()
        var next = stringExtractor.next()

        while (next != null)
        {
            stackEffects += StackEffect(next)
            next = stringExtractor.next()
        }

        this.stackEffects = stackEffects.toTypedArray()
    }

    fun canApply(stack: List<StackType>) = this.stackEffects.any { it.acceptedStack(stack) }

    fun apply(stack: MutableList<StackType>): Boolean
    {
        for (stackEffect in this.stackEffects)
        {
            if (stackEffect.acceptedStack(stack))
            {
                stackEffect.applyEffect(stack)
                return true
            }
        }

        return false
    }

    fun groups() = this.groups.map { it.first }

    fun match(string: String): Map<String, String>?
    {
        val matcher = this.regex.matcher(string)

        if (!matcher.matches())
        {
            return null
        }

        val extractedGroup = HashMap<String, String>()

        for (group in this.groups)
        {
            extractedGroup[group.first] = matcher.group(group.second)?.trim() ?: ""
        }

        return extractedGroup
    }

    override fun toString() = this.shortDescription
}
