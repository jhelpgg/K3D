package khelp.ui.textEditor.language

import java.util.regex.Pattern

final class XMLDescriptor : LanguageDescriptor()
{
    init
    {
        this.associate(Rules.COMMENT, Pattern.compile("<!--([^-]|-[^-]|--[^>]|\\n)*-->/"), 0);
        this.associate(Rules.KEY_WORD, Pattern.compile("</?\\s*([a-zA-Z0-9_][a-zA-Z0-9_.$:]*)(\\s|>)"), 1);
        this.associate(Rules.STRING, Pattern.compile("\".*\""), 0);
        this.associate(Rules.PRIMITIVE,
                       Pattern.compile("<\\s*([a-zA-Z0-9_][a-zA-Z0-9_.$:]*)(\\s*([a-zA-Z0-9_][a-zA-Z0-9_.$:]*)\\s*=)*"),
                       3);
    }
}