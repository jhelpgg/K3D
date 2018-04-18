package khelp.thread

import java.util.Optional
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

/**
 * Producer a value
 * @param R Produced value type
 */
interface Producer<R> : Transformer<Unit, R>
{
    /**
     * Produce the value
     * @return Optional that contains the value, may empty if it means something in the current case
     */
    fun produce(): Optional<R>

    /**
     * Transform an object to an other one
     * @param parameter Parameter embed in optional
     * @return Transformation result
     */
    override fun transform(parameter: Optional<Unit>): Optional<R> = this.produce()

    /**
     * Launch the produce in a separate thread
     * @return Future for track the result
     */
    fun parallel() = MainPool.produce(this)

    /**
     * Convert in a transformer
     * @param T Transformer parameter type
     * @return The transformer
     */
    fun <T> toTransformer() =
            object : Transformer<T, R>
            {
                override fun transform(parameter: Optional<T>): Optional<R> = this@Producer.produce()
            }
}

/**
 * Convert this task to a producer
 */
fun <R> (() -> R).producer() =
        object : Producer<R>
        {
            override fun produce(): Optional<R> = Optional.of(this@producer())
        }

/**
 * Convert a task to a producer that play in a given context
 * @param context Context where play the task when produce
 * @param producer Task to play
 * @param R Produced value type
 * @return Created producer
 */
fun <R> producer(context: CoroutineContext = MainPoolContext, producer: suspend () -> R) =
        object : Producer<R>
        {
            override fun produce(): Optional<R>
            {
                val promise = Promise<Optional<R>>()
                producer.startCoroutine(ConsumerContinuation(context, { promise.result(Optional.of(it)) }))
                return promise.future()()
            }
        }
