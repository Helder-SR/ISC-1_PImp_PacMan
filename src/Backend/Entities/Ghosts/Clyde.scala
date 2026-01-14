package Backend.Entities.Ghosts

import Backend.Logical
import Backend.global.Position

import java.awt.Color
import scala.math.{pow, random, sqrt}
import scala.util.Random

class Clyde extends Ghosts(Color.ORANGE) {
  override def toString: String = "Clyde"

  var isInCycle = false;
  val scatterPath = Map(
    new Position(1, 29) -> new Position(12, 29),
    new Position(12, 29) -> new Position(12, 26),
    new Position(12, 26) -> new Position(9, 26),
    new Position(9, 26) -> new Position(9, 23),
    new Position(9, 23) -> new Position(6, 23),
    new Position(6, 23) -> new Position(6, 26),
    new Position(6, 26) -> new Position(1, 26),
    new Position(1, 26) -> new Position(1, 29),
  );
  val DEFAULT_SCATTER_TARGET = new Position(1, 29)
  var scatterTarget: Position = DEFAULT_SCATTER_TARGET;

  // Targets directly the player but has 1/2 chance to take a random direction
  override def getTarget(logical: Logical): (Int, Int) = {
    if(Mode == GhostsMode.Frightened) {
      if (Random.nextInt(2) != 0) (logical.Player.X, logical.Player.Y) else randomTarget(logical)
    }
    else if(Mode == GhostsMode.Chase) {
      if(isInCycle) {
        isInCycle = false;
        scatterTarget = DEFAULT_SCATTER_TARGET
      }
      val dist = sqrt(pow(logical.Player.X - this.X, 2) + pow(logical.Player.Y - this.Y, 2))
      if (dist < 3){
        (logical.Player.X, logical.Player.Y)
      } else {
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
    } else {
      var res: Option[Position] = None;
      scatterPath.foreach(p => if(p._1.X == this.X && p._1.Y == this.Y) res = Option(p._2))
      if(res.isDefined) {
        isInCycle = true
        scatterTarget = res.get
      }
      (scatterTarget.X, scatterTarget.Y)
    }
  }
}

object Clyde {
  val INSTANCE = new Clyde();
}
