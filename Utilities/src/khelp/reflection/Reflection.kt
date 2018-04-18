package khelp.reflection

import java.lang.reflect.Array
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Stack

/**
 * Default Byte value
 */
const val DEFAULT_BYTE = 0.toByte()
/**
 * Default Character value
 */
const val DEFAULT_CHARACTER = '\u0000'
/**
 * Default Double value
 */
const val DEFAULT_DOUBLE = 0.0
/**
 * Default Float value
 */
const val DEFAULT_FLOAT = 0.0f
/**
 * Default Integer value
 */
const val DEFAULT_INTEGER = 0
/**
 * Default Long value
 */
const val DEFAULT_LONG = 0L
/**
 * Default Short value
 */
const val DEFAULT_SHORT = 0.toShort()

/**
 * Indicates if given class can be considered as void
 *
 * @param clazz Tested class
 * @return `true` if given class can be considered as void
 */
fun canBeConsideredAsVoid(clazz: Class<*>?) = clazz == null || Void.TYPE == clazz || Void::class.java == clazz

/**
 * Compute a default value for a given class
 *
 * @param clazz Class to have a default value
 * @return Default value
 */
fun defaultValue(clazz: Class<*>?): Any?
{
    if (clazz == null || canBeConsideredAsVoid(clazz))
    {
        return null
    }

    if (clazz.isPrimitive)
    {
        if (Boolean::class.javaPrimitiveType == clazz)
        {
            return false
        }

        if (Char::class.javaPrimitiveType == clazz)
        {
            return '\u0000'
        }

        if (Byte::class.javaPrimitiveType == clazz)
        {
            return 0.toByte()
        }

        if (Short::class.javaPrimitiveType == clazz)
        {
            return 0.toShort()
        }

        if (Int::class.javaPrimitiveType == clazz)
        {
            return 0
        }

        if (Long::class.javaPrimitiveType == clazz)
        {
            return 0L
        }

        if (Float::class.javaPrimitiveType == clazz)
        {
            return 0.0f
        }

        if (Double::class.javaPrimitiveType == clazz)
        {
            return 0.0
        }
    }

    if (Boolean::class.java == clazz)
    {
        return java.lang.Boolean.FALSE
    }

    if (Char::class.java == clazz)
    {
        return DEFAULT_CHARACTER
    }

    if (Byte::class.java == clazz)
    {
        return DEFAULT_BYTE
    }

    if (Short::class.java == clazz)
    {
        return DEFAULT_SHORT
    }

    if (Int::class.java == clazz)
    {
        return DEFAULT_INTEGER
    }

    if (Long::class.java == clazz)
    {
        return DEFAULT_LONG
    }

    if (Float::class.java == clazz)
    {
        return DEFAULT_FLOAT
    }

    if (Double::class.java == clazz)
    {
        return DEFAULT_DOUBLE
    }

    if (clazz.isEnum)
    {
        try
        {
            val values = clazz.getMethod("values").invoke(null)

            if (values != null && Array.getLength(values) > 0)
            {
                return Array.get(values, 0)
            }
        }
        catch (ignored: Exception)
        {
        }

    }

    if (CharSequence::class.java.isAssignableFrom(clazz))
    {
        return ""
    }

    if (clazz.isArray)
    {
        var componentType = clazz.componentType
        var count = 1

        while (componentType.isArray)
        {
            componentType = componentType.componentType
            count++
        }

        return Array.newInstance(componentType, *IntArray(count))
    }

    return null
}

/**
 * Invoke a public method from clazz.
 *
 * If method not static, a dummy instance is created and used for invoke the method
 * @param clazz Class where the method is
 * @param methodName Method name
 * @param parameters Method parameters
 * @return Invocation result
 */
@Throws(InvocationTargetException::class, IllegalAccessException::class, NoSuchMethodException::class)
fun invokePublicMethod(clazz: Class<*>, methodName: String, vararg parameters: Any): Any
{
    val types = obtainTypes(*parameters)
    val method = obtainPublicMethod(clazz, methodName, *types)

    return if (method.static())
    {
        method.invoke(null, *parameters)
    }
    else
    {
        method.invoke(newInstance(clazz), *parameters)
    }
}

/**
 * Indicates if a class extends an other one
 *
 * @param test   Class to test
 * @param parent Class to extends
 * @return `true` if class extends an other one
 */
