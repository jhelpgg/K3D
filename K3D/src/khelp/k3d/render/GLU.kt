package khelp.k3d.render

import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.DoubleBuffer
import java.nio.IntBuffer

private class Project
{
    companion object
    {
        private val IDENTITY_MATRIX = doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
                                                    0.0, 0.0, 1.0)

        private fun cross(var0: DoubleArray, var1: DoubleArray, var2: DoubleArray)
        {
            var2[0] = var0[1] * var1[2] - var0[2] * var1[1]
            var2[1] = var0[2] * var1[0] - var0[0] * var1[2]
            var2[2] = var0[0] * var1[1] - var0[1] * var1[0]
        }

        private fun cross(var0: DoubleBuffer, var1: DoubleBuffer, var2: DoubleBuffer)
        {
            val var3 = var0.position()
            val var4 = var1.position()
            val var5 = var2.position()
            var2.put(0 + var5, var0.get(1 + var3) * var1.get(2 + var4) - var0.get(2 + var3) * var1.get(1 + var4))
            var2.put(1 + var5, var0.get(2 + var3) * var1.get(0 + var4) - var0.get(0 + var3) * var1.get(2 + var4))
            var2.put(2 + var5, var0.get(0 + var3) * var1.get(1 + var4) - var0.get(1 + var3) * var1.get(0 + var4))
        }

        private fun normalize(var0: DoubleArray)
        {
            var var1 = Math.sqrt(var0[0] * var0[0] + var0[1] * var0[1] + var0[2] * var0[2])
            if (var1 != 0.0)
            {
                var1 = 1.0 / var1
                var0[0] *= var1
                var0[1] *= var1
                var0[2] *= var1
            }
        }

        private fun normalize(var0: DoubleBuffer)
        {
            val var3 = var0.position()
            var var1 = Math.sqrt(var0.get(0 + var3) * var0.get(0 + var3) + var0.get(1 + var3) * var0.get(1 + var3) +
                                         var0.get(2 + var3) * var0.get(2 + var3))
            if (var1 != 0.0)
            {
                var1 = 1.0 / var1
                var0.put(0 + var3, var0.get(0 + var3) * var1)
                var0.put(1 + var3, var0.get(1 + var3) * var1)
                var0.put(2 + var3, var0.get(2 + var3) * var1)
            }
        }

