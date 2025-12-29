package Backend.Entities.Ghosts

import java.awt.Color

class Clyde extends Ghosts(Color.ORANGE) {
  override def toString: String = "Clyde"
}

object Clyde {
  val INSTANCE = new Clyde();
}
