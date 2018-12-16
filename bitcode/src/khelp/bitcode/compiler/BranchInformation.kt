package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.BranchHandle

/**
 * Describe a branch instruction GOTO, IF*, ... to resolve later
 *
 * @param branchHandle Branch handle where put resolved target
 * @param label Label target
 * @param lineNumber Declaration line number
 */
class BranchInformation(val branchHandle: BranchHandle, val label: String, val lineNumber: Int)
