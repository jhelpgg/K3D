# Opcodes

Opcodes are instructions in method body.
They describe to method "job".

> **Notes :**
> 
> A method must always exit with one of :
**`RETURN`**, **`ARETURN`**, **`IRETURN`**, **`FRETURN`**, **`LRETURN`**, **`DRETURN`**
opcode.
> 
> Value are **high** typed. 
> That's mean, by example, that is impossible to add **float** and **int** directly.
> We have to do conversion (int to float) explicitly to add two floats.



Before start with opcodes, we need to talk about method stack, since opcodes use it.

## Method stack

When a method is called, a empty stack is created that live during the method playing.
Type that push/pop from stack are : `int`, `float`, `long`, `double`, `null`, `object reference`.

`boolen`, `char` `byte` and `short` are push on stack in `int` form. 
And when `int` read from stack, is truncated for fit one of those types.

True is represents by `1`, false by `0`

`int`, `float`, `null` and `object reference` are called type 1, because they take one place in stack.

`long` and `double` are called type 2, because they take two places in stack.

An opcode read and/or write 0 to several values in stack.

When something is read from the stack, it is the end of the stack that are read.
When something is read, it is removed from the stack.

When something is write to the stack, it is write at the end.

So stack work like LIFO (Last In First Out) algorithm.

To explains stack effect, we adopt the following notation:

![Stack notation](StackNotation.png)

* In red (`...`) : Part of stack not change by the opcode.
* In blue (`object, int`) : Value read and remove by the opcode. 
  If the stack not have enough elements or types are wrong, a crash or compiler error happen.
* In brown (`->`) : Separator of before opcode instruction and after it.
* In green (`double`) : Value write by the opcode.

We use special notation `[]` in right side to show stack is empty after the opcode.

## Opcodes

### Local variables

Inside method, it is possible to create local variable.

````ASM
VAR <type> <name>
````  

Where :
* **type** : Variable type. Examples : `int`, `byte[]`, `String`, ...
* **name** : Variable name.

There no stack effect.

**Warning :**
> The variable have no value. 
> That not means the value is `null` for object or `0` for numbers.
>
> It means that memory are reserved to store the value, 
> and it is developer responsibility to write a value before read it.

##### Load value

Those opcodes read value or object reference and write them to method stack.

The read source can be:
* **`this`** object
* Method parameter
* Local variable

````
     +--------------+--------------------+---------------------------------------------+
     |    Opcode    |    Stack effect    |                   Details                   |
     +--------------+--------------------+---------------------------------------------+
     | ALOAD <name> | ... -> ..., object | Load an object reference                    |
     | ILOAD <name> | ... -> ..., int    | Load an int (or boolean, char, byte, short) |
     | LLOAD <name> | ... -> ..., long   | Load a long                                 |
     | FLOAD <name> | ... -> ..., float  | Load a float                                |
     | DLOAD <name> | ... -> ..., double | Load a double                               |
     +--------------+--------------------+---------------------------------------------+
````

**Notes:**
> * The `name` is the parameter or local variable name
> * In non static method, `ALOAD` can use the special name `this` to have reference of the current instance.
> * Opcodes depends on the variable type. The first letter indicates the type :
>   * **`A`** for object reference
>   * **`I`** for integer, short, byte, char, boolean
>   * **`L`** for long
>   * **`F`** for float
>   * **`D`** for double
  
Example :

````ASM
method test
   parameter int p1
{
   VAR double p2
   // ... put a value to p2
   ALOAD this
   // ...
   ILOAD p1
   // ...
   DLOAD p2
   // ...
   RETURN
}
````

For fields see : [fields operations](FieldOperations.md)

### Store value

Those opcodes read value or object reference from stack and write them to variable.

The write source can be:
* Method parameter
* Local variable

````
     +---------------+--------------------+----------------------------------------------+
     |    Opcode     |    Stack effect    |                   Details                    |
     +---------------+--------------------+----------------------------------------------+
     | ASTORE <name> | ..., object -> ... | Store an object reference                    |
     | ISTORE <name> | ..., int -> ...    | Store an int (or boolean, char, byte, short) |
     | LSTORE <name> | ...,long -> ...    | Store a long                                 |
     | FSTORE <name> | ..., float -> ...  | Store a float                                |
     | DSTORE <name> | ..., double -> ... | Store a double                               |
     +---------------+--------------------+----------------------------------------------+
