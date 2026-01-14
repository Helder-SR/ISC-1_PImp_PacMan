package Backend.Entities.Ghosts

import Backend.Logical

import java.awt.Color
import scala.math.{pow, sqrt}
import scala.util.Random

class Inky extends Ghosts(Color.BLUE) {
  override def toString: String = "Inky"

  // Targets random point in the Map
  override def getTarget(logical: Logical): (Int, Int) = {
    if(IsVulnerable){
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y) else randomTarget(logical)
    } else {
      randomTarget(logical)
    }
  }
}

object Inky {
  val INSTANCE = new Inky();
}