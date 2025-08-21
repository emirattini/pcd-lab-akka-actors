package it.unibo.pcd.akka.basics.e11gui.view

import java.awt.{Color, Graphics2D}
import scala.swing.Point

// Implementation of a Drawable Rectangle
object Drawables:
  case class DrawableRectangle(x: Int, y: Int, width: Int, height: Int, color: Color) extends Drawable[Graphics2D]:
    override def draw(graphic: Graphics2D): Unit =
      graphic.setColor(color)
      graphic.fillRect(x, y, width, height)
    override def getPosition: Point = new Point(x+width/2, y+height/2)
  case class DrawableCircle(x: Int, y: Int, radius: Int, color: Color) extends Drawable[Graphics2D]:
    override def draw(graphic: Graphics2D): Unit =
      graphic.setColor(color)
      graphic.fillOval(x, y, radius, radius)
    override def getPosition: Point = new Point(x+radius/2, y+radius/2)