````

**Notes:**
> * The `name` is the parameter or local variable name
> * Opcodes depends on the variable type. The first letter indicates the type :
>   * **`A`** for object reference
>   * **`I`** for integer, short, byte, char, boolean
>   * **`L`** for long
>   * **`F`** for float
>   * **`D`** for double

Example :

````ASM
method test
   parameter int p1
{
   VAR String name 
   VAR int age
   // ...
   PUSH "Joe"   ; ... -> ..., String
   ASTORE name  ; ..., String -> ...
   // ...
   PUSH 42      ; ... -> ..., int
   ISTORE age   ; ..., int -> ...
   // ...   
   RETURN
}
````

For fields see : [fields operations](FieldOperations.md)

### Return opcodes

Every method (even `void` ones) must exit with a return instruction.


     +---------+-------------------+-----------------------------------------------+
     | Opcode  |   Stack effect    |                    Details                    |
     +---------+-------------------+-----------------------------------------------+
     | RETURN  |     ... -> []     | Exit from void method                         |
     | ARETURN | ..., object -> [] | Return an object or null                      |
     | IRETURN | ..., int -> []    | Return an int (or boolean, char, byte, short) |
     | LRETURN | ...,long -> []    | Return a long                                 |
     | FRETURN | ..., float -> []  | Return a float                                |
     | DRETURN | ..., double -> [] | Return a double                               |
     +---------+-------------------+-----------------------------------------------+

**Notes:**
> Opcodes depends on the variable type. The first letter indicates the type :
> * No letter, for void method
> * **`A`** for object reference
> * **`I`** for integer, short, byte, char, boolean
> * **`L`** for long
> * **`F`** for float
> * **`D`** for double

Import never forget return opcode. 
If method exit without one of return opcode : crash, generally strange method not found, will happen.

##### Computing opcodes 

Here we talk about for operation like addition, subtraction, ...

Those operation can't cross type, by example its impossible to multiply a **long** with a **double**.

It exists some conversion opcodes, explains in next point.

Basic operations :

     +--------+------------------------------------+------------------------------------+
     | Opcode |            Stack effect            |              Details               |
     +========+====================================+====================================+
     |  INEG  |        ..., int -> ..., int        | Oposite of given int (short, byte) |
     |  LNEG  |       ..., long -> ..., long       | Oposite of given long              |
     |  FNEG  |      ..., float -> ..., float      | Oposite of given float             |
     |  DNEG  |     ..., double -> ..., double     | Oposite of given double            |
     +--------+------------------------------------+------------------------------------+
     |  IADD  |     ..., int, int -> ..., int      | Add 2 int (short, byte)            |
     |  LADD  |    ..., long, long -> ..., long    | Add 2 long                         |
     |  FADD  |  ..., float, float -> ..., float   | Add 2 float                        |
     |  DADD  | ..., double, double -> ..., double | Add 2 double                       |
     +--------+------------------------------------+------------------------------------+
     |  ISUB  |     ..., int, int -> ..., int      | Subtract 2 int (short, byte)       |
     |  LSUB  |    ..., long, long -> ..., long    | Subtract 2 long                    |
     |  FSUB  |  ..., float, float -> ..., float   | Subtract 2 float                   |
     |  DSUB  | ..., double, double -> ..., double | Subtract 2 double                  |
     +--------+------------------------------------+------------------------------------+
     |  IMUL  |     ..., int, int -> ..., int      | Multiply 2 int (short, byte)       |
     |  LMUL  |    ..., long, long -> ..., long    | Multiply 2 long                    |
     |  FMUL  |  ..., float, float -> ..., float   | Multiply 2 float                   |
     |  DMUL  | ..., double, double -> ..., double | Multiply 2 double                  |
     +--------+------------------------------------+------------------------------------+
     |  IDIV  |     ..., int, int -> ..., int      | Divide 2 int (short, byte)         |
     |  LDIV  |    ..., long, long -> ..., long    | Divide 2 long                      |
     |  FDIV  |  ..., float, float -> ..., float   | Divide 2 float                     |
     |  DDIV  | ..., double, double -> ..., double | Divide 2 double                    |
     +--------+------------------------------------+------------------------------------+
     |  IREM  |     ..., int, int -> ..., int      | Remainder of 2 int (short, byte)   |
     |  LREM  |    ..., long, long -> ..., long    | Remainder of 2 long                |
     |  FREM  |  ..., float, float -> ..., float   | Remainder of 2 float               |
     |  DREM  | ..., double, double -> ..., double | Remainder of 2 double              |
     +--------+------------------------------------+------------------------------------+

