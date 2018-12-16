package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.generic.ArrayType
import com.sun.org.apache.bcel.internal.generic.ObjectType
import com.sun.org.apache.bcel.internal.generic.Type

class StackElement(val type: Type)
{
    /**
     * Indicates if given type can be use with the embed one
     */
    fun compatibleWith(type: Type) =
            when
            {
                this.type == type       -> true
                this.isInt()            -> type === Type.BOOLEAN || type === Type.CHAR || type === Type.BYTE || type === Type.SHORT || type === Type.INT
                this.type === Type.NULL -> type is ObjectType || type is ArrayType
                this.type is ObjectType -> type === Type.NULL || type is ObjectType
                this.type is ArrayType  -> type === Type.NULL || type is ObjectType || type is ArrayType
                else                    -> false
            }

    /**
     * Indicates if can be use as reference on array
     */
    fun isArrayRef() = this.type === Type.NULL || this.type is ArrayType

    /**
     * Indicates if it is a double
     */
    fun isDouble() = this.type === Type.DOUBLE

    /**
     * Indicates if it is a double or a long
     */
    fun isDoubleOrLong() = this.type === Type.LONG || this.type === Type.DOUBLE

    /**
     * Indicates if it is a float
     */
    fun isFloat() = this.type === Type.FLOAT

    /**
     * Indicates if can be consider as int
     */
    fun isInt() =
            this.type === Type.BOOLEAN || this.type === Type.CHAR || this.type === Type.BYTE || this.type === Type.SHORT || this.type === Type.INT

    /**
     * Indicates if it is a long
     */
    fun isLong() = this.type === Type.LONG

    /**
     * Indicates if reference on object
     */
    fun isObjectRef() = this.type === Type.NULL || this.type is ObjectType || this.type is ArrayType

    override fun toString() = this.type.toString()
}