package Backend.Entities.Ghosts

import java.awt.Color

class Inky extends Ghosts(Color.BLUE) {
  override def toString: String = "Inky"
}

object Inky {
  val INSTANCE = new Inky();
}