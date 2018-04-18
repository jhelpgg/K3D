package khelp.io

import khelp.debug.exception
import khelp.io.base64.Base64InputStream
import khelp.io.base64.Base64OutputStream
import khelp.text.StringCutter
import khelp.text.uf8
import khelp.text.utf8
import khelp.util.toUnsignedInt
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.math.BigInteger
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import java.util.Enumeration
import java.util.Stack
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * "Home" directory
 */
val homeDirectory: File by lazy {
    var directory = outsideDirectory

    try
    {
        val home = System.getProperty("user.home")

        if (home != null)
        {
            val homeDirectory = File(home)

            if (homeDirectory.exists() && homeDirectory.canRead() && homeDirectory.canWrite())
            {
                directory = homeDirectory
            }
        }
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to get home directory, use outside directory");
    }

    directory
}

private class UtilIO

/**
 * Directory external of the code
 */
val outsideDirectory: File by lazy {
    var className = UtilIO::class.java.name
    var index = className.lastIndexOf('.')

    if (index >= 0)
    {
        className = className.substring(index + 1)
    }

    className += ".class"
    val url = UtilIO::class.java.getResource(className)
    val path = url.file
    index = path.indexOf(".jar!")
    var start = 0

    if (path.startsWith("file://"))
    {
        start = 7
    }
    else if (path.startsWith("file:"))
    {
        start = 5
    }

    if (index > 0)
    {
        File(path.substring(start, path.lastIndexOf('/', index - 1)))
    }
    else
    {
        File(path).parentFile.parentFile.parentFile.parentFile
    }
}
/**
 * Temporary directory
 */
val temporaryDirectory: File by lazy {
    var directory: File? = null
    var path = System.getProperty("user.home")

    if (path != null)
    {
        directory = File(path)

        if (!directory.exists() || !directory.canRead() || !directory.canWrite())
        {
            directory = null
        }
    }

    if (directory == null)
    {
        path = System.getProperty("user.dir")

        if (path != null)
        {
            directory = File(path)
            if (!directory.exists() || !directory.canRead() || !directory.canWrite())
            {
                directory = null
            }
        }
    }

    if (directory == null)
    {
        directory = outsideDirectory
    }

    directory = File(directory, "JHelp/temporary")
    createDirectory(directory)

    directory!!
}
/**
 * Path that represents the current directory
 */
val CURRENT_DIRECTORY = "."
/**
 * One kilo-byte in bytes
 */
val KILO_BYTES = 1024
/**
 * Size of a file header
 */
val HEADER_SIZE = KILO_BYTES
/**
 * One mega-byte in bytes
 */
val MEGA_BYTES = 1024 * KILO_BYTES
/**
 * Buffer size
 */
val BUFFER_SIZE = 4 * MEGA_BYTES
/**
 * Path separator used in URL, ZIP, JAR
 */
const val PATH_SEPARATOR = '/'
/**
 * Path the represents the parent directory
 */
val PREVIOUS_DIRECTORY = ".."

/**
 * Create a double from a byte array.<br></br>
 * Work good with byte array generated with [.doubleToByteArray]
 *
 * @param array Array to convert
 * @return Double obtain
 */
fun byteArrayToDouble(array: ByteArray) = java.lang.Double.longBitsToDouble(byteArrayToLong(array))

/**
 * Create a long from a byte array.<br></br>
 * Work good with byte array generated with [.longToByteArray]
 *
 * @param array Array to convert
 * @return Long obtain
 */
fun byteArrayToLong(array: ByteArray) =
        ((array[0].toUnsignedInt()).toLong() shl 56
                or ((array[1].toUnsignedInt()).toLong() shl 48)
                or ((array[2].toUnsignedInt()).toLong() shl 40)
                or ((array[3].toUnsignedInt()).toLong() shl 32)
                or ((array[4].toUnsignedInt()).toLong() shl 24)
                or ((array[5].toUnsignedInt()).toLong() shl 16)
                or ((array[6].toUnsignedInt()).toLong() shl 8)
                or (array[7].toUnsignedInt()).toLong())

