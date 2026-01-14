package Backend.Entities.Ghosts

import Backend.Logical

import java.awt.Color
import scala.util.Random

class Pinky extends Ghosts(Color.PINK) {
  override def toString: String = "Pinky"

  // Targets directly the player but has 1/2 chance to take a random direction
  override def getTarget(logical: Logical): (Int, Int) = {
    if(Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y)
    else {
      var x = 0
      var y = 0
      var attempts = 0

      do {
        x = Random.nextInt(logical.Map(0).length)
        y = Random.nextInt(logical.Map.length)
        attempts += 1
      } while (!logical.IsPointInTheMap(x, y) && attempts < 10)

      (x, y)
    }
  }
}

object Pinky {
  val INSTANCE = new Pinky();
}