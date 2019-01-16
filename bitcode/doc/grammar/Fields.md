# Fields

To declare field in class :
````
field <Type> <Name> <Modifier>*
````

A filed have no values after declaration.
No value means it can't be read before a method write in it.

For final static fields, they must be initialize inside the [static initializer](StaticInitializer.md)

For final non static fields, they must be initialize in each [constructor](Constructors.md) 

Where:
* **Type:** The field type.
* **Name:** The field name.
* **Modifier:** List of modifier in : `public, protected, package, private, static, final, open`

Examples:
````ASM
field int age
field String description
field String nature public static
````

By default (if no modifier specified) fields are :
* Private
* Not static
* Not final

Modifiers can change this behavior. 

In examples the last field *"nature"* becomes:
* Public
* Static
* Not final (Since none of specified modifier change this status)

Modifiers that exclude together :
* public, protected, package, private
* final, open
* static

To get/set a field value, use the opcode (depend if field is static or not):

           +------------+-----------+
           | Not Static |  Static   |
     +=====+============+===========+
     | Get |  GETFIELD  | GETSTATIC |
     +-----+------------+-----------+
     | Set |  PUTFIELD  | PUTSTATIC |
     +-----+------------+-----------+

See [Fields operations](../opcodes/FieldOperations.md) for more details.
