# Constructors

If no constructor is specified, the compiler will add a default public minimal constructor.

Constructor are [method](Methods.md) named **`<init>`**

By nature, they are never `static` nor `final`.

Constructor must call a parent class constructor.

Example :

````ASM
class test.Person

field String name
field int age

method <init>
   parameter String name
   parameter int age
{                                 
   ALOAD this                    ; []                 -> this
   DUP                           ; this               -> this, this
   DUP                           ; this, this         -> this, this, this
   INVOKESPECIAL Object.<init>() ; this, this, this   -> this, this
   ALOAD name                    ; this, this         -> this, this, String
   PUTFIELD name                 ; this, this, String -> this
   ILOAD age                     ; this               -> this, int
   PUTFIELD age                  ; this, int          -> []
   RETURN                        ; []                 -> [] EXIT
}
````

[Menu](../Menu.md#menu)