package Backend.Entities.Ghosts

import Backend.Cases.Case

import java.awt.Color

class Pinky extends Ghosts(Color.PINK) {
  override def takeDecision(map: Array[Array[Case]]): Unit = {
    // To implement AI motion
  }
}

object Pinky {
  val INSTANCE = new Pinky();
}