package khelp.bitcode.compiler

/**
 * Description : Load object reference from array
 *
 * Syntax :
 *
 * `AALOAD`
 *
 * Operand stack : `..., array_reference, index => ..., value`
 *
 * Where :
 * * arrayref : Reference of array elements type
 * * index : Array index (int)
 * * value : Reference of the desired object inside the array
 *
 */
val AALOAD = "AALOAD"
/**
 * Description : Store into reference array
 *
 * Syntax :
 *
 * `AASTORE`
 *
 * Operand stack : `..., array_reference, index, value => ...`
 *
 * Where :
 *
 *  * arrayref : Reference of array elements type
 *  * index : Array index (int)
 *  * value : Reference object to put inside the array at desired index
 *
 */
val AASTORE = "AASTORE"
/**
 * Description : Push null
 *
 * Syntax :
 *
 * `ACONST_NULL`
 *
 * Operand stack : `... => ..., null`
 *
 * Details: Push null reference on stack
 */
val ACONST_NULL = "ACONST_NULL"
/**
 * Description : Load reference from local variable
 *
 * Syntax :
 *
 * `ALOAD <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name OR 'this'
 *
 *
 *
 * Operand stack : ... => ..., objectref
 *
 * Where :
 *
 *  * objectref : Reference object loaded
 *
 *
 *
 * Details: If `name` is a method variable name, the corresponding variable must have previously initialized (Even
 * with null)
 */
val ALOAD = "ALOAD"
/**
 * Description : Create new array of reference
 *
 * Syntax :
 *
 * `ANEWARRAY <Type>`
 *
 * Where :
 *
 *  * Type : is the variable type (see [ASM grammar definition of "Type"][Compiler].
 *
 *
 *
 * Operand stack : ..., count => ..., arrayref
 *
 * Where :
 *
 *  * count : Array size (int)
 *  * arrayref : Reference on created array
 *
 */
val ANEWARRAY = "ANEWARRAY"
/**
 * Description : Return reference from method
 *
 * Syntax :
 *
 * `ARETURN`
 *
 * Operand stack : ..., objectref => [ empty ]
 *
 * Where :
 *
 *  * objectref : reference of object to return
 *
 *
 *
 * Details:If the current method is a synchronized method, the monitor entered or reentered on invocation of the method is
 * updated and possibly exited as if by execution of a monitorexit instruction in the current thread. If no exception is
 * thrown, **objectref** is popped from the operand stack of the current frame and pushed onto the operand stack of the
 * frame of the invoker. Any other values on the operand stack of the current method are discarded.
 */
val ARETURN = "ARETURN"
/**
 * Description : Get length of array
 *
 * Syntax :
 *
 * `ARRAYLENGTH`
 *
 * Operand stack : ..., arrayref => ..., length
 *
 * Where :
 *
 *  * arrayref : Reference on aarray
 *  * length : Array length (int)
 *
 *
 *
 * Details:
 */
val ARRAYLENGTH = "ARRAYLENGTH"
/**
 * Description : Store reference into local variable
 *
 * Syntax :
 *
 * `ASTORE <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 *
 *
 * Operand stack : ..., objectref => ...
 *
 * Where :
 *
 *  * objectref : Object reference to push on local variable
 *
 */
val ASTORE = "ASTORE"
/**
 * Description : Throw exception or error
 *
 * Syntax :
 *
 * `ATHROW`
 *
 * Operand stack : .., objectref => objectref
 *
 * Where :
 *
 *  * objectref : Reference on exception to throw
 *
 */
val ATHROW = "ATHROW"
/**
 * Description : Load byte or boolean from array
 *
 * Syntax :
 *
 * `BALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref : reference on array
 *  * index : Array index (int)
 *  * value : Value in array at given index (int)
 *
 *
 *
 * Details: The byte value in the component of the array at **index** is retrieved, sign-extended to an int value, and
 * pushed onto the top of the operand stack.
 */
val BALOAD = "BALOAD"
/**
 * Description : Store into byte or boolean array
 *
 * Syntax :
 *
 * `BASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref : reference on array
 *  * index : Array index (int)
 *  * value : Value to store in array at given index (int)
 *
 *
 *
 * Details:The int **value** is truncated to a byte and stored as the component of the array indexed by **index**.
 */
val BASTORE = "BASTORE"
/**
 * Description : Push byte
 *
 * Syntax :
 *
 * `BIPUSH <value>`
 *
 * Where :
 *
 *  * value : Constant value to push
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value: pushed value (int)
 *
 *
 *
 * Details: The immediate **value** is sign-extended to an int **value**. That **value** is pushed onto the operand
 * stack.
 */
val BIPUSH = "BIPUSH"
/**
 * Description : Make a break point. Here does nothing
 *
 * Syntax :
 *
 * `BREAKPOINT`
 */
val BREAKPOINT = "BREAKPOINT"
/**
 * Description : Load char from array
 *
 * Syntax :
 *
 * `CALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref : reference on array
 *  * index : Array index (int)
 *  * value : Value in array at given index (int)
 *
 *
 *
 * Details: The component of the array at index is retrieved and zero-extended to an int value. That value is pushed onto the
 * operand stack.
 */
val CALOAD = "CALOAD"
/**
 * Description : Store into char array
 *
 * Syntax :
 *
 * `CASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref : reference on array
 *  * index : Array index (int)
 *  * value : Value to put in array at given index (int)
 *
 *
 *
 * Details: The int value is truncated to a char and stored as the component of the array indexed by index.
 */
val CASTORE = "CASTORE"
/**
 * Description : Cast object to given type
 *
 * Syntax :
 *
 * <table border=1>
 * <tr>
 * <td>CHECKCAST <;ClassName&gt;</td>
</tr> *
</table> *
 *
 *
 * Where :
 *
 *  * ClassName : The complete class name (with the package) or the short version (Must be in imports, "java.lang" or same
 * package of the class)
 *
 *
 *
 * Operand stack :
 * <table border=1>
 * <tr>
 * <td>..., objectref1 => ..., objectref2</td>
</tr> *
</table> *
 *
 *
 * Where :
 *
 *  * objectref1 : reference to the object to cast
 *  * objectref2 : casted reference
 *
 *
 *
 * Details: The cast must be possible else a ClassCastException happen
 */
val CHECKCAST = "CHECKCAST"
/**
 * Description : Convert double to float
 *
 * Syntax :
 *
 * `D2F`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : double to convert
 *  * result : float converted
 *
 *
 *
 * Details: The value is converted to a float result using IEEE 754 round to nearest mode
 */
val D2F = "D2F"
/**
 * Description : Convert double to int
 *
 * Syntax :
 *
 * `D2I`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : double to convert
 *  * result : int converted
 *
 *
 *
 * Details:
 *
 *  * If the value' is NaN, the result of the conversion is an int 0
 *  * Otherwise, if the value is not an infinity, it is rounded to an integer value V, rounding towards zero using IEEE 754
 * round towards zero mode. If this integer value V can be represented as an int, then the result is the int value V.
 *  * Otherwise, either the value must be too small (a negative value of large magnitude or negative infinity), and the
 * result is the smallest representable value of type int, or the value must be too large (a positive value of large
 * magnitude or positive infinity), and the result is the largest representable value of type int.
 *
 */
