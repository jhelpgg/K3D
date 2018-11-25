package khelp.neural

import khelp.debug.debug
import khelp.debug.mark
import khelp.util.BLACK_ALPHA_MASK
import khelp.util.BLUE
import khelp.util.DARK_GRAY
import khelp.util.DARK_RED
import khelp.util.GREEN
import khelp.util.LIGHT_BLUE
import khelp.util.LIGHT_GREEN
import khelp.util.RED
import khelp.util.WHITE

fun main(args: Array<String>)
{
    //R,G,B -> Light
    val neuralNetwork = NeuralNetwork(3, 1)
    val colors = arrayOf(RED, DARK_GRAY, GREEN, LIGHT_BLUE, BLUE, LIGHT_GREEN, DARK_RED, BLACK_ALPHA_MASK, WHITE)
    val expected = arrayOf(0.5, 0.25, 0.5, 0.75, 0.5, 0.75, 0.25, 0.0, 1.0)

    // Test like that
    mark("Before learning")
    (0 until colors.size).forEach { index ->
        val color = colors[index]
        neuralNetwork.input[0] = ((color shr 16) and 8) / 255.0
        neuralNetwork.input[1] = ((color shr 8) and 8) / 255.0
        neuralNetwork.input[2] = (color and 8) / 255.0
        neuralNetwork.compute()
        debug("initial error: ", expected[index] - neuralNetwork.output[0])
    }

    //Learn
    val inputs = Array<DoubleArray>(colors.size,
                                    { index ->
                                        val color = colors[index]
                                        doubleArrayOf(((color shr 16) and 8) / 255.0,
                                                      ((color shr 8) and 8) / 255.0,
                                                      (color and 8) / 255.0)
                                    })

    val expects = Array<DoubleArray>(expected.size, { doubleArrayOf(expected[it]) })
    neuralNetwork.learn(inputs, expects)

    mark("After learning")
    (0 until colors.size).forEach { index ->
        val color = colors[index]
        neuralNetwork.input[0] = ((color shr 16) and 8) / 255.0
        neuralNetwork.input[1] = ((color shr 8) and 8) / 255.0
        neuralNetwork.input[2] = (color and 8) / 255.0
        neuralNetwork.compute()
        debug("final error: ", expected[index] - neuralNetwork.output[0])
    }
}