**Notes:**
> Opcodes depends on the variable type. The first letter indicates the type :
> * **`I`** for integer, short, byte, char, boolean
> * **`L`** for long
> * **`F`** for float
> * **`D`** for double

Example :

````ASM
class jhelp.example.Operations

//Return : first + second
method addition
   parameter int first
   parameter int second
   return int
{
   ILOAD first  ; Push first on stack                   : [] => [first(int)] 
   ILOAD second ; Push second on stack                  : [first(int)] => [first(int), second(int)]
   IADD         ; Add 2 number on stack and push result : [first(int), second(int)] => [first+second(int)]
   IRETURN      ; Return last element on stack          : [first+second(int)] => []
}

//Return : first - second
method subtraction
   parameter float first
   parameter float second
   return float
{
   FLOAD first  ; Push first on stack                        : [] => [first(float)]
   FLOAD second ; Push second on stack                       : [first(float)] => [first(float), second(float)]
   FSUB         ; Subtract 2 number on stack and push result : [first(float), second(float)] => [first-second(float)] 
   FRETURN      ; Return last element on stack               : [first-second(float)] => []
}

//Return : first / second
method divide
   parameter double first
   parameter double second
   return double
{
   DLOAD first  ; Push first on stack                      : [] => [first(double)]
   DLOAD second ; Push second on stack                     : [first(double)] => [first(double), second(double)]
   DDIV         ; Divide 2 number on stack and push result : [first(double), second(double)] => [first/second(double)] 
   DRETURN      ; Return last element on stack             : [first/second(double)] => []
}

//Return : first * second
method multiply
   parameter long first
   parameter long second
   return long
{
   LLOAD first  ; Push first on stack                        : [] => [first(long)]
   LLOAD second ; Push second on stack                       : [first(long)] => [first(long), second(long)]
   LMUL         ; Multiply 2 number on stack and push result : [first(long), second(long)] => [first*second(long)] 
   LRETURN      ; Return last element on stack               : [first*second(long)] => []
}


//Return : first % second
method modulate
   parameter int first
   parameter int second
   return int
{
   ILOAD first  ; Push first on stack                        : [] => [first(int)]
   ILOAD second ; Push second on stack                       : [first(int)] => [first(int), second(int)]
   IREM         ; Modulate 2 number on stack and push result : [first(int), second(int)] => [first%second(int)] 
   IRETURN      ; Return last element on stack               : [first%second(int)] => []
}


//Return : -number
method negate
   parameter double number
   return double
{
   DLOAD number ; Push number on stack                       : [] => [number(double)]
   DNEG         ; Negate number on stack and push the result : [number(double)] => [-number(double)]
   DRETURN      ; Return last element on stack               : [-number(double)] = > []
}
````

Opcodes that do binary operations :

     +--------+------------------------------+----------------------------------------+
     | Opcode |         Stack effect         |                Details                 |
     +========+==============================+========================================+
     |  IAND  |  ..., int, int -> ..., int   | Binary AND of 2 int (short, byte)      |
     |  LAND  | ..., long, long -> ..., long | Binary AND of 2 long                   |
     +--------+------------------------------+----------------------------------------+
     |  IOR   |  ..., int, int -> ..., int   | Binary OR of 2 int (short, byte)       |
     |  LOR   | ..., long, long -> ..., long | Binary OR of 2 long                    |
     +--------+------------------------------+----------------------------------------+
     |  IXOR  |  ..., int, int -> ..., int   | Binary XOR of 2 int (short, byte)      |
     |  LXOR  | ..., long, long -> ..., long | Binary XOR of 2 long                   |
     +--------+------------------------------+----------------------------------------+
     |  ISHL  |  ..., int, int -> ..., int   | Shift left int (short, byte)           |
     |  LSHL  | ..., long, int -> ..., long  | Shift left long                        |
     +--------+------------------------------+----------------------------------------+
     |  ISHR  |  ..., int, int -> ..., int   | Shift right int (short, byte)          |
     |  LSHR  | ..., long, int -> ..., long  | Shift right long                       |
     +--------+------------------------------+----------------------------------------+
     | IUSHR  |  ..., int, int -> ..., int   | Unsigned shift right int (short, byte) |
     | LUSHR  | ..., long, int -> ..., long  | Unsigned shift right long              |
     +--------+------------------------------+----------------------------------------+

