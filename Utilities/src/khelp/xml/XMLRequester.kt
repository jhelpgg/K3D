package khelp.xml

import java.util.Stack

/**
 * Indicates if tag arguments match to a request
 * @param arguments Arguments to evaluate
 * @param xmlRequest XMLRequest to match
 * @return **`true`** if tag arguments match to request
 */
fun argumentsAreValid(arguments: Arguments, xmlRequest: XMLRequest) =
        xmlRequest.argumentsConstraints.all { (argumentRegex, valueRegex) ->
            arguments.entries.any { (argument, value) ->
                argumentRegex.matches(argument) && valueRegex.matches(value)
            }
        }

/**
 * Describe a tag
 * @property name Tag name
 */
class Tag(val name: String)
{
    /**Tag arguments*/
    val arguments = Arguments()
    /**Tag text*/
    var text = ""
    /**Tag children*/
    val children = ArrayList<Tag>()

    /**
     * Indicates if tag match a request
     * @param xmlRequest XMLRequest to match
     * @return **`true`** if tag match to request
     */
    fun match(xmlRequest: XMLRequest): Boolean
    {
        if (!xmlRequest.tagNameRegex.matches(this.name))
        {
            return false
        }

        if (!argumentsAreValid(this.arguments, xmlRequest))
        {
            return false
        }

        if (!xmlRequest.textRegex.matches(this.text))
        {
            return false
        }

        var matches = xmlRequest.childPositiveConstraints.all { childRequest ->
            this.children.any { it.match(childRequest) }
        }

        if (!matches)
        {
            return false
        }

        matches = xmlRequest.childNegativeConstraints.all { childRequest ->
            this.children.none { it.match(childRequest) }
        }

        return matches
    }
}

/**
 * Requester in XML to collect tags that match given filter
 * @property xmlRequest Request to select tags
 * @property dynamicReadXML XML where search
 */
class XMLRequester(private val xmlRequest: XMLRequest, private val dynamicReadXML: DynamicReadXML)
{
    /**
     * Read next matching tag
     *
     * If no more tag match, a tag with empty name is returned
     * @return Next matching tag
     */
    fun nextMatch(): Tag
    {
        if (this.dynamicReadXML.currentType == EventType.END_XML)
        {
            return Tag("")
        }

        var eventType = this.dynamicReadXML.next()
        var tag: Tag? = null
        val stackTags = Stack<Tag>()

        while (eventType != EventType.END_XML)
        {
            when (eventType)
            {
                EventType.START_XML -> Unit
                EventType.END_XML   -> Unit
                EventType.START_TAG ->
                {
                    val name = this.dynamicReadXML.tagName
                    val arguments = this.dynamicReadXML.arguments()

                    if (tag == null)
                    {
                        if (this.xmlRequest.tagNameRegex.matches(name) && argumentsAreValid(arguments, this.xmlRequest))
                        {
                            tag = Tag(name)
                            tag.arguments.putAll(arguments)
                            stackTags.push(tag)
                        }
                    }
                    else
                    {
                        val currentTag = stackTags.peek()
                        val child = Tag(name)
                        child.arguments.putAll(arguments)
                        currentTag.children += child
                        stackTags.push(child)
                    }
                }
                EventType.END_TAG   ->
                    if (tag != null)
                    {
                        stackTags.pop()

                        if (stackTags.empty())
                        {
                            if (tag.match(this.xmlRequest))
                            {
                                return tag
                            }

                            tag = null
                        }
                    }
                EventType.TEXT      ->
                    if (tag != null)
                    {
                        stackTags.peek().text = this.dynamicReadXML.text()
                    }
            }

            eventType = this.dynamicReadXML.next()
        }

        return Tag("")
    }
}