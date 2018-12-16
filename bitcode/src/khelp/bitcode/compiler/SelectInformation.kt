package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.InstructionHandle
import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH
import com.sun.org.apache.bcel.internal.generic.SWITCH
import com.sun.org.apache.bcel.internal.generic.Select
import com.sun.org.apache.bcel.internal.generic.TABLESWITCH
import khelp.list.SortedArray

class SelectInformation(val lineNumber: Int)
{
    private val cases = SortedArray<SelectCase>(SelectCase::class.java, unique = true)
    var defaultLabel: String? = null
    private var select: Select? = null
    val numberOfCases get() = this.cases.size

    /**
     * Create empty targets (They will be filled later)
     *
     * @return Empty targets
     */
    private fun createEmptyTargets() = arrayOfNulls<InstructionHandle>(this.cases.size)

    /**
     * Collect all matches cases
     */
    private fun createsMatches() = IntArray(this.cases.size) { this.cases[it].match }

    /**
     * Add a case
     */
    fun addCase(match: Int, label: String)
    {
        this.cases += SelectCase(match, label)
    }

    /**
     * Create a LOOKUPSWITCH instruction from current information
     */
    fun createLOOKUPSWITCH(): Select
    {
        this.select = LOOKUPSWITCH(this.createsMatches(),
                                   this.createEmptyTargets(),
                                   null)
        return this.select!!
    }

    /**
     * Create a SWITCH instruction from current information
     */
    fun createSWITCH(): Select
    {
        val switch1 = SWITCH(this.createsMatches(), this.createEmptyTargets(), null)
        this.select = switch1.instruction as Select
        return this.select!!
    }

    /**
     * Create a TABLESWITCH instruction from current information
     */
    fun createTABLESWITCH(): Select
    {
        this.select = TABLESWITCH(this.createsMatches(), this.createEmptyTargets(), null)
        return this.select!!
    }

    fun caseLabel(index: Int) = this.cases[index].label

    fun resolveCase(index: Int, instructionHandle: InstructionHandle) = this.select?.setTarget(index, instructionHandle)

    fun resolveDefaultLabel(instructionHandle: InstructionHandle)
    {
        this.select?.target = instructionHandle
    }
}