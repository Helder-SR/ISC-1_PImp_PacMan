package Frontend

import Backend.Logical
import Frontend.Sprite.SpriteManager
import hevs.graphics.FunGraphics

class MainView(private val Logical: Logical) {
  // TO DO : User interface of the game
  val renderer = new Renderer(Logical)
  Logical.subscribeCycle(refreshUserInterface)
  SpriteManager.loadSprites()

  def refreshUserInterface(logical: Logical): Unit = {
//    print(logical.Map.map(l => l.mkString).mkString("\n"))
    if(!logical.IsGameOver) renderer.displayMap()
    else if (logical.Player.Lives > 0) renderer.displayVictory()
    else renderer.displayGameOver()

  }
}