/**
 * Compute the SHA code of a stream
 *
 * @param inputStream Stream to read
 * @return SHA code
 * @throws NoSuchAlgorithmException If SHA not implemented (Should never append, its java base algorithm)
 * @throws IOException              On reading issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeBigIntegerSHA(inputStream: InputStream): BigInteger
{
    val temp = computeByteArraySHA(inputStream)
    var bigInteger = BigInteger.ZERO

    for (b in temp)
    {
        bigInteger = bigInteger.shiftLeft(8).add(BigInteger.valueOf((b.toUnsignedInt()).toLong()))
    }

    return bigInteger
}

/**
 * Compute SHA for a stream
 *
 * @param inputStream Stream to read
 * @return MD5 of the stream
 * @throws NoSuchAlgorithmException If system not support SHA
 * @throws IOException              On reading stream issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeByteArraySHA(inputStream: InputStream): ByteArray
{
    return computeMessageDigest("SHA", inputStream)
}

/**
 * Compute MD5 of an image
 *
 * @param bufferedImage Image to compute it's MD5
 * @return Image MD5
 * @throws NoSuchAlgorithmException If system not support MD5
 * @throws IOException              On reading image issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeMD5(bufferedImage: BufferedImage): String
{
    bufferedImage.flush()

    val width = bufferedImage.width
    val height = bufferedImage.height

    var pixels = IntArray(width * height + 2)

    pixels[0] = width
    pixels[1] = height

    pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 2, width)

    return computeMD5(pixels)
}

/**
 * Compute MD5 for an array of integer
 *
 * @param data Array to compute its MD5
 * @return Array MD5
 * @throws NoSuchAlgorithmException If system not support MD5
 * @throws IOException              On reading array issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeMD5(data: IntArray): String
{
    return computeMD5(IntegerArrayInputStream(data))
}

/**
 * Compute MD5 for a stream
 *
 * @param inputStream Stream to read
 * @return MD5 of the stream
 * @throws NoSuchAlgorithmException If system not support MD5
 * @throws IOException              On reading stream issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeMD5(inputStream: InputStream): String
{
    val temp = computeMessageDigest("MD5", inputStream)
    val stringBuffer = StringBuilder()
    var read: Int

    for (b in temp)
    {
        read = b.toUnsignedInt()
        stringBuffer.append(Integer.toHexString((read shr 4) and 0xF))
        stringBuffer.append(Integer.toHexString(read and 0xF))
    }

    return stringBuffer.toString()
}

/**
 * Compute MD5 of a file
 *
 * @param file File to compute its MD5
 * @return Computed MD5
 * @throws NoSuchAlgorithmException If system not support MD5
 * @throws IOException              On reading file issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeMD5(file: File): String?
{
    if (!file.exists() || !file.isFile || isVirtualLink(file))
    {
        return null
    }

    var fileInputStream: FileInputStream? = null

    try
    {
        fileInputStream = FileInputStream(file)

        return computeMD5(fileInputStream)
    }
    finally
    {
        if (fileInputStream != null)
        {
            try
            {
                fileInputStream.close()
            }
            catch (ignored: Exception)
            {
                //Nothing to do
            }

        }
    }
}

/**
 * Compute MD5 and SHA for a file, can be us as unique ID
 *
 * @param file File to read
 * @return MD5, SHA pair unique ID of the file
 * @throws NoSuchAlgorithmException If system not support MD5 or SHA
 * @throws IOException              On reading stream issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeMD5_SHA_ID(file: File): String?
{
    if (!file.exists() || !file.isFile || isVirtualLink(file))
    {
        return null
    }

    var fileInputStream: FileInputStream? = null

    try
    {
        fileInputStream = FileInputStream(file)

        return computeMD5_SHA_ID(fileInputStream)
    }
    finally
    {
        if (fileInputStream != null)
        {
            try
            {
                fileInputStream.close()
            }
            catch (ignored: Exception)
            {
                //Nothing to do
            }

        }
    }
}

/**
 * Compute MD5 and SHA for a stream, can be us as unique ID
 *
 * @param inputStream Stream to read
 * @return MD5, SHA pair unique ID of the stream
 * @throws NoSuchAlgorithmException If system not support MD5 or SHA
 * @throws IOException              On reading stream issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeMD5_SHA_ID(inputStream: InputStream): String
{
    var inputStream = inputStream
    val md5 = MessageDigest.getInstance("MD5")
    val sha = MessageDigest.getInstance("SHA")
    var temp = ByteArray(4096)

    var read = inputStream.read(temp)
    while (read >= 0)
    {
        md5.update(temp, 0, read)
        sha.update(temp, 0, read)

        read = inputStream.read(temp)
    }

    inputStream.close()

    temp = md5.digest()
    val stringBuffer = StringBuilder()
    for (b in temp)
    {
        read = b.toUnsignedInt()
        stringBuffer.append(Integer.toHexString((read shr 4) and 0xF))
        stringBuffer.append(Integer.toHexString(read and 0xF))
    }

    stringBuffer.append('_')

    temp = sha.digest()
    for (b in temp)
    {
        read = b.toUnsignedInt()
        stringBuffer.append(Integer.toHexString((read shr 4) and 0xF))
        stringBuffer.append(Integer.toHexString(read and 0xF))
    }

    return stringBuffer.toString()
}

@Throws(NoSuchAlgorithmException::class, IOException::class)
private fun computeMessageDigest(algorithm: String, inputStream: InputStream): ByteArray
{
    val sha = MessageDigest.getInstance(algorithm)
    var ioException: IOException? = null

    treatInputStream({ inputStream },
                     { inputStream1 ->
                         val temp = ByteArray(4096)

                         var read = inputStream1.read(temp)
                         while (read >= 0)
                         {
                             sha.update(temp, 0, read)

                             read = inputStream1.read(temp)
                         }
                     },
                     { ioException = it })

    if (ioException != null)
    {
        throw ioException!!
    }

    return sha.digest()
}

/**
 * Compute relative path for go from a file to an other
 *
 * @param start       Start file
 * @param destination Destination file
 * @return Relative path
 */
fun computeRelativePath(start: File, destination: File): String
{
    val pathStart = start.absolutePath
            .split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val lengthStart = pathStart.size

    val pathDestination = destination.absolutePath
            .split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val lengthDestination = pathDestination.size

    var common = 0
    val max = Math.min(lengthStart, lengthDestination)

    while (common < max)
    {
        if (pathStart[common] != pathDestination[common])
        {
            break
        }
        common++
    }

    val stringBuilder = StringBuilder()

    for (i in common until lengthStart)
    {
        stringBuilder.append(PREVIOUS_DIRECTORY)

        stringBuilder.append('/')
    }

    for (i in common until lengthDestination)
    {
        stringBuilder.append(pathDestination[i])

        stringBuilder.append('/')
    }

    if (stringBuilder.length > 0)
    {
        stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
    }

    return stringBuilder.toString()
}

/**
 * Compute SHA for a file
 *
 * @param file File to read
 * @return SHA of the file
 * @throws NoSuchAlgorithmException If system not support SHA
 * @throws IOException              On reading stream issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeSHA(file: File): String?
{
    if (!file.exists() || !file.isFile || isVirtualLink(file))
    {
        return null
    }

    var fileInputStream: FileInputStream? = null

    try
    {
        fileInputStream = FileInputStream(file)

        return computeSHA(fileInputStream)
    }
    finally
    {
        if (fileInputStream != null)
        {
            try
            {
                fileInputStream.close()
            }
            catch (ignored: Exception)
            {
                //Nothing to do
            }

        }
    }
}

/**
 * Compute SHA for a stream
 *
 * @param inputStream Stream to read
 * @return MD5 of the stream
 * @throws NoSuchAlgorithmException If system not support SHA
 * @throws IOException              On reading stream issue
 */