**Trick:**
> For doing a binary NOT operation, just use **`INEG`** or **`LNEG`**

**`ISHL`** and **`LSHL`** are the Java operation **`<<`**.

**`ISHR`** and **`LSHR`** are the Java operation **`>>`**.

**`IUSHR`** and **`LUSHR`** are the Java operation **`>>>`**.

Example with `-55` and `3`:

     +------------+----------------------------------+-----------+
     | Expression |              Binary              |  Decimal  |
     +============+==================================+===========+
     | PUSH -55   | 11111111111111111111111111001001 |    -55    |
     +------------+----------------------------------+-----------+
     | PUSH 3     | 00000000000000000000000000000011 |     3     |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 11111111111111111111111111111001 |    -7     |
     | ISHR       |                                  |           |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 00011111111111111111111111111001 | 536870905 |
     | IUSHR      |                                  |           |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 11111111111111111111111001001000 |   -440    |
     | ISHL       |                                  |           |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 11111111111111111111111001001000 |   -440    |
     | ISHL       |                                  |           |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 00000000000000000000000000000001 |     1     |
     | IAND       |                                  |           |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 11111111111111111111111111001011 |    -53    |
     | IOR        |                                  |           |
     +------------+----------------------------------+-----------+
     | PUSH -55   |                                  |           |
     | PUSH 3     | 11111111111111111111111111001010 |    -54    |
     | IXOR       |                                  |           |
     +------------+----------------------------------+-----------+

### Conversion opcodes

Those opcodes are for transform a number type to an other.

Converted type may be truncated, by example when convert **double** to **int** we loose decimal part.

     +--------+---------------------------+-------------------------+
     | Opcode |       Stack effect        |         Details         |
     +========+===========================+=========================+
     |  I2B   |   ..., int -> ..., int    | Truncate int to byte    |
     |  I2S   |   ..., int -> ..., int    | Truncate int to short   |
     |  I2C   |   ..., int -> ..., int    | Truncate int to char    |
     +--------+---------------------------+-------------------------+
     |  I2L   |   ..., int -> ..., long   | Convert int to long     |
     |  I2F   |  ..., int -> ..., float   | Convert int to float    |
     |  I2D   |  ..., int -> ..., double  | Convert int to double   |
     +--------+---------------------------+-------------------------+
     |  L2I   |   ..., long -> ..., int   | Convert long to int     |
     |  L2F   |  ..., long -> ..., float  | Convert long to float   |
     |  L2D   | ..., long -> ..., double  | Convert long to double  |
     +--------+---------------------------+-------------------------+
     |  F2I   |  ..., float -> ..., int   | Convert float to int    |
     |  F2L   |  ..., float -> ..., long  | Convert float to long   |
     |  F2D   | ..., float -> ..., double | Convert float to double |
     +--------+---------------------------+-------------------------+
     |  D2I   |  ..., double -> ..., int  | Convert double to int   |
     |  D2L   | ..., double -> ..., long  | Convert double to long  |
     |  D2F   | ..., double -> ..., float | Convert double to float |
     +--------+---------------------------+-------------------------+

### Push constants

To push a constant value on stack use `PUSH` opcode.

````ASM
PUSH <value>
````

Stack effect :

````ASM
... -> ..., <type>
````