val D2I = "D2I"
/**
 * Description : Convert double to long
 *
 * Syntax :
 *
 * `D2L`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : double to convert
 *  * result : long converted
 *
 *
 *
 * Details:
 *
 *  * If the value' is NaN, the result of the conversion is a long 0.
 *  * Otherwise, if the value' is not an infinity, it is rounded to an integer value V, rounding towards zero using IEEE 754
 * round towards zero mode. If this integer value V can be represented as a long, then the result is the long value V.
 *  * Otherwise, either the value' must be too small (a negative value of large magnitude or negative infinity), and the
 * result is the smallest representable value of type long, or the value' must be too large (a positive value of large
 * magnitude or positive infinity), and the result is the largest representable value of type long.
 *
 */
val D2L = "D2L"
/**
 * Description : Add double
 *
 * Syntax :
 *
 * `DADD`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : addition result (double)
 *
 */
val DADD = "DADD"
/**
 * Description : Load double from array
 *
 * Syntax :
 *
 * `DALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref : reference on array
 *  * index : Array index (int)
 *  * value : Value in array at given index (double)
 *
 */
val DALOAD = "DALOAD"
/**
 * Description : Store into double array
 *
 * Syntax :
 *
 * `DASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref : reference on array
 *  * index : Array index (int)
 *  * value : Value to put in array at given index (double)
 *
 */
val DASTORE = "DASTORE"
/**
 * Description : Compare double (Great if NaN)
 *
 * Syntax :
 *
 * `DCMPG`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : comparison result (int)
 *
 *
 *
 * Details:
 *
 *  * If value1' is greater than value2', the int value 1 is pushed onto the operand stack.
 *  * Otherwise, if value1' is equal to value2', the int value 0 is pushed onto the operand stack.
 *  * Otherwise, if value1' is less than value2', the int value -1 is pushed onto the operand stack.
 *  * Otherwise, at least one of value1' or value2' is NaN pushes the int value 1 onto the operand stack
 *
 */
val DCMPG = "DCMPG"
/**
 * Description : Compare double (Low if NaN)
 *
 * Syntax :
 *
 * `DCMPG`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : comparison result (int)
 *
 *
 *
 * Details:
 *
 *  * If value1' is greater than value2', the int value 1 is pushed onto the operand stack.
 *  * Otherwise, if value1' is equal to value2', the int value 0 is pushed onto the operand stack.
 *  * Otherwise, if value1' is less than value2', the int value -1 is pushed onto the operand stack.
 *  * Otherwise, at least one of value1' or value2' is NaN pushes the int value -1 onto the operand stack
 *
 */
val DCMPL = "DCMPL"
/**
 * Description : Push double 0 or 1
 *
 * Syntax :
 *
 * `DCONST <value>`
 *
 * Where :
 *
 *  * value : Must be 0 or 1
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : the value pushed (0 or 1) (double)
 *
 */
val DCONST = "DCONST"
/**
 * Description : Divide double
 *
 * Syntax :
 *
 * `DADD`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : division result (double)
 *
 */
val DDIV = "DDIV"
/**
 * Description : Load double from local variable
 *
 * Syntax :
 *
 * `DLOAD <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : value of the method variable or parameter
 *
 *
 *
 * Details: If `name` is a method variable name, the corresponding variable must have previously initialized (Even
 * with 0)
 */
val DLOAD = "DLOAD"
/**
 * Description : Multiply double
 *
 * Syntax :
 *
 * `DMUL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : multiplication result (double)
 *
 */
val DMUL = "DMUL"
/**
 * Description : Negate double
 *
 * Syntax :
 *
 * `DNEG`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : Value to negate (double)
 *  * result : Negated result (double)
 *
 */
val DNEG = "DNEG"
/**
 * Description : Remainder on double
 *
 * Syntax :
 *
 * `DREM`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : remainder result (double)
 *
 */
val DREM = "DREM"
/**
 * Description : Return double from method
 *
 * Syntax :
 *
 * `DRETURN`
 *
 * Operand stack : ..., value => [empty]
 *
 * Where :
 *
 *  * value : value to return (double)
 *
 */
val DRETURN = "DRETURN"
/**
 * Description : Store double to local variable
 *
 * Syntax :
 *
 * `DSTORE <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 * Operand stack : ...,value => ...
 *
 * Where :
 *
 *  * value : value to store in the method variable or parameter
 *
 */
val DSTORE = "DSTORE"
/**
 * Description : Subtract double
 *
 * Syntax :
 *
 * `DSUB`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first double
 *  * value2 : second double
 *  * result : subtraction result (double)
 *
 */
val DSUB = "DSUB"
/**
 * Description : Duplicate the top operand stack value
 *
 * Syntax :
 *
 * `DUP`
 *
 * Operand stack : ..., value => ..., value, value
 *
 * Where :
 *
 *  * value : value to duplicate
 *
 *
 *
 * Details: Value can't be a double or long
 */
val DUP = "DUP"
/**
 * Description : Duplicate the top operand stack value and insert two values down
 *
 * Syntax :
 *
 * `DUP_X1`
 *
 * Operand stack : ..., value2, value1 => ..., value1, value2, value1
 *
 * Where :
 *
 *  * value1 : value to duplicate
 *  * value2 : value to just move
 *
 *
 *
 * Details: value1 and value2 can't be double or long
 */
val DUP_X1 = "DUP_X1"
/**
 * Description : Duplicate the top operand stack value and insert two or three values down
 *
 * Syntax :
 *
 * `DUP_X2`
 *
 * Operand stack : .., value3, value2, value1 => ..., value1, value3, value2, value1
 *
 * Where :
 *
 *  * value1 : value to duplicate
 *  * value2 : value to just move
 *  * value3 : value to just move
 *
 *
 *
 * Details: value1, value2 and value3 can't be double or long
 *
 *
 *
 * <h1>**OR**</h1>
 *
 * Operand stack : .., value2, value1 => ..., value1, value2, value1
 *
 * Where :
 *
 *  * value1 : value to duplicate
 *  * value2 : value to just move
 *
 *
 *
 * Details: value1 not a long nor double, value2 double or long
 */
val DUP_X2 = "DUP_X2"
/**
 * Description : Duplicate the top one or two operand stack values
 *
 * Syntax :
 *
 * `DUP2`
 *
 * Operand stack : ..., value2, value1 => ..., value2, value1, value2, value1
 *
 * Where :
 *
 *  * value1 : value to duplicate
 *  * value2 : value to duplicate
 *
 *
 *
 * Details:value1 and value2 can't be double or long
 *
 *
 *
 * <h1>**OR**</h1>
 *
 * Operand stack : ..., value => ...,value, value
 *
 * Where
 *
 *  * value : value to duplicate *
 *
 *
 *
 * Details:value double or long
 */
val DUP2 = "DUP2"
/**
 * Description : Duplicate the top one or two operand stack values and insert two or three values down
 *
 * Syntax :
 *
 * `DUP2_X1`
 *
 * Operand stack : .., value3, value2, value1 => ..., value2, value1, value3, value2, value1
 *
 * Where :
 *
 *  * value1 : value to duplicate
 *  * value2 : value to duplicate
 *  * value3 : value to just move
 *
 * Details: value1, value2 and value3 not long nor double
 *
 *
 *
 * <h1>**OR**</h1>
 *
 * Operand stack : ..., value2, value1 => ..., value1, value2, value1
 *
 * Where
 *
 *  * value1 : value to duplicate
 *  * value2 : value to just move
 *
 * Details: value1 long or double and value2 not long nor double
 */
