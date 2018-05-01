package khelp.images.gif

import khelp.debug.warning
import khelp.util.and
import khelp.util.shl
import khelp.util.toUnsignedInt
import java.io.IOException
import java.io.InputStream

/**
 * Image description block
 * @param colorResolution Color resolution level
 */
internal class ImageDescriptorBlock internal constructor(val colorResolution: Int) : Block()
{
    companion object
    {
        /**
         * Jump between row to do if interlaced image
         */
        private val passJump = intArrayOf(8, 8, 4, 2)
        /**
         * Start y for each interlaced pass
         */
        private val passStart = intArrayOf(0, 4, 2, 1)
    }

    /**
     * Actual read bit position
     */
    private var bitPos = 0
    /**
     * Actual buffered 32 bits
     */
    private var buffer32: Int = 0
    /**
     * Image well ordered, uncompressed color indexes
     */
    lateinit var colorIndexes: IntArray
        private set
    /**
     * Image height
     */
    var height: Int = 0
        private set
    /**
     * Indicates if image is interlaced
     */
    private var interlaced: Boolean = false
    /**
     * Indicates that we reach the last block
     */
    private var lastBlockFound = false
    /**
     * Local image color table
     */
    var localColorTable: GIFColorTable? = null
        private set
    /**
     * Next byte index to read in current block
     */
    private var nextByte = 4
    /**
     * Pass current index
     */
    private var passIndex = 0
    /**
     * Pixel index where write
     */
    private var pix = 0
    /**
     * Image width
     */
    var width: Int = 0
        private set
    /**
     * Image x
     */
    var x: Int = 0
        private set
    /**
     * Current write pixel X
     */
    private var xx = 0
    /**
     * Image Y
     */
    var y: Int = 0
        private set
    /**
     * Current pixel write Y
     */
    private var yy = 0

    /**
     * Read the next code and next block couple
     *
     * @param codeSize    Size of code
     * @param codeMask    Code mask
     * @param inputStream Stream to read
     * @param subBlock    Current block to read
     * @param endCode     Code for terminate
     * @return Next code and next block pair
     * @throws IOException If stream reach end or close suddenly
     */
    @Throws(IOException::class)
    private fun getCode(codeSize: Int, codeMask: Int, inputStream: InputStream,
                        subBlock: SubBlock, endCode: Int): Pair<Int, SubBlock>
    {
        var subBlock = subBlock

        if (this.bitPos + codeSize > 32)
        {
            return Pair(endCode, subBlock) // No more data available
        }

        val code = this.buffer32 shr this.bitPos and codeMask
        this.bitPos += codeSize
        var blockLength = subBlock.size
        var block = subBlock.data

        // Shift in a byte of new data at a time
        while (this.bitPos >= 8 && !this.lastBlockFound)
        {
            this.buffer32 = this.buffer32 ushr 8
            this.bitPos -= 8

            // Check if current block is out of bytes
            if (this.nextByte >= blockLength)
            {
                // Get next block size
                subBlock = readSubBlock(inputStream)

                if (subBlock === EMPTY)
                {
                    this.lastBlockFound = true
                    return Pair(code, subBlock)
                }
                else
                {
                    blockLength = subBlock.size
                    block = subBlock.data
                    this.nextByte = 0
                }
            }

            this.buffer32 = this.buffer32 or (block[this.nextByte++] shl 24)
        }

        return Pair(code, subBlock)
    }

    /**
     * Initialize LZW table
     *
     * @param prefix  Prefixes to initialize
     * @param suffix  Suffixes to initialize
     * @param initial Initial values to initialize
     * @param length  Code lengths to initialize
     * @param lzwCode Read LZW code
     */
    private fun initializeStringTable(prefix: IntArray, suffix: ByteArray, initial: ByteArray, length: IntArray,
                                      lzwCode: Int)
    {
        val numEntries = 1 shl lzwCode

        for (i in 0 until numEntries)
        {
            prefix[i] = -1
            suffix[i] = i.toByte()
            initial[i] = i.toByte()
            length[i] = 1
        }

        // Fill in the entire table for robustness against
        // out-of-sequence codes.
        for (i in numEntries..4095)
        {
            prefix[i] = -1
            length[i] = 1
        }
    }