The pushed value type depends on expression. We choose a Java like notation.

     +------------------+------------------+-----------------------------------------------+
     |      Value       |       Type       |                    Details                    |
     +==================+==================+===============================================+
     |       null       |      object      | Push null on stack                            |
     +------------------+------------------+-----------------------------------------------+
     |       true       |       int        | Push 1 (true) on stack                        |
     +------------------+------------------+-----------------------------------------------+
     |      false       |       int        | Push 0 (false) on stack                       |
     +------------------+------------------+-----------------------------------------------+
     |       'a'        |       int        | Push unicode value of 'a'                     |
     |                  |                  | Like Java, put the character between '        |
     +------------------+------------------+-----------------------------------------------+
     |       123        |       int        | Push 123 (int)                                |
     +------------------+------------------+-----------------------------------------------+
     |      12.34f      |      float       | Push float 12.34                              |
     |                  |                  | Must add lower case or upper case F           |
     +------------------+------------------+-----------------------------------------------+
     |      1234L       |       long       | Push long 1234                                |
     |                  |                  | Must add lower case or upper case L           |
     +------------------+------------------+-----------------------------------------------+
     |      12.34       |                  | Push 12.34 double                             |
     |      12.34d      |      double      | If double can be confuse with int (like 125), |
     |                  |                  | must add lower case or upper case D           |
     +------------------+------------------+-----------------------------------------------+
     | "Example String" | java.lang.String | Push the string reference                     |
     |                  |                  | Like Java must put between "                  |
     +------------------+------------------+-----------------------------------------------+

### Increment opcode 

To do something like this :

````Java
// ...
int j = 10;
// ...
j += 32;
// ...
j --;
// ...
````

With what we see previously, we will do :

````ASM
// ...
VAR int j ; ...           -> ...
PUSH 10   ; ...           -> ..., int
ISTORE j  ; ..., int      -> ...
// ...
ILOAD j   ; ...           -> ..., int
PUSH 32   ; ..., int      -> ..., int, int
IADD      ; ..., int, int -> ..., int
ISTORE j  ; ..., int      -> ...
// ...
ILOAD j   ; ...           -> ..., int
PUSH 1    ; ..., int      -> ...., int, int
ISUB      ; ..., int, int -> ..., int
ISTORE j  ; ..., int      -> ...
````

But for `int` local variable or parameters to increment or decrement with a constant, 
the opcode **`IINC`** is more efficient.

````ASM
IINC <variableName> <incrementOrDecrement>
```` 

Where :
* **`variableName`** : Name of local variable of method parameters. 
  Not work with fields.
* **`incrementOrDecrement`** : Constant integer between -32768 and 32767

No stack effect.

Our example become :

````ASM
// ...
VAR int j ; ...      -> ...
PUSH 10   ; ...      -> ..., int
ISTORE j  ; ..., int -> ...
// ...
IINC j 32 ; ...      -> ... 
// ...
IINC j -1 ; ...      -> ... 
````

### Jump opcodes

Usually instructions are played from first (top) to last (bottom).
It is possible to change this order by jumping further or backward inside the method.

Each jump instruction have one or several position to jump. 
To mark a position to jump, use the **`LABEL`** opcode.

````ASM
LABEL <labelName>
````

Where:
* **`labelName`** : label name (No space in it) to mark the position where jump.

No stack effect.

#### Always jump

The opcode **`GOTO`** will always jump to corresponding label.

````ASM
GOTO <labelName>
````

Where:
* **`labelName`** : label to jump.

No stack effect.

Example :

````ASM
// ...
ILOAD j      ; ...      -> ..., int
GOTO further ; ..., int -> ..., int

ILOAD p      ; Ignored by the jump
IADD         ; Ignored by the jump

LABEL further 
ISTORE k     ; ..., int -> ...
```` 

#### Compare to `null`

Opcode **`IFNONNULL`** jump if object in stack not null.

Opcode **`IFNULL`** jump if object in stack null.

````ASM
IFNONNULL <labelName>
IFNULL <labelName>
````
Where:
* **`labelName`** : label to jump.

Stack effect :

````ASM
..., object -> ...
````

Where:
* **`object`** : Object instance to compare to null

#### Compare to `0`

Those opcode:
1. Read `int` from method stack
1. Compare this number 
1. Jump or not depends on comparison result

````
     +------------------+---------------------------------------------+
     |      Opcode      |               Jump condition                |
     +------------------+---------------------------------------------+
     | IFEQ <labelName> | Jump if read int is equals 0                |
     | IFNE <labelName> | Jump if read int in not 0                   |
     | IFGE <labelName> | Jump if read int is greater or equals 0     |
     | IFGT <labelName> | Jump if read int is strictly greater than 0 |
     | IFLE <labelName> | Jump if read int is lower o equals 0        |
     | IFLT <labelName> | Jump if read int is strictly lower than 0   |
     +------------------+---------------------------------------------+
