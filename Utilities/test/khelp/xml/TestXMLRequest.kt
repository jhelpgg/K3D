package khelp.xml

import khelp.text.ANY
import khelp.text.plus
import khelp.text.regex
import khelp.text.regexText
import khelp.text.zeroOrMore
import org.junit.Assert
import org.junit.Test

val xmlForTest =
        """
<XML>
   <Aa arg1="val1" arg2="val2">
      <B arg="valB" />
      <C arg="valC">
         Text of C
      </C>
   </Aa>
   <Ab arg1="valA1" arg2="valA2">
      <B arg="valB2" />
      <C arg="valC2">
         Text of C2
      </C>
   </Ab>
</XML>
"""

class TestXMLRequest
{
    @Test
    fun testSimpleRequest()
    {
        val dynamicReadXML = DynamicReadXML(xmlForTest)
        val request = XMLRequest("A".regexText() + ANY)
        val requester = XMLRequester(request, dynamicReadXML)

        var tag = requester.nextMatch()
        Assert.assertEquals("Aa", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("val1", tag.arguments["arg1"])
        Assert.assertEquals("val2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        var child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC", child.arguments["arg"])
        Assert.assertEquals("Text of C", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("Ab", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("valA1", tag.arguments["arg1"])
        Assert.assertEquals("valA2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB2", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC2", child.arguments["arg"])
        Assert.assertEquals("Text of C2", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("", tag.name)
        Assert.assertEquals(0, tag.arguments.size)
        Assert.assertEquals(0, tag.children.size)
        Assert.assertEquals("", tag.text)
    }

    @Test
    fun testFilterOnArgumentRequest()
    {
        val dynamicReadXML = DynamicReadXML(xmlForTest)
        val request = XMLRequest("A".regexText() + ANY)
        request.filterOnArgument("arg1".regexText(),
                                 ANY.zeroOrMore() + 'A'.regex() + ANY.zeroOrMore())
        val requester = XMLRequester(request, dynamicReadXML)

        var tag = requester.nextMatch()
        Assert.assertEquals("Ab", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("valA1", tag.arguments["arg1"])
        Assert.assertEquals("valA2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        var child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB2", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC2", child.arguments["arg"])
        Assert.assertEquals("Text of C2", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("", tag.name)
        Assert.assertEquals(0, tag.arguments.size)
        Assert.assertEquals(0, tag.children.size)
        Assert.assertEquals("", tag.text)
    }

    @Test
    fun testFilterOnChildRequest()
    {
        val dynamicReadXML = DynamicReadXML(xmlForTest)
        val request = XMLRequest("A".regexText() + ANY)
        val requestOnChild = XMLRequest("B".regexText())
        requestOnChild.filterOnArgument("arg".regexText(),
                                        ANY.zeroOrMore() + '2'.regex())
        request.oneChildHaveToMatch(requestOnChild)
        val requester = XMLRequester(request, dynamicReadXML)

        var tag = requester.nextMatch()
        Assert.assertEquals("Ab", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("valA1", tag.arguments["arg1"])
        Assert.assertEquals("valA2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        var child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB2", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC2", child.arguments["arg"])
        Assert.assertEquals("Text of C2", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("", tag.name)
        Assert.assertEquals(0, tag.arguments.size)
        Assert.assertEquals(0, tag.children.size)
        Assert.assertEquals("", tag.text)
    }

    @Test
    fun testFilterOnTextRequest()
    {
        val dynamicReadXML = DynamicReadXML(xmlForTest)
        val request = XMLRequest("A".regexText() + ANY)
        val requestOnChild = XMLRequest("C".regexText())
        requestOnChild.textRegex = ANY.zeroOrMore() + '2'.regex() + ANY.zeroOrMore()
        request.oneChildHaveToMatch(requestOnChild)
        val requester = XMLRequester(request, dynamicReadXML)

        var tag = requester.nextMatch()
        Assert.assertEquals("Ab", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("valA1", tag.arguments["arg1"])
        Assert.assertEquals("valA2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        var child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB2", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC2", child.arguments["arg"])
        Assert.assertEquals("Text of C2", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("", tag.name)
        Assert.assertEquals(0, tag.arguments.size)
        Assert.assertEquals(0, tag.children.size)
        Assert.assertEquals("", tag.text)
    }

    @Test
    fun testFilterNegativeOnChildRequest()
    {
        val dynamicReadXML = DynamicReadXML(xmlForTest)
        val request = XMLRequest("A".regexText() + ANY)
        val requestOnChild = XMLRequest("B".regexText())
        requestOnChild.filterOnArgument("arg".regexText(),
                                        ANY.zeroOrMore() + '2'.regex())
        request.noChildHaveToMatch(requestOnChild)
        val requester = XMLRequester(request, dynamicReadXML)

        var tag = requester.nextMatch()
        Assert.assertEquals("Aa", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("val1", tag.arguments["arg1"])
        Assert.assertEquals("val2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        var child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC", child.arguments["arg"])
        Assert.assertEquals("Text of C", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("", tag.name)
        Assert.assertEquals(0, tag.arguments.size)
        Assert.assertEquals(0, tag.children.size)
        Assert.assertEquals("", tag.text)
    }

    @Test
    fun testFilterNegativeOnTextRequest()
    {
        val dynamicReadXML = DynamicReadXML(xmlForTest)
        val request = XMLRequest("A".regexText() + ANY)
        val requestOnChild = XMLRequest("C".regexText())
        requestOnChild.textRegex = ANY.zeroOrMore() + '2'.regex() + ANY.zeroOrMore()
        request.noChildHaveToMatch(requestOnChild)
        val requester = XMLRequester(request, dynamicReadXML)

        var tag = requester.nextMatch()
        Assert.assertEquals("Aa", tag.name)
        Assert.assertEquals(2, tag.arguments.size)
        Assert.assertEquals("val1", tag.arguments["arg1"])
        Assert.assertEquals("val2", tag.arguments["arg2"])
        Assert.assertEquals("", tag.text)
        Assert.assertEquals(2, tag.children.size)
        var child = tag.children[0]
        Assert.assertEquals("B", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valB", child.arguments["arg"])
        Assert.assertEquals("", child.text)
        child = tag.children[1]
        Assert.assertEquals("C", child.name)
        Assert.assertEquals(1, child.arguments.size)
        Assert.assertEquals("valC", child.arguments["arg"])
        Assert.assertEquals("Text of C", child.text)

        tag = requester.nextMatch()
        Assert.assertEquals("", tag.name)
        Assert.assertEquals(0, tag.arguments.size)
        Assert.assertEquals(0, tag.children.size)
        Assert.assertEquals("", tag.text)
    }
}