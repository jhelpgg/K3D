package khelp.ui.textEditor.language

import khelp.ui.textEditor.JHelpAutoStyledTextArea
import java.util.regex.Pattern

open class LanguageDescriptor
{
    private val descriptors = HashMap<Rules, MutableList<Pair<Pattern, Int>>>()

    /**
     * Associate a regular expression to a rule
     *
     * @param rules Rule to associate
     * @param pattern Regular expression
     * @param groupInPattern Group, in regular expression, to apply decoration
     */
    final fun associate(rules: Rules, pattern: Pattern, groupInPattern: Int)
    {
        if (rules != Rules.DEFAULT)
        {
            var patterns = this.descriptors[rules]

            if (patterns == null)
            {
                patterns = ArrayList<Pair<Pattern, Int>>()
                this.descriptors[rules] = patterns
            }

            patterns.add(Pair(pattern, groupInPattern))
        }
    }

    final fun associate(rules: Rules, vararg keyWords: String)
    {
        if (rules != Rules.DEFAULT)
        {
            var patterns = this.descriptors[rules]

            if (patterns == null)
            {
                patterns = ArrayList<Pair<Pattern, Int>>()
                this.descriptors[rules] = patterns
            }

            for (keyWord in keyWords)
            {
                // We want get the word if it is alone, so we have to check if it in middle a word or not.
                // That's why we add something before and something after.
                patterns.add(Pair(Pattern.compile("(?:[^a-zA-Z0-9_]|^)(${Pattern.quote(keyWord)})(?:[^a-zA-Z0-9_]|$)"),
                                  1))
            }
        }
    }

    /**
     * Add the description to a [JHelpAutoStyledTextArea]
     *
     * The previous description on [JHelpAutoStyledTextArea] aren't remove, this is just an addition
     *
     * For having only this description use [JHelpAutoStyledTextArea.describeLanguage]
     *
     * @param autoStyledTextArea [JHelpAutoStyledTextArea] to add the description
     */
    final fun addToAutoStyledText(autoStyledTextArea: JHelpAutoStyledTextArea)
    {
        var name: String

        for ((rules, patterns) in this.descriptors)
        {
            name = rules.name

            for ((pattern, groupInPattern) in patterns)
            {
                autoStyledTextArea.associate(name, pattern, groupInPattern)
            }
        }

        autoStyledTextArea.refresh()
    }
}