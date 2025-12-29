import Backend.Logical
import Backend.global.Levels
import Frontend.MainView

class PacMan {
  val backend = new Logical();
  backend.LoadLevel(Levels.Level1)
  val frontend = new MainView(backend);
  Thread.sleep(5000)
  backend.startGame()
}
