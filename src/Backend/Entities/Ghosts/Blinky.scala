package Backend.Entities.Ghosts

import java.awt.Color

class Blinky extends Ghosts(Color.RED) {
  override def toString: String = "Blinky"
}

object Blinky {
  val INSTANCE = new Blinky();
}
