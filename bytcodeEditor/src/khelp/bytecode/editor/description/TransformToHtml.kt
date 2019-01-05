package khelp.bytecode.editor.description

import khelp.text.resolveImagesLinkInHTML

fun transformToHTML(text: String): String
{
    val html = StringBuilder()
    html.append("<html>")
    var lineReturned = false
    var insideList = false
    var boldStarted = false
    var imageStarted = false

    for (character in text.toCharArray())
    {
        when (character)
        {
            '['  ->
            {
                html.append("<table border=1><tr><td>")
                lineReturned = false
            }
            ']'  ->
            {
                html.append("</td></tr></table>")
                lineReturned = false
            }
            '\n' ->
            {
                html.append("<br>")
                lineReturned = true
            }
            '*'  ->
                if (boldStarted)
                {
                    html.append("</b>")
                    boldStarted = false
                    lineReturned = false
                }
                else
                {
                    html.append("<b>")
                    boldStarted = true
                    lineReturned = false
                }
            '-'  ->
                if (lineReturned)
                {
                    lineReturned = false

                    if (insideList)
                    {
                        html.append("</li>")
                    }
                    else
                    {
                        html.append("<ul>")
                    }

                    html.append("<li>")
                    insideList = true
                }
                else
                {
                    html.append(character)
                }
            '<'  ->
            {
                if (lineReturned)
                {
                    if (insideList)
                    {
                        html.append("</li></ul>")
                    }

                    insideList = false
                }

                lineReturned = false
                html.append("&lt;")
            }
            '>'  ->
            {
                if (lineReturned)
                {
                    if (insideList)
                    {
                        html.append("</li></ul>")
                    }

                    insideList = false
                }

                lineReturned = false
                html.append("&gt;")
            }
            '\t' ->
            {
                if (lineReturned)
                {
                    if (insideList)
                    {
                        html.append("</li></ul>")
                    }

                    insideList = false
                }

                lineReturned = false
                html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
            }
            '='  ->
            {
                if (!lineReturned || !insideList)
                {
                    html.append(character)
                }

                lineReturned = false
            }
            '@'  ->
            {
                lineReturned = false

                if (imageStarted)
                {
                    imageStarted = false
                    html.append("\" width=\"32\" height=\"32\"/>")
                }
                else
                {
                    imageStarted = true
                    html.append("<img src=\"")
                }
            }
            else ->
            {
                if (lineReturned)
                {
                    if (insideList)
                    {
                        html.append("</li></ul>")
                    }

                    insideList = false
                }

                lineReturned = false
                html.append(character)
            }
        }
    }

    if (boldStarted)
    {
        html.append("</b>")
    }

    if (insideList)
    {
        html.append("</li></ul>")
    }

    html.append("</html>")
    return resolveImagesLinkInHTML(html.toString())
}
