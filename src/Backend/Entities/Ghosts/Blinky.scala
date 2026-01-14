package Backend.Entities.Ghosts

import Backend.Logical

import java.awt.Color
import scala.util.Random

class Blinky extends Ghosts(Color.RED) {
  override def toString: String = "Blinky"

  // Targets directly the player
  override def getTarget(logical: Logical): (Int, Int) = {
    if(IsVulnerable){
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y) else randomTarget(logical)
    } else {
      (logical.Player.X, logical.Player.Y)
    }
  }
}

object Blinky {
  val INSTANCE = new Blinky();
}