@Throws(NoSuchAlgorithmException::class, IOException::class)
fun computeSHA(inputStream: InputStream): String
{
    var inputStream = inputStream
    val sha = MessageDigest.getInstance("SHA")
    var temp = ByteArray(4096)

    var read = inputStream.read(temp)
    while (read >= 0)
    {
        sha.update(temp, 0, read)

        read = inputStream.read(temp)
    }

    inputStream.close()

    temp = sha.digest()
    val stringBuffer = StringBuilder()
    for (b in temp)
    {
        read = b.toUnsignedInt()
        stringBuffer.append(Integer.toHexString((read shr 4) and 0xF))
        stringBuffer.append(Integer.toHexString(read and 0xF))
    }

    return stringBuffer.toString()
}

/**
 * Copy a file or directory. If directory, all content and sub-directory are copied
 *
 * @param source      File or directory source
 * @param destination File or directory destination
 * @throws IOException On coping issue
 */
@Throws(IOException::class)
fun copy(source: File, destination: File)
{
    val stack = Stack<Pair<File, File>>()
    var pair: Pair<File, File>

    stack.push(Pair(source, destination))

    while (!stack.isEmpty())
    {
        pair = stack.pop()

        if (!isVirtualLink(pair.first))
        {
            if (!pair.first.isDirectory)
            {
                write(pair.first, pair.second)
            }
            else
            {
                if (!createDirectory(pair.second))
                {
                    throw IOException("Can't create the directory: ${pair.second.absolutePath}")
                }

                val content = pair.first.listFiles()

                if (content != null)
                {
                    for (file in content)
                    {
                        stack.push(Pair(file, File(pair.second, file.name)))
                    }
                }
            }
        }
    }
}

/**
 * Create a directory and its parents if needs
 *
 * @param directory Directory to create
 * @return `true` if creation succeed. `false` if failed
 */
fun createDirectory(directory: File?): Boolean
{
    if (directory == null || isVirtualLink(directory))
    {
        return false
    }

    if (directory.exists())
    {
        return directory.isDirectory
    }

    return if (createDirectory(directory.parentFile))
    {
        directory.mkdir()
    }
    else false
}

/**
 * Create a file and its parent directory if need
 *
 * @param file File to create
 * @return `true` if creation succeed. `false` if failed
 */
fun createFile(file: File?): Boolean
{
    if (file == null || isVirtualLink(file))
    {
        return false
    }

    if (file.exists())
    {
        return !file.isDirectory
    }

    if (createDirectory(file.parentFile))
    {
        try
        {
            return file.createNewFile()
        }
        catch (exception: IOException)
        {
            khelp.debug.exception(exception, "Failed to create file : ", file.absolutePath)

            return false
        }

    }

    return false
}

/**
 * Create a temporary directory.<br></br>
 * That is to say, a new empty directory inside the temporary directory
 *
 * @return Created directory
 * @throws IOException On creation issue
 */
@Throws(IOException::class)
fun createTemporaryDirectory(): File
{
    var name = 0
    var file = File(temporaryDirectory, "temp_$name")

    while (file.exists())
    {
        name++
        file = File(temporaryDirectory, "temp_$name")
    }

    if (!createDirectory(file))
    {
        throw IOException("Can't create temporary directory " + file.getAbsolutePath())
    }

    file.deleteOnExit()
    return file
}

/**
 * Create a temporary file.<br></br>
 * That is to say, a file inside the temporary directory
 *
 * @param string File name
 * @return Created file
 * @throws IOException On creation issue
 */
@Throws(IOException::class)
fun createTemporaryFile(string: String): File
{
    val file = File(temporaryDirectory, string)

    if (!createFile(file))
    {
        throw IOException("Can't create temporary file " + file.getAbsolutePath())
    }

    file.deleteOnExit()
    return file
}

/**
 * Delete a file or a directory.<br></br>
 * If it is a directory, its delete all children first
 *
 * @param file File/directory to delete
 * @return `true` if succeed. `false` if failed, may be some deletion have happen
 */
fun delete(file: File?): Boolean
{
    if (file == null || !file.exists())
    {
        return true
    }

    if (!isVirtualLink(file) && file.isDirectory)
    {
        val content = file.listFiles()

        if (content != null)
        {
            for (child in content)
            {
                if (!delete(child))
                {
                    return false
                }
            }
        }
    }

    try
    {
        if (file.delete())
        {
            return true
        }
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to delete : ", file.absolutePath)

        return false
    }

    try
    {
        file.deleteOnExit()
    }
    catch (exception: Exception)
    {
        khelp.debug.exception(exception, "Failed to delete on exit : ", file.absolutePath)

        return false
    }

    return true
}

/**
 * Create a byte array from double.<br></br>
 * Can be revert with [.byteArrayToDouble]
 *
 * @param d Double to convert
 * @return Byte array created
 */
fun doubleToByteArray(d: Double): ByteArray
{
    return longToByteArray(java.lang.Double.doubleToLongBits(d))
}

/**
 * Write a base 64 String to stream as decoded binary.<br></br>
 * Stream not close by the method
 *
 * @param base64       Base 64 string
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun fromBase64(base64: String, outputStream: OutputStream)
{
    val stringInputStream = StringInputStream(base64)
    val base64InputStream = Base64InputStream(stringInputStream)
    write(base64InputStream, outputStream)
}

/**
 * Indicates if a file is a virtual link.
 *
 * A virtual link in Linux system is a way to have a reference to a file/directory as if it is in place, but the real
 * file is other place. It is a way to share the same file by several directory
 *
 * @param file File to test
 * @return `true` if it is a virtual link
 */
