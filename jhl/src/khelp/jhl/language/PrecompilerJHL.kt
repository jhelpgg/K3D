package khelp.jhl.language

import khelp.bitcode.compiler.Compiler
import khelp.bitcode.compiler.Precompiler
import khelp.bitcode.compiler.Z_VAR
import khelp.util.transform
import java.util.regex.Matcher
import java.util.regex.Pattern

private val regexDeclaration = Pattern.compile(
        "\\s*(class|abstract|interface)\\s+([a-zA-Z][a-zA-Z0-9_.]*)(?:\$|;.*|\\s.*)")
private const val groupDeclarationType = 1
private const val groupDeclarationClassName = 2
private val regexParameter = Pattern.compile(
        "\\s*parameter\\s+([a-zA-Z][a-zA-Z0-9_\\[\\]]*)\\s+([a-zA-Z][a-zA-Z0-9]*)(?:\$|;.*|\\s.*)")
private const val groupParameterType = 1
private const val groupParameterName = 2
private val regexVar = Pattern.compile(
        "\\s*$Z_VAR\\s+([a-zA-Z][a-zA-Z0-9_\\[\\]]*)\\s+([a-zA-Z][a-zA-Z0-9]*)(?:\$|;.*|\\s.*)")
private const val groupVarType = 1
private const val groupVarName = 2
private val regexNewInstance = Pattern.compile("\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\).*")
private const val groupNewInstanceClassName = 1
private const val groupNewInstanceParameters = 2
private val regexMethodCall = Pattern.compile(
        "\\s*([a-zA-Z][a-zA-Z0-9_]*)\\.([a-zA-Z][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\).*")
private const val groupMethodCallInstance = 1
private const val groupMethodCallMethodName = 2
private const val groupMethodCallParameters = 3

object PrecompilerJHL : Precompiler
{
    private val actions =
            arrayOf(Pair(regexDeclaration, this::foundDeclaration),
                    Pair(regexParameter, this::foundParameter),
                    Pair(regexVar, this::foundVar),
                    Pair(regexNewInstance, this::foundNewInstance),
                    Pair(regexMethodCall, this::foundMethodCall))
    private val vars = HashMap<String, String>()

    private fun foundDeclaration(line: String, matcher: Matcher): List<String>
    {
        var type = matcher.group(groupDeclarationClassName)
        val index = type.lastIndexOf('.')

        if (index >= 0)
        {
            type = type.substring(index + 1)
        }

        this.vars.put("this", type)
        return listOf(line)
    }

    private fun foundParameter(line: String, matcher: Matcher): List<String>
    {
        val type = matcher.group(groupParameterType)
        val name = matcher.group(groupParameterName)
        this.vars.put(name, type)
        return listOf(line)
    }

    private fun foundVar(line: String, matcher: Matcher): List<String>
    {
        val type = matcher.group(groupVarType)
        val name = matcher.group(groupVarName)
        this.vars.put(name, type)
        return listOf(line)
    }

    private fun foundNewInstance(line: String, matcher: Matcher): List<String>
    {
        val lines = ArrayList<String>()
        val className = matcher.group(groupNewInstanceClassName)
        val parameters = matcher.group(groupNewInstanceParameters)?.trim()
        lines.add("NEW $className")
        lines.add("DUP")
        val types = StringBuilder()

        if (parameters != null && parameters.isNotEmpty())
        {
            parameters.split(',').transform { it.trim() }.forEach { parameter ->
                val type = this.vars[parameter]!!
                lines.add(
                        when (type)
                        {
                            "boolean" -> "ILOAD $parameter"
                            "char"    -> "ILOAD $parameter"
                            "byte"    -> "ILOAD $parameter"
                            "short"   -> "ILOAD $parameter"
                            "int"     -> "ILOAD $parameter"
                            "long"    -> "LLOAD $parameter"
                            "float"   -> "FLOAD $parameter"
                            "double"  -> "DLOAD $parameter"
                            else      -> "ALOAD $parameter"
                        })

                if (types.isNotEmpty())
                {
                    types.append(", ")
                }

                types.append(type)
            }
        }

        lines.add("INVOKESPECIAL $className.<init>(${types.toString()})")
        return lines
    }

    private fun foundMethodCall(line: String, matcher: Matcher): List<String>
    {
        val lines = ArrayList<String>()
        val instance = matcher.group(groupMethodCallInstance)
        val methodName = matcher.group(groupMethodCallMethodName)
        val parameters = matcher.group(groupMethodCallParameters)?.trim()
        lines.add("ALOAD $instance")
        val types = StringBuilder()

        if (parameters != null && parameters.isNotEmpty())
        {
            parameters.split(',').transform { it.trim() }.forEach { parameter ->
                val type = this.vars[parameter]!!
                lines.add(
                        when (type)
                        {
                            "boolean" -> "ILOAD $parameter"
                            "char"    -> "ILOAD $parameter"
                            "byte"    -> "ILOAD $parameter"
                            "short"   -> "ILOAD $parameter"
                            "int"     -> "ILOAD $parameter"
                            "long"    -> "LLOAD $parameter"
                            "float"   -> "FLOAD $parameter"
                            "double"  -> "DLOAD $parameter"
                            else      -> "ALOAD $parameter"
                        })

                if (types.isNotEmpty())
                {
                    types.append(", ")
                }

                types.append(type)
            }
        }

        lines.add("INVOKEVIRTUAL ${this.vars[instance]}.$methodName(${types.toString()})")
        return lines
    }

    override fun precompile(line: String): List<String>
    {
        for ((regex, action) in this.actions)
        {
            val matcher = regex.matcher(line)

            if (matcher.matches())
            {
                return action(line, matcher)
            }
        }


        return listOf(line)
    }
}

val compilerJHL = Compiler(PrecompilerJHL)