````

Where:
* **`labelName`** : label to jump.

````ASM
..., object -> ...
````

Where:
* **`object`** : Object instance to compare to null

Example :

````ASM
//Divide two number
//If second number is 0 then 0 is return, else returns first/second
method divideNoZero
   parameter int first
   parameter int second
   return int
{
   ILOAD second        ; Push second           : [] => [second(int))]
   IFEQ zeroTreatment  ; Compare stack to 0    : [second(int))] => []

                       ; Reach if second not 0 
   ILOAD first         ; Load first            : [] => [first(int)]
   ILOAD second        ; Load second           : [first(int)] => [first(int), second(int)]
   IDIV                ; Divide                : [first(int), second(int)] => [first/second(int)]
   IRETURN             ; Return stack element  : [first/second(int)] => []
   
   LABEL zeroTreatment ; Reach if second is 0
   ICONST 0            ; Push 0                : [] => [0(int)]             
   IRETURN             ; Return stack element  : [0(int)] => []
}
````

#### Compare two objects

Those opcodes jump to label if two objects reference the same or not.
It is reference comparison like `==` in Java or `===` in Kotlin.
The method `equals` is not called.

Opcode **`IF_ACMPEQ`** jump if objects reference same object

Opcode **`IF_ACMPNE`** jump if objects reference different object.

````ASM
IF_ACMPEQ <labelName>
IF_ACMPNE <labelName>
````
Where:
* **`labelName`** : label to jump.

Stack effect :

````ASM
..., object1, object2 -> ...
````

Where :
* **`object1`** and **`object2`** are the reference to compare

#### Compare two `int`

Those opcodes jump to label depends on comparison of two `int` 

Stack effect :

````ASM
..., int1, int2 -> ...
````

Where :
* **`int1`** and **`int2`** are the int to compare

````
     +-----------------------+----------------------+
     |        Opcode         |    Jump condition    |
     +-----------------------+----------------------+
     | IF_ICMPEQ <labelName> | Jump if int1 == int2 |
     | IF_ICMPNE <labelName> | Jump if int1 != int2 |
     | IF_ICMPGE <labelName> | Jump if int1 >= int2 |
     | IF_ICMPGT <labelName> | Jump if int1 > int2  |
     | IF_ICMPLE <labelName> | Jump if int1 <= int2 |
     | IF_ICMPLT <labelName> | Jump if int1 < int2  |
     +-----------------------+----------------------+
````

Where:
* **`labelName`** : label to jump.

Example:

````ASM
//Minium of two number
 method min
  parameter int first
  parameter int second
  return int
{
   ILOAD fisrt             ; Push first                : [] => [first(int)]
   ILOAD second            ; Push second               : [first(int)] => [first(int), second(int)]
   IF_ICMPGT minimumSecond ; Jump if first>second      : [first(int), second(int)] => []
   
                           ; Reach if first <= second
   ILOAD first             ; Push first                : [] => [first(int)]
   IRETURN                 ; Return first              : [first(int)] => []

   LABEL minimumSecond     ; Reach if first > second
   ILOAD second            ; Push second               : [] => [second(int)]
   IRETURN                 ; Return second             : [second(int)] => []
}
````

#### Comparison opcodes

Those opcodes don't do any jump. 

They just compare two double, two float or two long.
And write comparison result as a `int`

Stack effect :

````
..., first, second -> ..., comparison
````

````
     +-----------------+---------------------+
     |    Condition    | int write in statck |
     +-----------------+---------------------+
     | first < second  |         -1          |
     | first == second |          0          |
     | first > second  |          1          |
     +-----------------+---------------------+
````

Opcodes:

````
     +--------+---------------------------------+---------------------------------------+
     | Opcode |          Stack effect           |                Details                |
     +--------+---------------------------------+---------------------------------------+
     | DCMPG  | ..., double, double -> ..., int | If one of number is NaN, 1 is pushed  |
     | DCMPL  | ..., double, double -> ..., int | If one of number is NaN, -1 is pushed |
     | FCMPG  |  ..., float, float -> ..., int  | If one of number is NaN, 1 is pushed  |
     | FCMPL  |  ..., float, float -> ..., int  | If one of number is NaN, -1 is pushed |
     |  LCMP  |   ..., long, long -> ..., int   |                                       |
     +--------+---------------------------------+---------------------------------------+