        private fun slice(var0: DoubleBuffer, var1: Int, var2: Int): DoubleBuffer
        {
            var0.position(var1)
            var0.limit(var1 + var2)
            return var0.slice()
        }
    }

    private val forward = DoubleArray(3)
    private val forwardBuf: DoubleBuffer
    private val `in` = DoubleArray(4)
    private val inBuf: DoubleBuffer
    private val matrix = DoubleArray(16)
    private val matrixBuf: DoubleBuffer
    private val out = DoubleArray(4)
    private val outBuf: DoubleBuffer
    private val side = DoubleArray(3)
    private val sideBuf: DoubleBuffer
    private val tempMatrix = Array(4) { DoubleArray(4) }
    private val tempMatrixBuf: DoubleBuffer
    private val up = DoubleArray(3)
    private val upBuf: DoubleBuffer

    init
    {
        val var1 = ByteBuffer.allocateDirect(128 * 8).order(ByteOrder.nativeOrder()).asDoubleBuffer()
        val var2: Byte = 0
        var var3: Byte = 16
        this.matrixBuf = Project.slice(var1, var2.toInt(), var3.toInt())
        var var4 = var2 + var3
        this.tempMatrixBuf = Project.slice(var1, var4, var3.toInt())
        var4 += var3.toInt()
        var3 = 4
        this.inBuf = Project.slice(var1, var4, var3.toInt())
        var4 += var3.toInt()
        this.outBuf = Project.slice(var1, var4, var3.toInt())
        var4 += var3.toInt()
        var3 = 3
        this.forwardBuf = Project.slice(var1, var4, var3.toInt())
        var4 += var3.toInt()
        this.sideBuf = Project.slice(var1, var4, var3.toInt())
        var4 += var3.toInt()
        this.upBuf = Project.slice(var1, var4, var3.toInt())
    }

    private fun __gluInvertMatrixd(var1: DoubleArray, var2: DoubleArray): Boolean
    {
        val var9 = this.tempMatrix

        var var3: Int
        var var4: Int
        var3 = 0
        while (var3 < 4)
        {
            var4 = 0
            while (var4 < 4)
            {
                var9[var3][var4] = var1[var3 * 4 + var4]
                ++var4
            }
            ++var3
        }

        this.__gluMakeIdentityd(var2)

        var3 = 0
        while (var3 < 4)
        {
            var var6 = var3

            var4 = var3 + 1
            while (var4 < 4)
            {
                if (Math.abs(var9[var4][var3]) > Math.abs(var9[var3][var3]))
                {
                    var6 = var4
                }
                ++var4
            }

            var var5: Int
            var var7: Double
            if (var6 != var3)
            {
                var5 = 0
                while (var5 < 4)
                {
                    var7 = var9[var3][var5]
                    var9[var3][var5] = var9[var6][var5]
                    var9[var6][var5] = var7
                    var7 = var2[var3 * 4 + var5]
                    var2[var3 * 4 + var5] = var2[var6 * 4 + var5]
                    var2[var6 * 4 + var5] = var7
                    ++var5
                }
            }

            if (var9[var3][var3] == 0.0)
            {
                return false
            }

            var7 = var9[var3][var3]

            var5 = 0
            while (var5 < 4)
            {
                var9[var3][var5] /= var7
                var2[var3 * 4 + var5] /= var7
                ++var5
            }

            var4 = 0
            while (var4 < 4)
            {
                if (var4 != var3)
                {
                    var7 = var9[var4][var3]

                    var5 = 0
                    while (var5 < 4)
                    {
                        var9[var4][var5] -= var9[var3][var5] * var7
                        var2[var4 * 4 + var5] -= var2[var3 * 4 + var5] * var7
                        ++var5
                    }
                }
                ++var4
            }
            ++var3
        }

        return true
    }

    private fun __gluInvertMatrixd(var1: DoubleBuffer, var2: DoubleBuffer): Boolean
    {
        val var9 = var1.position()
        val var10 = var2.position()
        val var11 = this.tempMatrixBuf

        var var3: Int
        var var4: Int
        var3 = 0
        while (var3 < 4)
        {
            var4 = 0
            while (var4 < 4)
            {
                var11.put(var3 * 4 + var4, var1.get(var3 * 4 + var4 + var9))
                ++var4
            }
            ++var3
        }

        this.__gluMakeIdentityd(var2)

        var3 = 0
        while (var3 < 4)
        {
            var var6 = var3

            var4 = var3 + 1
            while (var4 < 4)
            {
                if (Math.abs(var11.get(var4 * 4 + var3)) > Math.abs(var11.get(var3 * 4 + var3)))
                {
                    var6 = var4
                }
                ++var4
            }

            var var5: Int
            var var7: Double
            if (var6 != var3)
            {
                var5 = 0
                while (var5 < 4)
                {
                    var7 = var11.get(var3 * 4 + var5)
                    var11.put(var3 * 4 + var5, var11.get(var6 * 4 + var5))
                    var11.put(var6 * 4 + var5, var7)
                    var7 = var2.get(var3 * 4 + var5 + var10)
                    var2.put(var3 * 4 + var5 + var10, var2.get(var6 * 4 + var5 + var10))
                    var2.put(var6 * 4 + var5 + var10, var7)
                    ++var5
                }
            }

            if (var11.get(var3 * 4 + var3) == 0.0)
            {
                return false
            }

            var7 = var11.get(var3 * 4 + var3)

            var5 = 0
            while (var5 < 4)
            {
                var11.put(var3 * 4 + var5, var11.get(var3 * 4 + var5) / var7)
                var2.put(var3 * 4 + var5 + var10, var2.get(var3 * 4 + var5 + var10) / var7)
                ++var5
            }

            var4 = 0
            while (var4 < 4)
            {
                if (var4 != var3)
                {
                    var7 = var11.get(var4 * 4 + var3)

                    var5 = 0
                    while (var5 < 4)
                    {
                        var11.put(var4 * 4 + var5, var11.get(var4 * 4 + var5) - var11.get(var3 * 4 + var5) * var7)
                        var2.put(var4 * 4 + var5 + var10,
                                 var2.get(var4 * 4 + var5 + var10) - var2.get(var3 * 4 + var5 + var10) * var7)
                        ++var5
                    }
                }
                ++var4
            }
            ++var3
        }

        return true
    }

    private fun __gluMakeIdentityd(var1: DoubleBuffer)
    {
        val var2 = var1.position()
        var1.put(Project.IDENTITY_MATRIX)
        var1.position(var2)
    }

    private fun __gluMakeIdentityd(var1: DoubleArray)
    {
        for (var2 in 0..15)
        {
            var1[var2] = Project.IDENTITY_MATRIX[var2]
        }

    }

    private fun __gluMultMatricesd(var1: DoubleArray, var2: Int, var3: DoubleArray, var4: Int, var5: DoubleArray)
    {
        for (var6 in 0..3)
        {
            for (var7 in 0..3)
            {
                var5[var6 * 4 + var7] = var1[var6 * 4 + 0 + var2] * var3[0 + var7 + var4] +
                        var1[var6 * 4 + 1 + var2] * var3[4 + var7 + var4] +
                        var1[var6 * 4 + 2 + var2] * var3[8 + var7 + var4] +
                        var1[var6 * 4 + 3 + var2] * var3[12 + var7 + var4]
            }
        }

    }

    private fun __gluMultMatricesd(var1: DoubleBuffer, var2: DoubleBuffer, var3: DoubleBuffer)
    {
        val var4 = var1.position()
        val var5 = var2.position()
        val var6 = var3.position()

        for (var7 in 0..3)
        {
            for (var8 in 0..3)
            {
                var3.put(var7 * 4 + var8 + var6, var1.get(var7 * 4 + 0 + var4) * var2.get(0 + var8 + var5) +
                        var1.get(var7 * 4 + 1 + var4) * var2.get(4 + var8 + var5) +
                        var1.get(var7 * 4 + 2 + var4) * var2.get(8 + var8 + var5) +
                        var1.get(var7 * 4 + 3 + var4) * var2.get(12 + var8 + var5))
            }
        }

    }

    private fun __gluMultMatrixVecd(var1: DoubleArray, var2: Int, var3: DoubleArray, var4: DoubleArray)
    {
        for (var5 in 0..3)
        {
            var4[var5] = var3[0] * var1[0 + var5 + var2] + var3[1] * var1[4 + var5 + var2] +
                    var3[2] * var1[8 + var5 + var2] + var3[3] * var1[12 + var5 + var2]
        }

    }

    private fun __gluMultMatrixVecd(var1: DoubleBuffer, var2: DoubleBuffer, var3: DoubleBuffer)
    {
        val var4 = var2.position()
        val var5 = var3.position()
        val var6 = var1.position()

        for (var7 in 0..3)
        {
            var3.put(var7 + var5, var2.get(0 + var4) * var1.get(0 + var7 + var6) +
                    var2.get(1 + var4) * var1.get(4 + var7 + var6) +
                    var2.get(2 + var4) * var1.get(8 + var7 + var6) +
                    var2.get(3 + var4) * var1.get(12 + var7 + var6))
        }

    }

    fun gluLookAt(
            var2: Double, var4: Double, var6: Double, var8: Double, var10: Double, var12: Double, var14: Double,
            var16: Double, var18: Double)
    {
        val var20 = this.forwardBuf
        val var21 = this.sideBuf
        val var22 = this.upBuf
        var20.put(0, var8 - var2)
        var20.put(1, var10 - var4)
        var20.put(2, var12 - var6)
        var22.put(0, var14)
        var22.put(1, var16)
        var22.put(2, var18)
        Project.normalize(var20)
        Project.cross(var20, var22, var21)
        Project.normalize(var21)
        Project.cross(var21, var20, var22)
        this.__gluMakeIdentityd(this.matrixBuf)
        this.matrixBuf.put(0, var21.get(0))
        this.matrixBuf.put(4, var21.get(1))
        this.matrixBuf.put(8, var21.get(2))
        this.matrixBuf.put(1, var22.get(0))
        this.matrixBuf.put(5, var22.get(1))
        this.matrixBuf.put(9, var22.get(2))
        this.matrixBuf.put(2, -var20.get(0))
        this.matrixBuf.put(6, -var20.get(1))
        this.matrixBuf.put(10, -var20.get(2))
        GL11.glMultMatrixd(this.matrixBuf)
        GL11.glTranslated(-var2, -var4, -var6)
    }

    fun gluOrtho2D(var2: Double, var4: Double, var6: Double, var8: Double)
    {
        GL11.glOrtho(var2, var4, var6, var8, -1.0, 1.0)
    }

    fun gluPerspective(var2: Double, var4: Double, var6: Double, var8: Double)
    {
        val var16 = var2 / 2.0 * 3.141592653589793 / 180.0
        val var14 = var8 - var6
        val var10 = Math.sin(var16)
        if (var14 != 0.0 && var10 != 0.0 && var4 != 0.0)
        {
            val var12 = Math.cos(var16) / var10
            this.__gluMakeIdentityd(this.matrixBuf)
            this.matrixBuf.put(0, var12 / var4)
            this.matrixBuf.put(5, var12)
            this.matrixBuf.put(10, -(var8 + var6) / var14)
            this.matrixBuf.put(11, -1.0)
            this.matrixBuf.put(14, -2.0 * var6 * var8 / var14)
            this.matrixBuf.put(15, 0.0)
            GL11.glMultMatrixd(this.matrixBuf)
        }
    }

    fun gluPickMatrix(var2: Double, var4: Double, var6: Double, var8: Double, var10: IntBuffer)
    {
        if (var6 > 0.0 && var8 > 0.0)
        {
            val var11 = var10.position()
            GL11.glTranslated(
                    (var10.get(2 + var11).toDouble() - 2.0 * (var2 - var10.get(0 + var11).toDouble())) / var6,
                    (var10.get(3 + var11).toDouble() - 2.0 * (var4 - var10.get(1 + var11).toDouble())) / var8, 0.0)
            GL11.glScaled(var10.get(2).toDouble() / var6, var10.get(3).toDouble() / var8, 1.0)
        }
    }

    fun gluPickMatrix(var2: Double, var4: Double, var6: Double, var8: Double, var10: IntArray, var11: Int)
    {
        if (var6 > 0.0 && var8 > 0.0)
        {
            GL11.glTranslated((var10[2 + var11].toDouble() - 2.0 * (var2 - var10[0 + var11].toDouble())) / var6,
                              (var10[3 + var11].toDouble() - 2.0 * (var4 - var10[1 + var11].toDouble())) / var8, 0.0)
            GL11.glScaled(var10[2 + var11].toDouble() / var6, var10[3 + var11].toDouble() / var8, 1.0)
        }
    }

    fun gluProject(
            var1: Double, var3: Double, var5: Double, var7: DoubleArray, var8: Int, var9: DoubleArray, var10: Int,
            var11: IntArray,
            var12: Int, var13: DoubleArray, var14: Int): Boolean
    {
        val var15 = this.`in`
        val var16 = this.out
        var15[0] = var1
        var15[1] = var3
        var15[2] = var5
        var15[3] = 1.0
        this.__gluMultMatrixVecd(var7, var8, var15, var16)
        this.__gluMultMatrixVecd(var9, var10, var16, var15)
        if (var15[3] == 0.0)
        {
            return false
        }
        else
        {
            var15[3] = 1.0 / var15[3] * 0.5
            var15[0] = var15[0] * var15[3] + 0.5
            var15[1] = var15[1] * var15[3] + 0.5
            var15[2] = var15[2] * var15[3] + 0.5
            var13[0 + var14] = var15[0] * var11[2 + var12].toDouble() + var11[0 + var12].toDouble()
            var13[1 + var14] = var15[1] * var11[3 + var12].toDouble() + var11[1 + var12].toDouble()
            var13[2 + var14] = var15[2]
            return true
        }
    }

    fun gluProject(
            var1: Double, var3: Double, var5: Double, var7: DoubleBuffer, var8: DoubleBuffer, var9: IntBuffer,
            var10: DoubleBuffer): Boolean
    {
        val var11 = this.inBuf
        val var12 = this.outBuf
        var11.put(0, var1)
        var11.put(1, var3)
        var11.put(2, var5)
        var11.put(3, 1.0)
        this.__gluMultMatrixVecd(var7, var11, var12)
        this.__gluMultMatrixVecd(var8, var12, var11)
        if (var11.get(3) == 0.0)
        {
            return false
        }
        else
        {
            var11.put(3, 1.0 / var11.get(3) * 0.5)
            var11.put(0, var11.get(0) * var11.get(3) + 0.5)
            var11.put(1, var11.get(1) * var11.get(3) + 0.5)
            var11.put(2, var11.get(2) * var11.get(3) + 0.5)
            val var13 = var9.position()
            val var14 = var10.position()
            var10.put(0 + var14, var11.get(0) * var9.get(2 + var13).toDouble() + var9.get(0 + var13).toDouble())
            var10.put(1 + var14, var11.get(1) * var9.get(3 + var13).toDouble() + var9.get(1 + var13).toDouble())
            var10.put(2 + var14, var11.get(2))
            return true
        }
    }

    fun gluUnProject(
            var1: Double, var3: Double, var5: Double, var7: DoubleArray, var8: Int, var9: DoubleArray, var10: Int,
            var11: IntArray,
            var12: Int, var13: DoubleArray, var14: Int): Boolean
    {
        val var15 = this.`in`
        val var16 = this.out
        this.__gluMultMatricesd(var7, var8, var9, var10, this.matrix)
        if (!this.__gluInvertMatrixd(this.matrix, this.matrix))
        {
            return false
        }
        else
        {
            var15[0] = var1
            var15[1] = var3
            var15[2] = var5
            var15[3] = 1.0
            var15[0] = (var15[0] - var11[0 + var12].toDouble()) / var11[2 + var12].toDouble()
            var15[1] = (var15[1] - var11[1 + var12].toDouble()) / var11[3 + var12].toDouble()
            var15[0] = var15[0] * 2.0 - 1.0
            var15[1] = var15[1] * 2.0 - 1.0
            var15[2] = var15[2] * 2.0 - 1.0
            this.__gluMultMatrixVecd(this.matrix, 0, var15, var16)
            if (var16[3] == 0.0)
            {
                return false
            }
            else
            {
                var16[3] = 1.0 / var16[3]
                var13[0 + var14] = var16[0] * var16[3]
                var13[1 + var14] = var16[1] * var16[3]
                var13[2 + var14] = var16[2] * var16[3]
                return true
            }
        }
    }

    fun gluUnProject(
            var1: Double, var3: Double, var5: Double, var7: DoubleBuffer, var8: DoubleBuffer, var9: IntBuffer,
            var10: DoubleBuffer): Boolean
    {
        val var11 = this.inBuf
        val var12 = this.outBuf
        this.__gluMultMatricesd(var7, var8, this.matrixBuf)
        if (!this.__gluInvertMatrixd(this.matrixBuf, this.matrixBuf))
        {
            return false
        }
        else
        {
            var11.put(0, var1)
            var11.put(1, var3)
            var11.put(2, var5)
            var11.put(3, 1.0)
            val var13 = var9.position()
            val var14 = var10.position()
            var11.put(0, (var11.get(0) - var9.get(0 + var13).toDouble()) / var9.get(2 + var13).toDouble())
            var11.put(1, (var11.get(1) - var9.get(1 + var13).toDouble()) / var9.get(3 + var13).toDouble())
            var11.put(0, var11.get(0) * 2.0 - 1.0)
            var11.put(1, var11.get(1) * 2.0 - 1.0)
            var11.put(2, var11.get(2) * 2.0 - 1.0)
            this.__gluMultMatrixVecd(this.matrixBuf, var11, var12)
            if (var12.get(3) == 0.0)
            {
                return false
            }
            else
            {
                var12.put(3, 1.0 / var12.get(3))
                var10.put(0 + var14, var12.get(0) * var12.get(3))
                var10.put(1 + var14, var12.get(1) * var12.get(3))
                var10.put(2 + var14, var12.get(2) * var12.get(3))
                return true
            }
        }
    }

    fun gluUnProject4(
            var1: Double, var3: Double, var5: Double, var7: Double, var9: DoubleArray, var10: Int, var11: DoubleArray,
            var12: Int,
            var13: IntArray, var14: Int, var15: Double, var17: Double, var19: DoubleArray, var20: Int): Boolean
    {
        val var21 = this.`in`
        val var22 = this.out
        this.__gluMultMatricesd(var9, var10, var11, var12, this.matrix)
        if (!this.__gluInvertMatrixd(this.matrix, this.matrix))
        {
            return false
        }
        else
        {
            var21[0] = var1
            var21[1] = var3
            var21[2] = var5
            var21[3] = var7
            var21[0] = (var21[0] - var13[0 + var14].toDouble()) / var13[2 + var14].toDouble()
            var21[1] = (var21[1] - var13[1 + var14].toDouble()) / var13[3 + var14].toDouble()
            var21[2] = (var21[2] - var15) / (var17 - var15)
            var21[0] = var21[0] * 2.0 - 1.0
            var21[1] = var21[1] * 2.0 - 1.0
            var21[2] = var21[2] * 2.0 - 1.0
            this.__gluMultMatrixVecd(this.matrix, 0, var21, var22)
            if (var22[3] == 0.0)
            {
                return false
            }
            else
            {
                var19[0 + var20] = var22[0]
                var19[1 + var20] = var22[1]
                var19[2 + var20] = var22[2]
                var19[3 + var20] = var22[3]
                return true
            }
        }
    }

    fun gluUnProject4(
            var1: Double, var3: Double, var5: Double, var7: Double, var9: DoubleBuffer, var10: DoubleBuffer,
            var11: IntBuffer, var12: Double, var14: Double, var16: DoubleBuffer): Boolean
    {
        val var17 = this.inBuf
        val var18 = this.outBuf
        this.__gluMultMatricesd(var9, var10, this.matrixBuf)
        if (!this.__gluInvertMatrixd(this.matrixBuf, this.matrixBuf))
        {
            return false
        }
        else
        {
            var17.put(0, var1)
            var17.put(1, var3)
            var17.put(2, var5)
            var17.put(3, var7)
            val var19 = var11.position()
            var17.put(0, (var17.get(0) - var11.get(0 + var19).toDouble()) / var11.get(2 + var19).toDouble())
            var17.put(1, (var17.get(1) - var11.get(1 + var19).toDouble()) / var11.get(3 + var19).toDouble())
            var17.put(2, (var17.get(2) - var12) / (var14 - var12))
            var17.put(0, var17.get(0) * 2.0 - 1.0)
            var17.put(1, var17.get(1) * 2.0 - 1.0)
            var17.put(2, var17.get(2) * 2.0 - 1.0)
            this.__gluMultMatrixVecd(this.matrixBuf, var17, var18)
            if (var18.get(3) == 0.0)
            {
                return false
            }
            else
            {
                val var20 = var16.position()
                var16.put(0 + var20, var18.get(0))
                var16.put(1 + var20, var18.get(1))
                var16.put(2 + var20, var18.get(2))
                var16.put(3 + var20, var18.get(3))
                return true
            }
        }
    }
}