fun isInherit(test: Class<*>, parent: Class<*>): Boolean
{
    var test = test
    if (test == parent)
    {
        return true
    }

    if (test.isPrimitive || test.isArray)
    {
        return false
    }

    val stack = Stack<Class<*>>()
    stack.push(test)

    while (!stack.isEmpty())
    {
        test = stack.pop()

        if (test == parent)
        {
            return true
        }

        if (test.superclass != null)
        {
            stack.push(test.superclass)
        }

        for (interf in test.interfaces)
        {
            stack.push(interf)
        }
    }

    return false
}

/**
 * Indicates if given class is managed by [defaultValue].
 *
 * [defaultValue] will return `null` for void or not managed class
 *
 * @param clazz Class to test
 * @return `true` if given class is managed by [defaultValue].
 */
fun managedByDefault(clazz: Class<*>) =
        (canBeConsideredAsVoid(clazz)
                || clazz.isEnum
                || clazz.isPrimitive
                || clazz.isArray
                || Number::class.java.isAssignableFrom(clazz)
                || CharSequence::class.java.isAssignableFrom(clazz)
                || Char::class.java == clazz
                || Boolean::class.java == clazz)

/**
 * Try create an instance of a class.

 * It first look for "little" public constructor, that is to say a constructor with few parameters, if it founds
 * default
 * constructor without parameters, it use it.

 * If the constructor founds have parameters, it creates a instance of each parameters (on calling itself) an the use
 * them
 * for construct the instance.

 * **BEWARE:**
 *
 *  * In some case, it can falls in infinite loop
 *  * It is not human, so can't read documentation and may use illegal argument for create the instance
 *
 *
 * @param type Class type to create
 * @return Created instance
 */
fun newInstance(type: Class<*>): Any?
{
    if (managedByDefault(type) && !type.isArray)
    {
        return defaultValue(type)
    }

    if (type.isArray)
    {
        var componentType = type.componentType
        var count = 1

        while (componentType.isArray)
        {
            componentType = componentType.componentType
            count++
        }

        val dim = IntArray(count)
        for (i in 0 until count)
        {
            dim[i] = 1
        }

        val o = Array.newInstance(componentType, *dim)
        if (type.isPrimitive)
        {
            return o
        }

        var value = o
        for (i in 1 until count)
        {
            value = Array.get(value, 0)
        }

        Array.set(value, 0, newInstance(componentType))

        return o
    }

    val constructors = type.constructors
    var foundConstructor: Constructor<*>? = null
    var nb = Integer.MAX_VALUE
    var types: kotlin.Array<Class<*>>? = null

    for (constructor in constructors)
    {
        val typ = constructor.parameterTypes

        if (typ == null || typ.size < nb || foundConstructor == null)
        {
            types = typ
            foundConstructor = constructor
            nb = types?.size ?: 0
        }
    }

    if (foundConstructor != null)
    {
        val arguments = arrayOfNulls<Any>(nb)
        for (i in 0 until nb)
        {
            arguments[i] = newInstance(types!![i])
        }

        try
        {
            return foundConstructor.newInstance(*arguments)
        }
        catch (ignored: IllegalArgumentException)
        {
        }
        catch (ignored: InvocationTargetException)
        {
        }
        catch (ignored: IllegalAccessException)
        {
        }
        catch (ignored: InstantiationException)
        {
        }
    }

    return null
}

/**
 * Try create an instance of a class.

 * It first look for "little" public constructor, that is to say a constructor with few parameters, if it founds
 * default
 * constructor without parameters, it use it.

 * If the constructor founds have parameters, it creates a instance of each parameters (on calling itself) an the use
 * them
 * for construct the instance.

 * **BEWARE:**
 *
 *  * In some case, it can falls in infinite loop
 *  * It is not human, so can't read documentation and can use illegal argument for create the instance
 *
 *
 * @param typeName Class name
 * @return Instance created
 * @throws ClassNotFoundException If the class can't be resolve
 */
@Throws(ClassNotFoundException::class)
fun newInstance(typeName: String) = newInstance(Class.forName(typeName))

/**
 * Try create an instance of a class.

 * It first look for "little" public constructor, that is to say a constructor with few parameters, if it founds
 * default
 * constructor without parameters, it use it.

 * If the constructor founds have parameters, it creates a instance of each parameters (on calling itself) an the use
 * them
 * for construct the instance.

 * **BEWARE:**
 *
 *  * In some case, it can falls in infinite loop
 *  * It is not human, so can't read documentation and can use illegal argument for create the instance
 *
 *
 * @param typeName    Type name
 * @param classLoader Class loader to use
 * @return Instance created
 * @throws ClassNotFoundException If the class can't be resolve
 */
