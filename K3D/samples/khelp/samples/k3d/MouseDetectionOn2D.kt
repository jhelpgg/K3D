package khelp.samples.k3d

import khelp.k3d.k2d.Object2D
import khelp.k3d.k2d.event.Object2DListener
import khelp.k3d.render.Texture
import khelp.k3d.render.Window3D
import khelp.util.BLUE
import java.awt.Color

const val SIZE = 256
val THREE_QUARTER = Color(255, 255, 255, 192)
val HALF = Color(255, 255, 255, 128)
val QUARTER = Color(255, 255, 255, 64)

fun main(args: Array<String>)
{
    // 1) 3D scene with 2D object to detect
    val window3D = Window3D.createSizedWindow(800, 600, "Mouse detection", true)
    val guI2D = window3D.gui2d()
    val object2D = Object2D((800 - SIZE) / 2, (600 - SIZE) / 2, SIZE, SIZE)
    val texture = Texture("board", SIZE, SIZE, BLUE)
    object2D.texture(texture)
    guI2D.addOver3D(object2D)

    // 2) Add mouse listener
    object2D.registerObject2DListener(object : Object2DListener
                                      {
                                          /**
                                           * Call when mouse click on a object
                                           *
                                           * @param object2D    Object under mouse
                                           * @param x           Mouse X
                                           * @param y           Mouse Y
                                           * @param leftButton  Indicates if the left button is down
                                           * @param rightButton Indicates if the right button is down
                                           */
                                          override fun mouseClick(object2D: Object2D, x: Int, y: Int,
                                                                  leftButton: Boolean, rightButton: Boolean)
                                          {
                                              texture.drawPixel(x, y, Color.WHITE, true)

                                              texture.drawPixel(x, y - 1, THREE_QUARTER, true)
                                              texture.drawPixel(x - 1, y, THREE_QUARTER, true)
                                              texture.drawPixel(x + 1, y, THREE_QUARTER, true)
                                              texture.drawPixel(x, y + 1, THREE_QUARTER, true)

                                              texture.drawPixel(x, y - 2, HALF, true)
                                              texture.drawPixel(x - 1, y - 1, HALF, true)
                                              texture.drawPixel(x + 1, y - 1, HALF, true)
                                              texture.drawPixel(x - 2, y, HALF, true)
                                              texture.drawPixel(x + 2, y, HALF, true)
                                              texture.drawPixel(x - 1, y + 1, HALF, true)
                                              texture.drawPixel(x + 1, y + 1, HALF, true)
                                              texture.drawPixel(x, y + 2, HALF, true)

                                              texture.drawPixel(x, y - 3, QUARTER, true)
                                              texture.drawPixel(x - 1, y - 2, QUARTER, true)
                                              texture.drawPixel(x + 1, y - 2, QUARTER, true)
                                              texture.drawPixel(x - 2, y - 1, QUARTER, true)
                                              texture.drawPixel(x + 2, y - 1, QUARTER, true)
                                              texture.drawPixel(x - 3, y, QUARTER, true)
                                              texture.drawPixel(x + 3, y, QUARTER, true)
                                              texture.drawPixel(x - 2, y + 1, QUARTER, true)
                                              texture.drawPixel(x + 2, y + 1, QUARTER, true)
                                              texture.drawPixel(x - 1, y + 2, QUARTER, true)
                                              texture.drawPixel(x + 1, y + 2, QUARTER, true)
                                              texture.drawPixel(x, y + 3, QUARTER, true)
                                          }

                                          /**
                                           * Call when mouse drag on a object
                                           *
                                           * @param object2D    Object under mouse
                                           * @param x           Mouse X
                                           * @param y           Mouse Y
                                           * @param leftButton  Indicates if the left button is down
                                           * @param rightButton Indicates if the right button is down
                                           */
                                          override fun mouseDrag(object2D: Object2D, x: Int, y: Int,
                                                                 leftButton: Boolean, rightButton: Boolean)
                                          {
                                          }

                                          /**
                                           * Call when mouse enter on a object
                                           *
                                           * @param object2D Object enter
                                           * @param x        Mouse X
                                           * @param y        Mouse Y
                                           */
                                          override fun mouseEnter(object2D: Object2D, x: Int, y: Int)
                                          {
                                              texture.clear(Color.BLACK)
                                          }

                                          /**
                                           * Call when mouse exit on a object
                                           *
                                           * @param object2D Object exit
                                           * @param x        Mouse X
                                           * @param y        Mouse Y
                                           */
                                          override fun mouseExit(object2D: Object2D, x: Int, y: Int)
                                          {
                                              texture.clear(Color.BLUE)
                                          }

                                          /**
                                           * Call when mouse move on a object
                                           *
                                           * @param object2D Object under mouse
                                           * @param x        Mouse X
                                           * @param y        Mouse Y
                                           */
                                          override fun mouseMove(object2D: Object2D, x: Int, y: Int)
                                          {
                                          }
                                      })
}