fun isVirtualLink(file: File): Boolean
{
    if (!file.exists())
    {
        return false
    }

    try
    {
        return file.canonicalPath != file.absolutePath
    }
    catch (exception: IOException)
    {
        khelp.debug.exception(exception, "Failed to determine virtual link : ", file.absolutePath)

        return false
    }
}

/**
 * Create a byte array from long.<br></br>
 * Can be revert with [.byteArrayToLong]
 *
 * @param l Long to convert
 * @return Byte array created
 */
fun longToByteArray(l: Long): ByteArray
{
    val array = ByteArray(8)

    array[0] = (l shr 56 and 0xFF).toByte()
    array[1] = (l shr 48 and 0xFF).toByte()
    array[2] = (l shr 40 and 0xFF).toByte()
    array[3] = (l shr 32 and 0xFF).toByte()
    array[4] = (l shr 24 and 0xFF).toByte()
    array[5] = (l shr 16 and 0xFF).toByte()
    array[6] = (l shr 8 and 0xFF).toByte()
    array[7] = (l and 0xFF).toByte()

    return array
}

/**
 * Obtain a file outside of the code.<br></br>
 * If this class is in a jar called A.jar, and this jar is in /My/Path/A.jar then the file will be relative to /My/Path
 *
 * @param path Relative path
 * @return The file
 */
fun obtainExternalFile(path: String): File
{
    return obtainFile(outsideDirectory, path)
}

/**
 * Obtain a file relative to a directory
 *
 * @param directory Directory reference
 * @param path      Path search
 * @return The file
 */
fun obtainFile(directory: File, path: String, separator: Char = PATH_SEPARATOR): File
{
    var file = directory
    val stringCutter = StringCutter(path, separator)

    var next = stringCutter.next()

    while (next != null)
    {
        if (PREVIOUS_DIRECTORY == next)
        {
            file = file.parentFile
        }
        else if (CURRENT_DIRECTORY != next && next.isNotEmpty())
        {
            file = File(file, next)
        }

        next = stringCutter.next()
    }

    return file
}

/**
 * Try to get the local INET address
 *
 * @param onlyIPv4 If only IP v4 are accepted
 * @return Found local INET address or `null` if not found
 */
fun obtainLocalInetAddress(onlyIPv4: Boolean): InetAddress?
{
    try
    {
        var inetAddress: InetAddress? = null
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        var networkInterface: NetworkInterface
        var inetAdresses: Enumeration<InetAddress>
        var testedInetAddress: InetAddress

        while (networkInterfaces.hasMoreElements())
        {
            networkInterface = networkInterfaces.nextElement()

            if (!networkInterface.isUp || networkInterface.isLoopback || networkInterface.isVirtual)
            {
                continue
            }

            inetAdresses = networkInterface.inetAddresses

            while (inetAdresses.hasMoreElements())
            {
                testedInetAddress = inetAdresses.nextElement()

                if (testedInetAddress.isLoopbackAddress)
                {
                    continue
                }

                if (testedInetAddress is Inet4Address)
                {
                    if (onlyIPv4)
                    {
                        return testedInetAddress
                    }

                    inetAddress = testedInetAddress
                }
                else if (!onlyIPv4 && testedInetAddress is Inet6Address)
                {
                    return testedInetAddress
                }
            }
        }

        return if (inetAddress == null)
        {
            InetAddress.getLocalHost()
        }
        else inetAddress

    }
    catch (exception: Exception)
    {
        exception(exception)

        try
        {
            return InetAddress.getLocalHost()
        }
        catch (exception1: UnknownHostException)
        {
            exception(exception1)
            return null
        }
    }
}

/**
 * Read a [BigInteger] from a stream.<br></br>
 * Previously write with [.writeBigInteger]
 *
 * @param inputStream Stream to read
 * @return [BigInteger] read
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun readBigInteger(inputStream: InputStream): BigInteger
{
    val length = readInteger(inputStream)
    val temp = ByteArray(length)

    readStream(inputStream, temp)

    return BigInteger(temp)
}

/**
 * Read a [Binarizable] inside a stream.<br></br>
 * The [Binarizable] should be previously written by [.writeBinarizable]
 *
 * @param <B>         [Binarizable] type
 * @param clas        Class of the [Binarizable]
 * @param inputStream Stream to read
 * @return The [Binarizable] read
 * @throws IOException On read the stream or the data not represents the asked [Binarizable]
</B> */
@Throws(IOException::class)
fun <B : Binarizable> readBinarizable(clas: Class<B>, inputStream: InputStream): B
{
    try
    {
        val byteArray = ByteArrayStream()
        write(inputStream, byteArray.internalOutputStream)

        return byteArray.readBinarizable(clas)!!
    }
    catch (exception: Exception)
    {
        throw IOException("Failed to read the Binarizable " + clas.name + " in the given stream !",
                          exception)
    }
}

/**
 * Read a [Binarizable] inside a stream.<br></br>
 * The [Binarizable] should be previously written by [.writeBinarizableNamed]
 *
 * @param <B>         [Binarizable] type
 * @param inputStream Stream to read
 * @return The [Binarizable] read
 * @throws IOException On read the stream or the data not represents the asked [Binarizable]
</B> */
@Throws(IOException::class)
fun <B : Binarizable> readBinarizableNamed(inputStream: InputStream): B
{
    try
    {
        val name = readString(inputStream)
        val clas = Class.forName(name) as Class<B>
        return readBinarizable(clas, inputStream)
    }
    catch (exception: Exception)
    {
        throw IOException("Failed to read the Binarizable in the given stream !", exception)
    }
}

