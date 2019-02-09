package khelp.bitcode.compiler

interface Precompiler
{
    /**
     * Precompile the line to bytecode instructions.
     *
     * If the precompiler ignore the line or have nothing to change, it returns it as is in one element list
     */
    fun precompile(line: String): List<String>
}

object DefaultPrecompiler : Precompiler
{
    override fun precompile(line: String) = listOf(line)
}