private val PROJECT = Project()
val GLU_BEGIN = 100100
val GLU_CCW = 100121
val GLU_CW = 100120
val GLU_EDGE_FLAG = 100104
val GLU_END = 100102
val GLU_ERROR = 100103
val GLU_EXTENSIONS = 100801
val GLU_EXTERIOR = 100123
val GLU_FALSE = 0
val GLU_FILL = 100012
val GLU_FLAT = 100001
val GLU_INSIDE = 100021
val GLU_INTERIOR = 100122
val GLU_INVALID_ENUM = 100900
val GLU_INVALID_OPERATION = 100904
val GLU_INVALID_VALUE = 100901
val GLU_LINE = 100011
val GLU_NONE = 100002
val GLU_OUTSIDE = 100020
val GLU_OUT_OF_MEMORY = 100902
val GLU_POINT = 100010
val GLU_SILHOUETTE = 100013
val GLU_SMOOTH = 100000
val GLU_TESS_BEGIN = 100100
val GLU_TESS_BEGIN_DATA = 100106
val GLU_TESS_BOUNDARY_ONLY = 100141
val GLU_TESS_COMBINE = 100105
val GLU_TESS_COMBINE_DATA = 100111
val GLU_TESS_COORD_TOO_LARGE = 100155
val GLU_TESS_EDGE_FLAG = 100104
val GLU_TESS_EDGE_FLAG_DATA = 100110
val GLU_TESS_END = 100102
val GLU_TESS_END_DATA = 100108
val GLU_TESS_ERROR = 100103
val GLU_TESS_ERROR1 = 100151
val GLU_TESS_ERROR2 = 100152
val GLU_TESS_ERROR3 = 100153
val GLU_TESS_ERROR4 = 100154
val GLU_TESS_ERROR5 = 100155
val GLU_TESS_ERROR6 = 100156
val GLU_TESS_ERROR7 = 100157
val GLU_TESS_ERROR8 = 100158
val GLU_TESS_ERROR_DATA = 100109
val GLU_TESS_MAX_COORD = 1.0E150
val GLU_TESS_MISSING_BEGIN_CONTOUR = 100152
val GLU_TESS_MISSING_BEGIN_POLYGON = 100151
val GLU_TESS_MISSING_END_CONTOUR = 100154
val GLU_TESS_MISSING_END_POLYGON = 100153
val GLU_TESS_NEED_COMBINE_CALLBACK = 100156
val GLU_TESS_TOLERANCE = 100142
val GLU_TESS_VERTEX = 100101
val GLU_TESS_VERTEX_DATA = 100107
val GLU_TESS_WINDING_ABS_GEQ_TWO = 100134
val GLU_TESS_WINDING_NEGATIVE = 100133
val GLU_TESS_WINDING_NONZERO = 100131
val GLU_TESS_WINDING_ODD = 100130
val GLU_TESS_WINDING_POSITIVE = 100132
val GLU_TESS_WINDING_RULE = 100140
val GLU_TRUE = 1
val GLU_UNKNOWN = 100124
val GLU_VERSION = 100800
val GLU_VERTEX = 100101
val extensionString = "GLU_EXT_nurbs_tessellator GLU_EXT_object_space_tess "
val versionString = "1.3"