val DUP2_X1 = "DUP2_X1"
/**
 * Description : Duplicate the top one or two operand stack values and insert two, three, or four values down
 *
 * Syntax :
 *
 * `DUP2_X2`
 *
 * Operand stack : ..., value4, value3, value2, value1 => ..., value2, value1, value4, value3, value2, value1
 *
 * Where :
 *
 *  * value1 : not long nor double
 *  * value2 : not long nor double
 *  * value3 : not long nor double
 *  * value4 : not long nor double
 *
 *
 *
 * <h1>**OR**</h1>
 *
 * Operand stack : ..., value3, value2, value1 => ..., value1, value3, value2, value1
 *
 * Where :
 *
 *  * value1 : long or double
 *  * value2 : not long nor double
 *  * value3 : not long nor double
 *
 *
 *
 * <h1>**OR**</h1>
 *
 * Operand stack : ..., value3, value2, value1 => ..., value2, value1, value3, value2, value1
 *
 * Where :
 *
 *  * value1 : not long nor double
 *  * value2 : not long nor double
 *  * value3 : long or double
 *
 *
 *
 * <h1>**OR**</h1>
 *
 * Operand stack : ..., value2, value1 => ..., value1, value2, value1
 *
 * Where :
 *
 *  * value1 : long or double
 *  * value2 : long or double
 *
 */
val DUP2_X2 = "DUP2_X2"
/**
 * Description : Convert float to double
 *
 * Syntax :
 *
 * `F2D`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : Value to convert (float)
 *  * result : Converted value (double)
 *
 */
val F2D = "F2D"
/**
 * Description : Convert float to int
 *
 * Syntax :
 *
 * `F2I`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : Value to convert (float)
 *  * result : Converted value (int)
 *
 * Details:
 *
 *  * If the value' is NaN, the result of the conversion is an int 0
 *  * Otherwise, if the value' is not an infinity, it is rounded to an integer value V, rounding towards zero using IEEE 754
 * round towards zero mode. If this integer value V can be represented as an int, then the result is the int value V.
 *  * Otherwise, either the value' must be too small (a negative value of large magnitude or negative infinity), and the
 * result is the smallest representable value of type int, or the value' must be too large (a positive value of large
 * magnitude or positive infinity), and the result is the largest representable value of type int.
 *
 */
val F2I = "F2I"
/**
 * Description : Convert float to long
 *
 * Syntax :
 *
 * `F2L`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : Value to convert (float)
 *  * result : Converted value (long)
 *
 * Details:
 *
 *  * If the value' is NaN, the result of the conversion is a long 0.
 *  * Otherwise, if the value' is not an infinity, it is rounded to an integer value V, rounding towards zero using IEEE 754
 * round towards zero mode. If this integer value V can be represented as a long, then the result is the long value V.
 *  * Otherwise, either the value' must be too small (a negative value of large magnitude or negative infinity), and the
 * result is the smallest representable value of type long, or the value' must be too large (a positive value of large
 * magnitude or positive infinity), and the result is the largest representable value of type long.
 *
 */
val F2L = "F2L"
/**
 * Description : Add float
 *
 * Syntax :
 *
 * `FADD`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : addition result (float)
 *
 */
val FADD = "FADD"
/**
 * Description : Load float from array
 *
 * Syntax :
 *
 * `FALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref: Reference to array
 *  * index : Array index (int)
 *  * value : Value in array at given index (float)
 *
 */
val FALOAD = "FALOAD"
/**
 * Description :Store into float array
 *
 * Syntax :
 *
 * `FASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref: Reference to array
 *  * index : Array index (int)
 *  * value : Value to put in array at given index (float)
 *
 */
val FASTORE = "FASTORE"
/**
 * Description : Compare float (Great NaN)
 *
 * Syntax :
 *
 * `FCMPG`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : comparison result (int)
 *
 * Details:
 *
 *  * If value1' is greater than value2', the int value 1 is pushed onto the operand stack.
 *  * Otherwise, if value1' is equal to value2', the int value 0 is pushed onto the operand stack.
 *  * Otherwise, if value1' is less than value2', the int value -1 is pushed onto the operand stack.
 *  * Otherwise, at least one of value1' or value2' is NaN. The instruction pushes the int value 1 onto the operand stack
 *
 *
 */
val FCMPG = "FCMPG"
/**
 * Description : Compare float (Low NaN)
 *
 * Syntax :
 *
 * `FCMPL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : comparison result (int)
 *
 * Details:
 *
 *  * If value1' is greater than value2', the int value 1 is pushed onto the operand stack.
 *  * Otherwise, if value1' is equal to value2', the int value 0 is pushed onto the operand stack.
 *  * Otherwise, if value1' is less than value2', the int value -1 is pushed onto the operand stack.
 *  * Otherwise, at least one of value1' or value2' is NaN. The instruction pushes the int value -1 onto the operand stack
 *
 *
 */
val FCMPL = "FCMPL"
/**
 * Description : Push float constant 0,1 or 2
 *
 * Syntax :
 *
 * `FCONST <value>`
 *
 * Where :
 *
 *  * value : value to push 0, 1 or 2
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : value pushed 0, 1 or 2 (float)
 *
 */
val FCONST = "FCONST"
/**
 * Description : Divide float
 *
 * Syntax :
 *
 * `FDIV`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : division result (float)
 *
 */
val FDIV = "FDIV"
/**
 * Description : Load float from local variable
 *
 * Syntax :
 *
 * `FLOAD <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : value of the method variable or parameter
 *
 *
 *
 * Details: If `name` is a method variable name, the corresponding variable must have previously initialized (Even
 * with 0)
 */
val FLOAD = "FLOAD"
/**
 * Description : Multiply float
 *
 * Syntax :
 *
 * `FMUL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : multiplication result (float)
 *
 */
val FMUL = "FMUL"
/**
 * Description : Negate float
 *
 * Syntax :
 *
 * `FNEG`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : float to negate
 *  * result : negated float
 *
 */
val FNEG = "FNEG"
/**
 * Description : Remainder float
 *
 * Syntax :
 *
 * `FREM`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : remainder result (float)
 *
 */
val FREM = "FREM"
/**
 * Description : Return float from method
 *
 * Syntax :
 *
 * `FRETURN`
 *
 * Operand stack : ..., value => [empty]
 *
 * Where :
 *
 *  * value : vlaue to return (float)
 *
 *
 *
 * Details:
 */
val FRETURN = "FRETURN"
/**
 * Description : Store float to local variable
 *
 * Syntax :
 *
 * `FSTORE <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 * Operand stack : ...,value => ...
 *
 * Where :
 *
 *  * value : value to store in the method variable or parameter
 *
 */
val FSTORE = "FSTORE"
/**
 * Description : Subtract float
 *
 * Syntax :
 *
 * `FMUL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first float
 *  * value2 : second float
 *  * result : subtraction result (float)
 *
 */
val FSUB = "FSUB"
/**
 * Description : Fetch field from object
 *
 * Syntax :
 *
 * `GETFIELD <fieldName/fieldAlias>`
 *
 * Where :
 *
 *  * fieldName : Name of the field to get the value
 *
 *
 *
 * Operand stack : ..., objectref => ..., value
 *
 * Where :
 *
 *  * objectref : reference to the object
 *  * value : field value
 *
 */
val GETFIELD = "GETFIELD"
/**
 * Description : Get static field from class
 *
 * Syntax :
 *
 * `GETSTATIC  <fieldName/fieldAlias>`
 *
 * Where :
 *
 *  * fieldName : Name of the field to get the value
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : field value
 *
 *
 *
 * Details:
 */
