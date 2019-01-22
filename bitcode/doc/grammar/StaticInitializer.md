# Static initializer

It mus have no more than one static initializer.

Static initializer is a [method](Methods.md) called the first time class is load by JVM.

It is often used for initialized static [fields](Fields.md).

The method name is the reserved string : **`<clinit>`**

By nature its always `public static`.

Example :

````ASM
class test.MiniMath

field double PI public static final

method <clinit>
{
   PUSH 3.14    ; [] -> double
   PUTSTATIC PI ; double -> []
   RETURN       ; [] -> []
}
````

[Menu](../Menu.md#menu)