package khelp.k3d.k2d

import khelp.thread.Mutex

class GUI2D
{
    private val arrayListObject2DOver3D = ArrayList<Object2D>()
    private val arrayListObject2DUnder3D = ArrayList<Object2D>()
    private var exclusiveObject: Object2D? = null
    private val mutex = Mutex()

    /**
     * Add object over the 3D
     *
     * @param object2D Object to add
     */
    fun addOver3D(object2D: Object2D) =
            this.mutex.playInCriticalSectionVoid { this.arrayListObject2DOver3D.add(object2D) }

    /**
     * Add object under 3D
     *
     * @param object2D Object to add
     */
    fun addUnder3D(object2D: Object2D) =
            this.mutex.playInCriticalSectionVoid { this.arrayListObject2DUnder3D.add(object2D) }

    /**
     * Clear the GUI2D, all over and under 3D, 2D objects are removed
     */
    fun clearAll() =
            this.mutex.playInCriticalSectionVoid {
                this.arrayListObject2DUnder3D.clear()
                this.arrayListObject2DOver3D.clear()
            }

    /**
     * Remove all over 3D, 2D objects
     */
    fun clearOver3D() = this.mutex.playInCriticalSectionVoid { this.arrayListObject2DOver3D.clear() }

    /**
     * Remove all under 3D, 2D objects
     */
    fun clearUnder3D() = this.mutex.playInCriticalSectionVoid { this.arrayListObject2DUnder3D.clear() }

    /**
     * Looking for an object over 3D and under a position
     *
     * @param x X
     * @param y Y
     * @return The found object or `null`
     */
    fun detectOver3D(x: Int, y: Int) =
            this.mutex.playInCriticalSection {
                if (this.exclusiveObject != null)
                {
                    if (this.exclusiveObject!!.detected(x, y))
                    {
                        this.exclusiveObject
                    }
                    else
                        null
                }
                else
                {
                    var object2D: Object2D

                    for (i in this.arrayListObject2DOver3D.size - 1 downTo 0)
                    {
                        object2D = this.arrayListObject2DOver3D[i]

                        if (object2D.detected(x, y))
                        {
                            return@playInCriticalSection object2D
                        }
                    }

                    null
                }
            }

    /**
     * Looking for an object over 3D and under a position
     *
     * @param x X
     * @param y Y
     * @return The found object or `null`
     */
    private fun detectUnder3D(x: Int, y: Int) =
            this.mutex.playInCriticalSection {
                var object2D: Object2D

                for (i in this.arrayListObject2DUnder3D.size - 1 downTo 0)
                {
                    object2D = this.arrayListObject2DUnder3D[i]

                    if (object2D.detected(x, y))
                    {
                        return@playInCriticalSection object2D
                    }
                }

                null
            }

    fun detectOver3DorUnder3D(x: Int, y: Int) =
            this.detectOver3D(x, y) ?: if (this.exclusiveObject == null) this.detectUnder3D(x, y) else null

    /**
     * For the detection restricted to only one object<br></br>
     * This object will be the only one detected
     *
     * @param object2d Object to detect exclusively (Can use `null` for detect all objects)
     */
    fun exclusiveDetection(object2d: Object2D?) =
            this.mutex.playInCriticalSectionVoid { this.exclusiveObject = object2d }

    /**
     * Iterator for list all objects over 3D
     *
     * @return Iterator for list all objects over 3D
     */
    fun iteratorOver3D() = this.mutex.playInCriticalSection { this.arrayListObject2DOver3D.iterator() }

    /**
     * Iterator for list all objects under 3D
     *
     * @return Iterator for list all objects under 3D
     */
    fun iteratorUnder3D() = this.mutex.playInCriticalSection { this.arrayListObject2DUnder3D.iterator() }

    /**
     * Call when mouse state changed
     *
     * @param x           Mouse X
     * @param y           Mouse Y
     * @param buttonLeft  Indicates if the button left is down
     * @param buttonRight Indicates if the button right is down
     * @param drag        Indicates if we are on drag mode
     * @param over        Object under the mouse
     */
    fun mouseState(x: Int, y: Int, buttonLeft: Boolean, buttonRight: Boolean, drag: Boolean, over: Object2D?) =
            this.mutex.playInCriticalSectionVoid {
                for (object2D in this.arrayListObject2DUnder3D)
                {
                    object2D.mouseState(x, y, buttonLeft, buttonRight, drag, over === object2D)
                }

                for (object2D in this.arrayListObject2DOver3D)
                {
                    object2D.mouseState(x, y, buttonLeft, buttonRight, drag, over === object2D)
                }
            }

    /**
     * Remove object over the 3D
     *
     * @param object2D Object to remove
     */
    fun removeOver3D(object2D: Object2D) =
            this.mutex.playInCriticalSectionVoid { this.arrayListObject2DOver3D.remove(object2D) }

    /**
     * Remove object under the 3D
     *
     * @param object2D Object to remove
     */
    fun removeUnder3D(object2D: Object2D) =
            this.mutex.playInCriticalSectionVoid { this.arrayListObject2DUnder3D.remove(object2D) }
}