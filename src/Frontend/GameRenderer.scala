package Frontend

import hevs.graphics.FunGraphics
import java.awt.Color
import java.awt.event.{KeyAdapter, KeyEvent}
import Backend.Logical
import Backend.Cases._
import Backend.Entities.Directions
import Backend.Cases.Items // Assure-toi que l'objet Items est bien accessible

class GameRenderer(logical: Logical) {
  // Taille d'une case en pixels
  val CELL_SIZE = 30

  // On récupère la taille de la map depuis la logique
  // Attention: logical.Map peut être vide avant le chargement du niveau
  // On initialise FunGraphics plus tard ou on suppose que le niveau est chargé avant l'instanciation
  val mapHeight = logical.Map.length
  val mapWidth = logical.Map(0).length

  val display = new FunGraphics(mapWidth * CELL_SIZE, mapHeight * CELL_SIZE + 40, "Pac-Man Scala")

  // --- Gestion du Clavier ---
  display.setKeyManager(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      val newDir = e.getKeyCode match {
        case KeyEvent.VK_UP    => Some(Directions.Up)
        case KeyEvent.VK_DOWN  => Some(Directions.Down)
        case KeyEvent.VK_LEFT  => Some(Directions.Left)
        case KeyEvent.VK_RIGHT => Some(Directions.Right)
        case _ => None
      }

      // On envoie la demande de changement à la logique
      if(newDir.isDefined) {
        logical.ChangePlayerDirection(newDir.get)
      }
    }
  })
  // --- Méthode de dessin appelée par la Logique ---
  // Cette signature correspond au type attendu par logical.subscribeCycle
  def render(game: Logical): Unit = {
    // 1. Reset écran (Synchronisation du dessin pour éviter le scintillement)
    display.frontBuffer.synchronized {
      display.setColor(Color.BLACK)
      display.drawFillRect(0, 0, display.getFrameWidth, display.getFrameHeight)

      // 2. Dessiner la Grille
      for (y <- 0 until mapHeight) {
        for (x <- 0 until mapWidth) {
          val currentCase = game.Map(y)(x)
          drawCase(currentCase, x, y)
        }
      }

      // 3. Dessiner les Fantômes
      for (ghost <- game.Ghosts) {
        // Logique de couleur selon l'état du fantôme
        var ghostColor = ghost.MainColor

        if (ghost.IsVulnerable) {
          ghostColor = Color.BLUE // Vulnérable = Bleu
          if (ghost.IsBlinking) {
            ghostColor = Color.WHITE // Clignote avant la fin = Blanc
          }
        }

        drawEntity(ghost.X, ghost.Y, ghostColor, isRound = false) // isRound=false -> forme fantome
      }

      // 4. Dessiner Pacman
      if (game.Player.IsAlive) {
        drawEntity(game.Player.X, game.Player.Y, Color.YELLOW, isRound = true)
      }

      // 5. Game Over / Score (Optionnel)
      if (!game.IsGamePlaying) {
        display.setColor(Color.RED)
        display.drawString(10, mapHeight * CELL_SIZE + 20, "PAUSE / GAME OVER")
      }
    }
  }

  // Helper pour dessiner une case
  private def drawCase(c: Case, x: Int, y: Int): Unit = {
    val px = x * CELL_SIZE
    val py = y * CELL_SIZE

    c.CaseType match {
      case CaseType.Wall =>
        display.setColor(new Color(33, 33, 222)) // Bleu Arcade
        display.drawFillRect(px, py, CELL_SIZE, CELL_SIZE)
        display.setColor(Color.BLACK)
        display.drawRect(px + 4, py + 4, CELL_SIZE - 8, CELL_SIZE - 8) // Effet de bord

      case CaseType.Door =>
        display.setColor(Color.PINK)
        display.drawFillRect(px, py + (CELL_SIZE / 2) - 2, CELL_SIZE, 4)

      case CaseType.Road =>
        // Vérification des Items sur la route
        val road = c.asInstanceOf[RoadCase]
        road.Item match {
          case Items.PacDot =>
            display.setColor(new Color(255, 184, 174)) // Couleur saumon/points
            display.drawFillRect(px + CELL_SIZE/2 - 2, py + CELL_SIZE/2 - 2, 4, 4)
          case Items.PowerPellet =>
            display.setColor(new Color(255, 184, 174))
            display.drawFilledOval(px + CELL_SIZE/2 - 6, py + CELL_SIZE/2 - 6, 12, 12)
          case _ => // Rien (Items.None)
        }

      case CaseType.Empty =>
      // Noir
    }
  }

  // Helper pour dessiner une entité
  private def drawEntity(x: Int, y: Int, color: Color, isRound: Boolean): Unit = {
    val px = x * CELL_SIZE
    val py = y * CELL_SIZE

    display.setColor(color)
    if (isRound) {
      // Pacman
      display.drawFilledOval(px + 2, py + 2, CELL_SIZE - 4, CELL_SIZE - 4)
    } else {
      // Fantôme (Dôme + bas carré)
      display.drawFilledOval(px + 4, py + 4, CELL_SIZE - 8, CELL_SIZE - 8)
      display.drawFillRect(px + 4, py + (CELL_SIZE/2), CELL_SIZE - 8, (CELL_SIZE/2) - 2)
    }
  }
}