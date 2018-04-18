package khelp.thread

import khelp.reflection.canBeConsideredAsVoid
import khelp.util.async
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Proxy handler for call methods in given context
 * @param instance Instance used for invoke methods
 * @param context Context where call the methods
 * @param I Interface mapped by the proxy
 */
internal class ContextualCallerHandler<I>(private val instance: I,
                                          private val context: CoroutineContext) : InvocationHandler
{
    /**
     * Processes a method invocation on a proxy instance and returns
     * the result.  This method will be invoked on an invocation handler
     * when a method is invoked on a proxy instance that it is
     * associated with.
     *
     * @param   proxy the proxy instance that the method was invoked on
     *
     * @param   method the `Method` instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the `Method` object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param   args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or `null` if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * `java.lang.Integer` or `java.lang.Boolean`.
     *
     * @return  the value to return from the method invocation on the
     * proxy instance.  If the declared return type of the interface
     * method is a primitive type, then the value returned by
     * this method must be an instance of the corresponding primitive
     * wrapper class; otherwise, it must be a type assignable to the
     * declared return type.  If the value returned by this method is
     * `null` and the interface method's return type is
     * primitive, then a `NullPointerException` will be
     * thrown by the method invocation on the proxy instance.  If the
     * value returned by this method is otherwise not compatible with
     * the interface method's declared return type as described above,
     * a `ClassCastException` will be thrown by the method
     * invocation on the proxy instance.
     *
     * @throws  Throwable the exception to throw from the method
     * invocation on the proxy instance.  The exception's type must be
     * assignable either to any of the exception types declared in the
     * `throws` clause of the interface method or to the
     * unchecked exception types `java.lang.RuntimeException`
     * or `java.lang.Error`.  If a checked exception is
     * thrown by this method that is not assignable to any of the
     * exception types declared in the `throws` clause of
     * the interface method, then an
     * [UndeclaredThrowableException] containing the
     * exception that was thrown by this method will be thrown by the
     * method invocation on the proxy instance.
     *
     * @see UndeclaredThrowableException
     */
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?) =
            async<Any?>(this.context)({
                                          val result = if (args == null)
                                          {
                                              method!!.invoke(this.instance)
                                          }
                                          else
                                          {
                                              method!!.invoke(this.instance, *args)
                                          }

                                          if (canBeConsideredAsVoid(method.returnType))
                                          {
                                              Unit
                                          }
                                          else
                                          {
                                              result
                                          }
                                      })()
}

/**
 * Create instance of given interface where methods a called in given context.
 * It uses the given instance to compute the method result.
 *
 * Warning it may have issue with methods that returns **`null`**
 * @param interf Interface to map
 * @param instance Instance used to compute method result (Its method are called in given context)
 * @param context Context where call instance methods
 * @param I Interface to map type
 * @return Proxy to use for call method in given context
 */
fun <I> contextualCaller(interf: Class<I>, instance: I, context: CoroutineContext = MainPoolContext): I
{
    if (!interf.isInterface)
    {
        throw IllegalArgumentException("interf must represents an interface")
    }

    return Proxy.newProxyInstance(interf.classLoader, arrayOf(interf), ContextualCallerHandler(instance, context)) as I
}