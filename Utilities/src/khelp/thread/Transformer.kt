package khelp.thread

import khelp.debug.exception
import khelp.util.startCoroutine
import java.util.Optional
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

/**
 * Function that transform an object to an other one
 * @param P Function parameter type
 * @param R Function result type
 */
interface Transformer<P, R>
{
    /**
     * Transform an object to an other one
     * @param parameter Parameter embed in optional
     * @return Transformation result
     */
    fun transform(parameter: Optional<P>): Optional<R>

    /**
     * Called when error happen
     *
     * By default just log the issue
     * @param taskException Exception happened
     */
    fun error(taskException: TaskException)
    {
        exception(taskException, "Failed to make the transformation")
    }

    /**
     * Called when result computed
     *
     * By default does nothing
     * @param result Function result embed i optional
     */
    fun result(result: Optional<R>)
    {
    }

    /**
     * Launch the transformation in a background thread
     * @param parameter Option with the parameter to give to the transformation
     */
    fun parallel(parameter: Optional<P>) = MainPool.transform(this, parameter)

    /**
     * Launch the transformation in given context
     * @param context Context where play transformation
     * @param parameter Option with the parameter to give to the transformation
     */
    fun parallel(context: CoroutineContext, parameter: Optional<P>)
    {
        { param: P ->
            try
            {
                this.result(this.transform(Optional.of(param)))
            }
            catch (throwable: Throwable)
            {
                this.error(TaskException("Failed to launch task!", throwable))
            }
        }.startCoroutine(parameter.get(), context)
    }
}

/**
 * Transform the function to a transformation
 * @param P Transformation parameter type
 * @param R Transformation result type
 * @return Transformation result
 */
fun <P, R> ((P) -> R).transformer() =
        object : Transformer<P, R>
        {
            /**
             * Transform an object to an other one
             * @param parameter Parameter embed in optional
             * @return Transformation result
             */
            override fun transform(parameter: Optional<P>): Optional<R> =
                    if (parameter.isPresent) Optional.of(this@transformer(parameter.get()))
                    else Optional.empty()
        }
/**
 * Transform a function to a transformation
 * @param context Context where play the function when result transformation is used
 * @param transformer Function to play
 * @param P Transformation parameter type
 * @param R Transformation result type
 * @return Transformation result
 */
fun <P, R> transformer(context: CoroutineContext = MainPoolContext, transformer: suspend (P) -> R) =
        object : Transformer<P, R>
        {
            /**
             * Transform an object to an other one
             * @param parameter Parameter embed in optional
             * @return Transformation result
             */
            override fun transform(parameter: Optional<P>): Optional<R>
            {
                if (!parameter.isPresent)
                {
                    return Optional.empty()
                }

                val argument = parameter.get()
                val promise = Promise<Optional<R>>()
                transformer.startCoroutine(argument, ConsumerContinuation(context, { promise.result(Optional.of(it)) }))

                return promise.future()()
            }
        }