    /**
     * Read the image data
     *
     * @param inputStream Stream to read
     * @throws IOException If stream is not valid image data
     */
    @Throws(IOException::class)
    private fun readImage(inputStream: InputStream)
    {
        val lzwCode = inputStream.read()

        if (lzwCode < 0)
        {
            throw IOException("No enough data to read LZW code")
        }

        this.colorIndexes = IntArray(this.width * this.height)
        var subBlock = readSubBlock(inputStream)

        if (subBlock === EMPTY)
        {
            return
        }

        val data = subBlock.data

        if (data.size < 4)
        {
            // To avoid some malformed GIF
            return
        }

        this.buffer32 = data[0].toUnsignedInt() or (data[1].toUnsignedInt() shl 8) or
                (data[2].toUnsignedInt() shl 16) or (data[3].toUnsignedInt() shl 24)
        val clearCode = 1 shl lzwCode
        val endCode = clearCode + 1
        var code: Int
        var oldCode = 0
        val prefix = IntArray(4096)
        val suffix = ByteArray(4096)
        val initial = ByteArray(4096)
        val length = IntArray(4096)
        val string = ByteArray(4096)
        this.initializeStringTable(prefix, suffix, initial, length, lzwCode)
        var tableIndex = (1 shl lzwCode) + 2
        var codeSize = lzwCode + 1
        var codeMask = (1 shl codeSize) - 1
        var pair: Pair<Int, SubBlock> = Pair(0, subBlock)

        while (pair.first != endCode)
        {
            pair = this.getCode(codeSize, codeMask, inputStream, subBlock, endCode)
            code = pair.first
            subBlock = pair.second

            if (code == clearCode)
            {
                this.initializeStringTable(prefix, suffix, initial, length, lzwCode)
                tableIndex = (1 shl lzwCode) + 2
                codeSize = lzwCode + 1
                codeMask = (1 shl codeSize) - 1

                pair = this.getCode(codeSize, codeMask, inputStream, subBlock, endCode)
                code = pair.first
                subBlock = pair.second
                if (code == endCode)
                {
                    return
                }
            }
            else if (code == endCode)
            {
                return
            }
            else
            {
                val newSuffixIndex: Int
                if (code < tableIndex)
                {
                    newSuffixIndex = code
                }
                else
                { // code == tableIndex
                    newSuffixIndex = oldCode
                    if (code != tableIndex)
                    {
                        // warning - code out of sequence
                        // possibly data corruption
                        warning("Out-of-sequence code!")
                    }
                }

                val ti = tableIndex

                prefix[ti] = oldCode
                suffix[ti] = initial[newSuffixIndex]
                initial[ti] = initial[oldCode]
                length[ti] = length[oldCode] + 1

                tableIndex++
                if (tableIndex == 1 shl codeSize && tableIndex < 4096)
                {
                    codeSize++
                    codeMask = (1 shl codeSize) - 1
                }
            }

            // Reverse code
            var c = code
            val len = length[c]
            for (i in len - 1 downTo 0)
            {
                string[i] = suffix[c]
                c = prefix[c]
            }

            this.writeImage(string, len)
            oldCode = code
        }
    }

    /**
     * Write in correct order uncompressed indexes
     *
     * @param data   Uncompressed indexes
     * @param length Data length
     */
    private fun writeImage(data: ByteArray, length: Int)
    {
        val size = this.colorIndexes.size

        var i = 0
        while (i < length && this.pix < size)
        {
            this.colorIndexes[this.pix] = data[i] and 0xFF

            this.xx++
            this.pix++

            if (this.xx >= this.width)
            {
                this.xx = 0

                if (this.interlaced)
                {
                    this.yy += ImageDescriptorBlock.passJump[this.passIndex]

                    if (this.yy >= this.height)
                    {
                        this.passIndex = Math.min(3, this.passIndex + 1)
                        this.yy = ImageDescriptorBlock.passStart[this.passIndex]
                    }

                    this.pix = this.yy * this.width
                }
                else
                {
                    this.yy++
                }
            }
            i++
        }
    }

    /**
     * Read image descriptor block
     *
     *
     *
     * **Parent documentation:**
     *
     * {@inheritDoc}
     *
     * @param inputStream Stream to read
     * @throws IOException If stream is not a valid image descriptor block
     * @see Block.read
     */
    @Throws(IOException::class)
    override internal fun read(inputStream: InputStream)
    {
        this.x = read2ByteInt(inputStream)
        this.y = read2ByteInt(inputStream)
        this.width = read2ByteInt(inputStream)
        this.height = read2ByteInt(inputStream)
        val flags = inputStream.read()

        if (flags < 0)
        {
            throw IOException("No enough data for have image flags")
        }

        val colorTableFollow = (flags and MASK_COLOR_TABLE_FOLLOW) != 0
        this.interlaced = (flags and MASK_IMAGE_INTERLACED) != 0
        val ordered = (flags and MASK_COLOR_TABLE_ORDERED) != 0
        val localTableSize = 1 shl (flags and MASK_GLOBAL_COLOR_TABLE_SIZE) + 1

        if (colorTableFollow)
        {
            this.localColorTable = GIFColorTable(this.colorResolution, ordered, localTableSize)
            this.localColorTable?.read(inputStream)
        }

        this.readImage(inputStream)
    }
}