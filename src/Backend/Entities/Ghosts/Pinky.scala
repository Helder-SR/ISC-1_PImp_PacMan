package Backend.Entities.Ghosts

import Backend.Logical

import java.awt.Color
import scala.util.Random

class Pinky extends Ghosts(Color.PINK) {
  override def toString: String = "Pinky"

  // Targets directly the player but has 1/2 chance to take a random direction
  override def getTarget(logical: Logical): (Int, Int) = {
    if(IsVulnerable){
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y) else randomTarget(logical)
    } else {
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y)
      else {
        randomTarget(logical)
      }
    }
  }
}

object Pinky {
  val INSTANCE = new Pinky();
}