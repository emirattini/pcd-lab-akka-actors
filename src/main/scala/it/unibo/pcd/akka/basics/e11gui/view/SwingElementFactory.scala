package it.unibo.pcd.akka.basics.e11gui.view

import java.awt.Color
import scala.swing.Graphics2D
import Drawables.*

class SwingElementFactory extends ElementFactory[Graphics2D]:
  override def createRectangle(x: Int, y: Int, w: Int, h: Int): Drawable[Graphics2D] =
    DrawableRectangle(x, y, w, h, Color.BLACK)

  override def createCircle(x: Int, y: Int, radius: Int): Drawable[Graphics2D] =
    DrawableCircle(x, y, radius, Color.DARK_GRAY)

  override def moveElement(element: Drawable[Graphics2D], x: Int, y: Int): Drawable[Graphics2D] =
    element match
      case DrawableRectangle(_, _, width, height, color) => DrawableRectangle(x-width/2, y-height/2, width, height, color)
      case DrawableCircle(_, _, radius, color) => DrawableCircle(x-radius/2, y-radius/2, radius, color)
      case _ => throw NotImplementedError("Drawable not compatible")