/**
 * Read a byte array from stream
 *
 * @param inputStream Stream to read
 * @return Read array
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun readByteArray(inputStream: InputStream): ByteArray
{
    val length = readInteger(inputStream)

    if (length < 0)
    {
        throw IOException("Invalid array size: $length")
    }

    val array = ByteArray(length)
    val read = readStream(inputStream, array, 0, length)
    return Arrays.copyOfRange(array, 0, read)
}

/**
 * Read double from stream
 *
 * @param inputStream Stream to read
 * @return Read double
 * @throws IOException On reading problem
 */
@Throws(IOException::class)
fun readDouble(inputStream: InputStream) = java.lang.Double.longBitsToDouble(readLong(inputStream))

/**
 * Read a file header (First bytes of a file)
 *
 * @param file File to read header
 * @return Header read
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun readFileHeader(file: File): ByteArray
{
    val size = Math.min(HEADER_SIZE.toLong(), file.length()).toInt()
    val header = ByteArray(size)
    var fileInputStream: FileInputStream? = null

    try
    {
        fileInputStream = FileInputStream(file)
        readStream(fileInputStream, header)
    }
    catch (exception: Exception)
    {
        throw IOException("Failed to get header of " + file.absolutePath, exception)
    }
    finally
    {
        if (fileInputStream != null)
        {
            try
            {
                fileInputStream.close()
            }
            catch (ignored: Exception)
            {
            }

        }
    }

    return header
}

/**
 * Read float from a stream
 *
 * @param inputStream Stream to read
 * @return Float read
 * @throws IOException On read issue
 */
@Throws(IOException::class)
fun readFloat(inputStream: InputStream) = java.lang.Float.intBitsToFloat(readInteger(inputStream))

/**
 * Read float[] from a stream
 *
 * @param inputStream Stream to read
 * @return Float array read
 * @throws IOException On read issue
 */
@Throws(IOException::class)
fun readFloatArray(inputStream: InputStream): FloatArray?
{
    val length = readInteger(inputStream)

    if (length < 0)
    {
        return null
    }

    val array = FloatArray(length)
    for (a in 0 until length)
    {
        array[a] = readFloat(inputStream)
    }

    return array
}

/**
 * Read an integer from stream
 *
 * @param inputStream Stream to read
 * @return Integer read
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun readInteger(inputStream: InputStream) =
        (inputStream.read() shl 24) or (inputStream.read() shl 16) or (inputStream.read() shl 8) or inputStream.read()

/**
 * Read a array of integer from stream (Wrote with [.writeIntegerArray]
 *
 * @param inputStream Stream to read. May be `null` if [.writeIntegerArray]
 * was write
 * `null`
 * @return Read array
 * @throws IOException On stream read issue
 */
@Throws(IOException::class)
fun readIntegerArray(inputStream: InputStream): IntArray?
{
    val length = readInteger(inputStream)

    if (length < 0)
    {
        return null
    }

    val array = IntArray(length)
    for (a in 0 until length)
    {
        array[a] = readInteger(inputStream)
    }

    return array
}

/**
 * Read text lines in given stream
 * @param producerInput Function that create the stream to read
 * @param lineReader Called on each line read. The parameter is the read line
 * @param onError Action to do on error
 * @param I Input stream type
 * @return **`true`** If complete operation succeed without exception
 */
fun <I : InputStream> readLines(producerInput: () -> I,
                                lineReader: (String) -> Unit,
                                onError: (IOException) -> Unit = { exception(it, "Failed to read lines!!") }) =
        treatInputStream(producerInput,
                         { inputStream ->
                             val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"));
                             var line = bufferedReader.readLine()

                             while (line != null)
                             {
                                 lineReader(line)
                                 line = bufferedReader.readLine()
                             }

                             bufferedReader.close();
                         }, onError)

/**
 * Read long from stream
 *
 * @param inputStream Stream to read
 * @return Read long
 * @throws IOException On reading problem
 */
@Throws(IOException::class)
fun readLong(inputStream: InputStream): Long
{
    var integer = inputStream.read().toLong() shl 56
    integer = integer or (inputStream.read().toLong() shl 48)
    integer = integer or (inputStream.read().toLong() shl 40)
    integer = integer or (inputStream.read().toLong() shl 32)
    integer = integer or (inputStream.read().toLong() shl 24)
    integer = integer or (inputStream.read().toLong() shl 16)
    integer = integer or (inputStream.read().toLong() shl 8)
    integer = integer or inputStream.read().toLong()
    return integer
}

/**
 * Read stream and fill an array.<br>
 * The write in array start at the offset specify.<br>
 * It stop to read stream if stream reach its end or the specify length is reach
 *
 * @param inputStream Stream to read
 * @param array       Array to fill
 * @param offset      To start writing
 * @param length      Number of bytes to read at maximum
 * @return Number of bytes read (It can be less than specify length if stream have not enough data to respect the
 * length)
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun readStream(inputStream: InputStream, array: ByteArray, offset: Int = 0, length: Int = array.size - offset): Int
{
    var offset = offset
    var left = Math.min(array.size - offset, length)

    if (left <= 0)
    {
        return 0
    }

    var total = 0

    var read = inputStream.read(array, offset, left)

    if (read < 0)
    {
        return -1
    }

    offset += read
    total += read
    left -= read

    while (read >= 0 && left > 0)
    {
        read = inputStream.read(array, offset, left)

        if (read >= 0)
        {
            offset += read
            total += read
            left -= read
        }
    }

    return total
}

/**
 * Read stream from stream
 *
 * @param inputStream Stream to read
 * @return Read string
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun readString(inputStream: InputStream) = readByteArray(inputStream).uf8()

/**
 * Read text from given stream
 * @param inputStream Stream to read
 * @return Text read
 */
