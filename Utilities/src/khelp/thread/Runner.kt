package khelp.thread

import java.util.Optional
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

/**
 * Task can be played in separate thread
 */
interface Runner : Transformer<Unit, Unit>
{
    /**
     * Execute the task
     */
    fun run()

    /**
     * Transform an object to an other one
     * @param parameter Parameter embed in optional
     * @return Transformation result
     */
    override fun transform(parameter: Optional<Unit>): Optional<Unit> = Optional.of(this.run())

    /**
     * Convert the runner to transformer
     * @param T Transformer parameter type
     * @return The transformer
     */
    fun <T> toTransformer() =
            object : Transformer<T, Unit>
            {
                override fun transform(parameter: Optional<T>): Optional<Unit> = Optional.of(this@Runner.run())
            }
}

/**
 * Transform this [Runnable] to a [Runner]
 */
fun Runnable.runner() =
        object : Runner
        {
            override fun run() = this@runner.run()
        }

/**
 * Transform this function to a [Runner]
 */
fun (() -> Unit).runner() =
        object : Runner
        {
            override fun run() = this@runner()
        }

/**
 * Embed a function inside a [Runner] in a given [CoroutineContext]
 * @param context Contest where play the function
 * @param runner Task to convert
 * @return Created [Runner]
 */
fun runner(context: CoroutineContext = MainPoolContext, runner: suspend () -> Unit) =
        object : Runner
        {
            override fun run()
            {
                val promise = Promise<Unit>()
                runner.startCoroutine(ConsumerContinuation(context, { promise.result(Unit) }))
                promise.future()()
            }
        }