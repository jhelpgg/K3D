# Field reference

Filed reference describe a field from external class to use it later.
````
field_reference <Class> <Type> <Name> <Alias>
````

Example :
````ASM
import java.io.PrintStream
field_reference System PrintStream out sysout

// ...
GETSTATIC  sysout
// ...
````

Where :

     +-----------+------------------------------------------------+-----------------------------------------------------------------+
     | Parameter |                  Description                   |                           In example                            |
     +-----------+------------------------------------------------+-----------------------------------------------------------------+
     |   Class   | Class where the field is                       | java.lang.System : We get the field "out" from the System class |
     |   Type    | The field type                                 | java.io.PrintStream : The field "out" is type PrintStream       |
     |   Name    | The field name in the class                    | out : The field name in System                                  |
     |   Alias   | Alias used for reference in method instruction | sysout : Alias used in code                                     |
     +-----------+------------------------------------------------+-----------------------------------------------------------------+

To get value from the field, in the example we used `GETSTATIC` opcode instruction, because the field is static in `java.lnag.System` class.

To get a not static `GETFIELD` is used.

To change the value of a field, opcodes instruction : `PUTSTATIC` and `PUTFIELD`

They are also used for get internal fields. 

See [Fields operations](../opcodes/FieldOperations.md) for more details.

[Menu](../Menu.md#menu)