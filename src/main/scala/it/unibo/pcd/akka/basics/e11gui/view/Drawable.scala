package it.unibo.pcd.akka.basics.e11gui.view

import scala.swing.Point
import scala.swing.event.Key

// Trait for drawable elements
trait Drawable[G]:
  def draw(graphic: G): Unit
  def getPosition: Point

trait ElementFactory[G]:
  def createRectangle(x: Int, y: Int, width: Int, height: Int): Drawable[G]
  def createCircle(x: Int, y: Int, radius: Int): Drawable[G]
  def moveElement(element: Drawable[G], x: Int, y: Int): Drawable[G]

trait DrawablePanel[G]:
  def addElement(element: Drawable[G]): Unit
  def removeElement(element: Drawable[G]): Unit
  def getElement(x: Int, y: Int): Option[Drawable[G]]
  def whenClicked(listener: (Int, Int) => Unit): Unit
  def whenReleased(listener: (Int, Int) => Unit): Unit
  def keyClicked(listener: Key.Value => Unit): Unit
  def clear(): Unit
