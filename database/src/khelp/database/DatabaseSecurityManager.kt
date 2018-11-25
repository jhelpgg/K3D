package khelp.database

import khelp.debug.debug
import java.lang.reflect.ReflectPermission
import java.security.Permission
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manage security against reflection
 */
internal object DatabaseSecurityManager : SecurityManager()
{
    private val DEBUG = false
    private val ALLOWED = arrayOf("AWT-EventQueue", "AWT-XAWT")
    private val securityManager = System.getSecurityManager()
    internal var ready = AtomicBoolean(false)

    private fun check(permission: Permission)
    {
        when
        {
            permission is ReflectPermission && "suppressAccessChecks" == permission.name ->
            {
                val currentThread = Thread.currentThread().name

                if (ALLOWED.none { currentThread.startsWith(it) })
                {
                    if (DEBUG) debug("Reject : ", currentThread)

                    throw SecurityException("Reflection not allowed")
                }
            }
            permission is RuntimePermission && "setSecurityManager" == permission.name   ->
                throw SecurityException("Can't change the security manager")
        }
    }

    override fun checkPermission(permission: Permission)
    {
        if (this.ready.get())
        {
            this.check(permission)
            this.securityManager?.checkPermission(permission)
        }
    }

    override fun checkPermission(permission: Permission, context: Any?)
    {
        if (this.ready.get())
        {
            this.check(permission)
            this.securityManager?.checkPermission(permission, context)
        }
    }
}

/**
 * Load the security against reflection
 */
fun loadSecurity()
{
    if (!DatabaseSecurityManager.ready.get())
    {
        try
        {
            System.getSecurityManager()?.checkPermission(RuntimePermission("setSecurityManager"))
            System.setSecurityManager(DatabaseSecurityManager)
        }
        catch (ignored: Exception)
        {
        }

        DatabaseSecurityManager.ready.set(true)
    }
}