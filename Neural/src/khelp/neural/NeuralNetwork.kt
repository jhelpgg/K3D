package khelp.neural

import khelp.debug.debug
import khelp.debug.todo
import khelp.math.minimum
import khelp.math.random
import khelp.math.sigmoid
import khelp.math.square
import khelp.util.scramble
import kotlin.math.min

const val factorInputLayer1 = 1.0
const val factorLayer1Layer2 = 1.0
const val factorLayer2Output = 1.0

class NeuralNetwork(val inputSize: Int, val outputSize: Int)
{
    private val layerSize = this.inputSize + this.outputSize
    val input = DoubleArray(this.inputSize)
    private val inputLayer1Weights = DoubleArray(this.inputSize * this.layerSize, { random(1.0) })
    private val inputLayer1Bias = DoubleArray(this.layerSize, { random(1.0) })
    private val layer1 = DoubleArray(this.layerSize)
    private val layer1layer2Weights = DoubleArray(this.layerSize * this.layerSize, { random(1.0) })
    private val layer1layer2Bias = DoubleArray(this.layerSize, { random(1.0) })
    private val layer2 = DoubleArray(this.layerSize)
    private val layer2OutputWeights = DoubleArray(this.layerSize * this.outputSize, { random(1.0) })
    private val layer2OutputBias = DoubleArray(this.outputSize, { random(1.0) })
    val output = DoubleArray(this.outputSize)

    fun compute()
    {
        var index = 0
        var sum: Double

        for (layer1 in 0 until this.layerSize)
        {
            sum = 0.0

            for (input in 0 until this.inputSize)
            {
                sum += this.input[input] * this.inputLayer1Weights[index]
                index++
            }

            this.layer1[layer1] = sigmoid(sum + this.inputLayer1Bias[layer1])
        }

        index = 0

        for (layer2 in 0 until this.layerSize)
        {
            sum = 0.0

            for (layer1 in 0 until this.layerSize)
            {
                sum += this.layer1[layer1] * this.layer1layer2Weights[index]
                index++
            }

            this.layer2[layer2] = sigmoid(sum + this.layer1layer2Bias[layer2])
        }

        index = 0

        for (output in 0 until this.outputSize)
        {
            sum = 0.0

            for (layer2 in 0 until this.layerSize)
            {
                sum += this.layer2[layer2] * this.layer2OutputWeights[index]
                index++
            }

            this.output[output] = sigmoid(sum + this.layer2OutputBias[output])
        }
    }

    fun cost(inputs: Array<DoubleArray>, expects: Array<DoubleArray>, limit: Int): Double
    {
        val n = minimum(inputs.size, expects.size, limit)
        val computed = Array<DoubleArray>(n,
                                          { index ->
                                              System.arraycopy(inputs[index], 0, this.input, 0, this.inputSize)
                                              this.compute()
                                              DoubleArray(this.outputSize, { this.output[it] })
                                          })

        return ((0 until n).sumByDouble { index ->
            val computedVector = computed[index]
            val expectedVector = expects[index]
            (0 until this.outputSize).sumByDouble { square(computedVector[it] - expectedVector[it]) }
        }) / (2.0 * n)
    }

    fun stochasticGradientDescent(inputs: Array<DoubleArray>, expects: Array<DoubleArray>, bachSize: Int,
                                  learningRate: Double)
    {
        val n = inputs.size
        scramble(inputs, expects)
        var index = 0

        while (index < n)
        {
            this.updateBatch(inputs, expects, index, min(n, index + bachSize), learningRate)
            index += bachSize
        }
    }

