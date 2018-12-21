#  Introduction

The language proposed here is design for JVM. 
It is the bytecode compiled from Java or Kotlin files.

Note:
> We consider in this documentation that the reader have already develop application with an oriented object language like C++, Java, Kotlin, ... 
> 
> So we suppose reader knows what is class, interface, object and inheritance.

The instructions used inside methods codes and the method stack are exactly the bytecode behaviors.

We have simplified the constant pool management to by managing it automatically at compilation time.

We had some key words to be able easily manage:
* Labels and jump instructions
* Try/catch blocks
* Blocks

We also add :
* Import management to not have type the complete class name each time.
* The filed declaration 
* The external field reference
* Capacity to manage method signature in "more natural ways" for a Java or Kotlin developer. 
  We convert them to JNI like signature for the developer. Note: it is still possible to use the JNI syntax.

The format may look strict, but it is for the best. 
As example of rule : Only one interface or class per file. No lambda, internal class or anonymous ones.
It is a JVM constraints. Language that accept them, extract the code inside a separate file at compilation. 
See the files when compile an you will see some files with '$' inside witch are extracted classes.

Before understand completely the famous "Hello world" program, you may need to read more, it is normal.
We are deep level, some notion that not exists in high level languages are necessary. 
Don't panic, all will be explain in this documentation.

Just for teasing, the "Hello world" program :

````ASM
// Declare the class with full name
class khelp.asm.HelloWorld

import java.io.PrintStream

// Reference to the "out" field of "System" class 
field_reference System PrintStream out systemOut

// Create main method
method main static
   parameter String[] args
{
    // Push the reference of "out" field of "System" in stack
    GETSTATIC systemOut                                       ; [] -> [PrintStream]
    // Push the text on the stack
    PUSH "Hello World!"                                       ; [PrintStream] -> [PrintStream, String]
    // Call the method "println" on "out"
    INVOKEVIRTUAL PrintStream.println(String)                 ; [PrintStream, String] -> []
    // Return is mandatory even for void methods
    RETURN                                                    ; [] -> []EXIT
}
````

For comments we use the java comments : `//` and `/* ... */` and we add  the `;` like assembler that ignore all things after it.

Other important rule, there one and only one instruction per line. 
The `{` and `}` are instructions by them-selves. `{` means begin of method code and `}` means end of method code

In comment after each instruction, we show the influence of the instruction on method stack.
Before the arrow `->` the state before and after the state after. 
As we will see, know the stack state is very important, so we recommend to write it, specially for beginners. 

We hope our comments are enough to understand what is going on. 
The details will come at their time.

For compile the code in bytecode format, use [Compiler](../src/khelp/bitcode/compiler/Compiler.kt)

Example usage in [HelloWorldInFile](../sample/khelp/samples/bitcode/HelloWorldInFile.kt)

Extract: 

````Kotlin
val compiler = Compiler()
val className = compiler.compile(input, output)
````

Just create an instance, give it the source stream and the where to write stream.

It returns the generated class complete name.
In the sample, the `bytecode` file is generte inside a `khelp` repository. 
 
To use it in a project, just link the path where `khelp` directory is. 
 
In Intellij : Open module Settings => Dependencies => Add jar or directory : Then choose the directory where the `khelp` directory lies.

In terminal got to in the directory where `khelp` lies and type :
````SH
java khelp.asm.HelloWorld
````
Like any classic `class` file