````

Example:

````ASM
//Minium of two number
 method min
  parameter double first
  parameter double second
  return double
{
   DLOAD fisrt             ; Push first                : [] => [first(double)]
   DLOAD second            ; Push second               : [first(double)] => [first(double), second(double)]
   DCMPG                   ; Compare first, second     : [first(double), second(double)] => [int]
   IFGT minimumSecond      ; Jump if > 0               : [int] => []
   
                           ; Reach if first <= second
   DLOAD first             ; Push first                : [] => [first(double)]
   DRETURN                 ; Return first              : [first(double)] => []

   LABEL minimumSecond     ; Reach if first > second
   DLOAD second            ; Push second               : [] => [second(double)]
   DRETURN                 ; Return second             : [second(double)] => []
}
```` 

#### Switch instruction

Opcode **`SWITCH`** choose the label to jump depends a int value.

````ASM
SWITCH [<key> <label>]* <default>
````

Where:

* **`key`** : Constant int. If the read int is equals to this constant, jump to following **`label`**
* **`label`** : Label to jump if read int equals to ahead **`key`**
* **`default`** : Default label to jump if no **`key`*** match

All keys must have a different value.

**`default`** label is mandatory.

Stack effect:

````
..., int -> ...
````

Where :

* **`int`** : The value to compare to keys

Example:

````ASM
// ......                                   :  [...] => [..., int] 
SWITCH 0 zero 1 one 5 five default ; Switch : [..., int] => [...]
//...
LABEL zero                         ; Reach here if tested int was 0
//...
LABEL one                          ; Reach here if tested int was 1
//...
LABEL five                         ; Reach here if tested int was 5
//...
LABEL default                      ; Reach here if tested int wasn't 0, 1 nor 5
//...
````

#### Subroutines

Its a couple of opcodes : **`JSR`** and **`RET`**

**`JSR`** : Write in stack a return address and jump to given label. 
At this label starts the subroutine

**`RET`** : Exit from subroutine. 
Read return address from local variable and jump just after the **`JSR`** that enter in subroutine.

The address write by **`JSR`** should be store in an `Object` local variable when  subroutine starts.

The local variable should not changed in subroutine, because have to use it for exit subroutine with **`RET`**

````ASM
JSR <label>
// ...
LABEL <label>
VAR Object <addressToReturn>
ASTORE <addressToReturn>
// ....
RET <addressToReturn> 
````

Where :

* **`label`** : Label to jump
* **`addressToReturn`** : Local variable name where address is stored.

Example:

````ASM
class jhelp.example.Printer

import java.io.PrintStream

field_reference System PrintStream out systemOut
 
method print
{
   PUSH "Test 1"       ; Push "Test 1"                        : [] => ["Test 1"(String)]
   JSR subroutinePrint ; Execute subroutine 'subroutinePrint' : ["Test 1"(String)] => []

   PUSH "Test 2"       ; Push "Test 2"                        : [] => ["Test 2"(String)]
   JSR subroutinePrint ; Execute subroutine 'subroutinePrint' : ["Test 2"(String)] => []

   PUSH "Test 3"       ; Push "Test 3"                        : [] => ["Test 3"(String)]
   JSR subroutinePrint ; Execute subroutine 'subroutinePrint' : ["Test 3"(String)] => []
   
   RETURN

   // The subroutine print text on console
   // Effect on stack : [..., String] => [...]
   LABEL subroutinePrint
   VAR Object addressToReturn                
   ASTORE addressToReturn                     ; Save adress                : [..., String, address] => [..., String] 
   GETSTATIC systemOut                        ; get System.out             : [..., String] => [..., String, systemOut(PrintStream)]
   SWAP                                       ; Exchange                   : [..., String, systemOut(PrintStream)] => [..., systemOut(PrintStream), String] 
   INVOKEVIRTUAL PrintStream.println(String)  ; System.out.println(String) : [..., systemOut(PrintStream), String] => [...]
   RET addressToReturn                        ; Return to calling address  : [...] => [...]
}
````

### Call methods

I.6.j)
