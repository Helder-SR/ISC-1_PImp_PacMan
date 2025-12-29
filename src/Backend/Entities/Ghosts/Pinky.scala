package Backend.Entities.Ghosts

import java.awt.Color

class Pinky extends Ghosts(Color.PINK) {
  override def toString: String = "Pinky"
}

object Pinky {
  val INSTANCE = new Pinky();
}