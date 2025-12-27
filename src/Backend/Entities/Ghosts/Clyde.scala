package Backend.Entities.Ghosts

import Backend.Cases.Case

import java.awt.Color

class Clyde extends Ghosts(Color.ORANGE) {
  override def takeDecision(map: Array[Array[Case]]): Unit = {
    // To implement AI motion
  }
}
