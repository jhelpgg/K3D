package khelp.k3d.render

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWErrorCallbackI
import org.lwjgl.system.APIUtil
import java.lang.reflect.Field
import java.util.function.BiPredicate

/**
 * Map of error codes and their name
 */
private val ERROR_CODES = APIUtil.apiClassTokens(object : BiPredicate<Field, Int>
                                                 {
                                                     override fun test(field: Field, value: Int) =
                                                             0x10000 < value && value < 0x20000
                                                 },
                                                 null,
                                                 GLFW::class.java)

object DebugGLFErrorCallback : GLFWErrorCallbackI
{
    override fun invoke(error: Int, description: Long)
    {
        val errorType = ERROR_CODES.get(error)
        val message = GLFWErrorCallback.getDescription(description)
        khelp.debug.error("GLF -", errorType, "- ", message)
    }
}