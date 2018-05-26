package khelp.k3d.render.event

/**Action on node position*/
enum class ActionOnNodePosition
{
    /**Change the X position*/
    CHANGE_X,
    /**Change the Y position*/
    CHANGE_Y,
    /**Change the Z position*/
    CHANGE_Z,
    /**Rotate around X axis*/
    ROTATE_X,
    /**Rotate around Y axis*/
    ROTATE_Y,
    /**Rotate around Z axis*/
    ROTATE_Z
}