val GETSTATIC = "GETSTATIC"
/**
 * Description : Branch always
 *
 * Syntax :
 *
 * `GOTO <label>`
 *
 * Where :
 *
 *  * label : Label name to go
 *
 * Operand stack : No change
 *
 * Details: Be sure the label is declare somewhere in the method with [.Z_LABEL]
 */
val GOTO = "GOTO"
/**
 * Description : Branch always
 *
 * Syntax :
 *
 * `GOTO_W <label>`
 *
 * Where :
 *
 *  * label : Label name to go
 *
 * Operand stack : No change
 *
 * Details: Be sure the label is declare somewhere in the method with [.Z_LABEL]
 *
 * This instruction designed for big jump, but [.GOTO] here auto transform in [.GOTO_W] if jump is too big, so
 * use [.GOTO] and let compiler manage by it self
 */
val GOTO_W = "GOTO_W"
/**
 * Description : Convert int to byte
 *
 * Syntax :
 *
 * `I2B`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (int)
 *  * result : converted value (int)
 *
 * Details: Truncated to a byte then sign-extended to an int result
 */
val I2B = "I2B"
/**
 * Description :Convert int to char
 *
 * Syntax :
 *
 * `I2C`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (int)
 *  * result : converted value (int)
 *
 * Details: Truncated to char, then zero-extended to an int result
 */
val I2C = "I2C"
/**
 * Description : Convert int to double
 *
 * Syntax :
 *
 * `I2D`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (int)
 *  * result : converted value (double)
 *
 */
val I2D = "I2D"
/**
 * Description : Convert int to float
 *
 * Syntax :
 *
 * `I2F`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (int)
 *  * result : converted value (float)
 *
 */
val I2F = "I2F"
/**
 * Description : Convert int to long
 *
 * Syntax :
 *
 * `I2L`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (int)
 *  * result : converted value (long)
 *
 */
val I2L = "I2L"
/**
 * Description : Convert int to short
 *
 * Syntax :
 *
 * `I2S`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (int)
 *  * result : converted value (int)
 *
 * Details: Truncated to a short, then sign-extended to an int result
 */
val I2S = "I2S"
/**
 * Description : Add int
 *
 * Syntax :
 *
 * `IADD`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : addition result (int)
 *
 */
val IADD = "IADD"
/**
 * Description : Load int from array
 *
 * Syntax :
 *
 * `IALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref : Reference on array
 *  * index : Array index (int)
 *  * value : Value in array at given index (int)
 *
 */
val IALOAD = "IALOAD"
/**
 * Description : And on int
 *
 * Syntax :
 *
 * `IAND`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : and result (int)
 *
 */
val IAND = "IAND"
/**
 * Description : Store int to array
 *
 * Syntax :
 *
 * `IASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref : Reference on array
 *  * index : Array index (int)
 *  * value : Value to put in array at given index (int)
 *
 */
val IASTORE = "IASTORE"
/**
 * Description : Push int constant -1, 0, 1 ,2, 3, 4 or 5
 *
 * Syntax :
 *
 * `ICONST <value>`
 *
 * Where :
 *
 *  * value : Value to push -1, 0, 1 ,2, 3, 4 or 5
 *
 *
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * vlaue : value pushed -1, 0, 1 ,2, 3, 4 or 5 (int)
 *
 */
val ICONST = "ICONST"
/**
 * Description : Divide int
 *
 * Syntax :
 *
 * `IDIV`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : division result (int)
 *
 */
val IDIV = "IDIV"
/**
 * Description : Branch if reference comparison (equal) succeeds
 *
 * Syntax :
 *
 * `IF_ACMPEQ <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : reference to first object
 *  * value2 : reference to second object
 *
 */
val IF_ACMPEQ = "IF_ACMPEQ"
/**
 * Description : Branch if reference comparison (not equal) succeeds
 *
 * Syntax :
 *
 * `IF_ACMPNE <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : reference to first object
 *  * value2 : reference to second object
 *
 */
val IF_ACMPNE = "IF_ACMPNE"
/**
 * Description : Branch if int comparison (equal) succeeds
 *
 * Syntax :
 *
 * `IF_ICMPEQ <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *
 */
val IF_ICMPEQ = "IF_ICMPEQ"
/**
 * Description : Branch if int comparison (greater or equal) succeeds
 *
 * Syntax :
 *
 * `IF_ICMPGE  <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *
 */
val IF_ICMPGE = "IF_ICMPGE"
/**
 * Description : Branch if int comparison (greater) succeeds
 *
 * Syntax :
 *
 * `IF_ICMPGT <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *
 */
val IF_ICMPGT = "IF_ICMPGT"
/**
 * Description : Branch if int comparison (lower or equal) succeeds
 *
 * Syntax :
 *
 * `IF_ICMPLE <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *
 */
val IF_ICMPLE = "IF_ICMPLE"
/**
 * Description : Branch if int comparison (lower) succeeds
 *
 * Syntax :
 *
 * `IF_ICMPLT <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *
 */
val IF_ICMPLT = "IF_ICMPLT"
/**
 * Description : Branch if int comparison (not equal) succeeds
 *
 * Syntax :
 *
 * `IF_ICMPNE <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value1, value2 => ...
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *
 */
val IF_ICMPNE = "IF_ICMPNE"
/**
 * Description : Branch if int comparison (equal) with zero succeeds
 *
 * Syntax :
 *
 * `IFEQ <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : int to compare at 0
 *
 */
val IFEQ = "IFEQ"
/**
 * Description : Branch if int comparison (greater or equal) with zero succeeds
 *
 * Syntax :
 *
 * `IFGE <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : int to compare at 0
 *
 */
val IFGE = "IFGE"
/**
 * Description : Branch if int comparison (greater) with zero succeeds
 *
 * Syntax :
 *
 * `IFGT <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : int to compare at 0
 *
 */
val IFGT = "IFGT"
/**
 * Description : Branch if int comparison (lower or equal) with zero succeeds
 *
 * Syntax :
 *
 * `IFLE <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : int to compare at 0
 *
 */
val IFLE = "IFLE"
/**
 * Description : Branch if int comparison (lower) with zero succeeds
 *
 * Syntax :
 *
 * `IFLT <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : int to compare at 0
 *
 */
val IFLT = "IFLT"
/**
 * Description : Branch if int comparison (not equal) with zero succeeds
 *
 * Syntax :
 *
 * `IFNE <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : int to compare at 0
 *
 */
val IFNE = "IFNE"
/**
 * Description : Branch if reference not null
 *
 * Syntax :
 *
 * `IFNONNULL <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : reference object to compare with null
 *
 */
val IFNONNULL = "IFNONNULL"
/**
 * Description : Branch if reference is null
 *
 * Syntax :
 *
 * `IFNULL <label>`
 *
 * Where :
 *
 *  * label : Label name to go if condition respected
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : reference object to compare with null
 *
 */
val IFNULL = "IFNULL"
/**
 * Description : Increment local variable by constant
 *
 * Syntax :
 *
 * `IINC <name> <value>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *  * value : Value to increment with (int : signed byte)
 *
 * Operand stack : No change
 *
 */
val IINC = "IINC"
/**
 * Description : Load int from local variable
 *
 * Syntax :
 *
 * `ILOAD <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : method variable or parameter value (int)
 *
 * Details: If method variable, MUST be previously initialized (Even with 0)
 */
val ILOAD = "ILOAD"
/**
 * Description : Implementation dependent instruction (Not recommend to use it)
 *
 * Syntax :
 *
 * `IMPDEP1`
 *
 */
