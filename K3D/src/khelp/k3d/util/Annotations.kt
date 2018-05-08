package khelp.k3d.util

/**
 * Indicates if method called inside Animation thread.
 *
 * It may do unexpected result to call it manually in an other thread
 *
 * It also to remember not do heavy stuff in the method, else it will slow down animations
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class ThreadAnimation

/**
 * Indicates that the method called inside OpenGL thread.
 *
 * It can be dangerous (crash or unexpected result) to call it manually in an other thread
 *
 * It also to remember not do heavy stuff in the method, else it will slow down the rendering or crash the window because system consider it as not responding
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class ThreadOpenGL
