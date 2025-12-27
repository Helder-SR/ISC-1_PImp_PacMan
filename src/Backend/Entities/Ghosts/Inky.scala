package Backend.Entities.Ghosts

import Backend.Cases.Case

import java.awt.Color

class Inky extends Ghosts(Color.BLUE) {
  override def takeDecision(map: Array[Array[Case]]): Unit = {
    // To implement AI motion
  }
}

object Inky {
  val INSTANCE = new Inky();
}