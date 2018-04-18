package khelp.thread

import java.util.Optional
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

/**
 * Consumer task
 * @param P Consumed element type
 */
interface Consumer<P> : Transformer<P, Unit>
{
    /**
     * Consume an element
     * @param parameter Contains the element to consume
     */
    fun consume(parameter: Optional<P>)

    /**
     * Transform an object to an other one
     * @param parameter Parameter embed in optional
     * @return Transformation result
     */
    override fun transform(parameter: Optional<P>): Optional<Unit> = Optional.of(this.consume(parameter))
}

/**
 * Transform this task to consumer
 */
fun <P> ((P) -> Unit).consumer() =
        object : Consumer<P>
        {
            override fun consume(parameter: Optional<P>) = parameter.ifPresent { this@consumer(it) }
        }

/**
 * Transform a task to consumer played in given context
 * @param context Context where play the task
 * @param consumer Task to play
 * @param P Consumed element type
 * @return Consumer created
 */
fun <P> consumer(context: CoroutineContext = MainPoolContext, consumer: suspend (P) -> Unit) =
        object : Consumer<P>
        {
            override fun consume(parameter: Optional<P>)
            {
                if (!parameter.isPresent)
                {
                    return
                }

                val argument = parameter.get()
                val promise = Promise<Unit>(context)
                consumer.startCoroutine(argument, ConsumerContinuation(context, { promise.result(Unit) }))

                return promise.future()()
            }
        }
