package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.Type

/**
 * Describe a field of class
 *
 * @param alias Alias give to field
 * @param name Field name on class
 * @param className Class name where find the field
 * @param type Field type
 * @param reference Reference in constant pool
 * @param lineDeclaration Line number where field declare
 */
class FieldInformation(val alias: String, val name: String = alias,
                       val className: String? = null, val type: Type,
                       val reference: Int, val lineDeclaration: Int)