fun gluLookAt(
        var2: Double, var4: Double, var6: Double, var8: Double, var10: Double, var12: Double, var14: Double,
        var16: Double, var18: Double)
{
    PROJECT.gluLookAt(var2, var4, var6, var8, var10, var12, var14, var16, var18)
}

fun gluOrtho2D(var2: Double, var4: Double, var6: Double, var8: Double)
{
    PROJECT.gluOrtho2D(var2, var4, var6, var8)
}

/**
 * Compute and apply a matrix to have a perspective view of the scene
 *
 * @param angle Angle (in degree) for vanishing point
 * @param ratio View ratio (usually width/height)
 * @param nearZ Value of Z near the screen
 * @param farZ  Value of Z in depth
 */
fun gluPerspective(angle: Double, ratio: Double, nearZ: Double, farZ: Double)
{
    PROJECT.gluPerspective(angle, ratio, nearZ, farZ)
}

fun gluPickMatrix(var2: Double, var4: Double, var6: Double, var8: Double, var10: IntArray, var11: Int)
{
    PROJECT.gluPickMatrix(var2, var4, var6, var8, var10, var11)
}

fun gluPickMatrix(var2: Double, var4: Double, var6: Double, var8: Double, var10: IntBuffer)
{
    PROJECT.gluPickMatrix(var2, var4, var6, var8, var10)
}

