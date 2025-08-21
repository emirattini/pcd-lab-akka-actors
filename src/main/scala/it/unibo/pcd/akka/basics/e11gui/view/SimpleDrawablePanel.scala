package it.unibo.pcd.akka.basics.e11gui.view

import it.unibo.pcd.akka.basics.e11gui.view.Drawables.*

import java.awt.Graphics2D
import scala.swing.event.{Key, KeyPressed, KeyReleased, MousePressed, MouseReleased}
import scala.swing.{Panel, Swing}

// Custom panel for drawing elements
class SimpleDrawablePanel extends Panel with DrawablePanel[Graphics2D] {
  private var elements: List[Drawable[Graphics2D]] = List()
  listenTo(mouse.clicks)
  listenTo(keys)
  override def focusable: Boolean = true
  // Add a drawable element to the panel
  def addElement(element: Drawable[Graphics2D]): Unit =
    elements = element :: elements
    Swing.onEDT(repaint())

  override def getElement(x: Int, y: Int): Option[Drawable[Graphics2D]] =
    elements.find {
      case DrawableRectangle(xStart, yStart, width, height, _) =>
        x > xStart && x < xStart + width
        && y > yStart && y < yStart + height
      case DrawableCircle(xStart, yStart, radius, _) =>
        x > xStart && x < xStart + radius
          && y > yStart && y < yStart + radius
    }

  override def removeElement(element: Drawable[Graphics2D]): Unit =
    elements = elements.filterNot(_.equals(element))

  override def whenClicked(listener: (Int, Int) => Unit): Unit =
    reactions += { case MousePressed(_, point, _, _, _) =>
      listener(point.x, point.y)
      requestFocusInWindow()
    }

  override def whenReleased(listener: (Int, Int) => Unit): Unit =
    reactions += { case MouseReleased(_, point, _, _, _) =>
      listener(point.x, point.y)
      requestFocusInWindow()
    }

  override def keyClicked(listener: Key.Value => Unit): Unit =
    reactions += { case KeyPressed(_, key, _, _) =>
        println(s"$key pressed")
        listener(key)
    }

  // Custom painting of all elements
  override def paintComponent(g: Graphics2D): Unit =
    super.paintComponent(g)
    elements.foreach(_.draw(g))

  override def clear(): Unit =
    elements = List()
    Swing.onEDT(repaint())
}