val IMPDEP1 = "IMPDEP1"
/**
 * Description : Implementation dependent instruction (Not recommend to use it)
 *
 * Syntax :
 *
 * `IMPDEP2`
 *
 */
val IMPDEP2 = "IMPDEP2"
/**
 * Description : Multiply int
 *
 * Syntax :
 *
 * `IMUL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : multiplication result (int)
 *
 */
val IMUL = "IMUL"
/**
 * Description : Negate int
 *
 * Syntax :
 *
 * `INEG`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to negate (int)
 *  * result : negative result (int)
 *
 */
val INEG = "INEG"
/**
 * Description : Determine if object is of given type
 *
 * Syntax :
 *
 * `INSTANCEOF <ClassName>`
 *
 * Where :
 *
 *  * ClassName : class name in complete version or short version (If short version must be in imports, java.lang or current
 * class package)
 *
 * Operand stack : .., objectref => ..., result
 *
 * Where :
 *
 *  * objectref : Reference on object to check the instance
 *  * result : Comparison result (int)
 *
 * Details: If objectref is null, the instanceof instruction pushes an int result of 0 as an int on the operand stack.
 *
 * Otherwise, the named class, array, or interface type is resolved (§5.4.3.1). If objectref is an instance of the resolved
 * class or array or implements the resolved interface, the instanceof instruction pushes an int result of 1 as an int on the
 * operand stack; otherwise, it pushes an int result of 0
 */
val INSTANCEOF = "INSTANCEOF"
/**
 * Description : Invoke interface method
 *
 * Syntax :
 *
 * `INVOKEINTERFACE <methodCompleteDescription> <number>`
 *
 * Where :
 *
 *  * methodCompleteDescription : Complete reference to a method :
 * <;classCompleteName&gt;.<;methodName&gt;<;methodSignature&gt;
 *  * number : number of arguments > 0
 *
 *
 *
 * Operand stack : ..., objectref, [arg1, [arg2 ...]] => ...
 *
 * Where :
 *
 *  * objectref : Reference to object to call
 *  * arg(i) : arguments send to method
 *  *
 *
 * Details:Let C be the class of objectref. The actual method to be invoked is selected by the following lookup procedure:
 *
 *  * If C contains a declaration for an instance method with the same name and descriptor as the resolved method, then this
 * is the method to be invoked, and the lookup procedure terminates.
 *  * Otherwise, if C has a superclass, this same lookup procedure is performed recursively using the direct superclass of
 * C; the method to be invoked is the result of the recursive invocation of this lookup procedure.
 *  * Otherwise, an AbstractMethodError is raised.
 *
 */
val INVOKEINTERFACE = "INVOKEINTERFACE"
/**
 * Description : Invoke instance method; special handling for superclass, private, and instance initialization method
 * invocations
 *
 * Syntax :
 *
 * `INVOKESPECIAL <methodCompleteDescription>`
 *
 * Where :
 *
 *  * methodCompleteDescription : Complete reference to a method :
 * <;classCompleteName&gt;.<;methodName&gt;<;methodSignature&gt;
 *
 * Operand stack : ..., objectref, [arg1, [arg2 ...]] => ...
 *
 * Where :
 *
 *  * objectref : Reference to object to call
 *  * arg(i) : arguments send to method
 *  *
 *
 */
val INVOKESPECIAL = "INVOKESPECIAL"
/**
 * Description : Invoke a class (static) method
 *
 * Syntax :
 *
 * `INVOKESPECIAL <methodCompleteDescription>`
 *
 * Where :
 *
 *  * methodCompleteDescription : Complete reference to a method :
 * <;classCompleteName&gt;.<;methodName&gt;<;methodSignature&gt;
 *
 * Operand stack : ..., [arg1, [arg2 ...]] => ...
 *
 * Where :
 *
 *  * arg(i) : arguments send to method
 *  *
 *
 */
val INVOKESTATIC = "INVOKESTATIC"
/**
 * Description : Invoke instance method; dispatch based on class
 *
 * Syntax :
 *
 * `INVOKEVIRTUAL <methodCompleteDescription>`
 *
 * Where :
 *
 *  * methodCompleteDescription : Complete reference to a method :
 * <;classCompleteName&gt;.<;methodName&gt;<;methodSignature&gt;
 *
 * Operand stack : ..., objectref, [arg1, [arg2 ...]] => ...
 *
 * Where :
 *
 *  * objectref : Reference to object to call
 *  * arg(i) : arguments send to method
 *  *
 *
 */
val INVOKEVIRTUAL = "INVOKEVIRTUAL"
/**
 * Description : Or on int
 *
 * Syntax :
 *
 * `IOR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : or result (int)
 *
 */
val IOR = "IOR"
/**
 * Description : Remainder on int
 *
 * Syntax :
 *
 * `IREM`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : remainder result (int)
 *
 */
val IREM = "IREM"
/**
 * Description : Return int from method
 *
 * Syntax :
 *
 * `IRETURN`
 *
 * Operand stack : ..., value => [empty]
 *
 * Where :
 *
 *  * value : Value to return (int)
 *
 */
val IRETURN = "IRETURN"
/**
 * Description : Shift left int (<;<;)
 *
 * Syntax :
 *
 * `ISHL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : shift left result (int)
 *
 */
val ISHL = "ISHL"
/**
 * Description : Shift right int (&gt;&gt;)
 *
 * Syntax :
 *
 * `ISHR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : shift right result (int)
 *
 */
val ISHR = "ISHR"
/**
 * Description : Store int to local variable
 *
 * Syntax :
 *
 * `ISTORE <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : value to put in method variable or parameter (int)
 *
 */
val ISTORE = "ISTORE"
/**
 * Description : Subtract int
 *
 * Syntax :
 *
 * `ISUB`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : subtraction result (int)
 *
 */
val ISUB = "ISUB"
/**
 * Description : Logical shift right int (&gt;&gt;&gt;)
 *
 * Syntax :
 *
 * `ISHR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : shift right result (int)
 */
val IUSHR = "IUSHR"
/**
 * Description : XOR on int
 *
 * Syntax :
 *
 * `IXOR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first int
 *  * value2 : second int
 *  * result : XOR result (int)
 *
 */
val IXOR = "IXOR"
/**
 * Description : Jump subroutine
 *
 * Syntax :
 *
 * `JSR <label>`
 *
 * Where :
 *
 *  * label : Label to jump
 *
 * Operand stack : ... => ..., address
 *
 * Where :
 *
 *  * address : Address to return
 *
 * Details: In sub routine remember to store return address in local variable to use with [.RET]
 */
val JSR = "JSR"
/**
 * Description : Jump subroutine
 *
 * Syntax :
 *
 * `JSR_W <label>`
 *
 * Where :
 *
 *  * label : Label to jump
 *
 * Operand stack : ... => ..., address
 *
 * Where :
 *
 *  * address : Address to return
 *
 * Details: In sub routine remember to store return address in local variable to use with [.RET]
 *
 * [.JSR_W] are for long jump, use [.JSR] and compiler will choose the best one for you
 */
val JSR_W = "JSR_W"
/**
 * Description : Convert long to double
 *
 * Syntax :
 *
 * `L2D`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (long)
 *  * result : converted value (double)
 *
 * Details: Converted to a double result using IEEE 754 round to nearest mode
 */
val L2D = "L2D"
/**
 * Description : Convert long to float
 *
 * Syntax :
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (long)
 *  * result : converted value (float)
 *
 * Details: Converted to a float result using IEEE 754 round to nearest mode.
 */
