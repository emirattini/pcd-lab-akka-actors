package it.unibo.pcd.akka.basics.e11gui.actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.akka.basics.e11gui.actors.DrawMessage.{ChangeShape, Move, MoveToPoint, Reset}
import it.unibo.pcd.akka.basics.e11gui.view.{Drawable, DrawablePanel, ElementFactory, SimpleDrawablePanel, SwingElementFactory}
import it.unibo.pcd.akka.basics.e11gui.view.Drawables.*
import akka.actor.typed.scaladsl.AskPattern.*

import scala.swing.{BoxPanel, Button, Dimension, Frame, MainFrame, Orientation, SimpleSwingApplication}
import scala.swing.event.{ButtonClicked, Key, MousePressed}
import Key.{Down, Left, Right, Up}
import scala.concurrent.Future
import scala.util.Success

enum DrawMessage:
  case Rectangle(x: Int, y: Int, width: Int, height: Int)
  case Select(x: Int, y: Int)
  case MoveToPoint(x: Int, y: Int)
  case Move(dx: Int, dy: Int)
  case Reset, ChangeShape

object DrawerActor:
  def apply[G](panel: DrawablePanel[G], elementFactory: ElementFactory[G]): Behavior[DrawMessage] =
    Behaviors.setup {ctx =>
      var selected: Option[Drawable[G]] = None
      Behaviors.receiveMessage:
        case DrawMessage.Rectangle(x, y, width, height) =>
          val newElement = elementFactory.createRectangle(x, y, width, height)
          panel.addElement(newElement)
          selected = Some(newElement)
          Behaviors.same
        case DrawMessage.Select(x, y) =>
          selected = panel.getElement(x, y)
          ctx.log.info(s"Select ${selected.get} at $x, $y")
          Behaviors.same
        case DrawMessage.Reset =>
          selected = None
          ctx.log.info(s"Reset selection")
          Behaviors.same
        case DrawMessage.Move(dx, dy) =>
          selected match
            case Some(value) =>
              ctx.log.info(s"Move $value of $dx, $dy")
              panel.removeElement(value)
              val position = value.getPosition
              val movedElement = elementFactory.moveElement(value, position.x + dx, position.y + dy)
              panel.addElement(movedElement)
              selected = Some(movedElement)
              Behaviors.same
            case _ => ctx.log.info("Nothing selected"); Behaviors.same
        case DrawMessage.MoveToPoint(x, y) =>
          selected match
            case Some(value) =>
              ctx.log.info(s"Move $value at $x, $y")
              panel.removeElement(value)
              val movedElement = elementFactory.moveElement(value, x, y)
              panel.addElement(movedElement)
              selected = Some(movedElement)
              Behaviors.same
            case _ => ctx.log.info("Nothing selected"); Behaviors.same
        case DrawMessage.ChangeShape =>
          selected match
            case Some(value) =>
              panel.removeElement(value)
              value match
                case DrawableRectangle(x, y, width, _, _) =>
                  ctx.log.info(s"Change shape from Rect to Circle at $x, $y")
                  val changed = elementFactory.createCircle(x, y, width)
                  panel.addElement(changed)
                  selected = Some(changed)
                case DrawableCircle (x, y, radius, _) =>
                  ctx.log.info(s"Change shape from Circle to Rect at $x, $y")
                  val changed = elementFactory.createRectangle(x, y, radius, radius)
                  panel.addElement(changed)
                  selected = Some(changed)
            case None => ctx.log.info("No shape selected")
          Behaviors.same
    }

enum ClickActorListener:
  case Click(x: Int, y: Int)
  case Release(x: Int, y: Int)

