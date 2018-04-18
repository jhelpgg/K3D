package khelp.io

/**
 * Binarizable object
 */
interface Binarizable
{
    /**
     * Parse the array for fill binarizable information.
     *
     * See [serializeBinary] for fill information
     *
     * @param byteArray Byte array to parse
     */
    fun parseBinary(byteArray: ByteArrayStream)

    /**
     * Write the binarizable information inside a byte array.
     *
     * See [parseBinary] for read information
     *
     * @param byteArray Byte array where write
     */
    fun serializeBinary(byteArray: ByteArrayStream)
}