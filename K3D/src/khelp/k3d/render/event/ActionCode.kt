package khelp.k3d.render.event

import org.lwjgl.glfw.GLFW

enum class ActionCode private constructor(public val preferenceKey: String,
                                          public val defaultKeyCode: Int,
                                          public val defaultJoystickCode: JoystickCode)
{
    /**
     * Go up
     */
    ACTION_UP("ACTION_UP", GLFW.GLFW_KEY_UP, JoystickCode.AXIS_2_NEGATIVE),
    /**
     * Go down
     */
    ACTION_DOWN("ACTION_DOWN", GLFW.GLFW_KEY_DOWN, JoystickCode.AXIS_2_POSITIVE),
    /**
     * Go left
     */
    ACTION_LEFT("ACTION_LEFT", GLFW.GLFW_KEY_LEFT, JoystickCode.AXIS_1_NEGATIVE),
    /**
     * Go right
     */
    ACTION_RIGHT("ACTION_RIGHT", GLFW.GLFW_KEY_RIGHT, JoystickCode.AXIS_1_POSITIVE),
    /**
     * Button 1
     */
    ACTION_BUTTON_1("ACTION_BUTTON_1", GLFW.GLFW_KEY_K, JoystickCode.BUTTON_1),
    /**
     * Button 2
     */
    ACTION_BUTTON_2("ACTION_BUTTON_2", GLFW.GLFW_KEY_J, JoystickCode.BUTTON_2),
    /**
     * Button 3
     */
    ACTION_BUTTON_3("ACTION_BUTTON_3", GLFW.GLFW_KEY_L, JoystickCode.BUTTON_3),
    /**
     * Button 4
     */
    ACTION_BUTTON_4("ACTION_BUTTON_4", GLFW.GLFW_KEY_I, JoystickCode.BUTTON_4),
    /**
     * Button 5
     */
    ACTION_BUTTON_5("ACTION_BUTTON_5", GLFW.GLFW_KEY_E, JoystickCode.BUTTON_5),
    /**
     * Button 6
     */
    ACTION_BUTTON_6("ACTION_BUTTON_6", GLFW.GLFW_KEY_D, JoystickCode.BUTTON_6),
    /**
     * Button 7
     */
    ACTION_BUTTON_7("ACTION_BUTTON_7", GLFW.GLFW_KEY_R, JoystickCode.BUTTON_7),
    /**
     * Button 8
     */
    ACTION_BUTTON_8("ACTION_BUTTON_8", GLFW.GLFW_KEY_F, JoystickCode.BUTTON_8),
    /**
     * Exit action
     */
    ACTION_EXIT("ACTION_EXIT", GLFW.GLFW_KEY_ESCAPE, JoystickCode.NONE);
}