fun gluProject(
        var1: Double, var3: Double, var5: Double, var7: DoubleArray, var8: Int, var9: DoubleArray, var10: Int,
        var11: IntArray,
        var12: Int, var13: DoubleArray, var14: Int): Boolean
{
    return PROJECT.gluProject(var1, var3, var5, var7, var8, var9, var10, var11, var12, var13, var14)
}

fun gluUnProject(
        var1: Double, var3: Double, var5: Double, var7: DoubleArray, var8: Int, var9: DoubleArray, var10: Int,
        var11: IntArray,
        var12: Int, var13: DoubleArray, var14: Int): Boolean
{
    return PROJECT.gluUnProject(var1, var3, var5, var7, var8, var9, var10, var11, var12, var13, var14)
}

fun gluUnProject(
        var1: Double, var3: Double, var5: Double, var7: DoubleBuffer, var8: DoubleBuffer, var9: IntBuffer,
        var10: DoubleBuffer): Boolean
{
    return PROJECT.gluUnProject(var1, var3, var5, var7, var8, var9, var10)
}

fun gluUnProject4(
        var1: Double, var3: Double, var5: Double, var7: Double, var9: DoubleArray, var10: Int,
        var11: DoubleArray, var12: Int,
        var13: IntArray, var14: Int, var15: Double, var17: Double, var19: DoubleArray, var20: Int): Boolean
{
    return PROJECT.gluUnProject4(var1, var3, var5, var7, var9, var10, var11, var12, var13, var14, var15,
                                 var17,
                                 var19, var20)
}

fun gluUnProject4(
        var1: Double, var3: Double, var5: Double, var7: Double, var9: DoubleBuffer, var10: DoubleBuffer,
        var11: IntBuffer, var12: Double, var14: Double, var16: DoubleBuffer): Boolean
{
    return PROJECT.gluUnProject4(var1, var3, var5, var7, var9, var10, var11, var12, var14, var16)
}

fun gluProject(
        var1: Double, var3: Double, var5: Double, var7: DoubleBuffer, var8: DoubleBuffer, var9: IntBuffer,
        var10: DoubleBuffer): Boolean
{
    return PROJECT.gluProject(var1, var3, var5, var7, var8, var9, var10)
}
