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