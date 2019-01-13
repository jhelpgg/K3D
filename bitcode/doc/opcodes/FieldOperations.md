# Field operations

To manipulate fields, it is necessary they exists.

They can ben own by the current class, see [Declare current class field](../grammar/Fields.md).

Or they can be comes from an other class, see [Reference to other class fields](../grammar/FieldsReference.md)

After their declaration, they can be read/write inside a method.

### GETFIELD 

The opcode `GETFIELD` read a non static field.

````ASM
GETFIELD <name>
````

Where `name` is the field name ([field](../grammar/Fields.md)) or alias ([filed_reference](../grammar/FieldsReference.md))

Stack effect : 

````ASM
..., object -> ..., <value>
````

Where `object` is the object instance where the field is.

And `value` the field value.

Example :

````ASM
class test.Test

import test.util.Example

field_reference Example String fieldNotStatic aliasExample
field int age

method test
   parameter Example example
{
   // ...
   ALOAD this    ; ... -> ..., this
   GETFIELD age  ; ..., this -> ..., age(int)
   // ...
   ALOAD example ; ... -> ..., example
   GETFIELD aliasExample ; ..., example -> ..., value(String)
   // ...
   RETURN
}
````

### GETSTATIC 

The opcode `GETSTATIC` read a static field.

````ASM
GETSTATIC <name>
````

Stack effect : 

````ASM
... -> ..., <value>
````

Since field is static nothing more than the name/alias is necessary.

Example :

````ASM
class test.Test

import java.io.PrintStream

field_reference System PrintStream out sysOut
field int instanceCount static

method test
   parameter Example example
{
   // ...
   GETSTATIC count  ; ... -> ..., count(int)
   // ...
   GETSTATIC sysOut ; ... -> ..., out(PrintStream)
   // ...
   RETURN
}
````

### PUTFIELD

The opcode `PUTFIELD` write in not final, not static fields.

````ASM
PUTFILED name
````

Stack effect :

````ASM
..., object, <value> -> ...
````

Where `object` is the object instance where the field is.

And `value` the value to write.

````ASM
class test.Test

import test.util.Example

field_reference Example String fieldNotStatic aliasExample
field int age

method test
   parameter Example example
{
   // ...
   ALOAD this    ; ... -> ..., this
   PUSH 42       ; ..., this -> ..., this, 42
   PUTFIELD age  ; ..., this, 42 -> ...
   // ...
   ALOAD example ; ... -> ..., example
   PUSH "Hello" ; ..., example -> ..., example, "Hello"
   PUTFIELD aliasExample ; ..., example, "Hello" -> ...
   // ...
   RETURN
}
````

### PUTSTATIC

Opcode `PUTSTATIC` write in static, not final fields.

````ASM
PUTSTATIC anme
````

Stack effect :

````ASM
..., <value> -> ...
````

````ASM
class test.Test

import test.util.Example

field_reference Example String fieldStatic aliasExample
field int count static

method test
   parameter Example example
{
   // ...
   PUSH 73       ; ... -> ..., 73
   PUTSTATIC count  ; ..., 73 -> ...
   // ...
   PUSH "Hello" ; ... -> ..., "Hello"
   PUTSTATIC aliasExample ; ...,  "Hello" -> ...
   // ...
   RETURN
}
````