@Throws(ClassNotFoundException::class)
fun newInstance(typeName: String, classLoader: ClassLoader) = newInstance(classLoader.loadClass(typeName))

fun newInstanceDangerous(type: Class<*>?): Any?
{
    if (type == null)
    {
        throw NullPointerException("type MUST NOT be null")
    }

    if (managedByDefault(type) && !type.isArray)
    {
        return defaultValue(type)
    }

    if (type.isArray)
    {
        var componentType = type.componentType
        var count = 1

        while (componentType.isArray)
        {
            componentType = componentType.componentType
            count++
        }

        val dim = IntArray(count)
        for (i in 0 until count)
        {
            dim[i] = 1
        }

        val o = Array.newInstance(componentType, *dim)
        if (type.isPrimitive)
        {
            return o
        }

        var value = o
        for (i in 1 until count)
        {
            value = Array.get(value, 0)
        }

        Array.set(value, 0, newInstanceDangerous(componentType))

        return o
    }

    val constructors = type.declaredConstructors
    var foundConstructor: Constructor<*>? = null
    var nb = Integer.MAX_VALUE
    var types: kotlin.Array<Class<*>>? = null

    for (constructor in constructors)
    {
        val typ = constructor.parameterTypes

        if (typ == null || typ.size < nb || foundConstructor == null)
        {
            types = typ
            foundConstructor = constructor
            nb = types?.size ?: 0
        }
    }

    if (foundConstructor != null)
    {
        val arguments = arrayOfNulls<Any>(nb)
        for (i in 0 until nb)
        {
            arguments[i] = newInstanceDangerous(types!![i])
        }

        try
        {
            foundConstructor.isAccessible = true
            return foundConstructor.newInstance(*arguments)
        }
        catch (ignored: IllegalArgumentException)
        {
        }
        catch (ignored: InvocationTargetException)
        {
        }
        catch (ignored: IllegalAccessException)
        {
        }
        catch (ignored: InstantiationException)
        {
        }

    }

    return null
}

/**
 * Obtain an object field value
 * @param instance Object instance
 * @param name Field name
 * @param I Object type
 * @return Field value
 */
fun <I : Any> obtainField(instance: I, name: String): Field
{
    var clazz: Class<*>? = instance.javaClass
    var field: Field? = null

    while (clazz != null)
    {
        for (fieldLook in clazz.declaredFields)
        {
            if (name == fieldLook.name)
            {
                field = fieldLook
                break
            }
        }
        clazz = clazz.superclass
    }

    if (field == null)
    {
        throw IllegalArgumentException(
                "Filed '" + name + "' not found in class " + instance.javaClass.getName())
    }

    field.isAccessible = true
    return field
}

/**
 * Obtain public method from a class
 *
 * @param clazz      Class where method lies
 * @param methodName Method name
 * @param types      Method parameters type
 * @return The method
 * @throws NoSuchMethodException If the method not public or not exists with specified types
 */
@Throws(NoSuchMethodException::class)
fun obtainPublicMethod(clazz: Class<*>, methodName: String, vararg types: Class<*>?): Method
{
    var method: Method? = null
    val publicMethods = clazz.methods
    if (publicMethods != null)
    {
        var i = 0
        while (i < publicMethods.size && method == null)
        {
            val m = publicMethods[i]
            if (m.name == methodName && typeMatch(types as kotlin.Array<Class<*>?>,
                                                  m.parameterTypes as kotlin.Array<Class<*>?>))
            {
                method = m
            }
            i++
        }
    }
    if (method == null)
    {
        val stringBuffer = StringBuilder()
        stringBuffer.append(clazz.name)
        stringBuffer.append('.')
        stringBuffer.append(methodName)
        stringBuffer.append('(')
        if (types.isNotEmpty())
        {
            if (types[0] == null)
            {
                stringBuffer.append("null")
            }
            else
            {
                stringBuffer.append(types[0]?.name)

            }
            for (i in 1 until types.size)
            {
                stringBuffer.append(", ")
                if (types[i] == null)
                {
                    stringBuffer.append("null")
                }
                else
                {
                    stringBuffer.append(types[i]!!.name)

                }
            }
        }
        stringBuffer.append(')')
        throw NoSuchMethodException(stringBuffer.toString())
    }
    return method
}