@Throws(IOException::class)
fun readText(inputStream: InputStream): String
{
    val stringBuilder = StringBuilder()
    var exception: IOException? = null

    readLines({ inputStream },
              { line ->
                  stringBuilder.append(line)
                  stringBuilder.append('\n')
              },
              { exception = it })

    if (exception != null)
    {
        throw exception!!
    }

    return stringBuilder.toString()
}

/**
 * Do a prompt in console, waiting user type something (finish by enter) in console.
 *
 * `null` is return in case of issue
 *
 * @return The user input OR `null` in case of issue
 */
fun readUserInputInConsole(): String?
{
    try
    {
        val bufferedReader = BufferedReader(InputStreamReader(System.`in`))
        return bufferedReader.readLine()
    }
    catch (exception: Exception)
    {
        exception(exception, "Issue while reading console user input")
        return null
    }
}

/**
 * Rename a file
 *
 * @param source      File source
 * @param destination File destination
 * @throws IOException On rename issue
 */
@Throws(IOException::class)
fun rename(source: File, destination: File)
{
    if (source.absolutePath == destination.absolutePath)
    {
        return
    }

    copy(source, destination)
    delete(source)
}

/**
 * Try skip bytes from given stream (unread bytes).<br></br>
 * It stop if reach the end of stream or if manage to skip the number of byte asked
 *
 * @param inputStream Stream to skip some bytes
 * @param count       Number of byte to skip
 * @return Real number bytes skip (Lower that asked if end of stream reach)
 * @throws IOException On issue reading stream
 */
@Throws(IOException::class)
fun skip(inputStream: InputStream, count: Int): Int
{
    var count = count
    if (count <= 0)
    {
        return 0
    }

    var skipped = 0
    var toSkip: Int
    var skip: Long = 1

    while (count > 0 && skip > 0)
    {
        try
        {
            toSkip = count
            skip = inputStream.skip(count.toLong())

            while (skip <= 0 && toSkip > 0)
            {
                toSkip = toSkip shr 1

                if (toSkip > 0)
                {
                    skip = inputStream.skip(toSkip.toLong())
                }
            }

            if (skip > 0)
            {
                count -= skip.toInt()
                skipped += skip.toInt()
            }
        }
        catch (ignored: EOFException)
        {
        }
    }

    return skipped
}

/**
 * Transform a binary stream to base 64 string.<br></br>
 * Stream not close by the method
 *
 * @param inputStream Stream to read
 * @return Base 64 string
 * @throws IOException On reading issue
 */
@Throws(IOException::class)
fun toBase64(inputStream: InputStream): String
{
    val stringOutputStream = StringOutputStream()
    val base64OutputStream = Base64OutputStream(stringOutputStream)
    write(inputStream, base64OutputStream)
    return stringOutputStream.string
}

/**
 * Manage properly an input and an output streams, to simplify the open, close and error management
 * @param producerInput Function that create the input stream
 * @param producerOutput Function that create the output stream
 * @param operation Operation to do with input and output streams
 * @param onError Called if error happen
 * @param I Input stream type
 * @param O Output stream type
 * @return **`true`** If complete operation succeed without exception
 */
fun <I : InputStream, O : OutputStream> treatInputOutputStream(
        producerInput: () -> I, producerOutput: () -> O,
        operation: (I, O) -> Unit,
        onError: (IOException) -> Unit = { exception(it, "Issue on treat input/output streams!") }): Boolean
{
    var ioException: IOException? = null
    var inputStream: I? = null
    var outputStream: O? = null

    try
    {
        inputStream = producerInput()
        outputStream = producerOutput()
        operation(inputStream, outputStream)
    }
    catch (io: IOException)
    {
        ioException = io
    }
    catch (e: Exception)
    {
        ioException = IOException("Failed to do operation!", e)
    }
    finally
    {
        if (outputStream != null)
        {
            try
            {
                outputStream.flush()
            }
            catch (ignored: Exception)
            {
            }

            try
            {
                outputStream.close()
            }
            catch (ignored: Exception)
            {
            }

        }

        if (inputStream != null)
        {
            try
            {
                inputStream.close()
            }
            catch (ignored: Exception)
            {
            }

        }
    }

    if (ioException != null)
    {
        onError(ioException)
        return false
    }

    return true
}

/**
 * Manage properly an input stream, to simplify the open, close and error management
 * @param producerInput Function that create the input stream
 * @param operation Operation to do with input stream
 * @param onError Called if error happen
 * @param I Input stream type
 * @return **`true`** If complete operation succeed without exception
 */
fun <I : InputStream> treatInputStream(
        producer: () -> I, operation: (I) -> Unit,
        onError: (IOException) -> Unit = { exception(it, "Failed to treat input stream!") }): Boolean
{
    var ioException: IOException? = null
    var inputStream: I? = null

    try
    {
        inputStream = producer()
        operation(inputStream)
    }
    catch (io: IOException)
    {
        ioException = io
    }
    catch (e: Exception)
    {
        ioException = IOException("Failed to do operation!", e)
    }
    finally
    {
        if (inputStream != null)
        {
            try
            {
                inputStream.close()
            }
            catch (ignored: Exception)
            {
            }

        }
    }

    if (ioException != null)
    {
        onError(ioException)
        return false
    }

    return true
}

/**
 * Manage properly an output streams to simplify the open, close and error management
 * @param producerOutput Function that create the output stream
 * @param operation Operation to do with output stream
 * @param onError Called if error happen
 * @param O Output stream type
 * @return **`true`** If complete operation succeed without exception
 */
