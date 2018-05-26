package khelp.k3d.render.event

/**Way to apply the change*/
enum class ActionOnNodeWay
{
    /**Normal way: positive mouse movement, make positive change value. Negative mouse movement, make negative change value*/
    NORMAL_WAY,
    /**Reverse way: positive mouse movement, make negative change value. Negative mouse movement, make positive change value*/
    REVERSE_WAY
}