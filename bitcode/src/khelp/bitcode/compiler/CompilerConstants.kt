package khelp.bitcode.compiler

import com.sun.org.apache.bcel.internal.Constants
import kotlin.experimental.or

/** Flags applied to the class  */
val ACCES_FLAGS_CLASS = Constants.ACC_PUBLIC or Constants.ACC_SUPER
/** Check if public, private or protected are already specified  */
val ACCES_FLAGS_CONTROL = Constants.ACC_PUBLIC.toInt() or Constants.ACC_PRIVATE.toInt() or Constants.ACC_PROTECTED.toInt()
/** Flags applied to class fields  */
val ACCES_FLAGS_FIELD = Constants.ACC_PRIVATE.toInt()
/** Flags applied to class methods  */
val ACCES_FLAGS_METHOD = Constants.ACC_PUBLIC.toInt() or Constants.ACC_FINAL.toInt()
val ACCES_FLAGS_STATIC_FINAL = Constants.ACC_STATIC.toInt() or Constants.ACC_FINAL.toInt()

// *********************
// *** ASM key words ***
// *********************

/**
 * Declare the class in ASM files:<br></br>
 * `class <ClassCompleteName>`
 */
val CLASS = "class"
/** Close the code for a method (End method declaration)  */
val CLOSE_BLOCK = "}"
/**
 * Specifies the parent class:<br></br>
 * `extends <ClassName>`
 */
val EXTENDS = "extends"
/**
 * Declare a field:<br></br>
 * `field <type> <name>`
 */
val FIELD = "field"
/**
 * Declare a reference to an external field:<br></br>
 * `field_reference <className> <type> <name> <alias>`
 */
val FIELD_REFERENCE = "field_reference"
/**
 * Add interface to implements:<br></br>
 * `implements <ClassName>`
 */
val IMPLEMENTS = "implements"
/**
 * Add an import:<br></br>
 * `import <ClassCompleteName>`
 */
val IMPORT = "import"
/**
 * Start method declaration:<br></br>
 * `method <name>`
 */
val METHOD = "method"
/** Start the method code  */
val OPEN_BLOCK = "{"
/** For make a method or field package access  */
val PACKAGE = "package"
/**
 * Add method parameter:<br></br>
 * `parameter <type> <name>`
 */
val PARAMETER = "parameter"
/** Make method or filed private access  */
val PRIVATE = "private"
/** Make method or filed protected access  */
val PROTECTED = "protected"
/** Make method or filed public access  */
val PUBLIC = "public"
/**
 * Declare method return type:<br></br>
 * `return <type>`
 */
val RETURN_TYPE = "return"
/** Make method or filed static  */
val STATIC = "static"
/** Make method or filed final  */
val FINAL = "final"
/** Remove method or filed final  */
val OPEN = "open"

/**
 * Add exception throws by method:<br></br>
 * `throws <ClassName>`
 */
val THROWS = "throws"