fun <O : OutputStream> treatOutputStream(
        producer: () -> O, operation: (O) -> Unit,
        onError: (IOException) -> Unit = { exception(it, "Failed to treat output stream") }): Boolean
{
    var ioException: IOException? = null
    var outputStream: O? = null

    try
    {
        outputStream = producer()
        operation(outputStream)
    }
    catch (io: IOException)
    {
        ioException = io
    }
    catch (e: Exception)
    {
        ioException = IOException("Failed to do operation!", e)
    }
    finally
    {
        if (outputStream != null)
        {
            try
            {
                outputStream.flush()
            }
            catch (ignored: Exception)
            {
            }

            try
            {
                outputStream.close()
            }
            catch (ignored: Exception)
            {
            }

        }
    }

    if (ioException != null)
    {
        onError(ioException)
        return false
    }

    return true
}

/**
 * Unzip a file inside a directory
 *
 * @param directoryDestination Directory where unzip
 * @param zip                  Zip file
 * @throws IOException On extracting issue
 */
@Throws(IOException::class)
fun unzip(directoryDestination: File, zip: File)
{
    var fileInputStream: FileInputStream? = null

    try
    {
        fileInputStream = FileInputStream(zip)
        unzip(directoryDestination, fileInputStream)
    }
    finally
    {
        if (fileInputStream != null)
        {
            try
            {
                fileInputStream.close()
            }
            catch (ignored: Exception)
            {
                //Nothing to do
            }

        }
    }
}

/**
 * Unzip a stream inside a directory
 *
 * @param directoryDestination Directory where unzip
 * @param inputStreamZip       Stream to unzip
 * @throws IOException On unzipping issue
 */
@Throws(IOException::class)
fun unzip(directoryDestination: File, inputStreamZip: InputStream)
{
    var destination: File
    val zipInputStream = ZipInputStream(inputStreamZip)

    var zipEntry: ZipEntry? = zipInputStream.nextEntry
    var name: String

    while (zipEntry != null)
    {
        name = zipEntry.name
        destination = obtainFile(directoryDestination, name)

        if (name.endsWith("/"))
        {
            if (!createDirectory(destination))
            {
                throw IOException("Can't create the directory " + destination.absolutePath)
            }
        }
        else
        {
            if (!createFile(destination))
            {
                throw IOException("Can't create the file " + destination.absolutePath)
            }

            write(zipInputStream, destination)
        }

        zipInputStream.closeEntry()

        zipEntry = zipInputStream.nextEntry
    }
}

/**
 * Write a stream inside on other one
 *
 * @param inputStream  Stream source
 * @param outputStream Stream destination
 * @throws IOException On copying issue
 */
@Throws(IOException::class)
fun write(inputStream: InputStream, outputStream: OutputStream)
{
    val buffer = ByteArray(BUFFER_SIZE)
    var read = inputStream.read(buffer)

    while (read >= 0)
    {
        outputStream.write(buffer, 0, read)
        read = inputStream.read(buffer)
    }
}

/**
 * Copy a file inside an other one
 *
 * @param fileSource      Source file
 * @param fileDestination Destination file
 * @throws IOException On copying issue
 */
@Throws(IOException::class)
fun write(fileSource: File, fileDestination: File)
{
    if (!createFile(fileDestination))
    {
        throw IOException("Can't create the file " + fileDestination.absolutePath)
    }

    var exception: IOException? = null

    treatInputOutputStream({ FileInputStream(fileSource) },
                           { FileOutputStream(fileDestination) },
                           { fileInputStream, fileOutputStream ->
                               write(fileInputStream, fileOutputStream)
                           },
                           { exception = it })

    if (exception != null)
    {
        throw exception!!
    }
}

/**
 * Write a stream inside a file
 *
 * @param inputStream     Stream source
 * @param fileDestination File destination
 * @throws IOException On copying issue
 */
@Throws(IOException::class)
fun write(inputStream: InputStream, fileDestination: File)
{
    if (!createFile(fileDestination))
    {
        throw IOException("Can't create the file " + fileDestination.absolutePath)
    }

    var exception: IOException? = null

    treatOutputStream({ FileOutputStream(fileDestination) },
                      { write(inputStream, it) },
                      { exception = it })

    if (exception != null)
    {
        throw exception!!
    }
}

/**
 * Write a file inside a stream
 *
 * @param fileSource   Source file
 * @param outputStream Stream where write
 * @throws IOException On copying issue
 */
@Throws(IOException::class)
fun write(fileSource: File, outputStream: OutputStream)
{
    var exception: IOException? = null

    treatInputStream({ FileInputStream(fileSource) },
                     { write(it, outputStream) },
                     { exception = it })

    if (exception != null)
    {
        throw exception!!
    }
}

/**
 * Write a [BigInteger] in stream.<br></br>
 * To read later, you can use [.readBigInteger]
 *
 * @param bigInteger   [BigInteger] to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeBigInteger(bigInteger: BigInteger, outputStream: OutputStream)
{
    val temp = bigInteger.toByteArray()
    writeInteger(temp.size, outputStream)
    outputStream.write(temp)
}

/**
 * Write a [Binarizable] inside a stream.<br></br>
 * To read it later, use [.readBinarizable]
 *
 * @param binarizable  [Binarizable] to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeBinarizable(binarizable: Binarizable, outputStream: OutputStream)
{
    val byteArray = ByteArrayStream()
    byteArray.writeBinarizable(binarizable)
    write(byteArray.internalInputStream, outputStream)
}

/**
 * Write a [Binarizable] inside a stream.<br></br>
 * To read it later, use [.readBinarizableNamed]
 *
 * @param binarizable  [Binarizable] to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeBinarizableNamed(binarizable: Binarizable, outputStream: OutputStream)
{
    writeString(binarizable.javaClass.name, outputStream)
    writeBinarizable(binarizable, outputStream)
}

/**
 * Write a part of byte array on stream
 *
 * @param array        Array to write
 * @param offset       Offset where start read the array
 * @param length       Number of byte to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeByteArray(array: ByteArray, offset: Int = 0, length: Int = array.size - offset, outputStream: OutputStream)
{
    val len = Math.min(array.size - offset, length)
    writeInteger(len, outputStream)
    outputStream.write(array, offset, len)
}

/**
 * Write double in a stream
 *
 * @param d            Double to write
 * @param outputStream Stream where write
 * @throws IOException On writing problem
 */
