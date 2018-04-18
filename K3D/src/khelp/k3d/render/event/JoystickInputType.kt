package khelp.k3d.render.event

enum class JoystickInputType
{
    /**
     * Axis with positive value
     */
    AXIS_POSITIVE,
    /**
     * Axis with negative value
     */
    AXIS_NEGATIVE,
    /**
     * Joystick button
     */
    BUTTON,
    /**
     * Undefined type
     */
    NONE
}