/**
 * Extract all types of given parameters.

 * If the parameter is `null`, just `null` is return.

 * If some parameters are `null`, the corresponding type was also `null`
 *
 * @param parameters Parameters to extract is types
 * @return Extracted types.
 */
fun obtainTypes(vararg parameters: Any?): kotlin.Array<Class<*>?>
{
    val length = parameters.size
    val types = arrayOfNulls<Class<*>>(length)

    for (i in 0 until length)
    {
        types[i] = parameters[i]?.javaClass
    }

    return types
}

/**
 * Indicates if 2 types are similar.

 * `null` is accepted for parameters
 *
 * @param class1 First type
 * @param class2 Second type
 * @return `true` if types are similar
 */
fun typeMatch(class1: Class<*>?, class2: Class<*>?): Boolean
{
    if (class1 == null)
    {
        return if (class2 == null)
        {
            true
        }
        else !class2.isPrimitive
    }

    if (class2 == null)
    {
        return !class1.isPrimitive
    }

    if (class1 == class2)
    {
        return true
    }

    if (class1.name == class2.name)
    {
        return true
    }

    if (class1.isPrimitive)
    {
        if (Boolean::class.javaPrimitiveType == class1)
        {
            return class2.name == Boolean::class.java.name
        }
        if (Byte::class.javaPrimitiveType == class1)
        {
            return class2.name == Byte::class.java.name
        }
        if (Short::class.javaPrimitiveType == class1)
        {
            return class2.name == Short::class.java.name
        }
        if (Int::class.javaPrimitiveType == class1)
        {
            return class2.name == Int::class.java.name
        }
        if (Float::class.javaPrimitiveType == class1)
        {
            return class2.name == Float::class.java.name
        }
        if (Long::class.javaPrimitiveType == class1)
        {
            return class2.name == Long::class.java.name
        }
        if (Double::class.javaPrimitiveType == class1)
        {
            return class2.name == Double::class.java.name
        }
        if (Char::class.javaPrimitiveType == class1)
        {
            return class2.name == Char::class.java.name
        }
    }

    if (class2.isPrimitive)
    {
        if (Boolean::class.javaPrimitiveType == class2)
        {
            return class1.name == Boolean::class.java.name
        }
        if (Byte::class.javaPrimitiveType == class2)
        {
            return class1.name == Byte::class.java.name
        }
        if (Short::class.javaPrimitiveType == class2)
        {
            return class1.name == Short::class.java.name
        }
        if (Int::class.javaPrimitiveType == class2)
        {
            return class1.name == Int::class.java.name
        }
        if (Float::class.javaPrimitiveType == class2)
        {
            return class1.name == Float::class.java.name
        }
        if (Long::class.javaPrimitiveType == class2)
        {
            return class1.name == Long::class.java.name
        }
        if (Double::class.javaPrimitiveType == class2)
        {
            return class1.name == Double::class.java.name
        }
        if (Char::class.javaPrimitiveType == class2)
        {
            return class1.name == Char::class.java.name
        }
    }

    return if (class1.isArray && class2.isArray)
    {
        typeMatch(class1.componentType, class2.componentType)
    }
    else false
}

/**
 * Indicates if 2 arrays of types are similar.

 * Array says similar if they have same length and each type of arrays are one to one similar

 * `null` are consider like zero length array

 * Each array can contains `null` elements.

 * For the compare 2 types, it use [.typeMatch]
 *
 * @param types1 First array
 * @param types2 Second array
 * @return `true` if arrays are similar
 */
fun typeMatch(types1: kotlin.Array<Class<*>?>?, types2: kotlin.Array<Class<*>?>?): Boolean
{
    if (types1 == null)
    {
        return types2 == null || types2.isEmpty()
    }

    if (types2 == null)
    {
        return types1.isEmpty()
    }

    if (types1.size != types2.size)
    {
        return false
    }

    for (i in types1.indices)
    {
        if (!typeMatch(types1[i], types2[i]))
        {
            return false
        }
    }

    return true
}

/**
 * Indicates if this method is static
 */
fun Method.static() = Modifier.isStatic(this.modifiers)

/**
 * Indicates if this field is static
 */
fun Field.static() = Modifier.isStatic(this.modifiers)