    private fun updateBatch(inputs: Array<DoubleArray>, expects: Array<DoubleArray>, start: Int, end: Int,
                            learningRate: Double)
    {
        val nablaInputLayer1Bias = DoubleArray(this.inputLayer1Bias.size)
        val nablaLayer1layer2Bias = DoubleArray(this.layer1layer2Bias.size)
        val nablaLayer2OutputBias = DoubleArray(this.layer2OutputBias.size)
        val nablaInputLayer1Weights = DoubleArray(this.inputLayer1Weights.size)
        val nablaLayer1layer2Weights = DoubleArray(this.layer1layer2Weights.size)
        val nablaLayer2OutputWeights = DoubleArray(this.layer2OutputWeights.size)
        val deltaInputLayer1Bias = DoubleArray(this.inputLayer1Bias.size)
        val deltaLayer1layer2Bias = DoubleArray(this.layer1layer2Bias.size)
        val deltaLayer2OutputBias = DoubleArray(this.layer2OutputBias.size)
        val deltaInputLayer1Weights = DoubleArray(this.inputLayer1Weights.size)
        val deltaLayer1layer2Weights = DoubleArray(this.layer1layer2Weights.size)
        val deltaLayer2OutputWeights = DoubleArray(this.layer2OutputWeights.size)

        for (index in start until end)
        {
            val input = inputs[index]
            val expect = expects[index]
            this.backdrop(input, expect,
                          deltaInputLayer1Bias, deltaInputLayer1Weights,
                          deltaLayer1layer2Bias, deltaLayer1layer2Weights,
                          deltaLayer2OutputBias, deltaLayer2OutputWeights)

            for (index1 in 0 until nablaInputLayer1Bias.size)
            {
                nablaInputLayer1Bias[index1] += deltaInputLayer1Bias[index1]
            }

            for (index1 in 0 until nablaLayer1layer2Bias.size)
            {
                nablaLayer1layer2Bias[index1] += deltaLayer1layer2Bias[index1]
            }

            for (index1 in 0 until nablaLayer2OutputBias.size)
            {
                nablaLayer2OutputBias[index1] += deltaLayer2OutputBias[index1]
            }

            for (index1 in 0 until nablaInputLayer1Weights.size)
            {
                nablaInputLayer1Weights[index1] += deltaInputLayer1Weights[index1]
            }

            for (index1 in 0 until nablaLayer1layer2Weights.size)
            {
                nablaLayer1layer2Weights[index1] += deltaLayer1layer2Weights[index1]
            }

            for (index1 in 0 until nablaLayer2OutputWeights.size)
            {
                nablaLayer2OutputWeights[index1] += deltaLayer2OutputWeights[index1]
            }
        }

        val factor = learningRate / (end - start)

        for (index in 0 until this.inputLayer1Bias.size)
        {
            this.inputLayer1Bias[index] -= factor * nablaInputLayer1Bias[index]
        }

        for (index in 0 until this.layer1layer2Bias.size)
        {
            this.layer1layer2Bias[index] -= factor * nablaLayer1layer2Bias[index]
        }

        for (index in 0 until this.layer2OutputBias.size)
        {
            this.layer2OutputBias[index] -= factor * nablaLayer2OutputBias[index]
        }

        for (index in 0 until this.inputLayer1Weights.size)
        {
            this.inputLayer1Weights[index] -= factor * nablaInputLayer1Weights[index]
        }

        for (index in 0 until this.layer1layer2Weights.size)
        {
            this.layer1layer2Weights[index] -= factor * nablaLayer1layer2Weights[index]
        }

        for (index in 0 until this.layer2OutputWeights.size)
        {
            this.layer2OutputWeights[index] -= factor * nablaLayer2OutputWeights[index]
        }
    }

    private fun backdrop(input: DoubleArray, expect: DoubleArray,
                         deltaInputLayer1Bias: DoubleArray, deltaInputLayer1Weights: DoubleArray,
                         deltaLayer1layer2Bias: DoubleArray, deltaLayer1layer2Weights: DoubleArray,
                         deltaLayer2OutputBias: DoubleArray, deltaLayer2OutputWeights: DoubleArray)
    {
        var activation = input
        val activations = ArrayList<DoubleArray>()
        activations.add(activation)
        val zs = ArrayList<Double>()

        // TODO implements backdrop [input, expect, deltaInputLayer1Bias, deltaInputLayer1Weights]
        todo("implements backdrop [input, expect, deltaInputLayer1Bias, deltaInputLayer1Weights]")
        TODO("implements backdrop [input, expect, deltaInputLayer1Bias, deltaInputLayer1Weights]")
    }

    val weights
        get() =
            this.inputLayer1Weights.sum() * factorInputLayer1 + this.layer1layer2Weights.sum() * factorLayer1Layer2 + this.layer2OutputWeights.sum() * factorLayer2Output

    val bias
        get() =
            this.inputLayer1Bias.sum() * factorInputLayer1 + this.layer1layer2Bias.sum() * factorLayer1Layer2 + this.layer2OutputBias.sum() * factorLayer2Output

    fun learn(inputs: Array<DoubleArray>, expects: Array<DoubleArray>)
    {
        val max = min(inputs.size, expects.size)

        (1..max).forEach { limit ->
            val cost = this.cost(inputs, expects, limit)
            debug("cost=", cost)
            val weights = this.weights
            (0 until this.inputLayer1Weights.size).forEach { this.inputLayer1Weights[it] -= (this.inputLayer1Weights[it] * cost * factorInputLayer1) / weights }
            (0 until this.layer1layer2Weights.size).forEach { this.layer1layer2Weights[it] -= (this.layer1layer2Weights[it] * cost * factorLayer1Layer2) / weights }
            (0 until this.layer2OutputWeights.size).forEach { this.layer2OutputWeights[it] -= (this.layer2OutputWeights[it] * cost * factorLayer2Output) / weights }
            val bias = this.bias
            (0 until this.inputLayer1Bias.size).forEach { this.inputLayer1Bias[it] -= (this.inputLayer1Bias[it] * cost * factorInputLayer1) / bias }
            (0 until this.layer1layer2Bias.size).forEach { this.layer1layer2Bias[it] -= (this.layer1layer2Bias[it] * cost * factorLayer1Layer2) / bias }
            (0 until this.layer2OutputBias.size).forEach { this.layer2OutputBias[it] -= (this.layer2OutputBias[it] * cost * factorLayer2Output) / bias }
        }
    }
}