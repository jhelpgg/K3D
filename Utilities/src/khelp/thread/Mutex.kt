package khelp.thread

import java.util.concurrent.Semaphore

/**
 * Mutex for do things inside a critical section.
 * 
 * Only one thread can enter in critical section in same time.
 * 
 * If a second thread arrive while one already in critical section,
 * the arrived thread will wait for the critical section is free (First leave it) before able to enter in critical section
 */
class Mutex
{
    /**
     * The mutex
     */
    private val mutex = Semaphore(1, true)

    /**
     * Play a task in critical section.
     * 
     * If a task is already inside the critical section, the current thread will be enqueue and asleep.
     * It will be wake up when its turn comes to enter inside the critical section.
     *
     * @param function  Task to play
     * @param parameter Parameter to give to task when its turn comes
     * @param P       Task parameter type
     * @param R       Task return type
     * @return Task result
     */
    fun <P, R> playInCriticalSection(function: (P) -> R, parameter: P): R
    {
        var locked = false

        try
        {
            this.mutex.acquire()
            locked = true
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to acquire the mutex")
        }

        try
        {
            return function(parameter)
        }
        finally
        {
            if (locked)
            {
                this.mutex.release()
            }
        }
    }

    /**
     * Play a task in critical section.

     * If a task is already inside the critical section, the current thread will be enqueue and asleep.
     * It will be wake up when its turn comes to enter inside the critical section.
     *
     * @param function Task to play
     * @param R      Task return type
     * @return Task result
     */
    fun <R> playInCriticalSection(function: () -> R): R
    {
        var locked = false

        try
        {
            this.mutex.acquire()
            locked = true
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to acquire the mutex")
        }

        try
        {
            return function()
        }
        finally
        {
            if (locked)
            {
                this.mutex.release()
            }
        }
    }

    /**
     * Play a task in critical section.

     * If a task is already inside the critical section, the current thread will be enqueue and asleep.
     * It will be wake up when its turn comes to enter inside the critical section.
     *
     * @param function  Task to play
     * @param parameter Parameter to give to task when its turn comes
     * @param P       Task parameter type
     */
    fun <P> playInCriticalSectionVoid(function: (P) -> Unit, parameter: P)
    {
        var locked = false

        try
        {
            this.mutex.acquire()
            locked = true
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to acquire the mutex")
        }

        try
        {
            function(parameter)
        }
        finally
        {
            if (locked)
            {
                this.mutex.release()
            }
        }
    }

    /**
     * Play a task in critical section.

     * If a task is already inside the critical section, the current thread will be enqueue and asleep.
     * It will be wake up when its turn comes to enter inside the critical section.
     *
     * @param function Task to play
     */
    fun playInCriticalSectionVoid(function: () -> Unit)
    {
        var locked = false

        try
        {
            this.mutex.acquire()
            locked = true
        }
        catch (exception: Exception)
        {
            khelp.debug.exception(exception, "Failed to acquire the mutex")
        }

        try
        {
            function()
        }
        finally
        {
            if (locked)
            {
                this.mutex.release()
            }
        }
    }
}
