package it.unibo.pcd.akka.basics.e11gui.view

import java.awt.Color
import scala.swing.{Dimension, Frame, MainFrame, SimpleSwingApplication}
import Drawables.DrawableRectangle

// Main frame setup
object MyDrawingApp extends SimpleSwingApplication:
  val panel = SimpleDrawablePanel()
  def top: Frame = new MainFrame:
    title = "Drawable Panel Example"
    preferredSize = new Dimension(400, 300)
    contents = panel

    // Adding some rectangles to the panel

  panel.addElement(SwingElementFactory().createRectangle(10, 10, 100, 50))
  panel.addElement(DrawableRectangle(50, 70, 150, 100, Color.blue))
