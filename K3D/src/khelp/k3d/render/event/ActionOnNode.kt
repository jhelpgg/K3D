package khelp.k3d.render.event

/**
 * Describes an action on manipulated node
 * @param actionOnNodePosition Which node position change
 * @param actionOnNodeWay Way of the change
 */
data class ActionOnNode(val actionOnNodePosition: ActionOnNodePosition, val actionOnNodeWay: ActionOnNodeWay)
