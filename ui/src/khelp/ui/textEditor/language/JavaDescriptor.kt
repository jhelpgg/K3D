package khelp.ui.textEditor.language

import java.util.regex.Pattern

final class JavaDescriptor : LanguageDescriptor()
{
    init
    {
        this.associate(Rules.COMMENT, Pattern.compile("//.*"), 0)
        this.associate(Rules.COMMENT, Pattern.compile("/\\*([^*]|\\*[^/]|\\n)*\\*/"), 0)
        this.associate(Rules.STRING, Pattern.compile("\".*\""), 0)
        this.associate(Rules.STRING, Pattern.compile("'.*'"), 0)
        this.associate(Rules.PRIMITIVE, "boolean", "char", "byte", "short", "int", "long", "float", "double");
        this.associate(Rules.KEY_WORD, "public", "protected", "private", "abstract", "class", "interface", "enum",
                       "static", "transient", "final", "if", "else", "for", "while", "do", "return", "new", "switch",
                       "case", "break", "continue", "default", "throw", "throws", "try", "catch", "finally", "void",
                       "true", "false", "package", "import", "extends", "implements", "null", "this", "super");
    }
}