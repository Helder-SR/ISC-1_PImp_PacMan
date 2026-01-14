package Backend.Entities.Ghosts

import Backend.Logical

import java.awt.Color
import scala.math.{pow, random, sqrt}
import scala.util.Random

class Clyde extends Ghosts(Color.ORANGE) {
  override def toString: String = "Clyde"

  // Targets directly the player if Player is near Clyde
  override def getTarget(logical: Logical): (Int, Int) = {
    if(IsVulnerable){
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y) else randomTarget(logical)
    } else {
      val dist = sqrt(pow(logical.Player.X - this.X, 2) + pow(logical.Player.Y - this.Y, 2))
      if (dist < 3){
        (logical.Player.X, logical.Player.Y)
      } else {
        randomTarget(logical)
      }
    }
  }
}

object Clyde {
  val INSTANCE = new Clyde();
}