val L2F = "L2F"
/**
 * Description : Convert long to int
 *
 * Syntax :
 *
 * `L2I`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : value to convert (long)
 *  * result : converted value (int)
 *
 * Details: Converted to an int result by taking the low-order 32 bits of the long value and discarding the high-order 32
 * bits.
 */
val L2I = "L2I"
/**
 * Description : Add long
 *
 * Syntax :
 *
 * `LADD`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : addition result (long)
 *
 */
val LADD = "LADD"
/**
 * Description : Load long from array
 *
 * Syntax :
 *
 * `LALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref : Reference on array
 *  * index : Array index (int)
 *  * value : Value in array at given index (long)
 *
 */
val LALOAD = "LALOAD"
/**
 * Description : And on long
 *
 * Syntax :
 *
 * `LAND`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : and result (long)
 *
 */
val LAND = "LAND"
/**
 * Description : Load long from array
 *
 * Syntax :
 *
 * `LASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref : Reference on array
 *  * index : Array index (int)
 *  * value : Value to put in array at given index (long)
 *
 */
val LASTORE = "LASTORE"
/**
 * Description : Compare long
 *
 * Syntax :
 *
 * `LCMP`
 *
 * Operand stack : ..., value1, value2 => result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : comparison result (int)
 *
 *
 *
 * Details:If value1 is greater than value2, the int value 1 is pushed onto the operand stack. If value1 is equal to value2,
 * the int value 0 is pushed onto the operand stack. If value1 is less than value2, the int value -1 is pushed onto the
 * operand stack.
 */
val LCMP = "LCMP"
/**
 * Description : Push long constant 0 or 1
 *
 * Syntax :
 *
 * `LCONST <value>`
 *
 * Where :
 *
 *  * value : value to push 0 or 1
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value pushed value 0 or 1 (long)
 *
 */
val LCONST = "LCONST"
/**
 * Description : Push item from run-time constant pool. Laod a constant
 *
 * Syntax :
 *
 * `LDC <value>`
 *
 * Where :
 *
 *  * value : constant value
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value reference to value (If String) or value itself
 *
 * Details: Value of constant can be :
 *
 *  * boolean : true or false
 *  * character : 'a', 'z', '\n', ... (Must be inside ' like Java convention for character)
 *  * int : 123, 89, 20, -9, ...
 *  * float : 1.23f, 5f, -.36f, ... Must end with f
 *  * String : "This is a phrase.", "", "Blabla\n\t\"New line\"", ... Like Java String convention
 *
 * Since byte and short are carry by int, use int for them
 *
 * For long and double use [.LDC2_W]
 */
val LDC = "LDC"
/**
 * Description : Push item from run-time constant pool. Laod a constant
 *
 * Syntax :
 *
 * `LDC_W <value>`
 *
 * Where :
 *
 *  * value : constant value
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value reference to value (If String) or value itself
 *
 * Details: Value of constant can be :
 *
 *  * boolean : true or false
 *  * character : 'a', 'z', '\n', ... (Must be inside ' like Java convention for character)
 *  * int : 123, 89, 20, -9, ...
 *  * float : 1.23f, 5f, -.36f, ... Must end with f
 *  * String : "This is a phrase.", "", "Blabla\n\t\"New line\"", ... Like Java String convention
 *
 * Since byte and short are carry by int, use int for them
 *
 * For long and double use [.LDC2_W]
 */
val LDC_W = "LDC_W"
/**
 * Description : Push item from run-time constant pool. Laod a constant
 *
 * Syntax :
 *
 * `LDC2_W <value>`
 *
 * Where :
 *
 *  * value : constant value
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value reference to value (If String) or value itself
 *
 * Details: Value of constant can be :
 *
 *  * long : 3l, 40L, -8l, ... end with l like Java
 *  * double : 12.3, 0.3, 8d, 3.2D, d at end is mandatory if can be confused with int (No decimal separator)
 *
 * For other constants use [.LDC] or [.LDC_W]
 */
val LDC2_W = "LDC2_W"
/**
 * Description : Divide long
 *
 * Syntax :
 *
 * `LDIV`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : division result (long)
 *
 */
val LDIV = "LDIV"
/**
 * Description : Load long from local variable
 *
 * Syntax :
 *
 * `LLOAD <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : method variable or parameter value (long)
 *
 * Details: If method variable, MUST be previously initialized (Even with 0)
 */
val LLOAD = "LLOAD"
/**
 * Description : Multiply long
 *
 * Syntax :
 *
 * `LMUL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : multiplication result (long)
 *
 */
val LMUL = "LMUL"
/**
 * Description : Negate long
 *
 * Syntax :
 *
 * `LNEG`
 *
 * Operand stack : ..., value => ..., result
 *
 * Where :
 *
 *  * value : Value to negate (long)
 *  * result : Negative result (long)
 *
 *
 *
 * Details:
 */
val LNEG = "LNEG"
/**
 * Description : Switch to label depends on given key
 *
 * Syntax :
 *
 * `LOOKUPSWITCH (<match> <label>)* <defaultLabel>  `
 *
 * Where :
 *
 *  * match : Match value (int)
 *  * label : Label to go if key match to match value
 *  * defaultLabel : Label to go if key not match
 *
 *
 *
 * Operand stack : ..., key => ...
 *
 * Where :
 *
 *  * key : the key value (int)
 *
 *
 *
 * Details: [.LOOKUPSWITCH] is designed for switch can't respect [.TABLESWITCH] constraints.
 *
 * If you don't know the best choose between [.LOOKUPSWITCH] and [.TABLESWITCH] use the [.SWITCH]
 * instruction it will choose the best optimized instruction to use
 */
val LOOKUPSWITCH = "LOOKUPSWITCH"
/**
 * Description : Or on long
 *
 * Syntax :
 *
 * `LOR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : or result (long)
 *
 */
val LOR = "LOR"
/**
 * Description : Remainder long
 *
 * Syntax :
 *
 * `LREM`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : remainder result (long)
 *
 */
val LREM = "LREM"
/**
 * Description : Return long from method
 *
 * Syntax :
 *
 * `LRETURN`
 *
 * Operand stack : ..., value => [empty]
 *
 * Where :
 *
 *  * value : Value to return (long)
 *
 */
val LRETURN = "LRETURN"
/**
 * Description : Shift left long (<;<;)
 *
 * Syntax :
 *
 * `LSHL`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second int
 *  * result : shift left result (long)
 *
 */
val LSHL = "LSHL"
/**
 * Description : Shift right long (&gt;&gt;)
 *
 * Syntax :
 *
 * `LSHR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second int
 *  * result : shift right result (long)
 *
 */
val LSHR = "LSHR"
/**
 * Description : Store long to local variable
 *
 * Syntax :
 *
 * `LLOAD <name>`
 *
 * Where :
 *
 *  * name : Current method variable name OR current method parameter name
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : value to put in method variable or parameter (long)
 *
 */
val LSTORE = "LSTORE"
/**
 * Description : Subtract long
 *
 * Syntax :
 *
 * `LSUB`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : subtraction result (long)
 *
 */
val LSUB = "LSUB"
/**
 * Description : Logical Shift right long (<;<;<;)
 *
 * Syntax :
 *
 * `LSHR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second int
 *  * result : shift right result (long)
 *
 */
val LUSHR = "LUSHR"
/**
 * Description : XOR long
 *
 * Syntax :
 *
 * `LSHR`
 *
 * Operand stack : ..., value1, value2 => ..., result
 *
 * Where :
 *
 *  * value1 : first long
 *  * value2 : second long
 *  * result : XOR result (long)
 *
 */