object ClickActor:
  def apply[G](panel: DrawablePanel[G], drawer: akka.actor.typed.ActorRef[DrawMessage]): Behavior[ClickActorListener] =
    Behaviors.setup: context =>
      panel.whenClicked { case (x, y) =>
        context.pipeToSelf(Future.successful((x, y))) {
          case Success((x, y)) => ClickActorListener.Click(x, y)
          case _ => ClickActorListener.Click(0, 0)
        }
      }
      panel.whenReleased { case (x, y) =>
        context.pipeToSelf(Future.successful((x, y))) {
          case Success((x, y)) => ClickActorListener.Release(x, y)
          case _ => ClickActorListener.Release(0, 0)
        }
      }
      Behaviors.receiveMessage:
        case ClickActorListener.Click(x, y) =>
          context.log.info(s"Click at $x, $y")
          panel.getElement(x, y) match
            case Some(_) =>
              drawer ! DrawMessage.Select(x, y)
              Behaviors.same
            case None =>
              drawer ! DrawMessage.Reset
              drawer ! DrawMessage.Rectangle(x-20, y-20, 40, 40)
              Behaviors.same
        case ClickActorListener.Release(x, y) =>
          context.log.info(s"Release at $x, $y")
          drawer ! DrawMessage.MoveToPoint(x, y)
          Behaviors.same

enum KeyListener:
  case Up, Down, Left, Right
object KeyActor:
  def apply[G](panel: DrawablePanel[G], drawer: akka.actor.typed.ActorRef[DrawMessage]): Behavior[KeyListener] =
    Behaviors.setup: context =>
      panel.keyClicked {
        case Key.Up => context.self ! KeyListener.Up
        case Key.Down => context.self ! KeyListener.Down
        case Key.Left => context.self ! KeyListener.Left
        case Key.Right => context.self ! KeyListener.Right
      }
      Behaviors.receiveMessage:
        case KeyListener.Up =>
          context.log.info("Key Up pressed")
          drawer ! DrawMessage.Move(0, -5); Behaviors.same
        case KeyListener.Down =>
          context.log.info("Key Down pressed")
          drawer ! DrawMessage.Move(0, 5); Behaviors.same
        case KeyListener.Left =>
          context.log.info("Key Left pressed")
          drawer ! DrawMessage.Move(-5, 0); Behaviors.same
        case KeyListener.Right =>
          context.log.info("Key Right pressed")
          drawer ! DrawMessage.Move(5, 0); Behaviors.same

enum MainActorListener:
  case ChangeShape, Reset
object MainActor:
  def apply[G](panel: DrawablePanel[G], factory: ElementFactory[G]): Behavior[MainActorListener] =
    Behaviors.setup: context =>
      val drawer = context.spawn(DrawerActor[G](panel, factory), "drawer")
      val clickActor = context.spawn(ClickActor(panel, drawer), "clickActor")
      val keyActor = context.spawn(KeyActor(panel, drawer), "keyActor")
      Behaviors.receiveMessage:
        case MainActorListener.ChangeShape => drawer ! DrawMessage.ChangeShape; Behaviors.same
        case MainActorListener.Reset => drawer ! DrawMessage.Reset; Behaviors.same

object MyDrawingApp extends SimpleSwingApplication:
  val panel = SimpleDrawablePanel()
  val factory = SwingElementFactory()
  private val clearButton: Button = new Button("Clear")
  private val changeShapeButton: Button = new Button("Change Shape")
  private val buttons: BoxPanel = new BoxPanel(Orientation.Horizontal) {
    contents += clearButton
    contents += changeShapeButton
  }

  def top: Frame = new MainFrame:
    title = "Drawable Panel Example"
    preferredSize = new Dimension(400, 300)
    contents = new BoxPanel(Orientation.Vertical) {
      contents += panel
      contents += buttons
    }

    // Adding some rectangles to the panel
  private val system: ActorSystem[MainActorListener] = akka.actor.typed.ActorSystem(MainActor(panel, factory), "main")
  clearButton.reactions += { case ButtonClicked(`clearButton`) =>
    panel.clear()
    system ! MainActorListener.Reset
    panel.requestFocusInWindow()}
  changeShapeButton.reactions += { case ButtonClicked(`changeShapeButton`) =>
    system ! MainActorListener.ChangeShape
    panel.requestFocusInWindow()}
  panel.requestFocusInWindow()