@Throws(IOException::class)
fun writeDouble(d: Double, outputStream: OutputStream) = writeLong(java.lang.Double.doubleToLongBits(d), outputStream)

/**
 * Write a float in stream
 *
 * @param f            Float to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeFloat(f: Float, outputStream: OutputStream) = writeInteger(java.lang.Float.floatToIntBits(f), outputStream)

/**
 * Write a float[] in stream
 *
 * @param array        Float array to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeFloatArray(array: FloatArray?, outputStream: OutputStream)
{
    if (array == null)
    {
        writeInteger(-1, outputStream)

        return
    }

    val length = array.size
    writeInteger(length, outputStream)

    for (anArray in array)
    {
        writeFloat(anArray, outputStream)
    }
}

/**
 * Write an integer to stream
 *
 * @param integer      Integer to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeInteger(integer: Int, outputStream: OutputStream)
{
    outputStream.write((integer shr 24) and 0xFF)
    outputStream.write((integer shr 16) and 0xFF)
    outputStream.write((integer shr 8) and 0xFF)
    outputStream.write(integer and 0xFF)
}

/**
 * Write an array of integer in stream (Can be read later with [.readIntegerArray]
 *
 * @param array        Array to write (Can be `null`)
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeIntegerArray(array: IntArray?, outputStream: OutputStream)
{
    if (array == null)
    {
        writeInteger(-1, outputStream)

        return
    }

    val length = array.size
    writeInteger(length, outputStream)

    for (anArray in array)
    {
        writeInteger(anArray, outputStream)
    }
}

/**
 * Write long in a stream
 *
 * @param integer      Long to write
 * @param outputStream Stream where write
 * @throws IOException On writing problem
 */
@Throws(IOException::class)
fun writeLong(integer: Long, outputStream: OutputStream)
{
    outputStream.write((integer shr 56 and 0xFF).toInt())
    outputStream.write((integer shr 48 and 0xFF).toInt())
    outputStream.write((integer shr 40 and 0xFF).toInt())
    outputStream.write((integer shr 32 and 0xFF).toInt())
    outputStream.write((integer shr 24 and 0xFF).toInt())
    outputStream.write((integer shr 16 and 0xFF).toInt())
    outputStream.write((integer shr 8 and 0xFF).toInt())
    outputStream.write((integer and 0xFF).toInt())
}

/**
 * Write string to stream
 *
 * @param string       String to write
 * @param outputStream Stream where write
 * @throws IOException On writing issue
 */
@Throws(IOException::class)
fun writeString(string: String, outputStream: OutputStream) = writeByteArray(string.utf8(), outputStream = outputStream)

/**
 * Zip a file or directory inside a file
 *
 * @param source      File/directory to zip
 * @param destination File destination
 * @throws IOException On zipping issue
 */
@Throws(IOException::class)
fun zip(source: File, destination: File, onlyContentIfDirectory: Boolean = false)
{
    if (!createFile(destination))
    {
        throw IOException("Can't create " + destination.absolutePath)
    }

    var fileOutputStream: FileOutputStream? = null

    try
    {
        fileOutputStream = FileOutputStream(destination)

        zip(source, fileOutputStream, onlyContentIfDirectory)
    }
    finally
    {
        if (fileOutputStream != null)
        {
            try
            {
                fileOutputStream.flush()
            }
            catch (ignored: Exception)
            {
                //Nothing to do
            }

            try
            {
                fileOutputStream.close()
            }
            catch (ignored: Exception)
            {
            }

        }
    }
}

/**
 * Zip a file or directory inside a stream
 *
 * @param source          File/directory to zip
 * @param outputStreamZip Where write the zip
 * @throws IOException On zipping issue
 */
@Throws(IOException::class)
fun zip(source: File, outputStreamZip: OutputStream, onlyContentIfDirectory: Boolean = false)
{
    var zipEntry: ZipEntry
    val zipOutputStream = ZipOutputStream(outputStreamZip)
    // For the best compression
    zipOutputStream.setLevel(9)

    var pair: Pair<String, File> = Pair(source.name, source)
    val stack = Stack<Pair<String, File>>()

    stack.push(pair)
    var ignore = source.isDirectory && onlyContentIfDirectory

    while (!stack.isEmpty())
    {
        pair = stack.pop()

        if (!isVirtualLink(pair.second))
        {
            if (pair.second.isDirectory)
            {
                val content = pair.second.listFiles()

                if (content != null)
                {
                    if (!ignore)
                    {
                        for (child in content)
                        {
                            stack.push(Pair(pair.first + "/" + child.name, child))
                        }
                    }
                    else
                    {
                        for (child in content)
                        {
                            stack.push(Pair(child.name, child))
                        }
                    }
                }
            }
            else if (!ignore)
            {
                zipEntry = ZipEntry(pair.first)
                // For the best compression
                zipEntry.method = ZipEntry.DEFLATED

                zipOutputStream.putNextEntry(zipEntry)

                write(pair.second, zipOutputStream)

                zipOutputStream.closeEntry()
            }
        }

        ignore = false
    }

    zipOutputStream.finish()
    zipOutputStream.flush()
}