val LXOR = "LXOR"
/**
 * Description : Enter monitor for object
 *
 * Syntax :
 *
 * `MONITORENTER`
 *
 * Operand stack : ..., objectref => ...
 *
 * Where :
 *
 *  * objectref : reference of object to enter in monitor
 *
 * Details: Each object is associated with a monitor. A monitor is locked if and only if it has an owner. The thread that
 * executes monitorenter attempts to gain ownership of the monitor associated with objectref, as follows:
 *
 *  * If the entry count of the monitor associated with objectref is zero, the thread enters the monitor and sets its entry
 * count to one. The thread is then the owner of the monitor.
 *  * If the thread already owns the monitor associated with objectref, it reenters the monitor, incrementing its entry
 * count.
 *  * If another thread already owns the monitor associated with objectref, the thread blocks until the monitor's entry
 * count is zero, then tries again to gain ownership.
 *
 */
val MONITORENTER = "MONITORENTER"
/**
 * Description : Exit monitor for object
 *
 * Syntax :
 *
 * `MONITOREXIT`
 *
 * Operand stack : ..., objectref => ...
 *
 * Where :
 *
 *  * objectref: reference to object to exit monitor
 *
 * Details: The thread that executes monitorexit must be the owner of the monitor associated with the instance referenced by
 * objectref.
 *
 * The thread decrements the entry count of the monitor associated with objectref. If as a result the value of the entry
 * count is zero, the thread exits the monitor and is no longer its owner. Other threads that are blocking to enter the
 * monitor are allowed to attempt to do so.
 */
val MONITOREXIT = "MONITOREXIT"
/**
 * Description : Create new multidimensional array
 *
 * Syntax :
 *
 * `MULTIANEWARRAY type> <numberOfDimensions>`
 *
 * Where :
 *
 *  * type : Array type (signature or class name)
 *  * numberOfDimensions : Number of dimensions (int)
 *
 * Operand stack : .., count1, [count2, ...] => ..., arrayref
 *
 * Where :
 *
 *  * count(i) : size for dimension i
 *  * arrayref : reference on created array
 *
 */
val MULTIANEWARRAY = "MULTIANEWARRAY"
/**
 * Description : Create new object
 *
 * Syntax :
 *
 * `NEW <type>`
 *
 * Where :
 *
 *  * type : array type class name or signature
 *
 * Operand stack : ... => ..., objectref
 *
 * Where :
 *
 *  * objectref : reference on created object
 *
 */
val NEW = "NEW"
/**
 * Description : Create new array
 *
 * Syntax :
 *
 * `NEWARRAY <primitiveType>`
 *
 * Where :
 *
 *  * primitiveType : primitive array type : boolean, char, byte, short, int, long, float, double
 *
 * Operand stack : ..., count => ..., arrayref
 *
 * Where :
 *
 *  * count : Array size (int)
 *  * arrayref : Reference on array
 *
 */
val NEWARRAY = "NEWARRAY"
/**
 * Description : Do nothing
 *
 * Syntax :
 *
 * `NOP`
 *
 * Operand stack : No change
 */
val NOP = "NOP"
/**
 * Description : Pop the top operand stack value
 *
 * Syntax :
 *
 * `POP`
 *
 * Operand stack : .., value => ...
 *
 * Where :
 *
 *  * value : value to pop (Not long nor double)
 *
 */
val POP = "POP"
/**
 * Description : Pop the top one or two operand stack values
 *
 * Syntax :
 *
 * `POP2`
 *
 * Operand stack : ..., value2, value1 => ...
 *
 * Where :
 *
 *  * value1 : value to pop (Not long nor double)
 *  * value2 : value to pop (Not long nor double)
 *
 *
 *
 * <h1>**OR**</h1>
 *
 *
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : value to pop (long or double)
 *
 */
val POP2 = "POP2"
/**
 * Description : Convenient instruction for push a value (It choose the good instruction for push the value)
 *
 * Syntax :
 *
 * `PUSH  <constantValue>`
 *
 * Where :
 *
 *  * constantValue : constant value to push
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value : Value pushed
 *
 * Details: The constant can have several form :
 *
 *  * boolean : true or false (int pushed)
 *  * char : 'a', 'z', '\t', ... like Java character (int pushed)
 *  * int : 12, -5, 3, ... (int pushed)
 *  * long : 15l, -8L, ... L end to indicates it is long (long pushed)
 *  * float : 1f, 0.2f, .3F, ... F end to indicates it is float (float pushed)
 *  * double : 1.0, 5d, 3.2D, ... with ., or end with D (double pushed)
 *  * String : "file", "", "phrase\nhi" Like Java String (reference to String pushed)
 *
 */
val PUSH = "PUSH"
/**
 * Description : Set field in object
 *
 * Syntax :
 *
 * `PUTFIELD <fieldName>`
 *
 * Where :
 *
 *  * fieldName : Name of the field
 *
 * Operand stack : ..., objectref, value => ...
 *
 * Where :
 *
 *  * objectref : Reference on object (Must be this)
 *  * value : Value to push on field
 *
 */
val PUTFIELD = "PUTFIELD"
/**
 * Description : Set static field in object
 *
 * Syntax :
 *
 * `PUTFIELD <fieldName>`
 *
 * Where :
 *
 *  * fieldName : Name of the field
 *
 * Operand stack : ..., value => ...
 *
 * Where :
 *
 *  * value : Value to push on field
 *
 */
val PUTSTATIC = "PUTSTATIC"
/**
 * Description : Return from subroutine
 *
 * Syntax :
 *
 * `RET <localVaraible>`
 *
 * Where :
 *
 *  * localVaraible : Name of local variable where return address is stored
 *
 * Operand stack : No change
 *
 * Details: It is used in combination with [.JSR]/[.JSR_W].
 *
 * When enter in subroutine, the return address is on the top of the stack, if you want things go properly, you can adopt 2
 * strategies :
 *
 *  1. The most easy is to store the return address in local variable as first instruction of subroutine and not modify this
 * variable until the end of the subroutine to use it only for [.RET].
 *  1. Or pay attention to stack status in way that the address not delete from the stack and be at top at the end of
 * subroutine and store it in local variable just before call the [.RET].
 *
 */
val RET = "RET"
/**
 * Description : Return void from method
 *
 * Syntax :
 *
 * `RETURN`
 *
 * Operand stack : ... => [empty]
 *
 */
val RETURN = "RETURN"
/**
 * Description : Load short from array
 *
 * Syntax :
 *
 * `SALOAD`
 *
 * Operand stack : ..., arrayref, index => ..., value
 *
 * Where :
 *
 *  * arrayref : Array reference
 *  * index : Array index (int)
 *  * value : Value in array at index (int)
 *
 * Details: The short value is sign-extended to an int value
 */
val SALOAD = "SALOAD"
/**
 * Description : Store short to array
 *
 * Syntax :
 *
 * `SASTORE`
 *
 * Operand stack : ..., arrayref, index, value => ...
 *
 * Where :
 *
 *  * arrayref : Array reference
 *  * index : Array index (int)
 *  * value : Value to put in array at index (int)
 *
 * Details: The int value is truncated to short value
 */
val SASTORE = "SASTORE"
/**
 * Description : Push short
 *
 * Syntax :
 *
 * `SIPUSH <value>`
 *
 * Where :
 *
 *  * value : Value to push (short)
 *
 * Operand stack : ... => ..., value
 *
 * Where :
 *
 *  * value ; Value pushed (int)
 *
 * Details: The short value is sign-extended to an int value
 */
