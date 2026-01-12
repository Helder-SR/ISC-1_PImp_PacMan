package Frontend

import Backend.Entities.{Directions, Player}
import Frontend.Sprite.{Sprite, SpriteManager}
import hevs.graphics.FunGraphics
import Backend.Cases._
import Backend.Entities.Directions
import Backend.Cases.Items
import Backend.Entities.Ghosts.{Blinky, Clyde, Ghosts, Inky, Pinky}
import Backend.Logical

import java.awt.{Color, Font}
import java.awt.event.{KeyAdapter, KeyEvent}

class Renderer(logical: Logical) {
  val WIDTH = logical.Map.length
  val HEIGHT = logical.Map(0).length

  var isBlikingCount = 0
  var isBlinkedImage = false
  val BLINKING_RATE = 11 // every x map reload

  val display = new FunGraphics(HEIGHT*SpriteManager.SPRITE_SIZE, WIDTH*SpriteManager.SPRITE_SIZE)

  display.setKeyManager(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      val newDir = e.getKeyCode match {
        case KeyEvent.VK_UP    => Some(Directions.Up)
        case KeyEvent.VK_DOWN  => Some(Directions.Down)
        case KeyEvent.VK_LEFT  => Some(Directions.Left)
        case KeyEvent.VK_RIGHT => Some(Directions.Right)
        case _ => None
      }

      if(newDir.isDefined) {
        logical.ChangePlayerDirection(newDir.get)
      }
    }
  })

  def displayMap(): Unit = {
    display.frontBuffer.synchronized {
      display.setColor(Color.BLACK)
      display.drawFillRect(0, 0, display.getFrameWidth, display.getFrameHeight)
      for (x <- 0 until WIDTH) {
        for (y <- 0 until HEIGHT) {
          val currentCase = logical.Map(x)(y)
          drawCase(currentCase, y, x)
        }
      }
      for (ghost <- logical.Ghosts) {
        var ghostColor = ghost.MainColor
        if (ghost.IsVulnerable) {
          ghostColor = Color.BLUE
          if (ghost.IsBlinking) {
            ghostColor = Color.WHITE
          }
        }
        drawGhost(ghost)
      }
      if (logical.Player.IsAlive) {
        drawPlayer(logical.Player)
      }
      displayLives()
      displayScore()
    }
  }

  private def drawPlayer(player: Player): Unit = {
    drawSprite(SpriteManager.getSprite("player"), player.X, player.Y)
  }

  private def drawGhost(ghost: Ghosts) : Unit = {
    var name = ""

    if (ghost.IsBlinking){
      isBlikingCount += 1
      if (isBlikingCount % BLINKING_RATE == 0){
        isBlinkedImage = !isBlinkedImage
      }
    }

    ghost match {
      case Blinky.INSTANCE =>
        name = "python"
      case Clyde.INSTANCE =>
        name = "windows"
      case Pinky.INSTANCE =>
        name = "office"
      case Inky.INSTANCE =>
        name = "windev"
      case _ =>
    }

    if (ghost.IsVulnerable){
      if(isBlinkedImage){
        name = "blinking"
      } else {
        name = "vulnerable"
      }
    }

    if (!ghost.IsAlive) name = "dead"

    drawSprite(SpriteManager.getSprite(name), ghost.X, ghost.Y)
  }

  private def drawCase(c: Case, x: Int, y: Int): Unit = {
    c.CaseType match {
      case CaseType.Wall =>
        drawSprite(SpriteManager.getSprite("wall"), x, y)
      case CaseType.Door =>
        drawSprite(SpriteManager.getSprite("door"), x, y)
      case CaseType.Road =>
        val road = c.asInstanceOf[RoadCase]
        var name = ""
        road.Item match {
          case Items.PacDot =>
            name = "pacdot"
          case Items.PowerPellet =>
            name = "powerpellet"
          case Items.Cherry =>
            name = "vim"
          case Items.Strawberry =>
            name = "linux"
          case Items.Orange =>
            name = "vscode"
          case Items.Apple =>
            name = "golang"
          case Items.Melon =>
            name = "git"
          case Items.Galaxian =>
            name = "rust"
          case Items.Bell =>
            name = "typescript"
          case Items.Key =>
            name = "vlc"
          case _ =>
        }
        if(name != "") drawSprite(SpriteManager.getSprite(name), x, y)
      case CaseType.Empty =>
    }
  }

  private def drawSprite(sprite: Sprite, x: Int, y: Int): Unit = {
    if (x < 0 || x >= display.width / sprite.size) return
    if (y < 0 || y >= display.height / sprite.size) return

    for (i <- 0 until sprite.size) {
      for (j <- 0 until sprite.size) {
        display.setPixel(x*sprite.size + i, y*sprite.size + j, sprite.pixels(i)(j))
      }
    }
  }

  private def displayLives(): Unit = {
    display.drawString(20, 20, s"PV: ${logical.Player.Lives}", "Arial", Font.BOLD, 20, Color.YELLOW)
  }

  private def displayScore(): Unit = {
    display.drawString(500, 20, s"SCORE: ${logical.Player.Score}", "Arial", Font.BOLD, 20, Color.YELLOW)
  }
}
