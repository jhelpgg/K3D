package khelp.bytecode.editor.ui

import khelp.ui.textEditor.language.LanguageDescriptor
import khelp.ui.textEditor.language.Rules
import java.util.regex.Pattern

class BytecodeDescriptor : LanguageDescriptor()
{
    init
    {
        this.associate(Rules.COMMENT, Pattern.compile("//.*"), 0);
        this.associate(Rules.COMMENT, Pattern.compile("/\\*([^*]|\\*[^/]|\\n)*\\*/"), 0);
        this.associate(Rules.COMMENT, Pattern.compile(";.*"), 0);
        this.associate(Rules.STRING, Pattern.compile("\".*\""), 0);
        this.associate(Rules.STRING, Pattern.compile("'.*'"), 0);
        this.associate(Rules.PRIMITIVE, "boolean", "char", "byte", "short", "int", "long", "float", "double");
        this.associate(Rules.KEY_WORD,
                       "class", "interface", "abstract",
                       "import",
                       "extends",
                       "implements",
                       "field", "field_reference",
                       "public", "protected", "package", "private",
                       "final", "open",
                       "static",
                       "method", "parameter", "return", "throws",
                       "{", "}", "(", ")",
                       "this")
        this.associate(Rules.KEY_WORD, Pattern.compile("<clinit>"), 0)
        this.associate(Rules.KEY_WORD, Pattern.compile("<init>"), 0)
        this.associate(Rules.OPERAND, "AALOAD", "AASTORE", "ACONST_NULL", "ALOAD", "ANEWARRAY", "ARETURN",
                       "ARRAYLENGTH", "ASTORE", "ATHROW",
                       "BALOAD", "BASTORE", "BIPUSH", "BREAKPOINT",
                       "CALOAD", "CASTORE", "CHECKCAST",
                       "D2F", "D2I", "D2L", "DADD", "DALOAD", "DASTORE", "DCMPG", "DCMPL", "DCONST", "DDIV", "DLOAD",
                       "DMUL", "DNEG", "DREM", "DRETURN", "DSTORE", "DSUB", "DUP", "DUP_X1", "DUP_X2", "DUP2",
                       "DUP2_X1", "DUP2_X2",
                       "F2D", "F2I", "F2L", "FADD", "FALOAD", "FASTORE", "FCMPG", "FCMPL", "FCONST", "FDIV", "FLOAD",
                       "FMUL", "FNEG", "FREM", "FRETURN", "FSTORE", "FSUB",
                       "GETFIELD", "GETSTATIC", "GOTO", "GOTO_W",
                       "I2B", "I2C", "I2D", "I2F", "I2L", "I2S", "IADD", "IALOAD", "IAND", "IASTORE", "ICONST", "IDIV",
                       "IF_ACMPEQ", "IF_ACMPNE", "IF_ICMPEQ", "IF_ICMPGE", "IF_ICMPGT", "IF_ICMPLE", "IF_ICMPLT",
                       "IF_ICMPNE", "IFEQ", "IFGE", "IFGT", "IFLE", "IFLT", "IFNE", "IFNONNULL", "IFNULL", "IINC",
                       "ILOAD", "IMPDEP1", "IMPDEP2", "IMUL", "INEG", "INSTANCEOF", "INVOKEINTERFACE", "INVOKESPECIAL",
                       "INVOKESTATIC", "INVOKEVIRTUAL", "IOR", "IREM", "IRETURN", "ISHL", "ISHR", "ISTORE", "ISUB",
                       "IUSHR", "IXOR",
                       "JSR", "JSR_W",
                       "L2D", "L2F", "L2I", "LADD", "LALOAD", "LAND", "LASTORE", "LCMP", "LCONST", "LDC", "LDC_W",
                       "LDC2_W", "LDIV", "LLOAD", "LMUL", "LNEG", "LOOKUPSWITCH", "LOR", "LREM", "LRETURN", "LSHL",
                       "LSHR", "LSTORE", "LSUB", "LUSHR", "LXOR",
                       "MONITORENTER", "MONITOREXIT", "MULTIANEWARRAY",
                       "NEW", "NEWARRAY", "NOP",
                       "POP", "POP2", "PUSH", "PUTFIELD", "PUTSTATIC",
                       "RET", "RETURN",
                       "SALOAD", "SASTORE", "SIPUSH", "SWAP", "SWITCH",
                       "TABLESWITCH",
                       "CATCH", "LABEL", "SUB_C", "SUB_E", "SUB_S", "TRY", "VAR")
    }
}