val SIPUSH = "SIPUSH"
/**
 * Description : Swap the top two operand stack values
 *
 * Syntax :
 *
 * `SWAP`
 *
 * Operand stack : ..., value1, value2 => ..., value2, value1
 *
 * Where :
 *
 *  * value1 : Value to swap (Not double nor long)
 *  * value2 : Value to swap (Not double nor long)
 *
 */
val SWAP = "SWAP"
/**
 * Description : Switch to label depends on given key
 *
 * Syntax :
 *
 * `SWITCH (<match> <label>)* <defaultLabel>  `
 *
 * Where :
 *
 *  * match : Match value (int)
 *  * label : Label to go if key match to match value
 *  * defaultLabel : Label to go if key not match
 *
 *
 *
 * Operand stack : ..., key => ...
 *
 * Where :
 *
 *  * key : the key value (int)
 *
 *
 *
 * Details: [SWITCH] instruction will choose the best optimized instruction choose between [LOOKUPSWITCH] and
 * [TABLESWITCH] depends on given match/label pairs
 */
val SWITCH = "SWITCH"
/**
 * Description : Switch to label depends on given key
 *
 * Syntax :
 *
 * `TABLESWITCH (<match> <label>)* <defaultLabel>  `
 *
 * Where :
 *
 *  * match : Match value (int)
 *  * label : Label to go if key match to match value
 *  * defaultLabel : Label to go if key not match
 *
 *
 *
 * Operand stack : ..., key => ...
 *
 * Where :
 *
 *  * key : the key value (int)
 *
 *
 *
 * Details: [TABLESWITCH] is designed for switch with a limited number of case and gap between case not too big
 *
 * If you don't know the best choose between [LOOKUPSWITCH] and [TABLESWITCH] use the [SWITCH]
 * instruction it will choose the best optimized instruction to use
 */
val TABLESWITCH = "TABLESWITCH"

// -----

/**
 * Special instruction for close a try/catch block.
 *
 * Syntax :
 *
 * `CATCH <exceptionName> <label>`
 *
 * Where :
 *
 *  * exceptionName : Name f exception, same as corresponding [.Z_TRY]
 *  * label : Label to go if exception happen
 *
 * Inside try/catch block, no jump outside the block. The ?RETURN ([.RETURN], [.ARETURN], [.DRETURN],
 * [.FRETURN], [.IRETURN] or [.LRETURN]) **MUST** be before the catch
 *
 * Avoid also jump from outside the block to inside the block.
 *
 * A [OpcodeConstants.Z_CATCH] **MUST** corresponds to one [.Z_TRY]
 */
val Z_CATCH = "CATCH"
/**
 * Special instruction (Not opcode) for declare a label
 *
 * Syntax :
 *
 * `LABEL <name>`
 *
 * Where `name` is the label name and respects [a-zA-Z][a-zA-Z0-9_]*
 */
val Z_LABEL = "LABEL"
/**
 * Special instruction for call a subroutine
 *
 * Syntax :
 *
 * `SUB_C <name>`
 *
 * Where `name` is the subroutine name to call.
 *
 * Operand stack : ... => ..., address
 *
 * Where address is the address to return
 *
 * Details : Instructions [.Z_SUB_C], [.Z_SUB_S] and [.Z_SUB_E] are to facilitate the subroutine creation
 * to resolve the problem of return address
 *
 * Here it call the subroutine, just use its name.
 *
 * Warning subroutines must be have one [.Z_SUB_S] and one [.Z_SUB_E] (The sub routine code is between this 2
 * instructions). Then they MUST be call with [.Z_SUB_C] (You can use it in several places), other branch instruction
 * outside subroutine code that goes inside subroutine code may cause unexpected result or crash. Same risk apply if exit
 * from subroutine code without [.Z_SUB_E] or call an other subroutine with [.Z_SUB_C]. The subroutine don't
 * manage well the recursive call here, if you want some, you have to deal with [.JSR]/[.JSR_W] and [.RET]
 * and apply the second strategy explains in [.RET].
 */
val Z_SUB_C = "SUB_C"
/**
 * Special instruction for end a subroutine code
 *
 * Syntax :
 *
 * `SUB_E <name>`
 *
 * Where `name` is the subroutine name to exit.
 *
 * Operand stack : No change
 *
 * Details : Instructions [.Z_SUB_C], [.Z_SUB_S] and [.Z_SUB_E] are to facilitate the subroutine creation
 * to resolve the problem of return address
 *
 * Use this instruction to terminate the subroutine code
 *
 * Warning subroutines must be have one [.Z_SUB_S] and one [.Z_SUB_E] (The sub routine code is between this 2
 * instructions). Then they MUST be call with [.Z_SUB_C] (You can use it in several places), other branch instruction
 * outside subroutine code that goes inside subroutine code may cause unexpected result or crash. Same risk apply if exit
 * from subroutine code without [.Z_SUB_E] or call an other subroutine with [.Z_SUB_C]. The subroutine don't
 * manage well the recursive call here, if you want some, you have to deal with [.JSR]/[.JSR_W] and [.RET]
 * and apply the second strategy explains in [.RET].
 */
val Z_SUB_E = "SUB_E"
/**
 * Special instruction for start a subroutine code
 *
 * Syntax :
 *
 * `SUB_S <name>`
 *
 * Where `name` is the subroutine name to start.
 *
 * Operand stack : ..., address -> ...
 *
 * Details : Instructions [.Z_SUB_C], [.Z_SUB_S] and [.Z_SUB_E] are to facilitate the subroutine creation
 * to resolve the problem of return address
 *
 * Use this instruction to start the subroutine code
 *
 * Warning subroutines must be have one [.Z_SUB_S] and one [.Z_SUB_E] (The sub routine code is between this 2
 * instructions). Then they MUST be call with [.Z_SUB_C] (You can use it in several places), other branch instruction
 * outside subroutine code that goes inside subroutine code may cause unexpected result or crash. Same risk apply if exit
 * from subroutine code without [.Z_SUB_E] or call an other subroutine with [.Z_SUB_C]. The subroutine don't
 * manage well the recursive call here, if you want some, you have to deal with [.JSR]/[.JSR_W] and [.RET]
 * and apply the second strategy explains in [.RET].
 */
val Z_SUB_S = "SUB_S"
/**
 * Special instruction to start a try/catch block
 *
 * Syntax :
 *
 * `TRY <ExceptionType> <exceptionName>` Where :
 *
 *  * ExceptionType : Type of exception to catch.
 *  * exceptionName : Exception name, this also declare a local variable, so be sure no already a local variable with same
 * name. Name must be unique to be sure no confusion for [.Z_CATCH]
 *
 * Inside try/catch block, no jump outside the block. The ?RETURN ([.RETURN], [.ARETURN], [.DRETURN],
 * [.FRETURN], [.IRETURN] or [.LRETURN]) **MUST** be before the catch
 *
 * Avoid also jump from outside the block to inside the block.
 *
 * A [.Z_TRY] **MUST** have its corresponding [.Z_CATCH]
 */
val Z_TRY = "TRY"
/**
 * Special instruction (Not opcode) for declare a variable
 *
 * Syntax:
 *
 * `VAR <Type> <name>`
 *
 * Where `Type` is the variable type (see [ASM grammar definition of "Type"][Compiler].
 *
 * Where `name` is the variable name and respects [a-zA-Z][a-zA-Z0-9_]*
 */
val Z_VAR = "VAR"