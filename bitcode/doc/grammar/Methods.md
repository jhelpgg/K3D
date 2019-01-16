# Methods

### Method declaration

To declare a method:

````ASM
method <methodName> <modifier>*
````

The method name is mandatory.

Modifiers are here to change to method default visibility.
By default, method is:
* public
* final (If the method not abstract)
* not static

Their two special method's name :
* **`<clinit>`** : [The static initializer](StaticInitializer.md)
* **`<init>`** : [Class constructors](Constructors.md) 

````
     +-----------+----------------------------------------------------------+
     | Modifier  |                          Effect                          |
     +===========+==========================================================+
     |  public   | Can be invoke by every one                               |
     | protected | Can be invoke by class of same package or class innerits |
     |  pacakge  | Can be invoke only by class of same package              |
     |  private  | Can be invoke only by the current class                  |
     +-----------+----------------------------------------------------------+
     |   final   | Can't be override                                        |
     |   open    | Can be override                                          |
     +-----------+----------------------------------------------------------+
     |  static   | Static method                                            |
     +-----------+----------------------------------------------------------+
````

Specifies `public` or `final` can be considered useless (since its the default status), 
but it is not an error, so you can be explicit.

Since, by definition, `abstract` can't ne final (since they need to be overridden),
`final` status is not set.

To remove the default `final` status, use the `open` modifier. 
(Not necessary for `abstract` method)

### Method parameters

After the method declaration, comes the method parameters. (If the method have some).
Parameters are specified with `parameter` instruction. One per line.

````ASM
parameter <type> <name>
````

Where:
* **type** : Parameter type. Can be: 
  * A primitive : **boolean**, **int**, ...
  * A class : **String**, **Runnable**, ...
  * An array : **byte[]**, **String[][]**, ...
* **name** : The parameter's name

### Method return value

If method return something (Not a void), 
specifies the returned type with `return` instruction :

````ASM
return <type>
````

Where:
* **type** : Return type. Can be: 
  * A primitive : **boolean**, **int**, ...
  * A class : **String**, **Runnable**, ...
  * An array : **byte[]**, **String[][]**, ...

### Method throw exception

If the method may throw one or more exception, specifies thew with `throws` instruction

One exception per line

````ASM
throws <exceptionType>
````

### Method body

If the method is **abstract**, just use the `abstract` keyword.

If the method have concrete code, use `{` and `}` instructions. 
`{` Start the method. `}` End the method. 
They are instruction, so they have to be alone in their lines.

### Abstract rule

Method and **abstract** body capacity depends if the current file represents: 
* A concrete class, declared with `class`
* An abstract class, declared with `abstract`
* An interface, declared with `interface`

````
     +-------------+---------------------------------------------+
     | Declaration |            Abstract or concrete             |
     +=============+=============================================+
     |    class    | All methods have a body. No abstract method |
     +-------------+---------------------------------------------+
     |  abstract   | Method can have a body or be abstract       |
     +-------------+---------------------------------------------+
     |  interface  | All methods are abstract. No body method    |
     +-------------+---------------------------------------------+
````

### Body constraint

A method with a body, must always contains a return instruction it it.
Even if  the methods returns nothing.

Forget to exit method without a return opcode will do a execution crash.

### Declaration examples

````ASM
method add
   parameter int first
   parameter int second
   return int
{
   // Compute addition ...
   IRETURN
}
````

````ASM
method readStream 
   parameter InputStream stream
   throws IOException
abstract  
````

````ASM
method decorate open
   parameter String text
   return String
{
	//Do decoration ...
    ARETURN
} 
````

````ASM
method getInstance static
   return MySingleton
{
	//Initilialize singleton ...
	ARETURN
}
````

````ASM
method internalComputing private
{
    // Do the compunting ...
    RETURN
}
````
