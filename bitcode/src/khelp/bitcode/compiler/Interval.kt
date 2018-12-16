package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.InstructionHandle

/**
 * Define a block code interval
 */
class Interval
{
    /**First instruction of the block handle*/
    var handleStart: InstructionHandle? = null
    /**First instruction of the block line number*/
    var lineStart = -1
    /**Last instruction of the block handle*/
    var handleEnd: InstructionHandle? = null
    /**Last instruction of the block line number*/
    var lineEnd = -1

    override fun toString() = "${this.lineStart}<>${this.lineEnd} : ${this.handleStart}<>${this.handleEnd}"
}