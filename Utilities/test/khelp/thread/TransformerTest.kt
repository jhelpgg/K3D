package khelp.thread

import org.junit.Assert
import org.junit.Test
import java.util.Optional

class TransformerTest
{
    class MyTransformer : Transformer<String, Int>
    {
        var result: Optional<Int> = Optional.empty()
        var taskException: Optional<TaskException> = Optional.empty()

        fun initialize()
        {
            this.result = Optional.empty()
            this.taskException = Optional.empty()
        }

        /**
         * Transform an object to an other one
         * @param parameter Parameter embed in optional
         * @return Transformation result
         */
        override fun transform(parameter: Optional<String>): Optional<Int>
        {
            return Optional.of(parameter.get().toInt())
        }

        /**
         * Called when error happen
         *
         * By default just log the issue
         * @param taskException Exception happened
         */
        override fun error(taskException: TaskException)
        {
            this.taskException = Optional.of(taskException)
        }

        /**
         * Called when result computed
         *
         * By default does nothing
         * @param result Function result embed i optional
         */
        override fun result(result: Optional<Int>)
        {
            this.result = result
        }
    }

    @Test
    fun testSucceed()
    {
        val myTransformer = MyTransformer()
        MainPool.transform(myTransformer, Optional.of("42")).waitFinish()
        Assert.assertFalse(myTransformer.taskException.isPresent)
        Assert.assertTrue(myTransformer.result.isPresent)
        Assert.assertEquals(42, myTransformer.result.get())
    }

    @Test
    fun testFailed()
    {
        val myTransformer = MyTransformer()
        MainPool.transform(myTransformer, Optional.of("The answer")).waitFinish()
        Assert.assertTrue(myTransformer.taskException.isPresent)
        Assert.assertFalse(myTransformer.result.isPresent)
    }

    @Test
    fun testParallel()
    {
        val myTransformer = MyTransformer()
        myTransformer.parallel(Optional.of("73")).waitFinish()
        Assert.assertFalse(myTransformer.taskException.isPresent)
        Assert.assertTrue(myTransformer.result.isPresent)
        Assert.assertEquals(73, myTransformer.result.get())

        val simpleContext = SimpleContext()
        myTransformer.parallel(simpleContext, Optional.of("666"))
        Thread.sleep(128)
        Assert.assertFalse(myTransformer.taskException.isPresent)
        Assert.assertTrue(myTransformer.result.isPresent)
        Assert.assertEquals(666, myTransformer.result.get())
    }

    @Test
    fun transformerTest()
    {
        val transformer = { string: String -> string.toUpperCase() }.transformer()
        val result = MainPool.transform(transformer, Optional.of("Some String"))()
        Assert.assertEquals("SOME STRING", result)

        val simpleContext = SimpleContext()
        val transformer2 = transformer(simpleContext, { string: String -> string.toLowerCase() })
        val result2 = MainPool.transform(transformer2, Optional.of("Some String"))()
        Assert.assertEquals("some string", result2)
    }
}