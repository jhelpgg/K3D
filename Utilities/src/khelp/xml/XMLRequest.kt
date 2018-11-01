package khelp.xml

import khelp.text.ANY
import khelp.text.RegexPart
import khelp.text.regexText
import khelp.text.zeroOrMore

/**
 * Request in XML.
 *
 * Use with [XMLRequester] as filter to receive only tags that match to criteria
 * @property tagNameRegex Regular expression that the tag name must match
 */
class XMLRequest(val tagNameRegex: RegexPart)
{
    /**
     * Search te exact given tag name
     * @param name Tag searched
     * @constructor
     */
    constructor(name: String) : this(name.regexText())

    /**Constraints on tag arguments*/
    internal val argumentsConstraints = ArrayList<Pair<RegexPart, RegexPart>>()

    /** Regular expression that tag text must match*/
    var textRegex = ANY.zeroOrMore()

    /**Constraints that at least one tag child have to fulfill*/
    internal val childPositiveConstraints = ArrayList<XMLRequest>()

    /**Constraints that no tag child have to fulfill*/
    internal val childNegativeConstraints = ArrayList<XMLRequest>()

    /**
     * Add constraint for one tag argument
     *
     * Only tags that have argument match all constraints add here are reported
     * @param nameRegex Regular expression that argument name have to match
     * @param valueRegex  Regular expression that argument value have to match
     */
    fun filterOnArgument(nameRegex: RegexPart, valueRegex: RegexPart = ANY.zeroOrMore())
    {
        this.argumentsConstraints += Pair(nameRegex, valueRegex)
    }

    /**
     * Add constraint that at least one of child have to match for the tag be report
     * @param request Request that one child have to fulfill
     */
    fun oneChildHaveToMatch(request: XMLRequest)
    {
        this.childPositiveConstraints += request
    }

    /**
     * Add constraint that no child have to match for the tag be report
     * @param request Request that no child have to fulfill
     */
    fun noChildHaveToMatch(request: XMLRequest)
    {
        this.childNegativeConstraints += request
    }
}

