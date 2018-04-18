package khelp.k3d.render.event

enum class JoystickStatus
{
    /**
     * Input is pressed
     */
    PRESSED,
    /**
     * Input is repeated (Still pressed)
     */
    REPEATED,
    /**
     * Input is released
     */
    RELEASED;

    /**
     * Compute new status after a press
     *
     * @return Computed status
     */
    fun press() =
        when (this)
        {
            PRESSED, REPEATED ->  JoystickStatus.REPEATED
            RELEASED          ->  JoystickStatus.PRESSED
        }
}